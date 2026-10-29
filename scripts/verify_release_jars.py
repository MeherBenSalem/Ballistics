"""Verify the packaged release jars against the generated resources and record release hashes.

This is an artifact audit, separate from the unit tests (./gradlew :common:test), the in-game GameTests
(./gradlew :neoforge:runGameTestServer, :fabric:runGameTestServer) and the source-level asset checks
(tools/assets/check_assets.py).

    python scripts/verify_release_jars.py --version 0.4.0 --jar-dir releases --output releases/manifest.json
    python scripts/verify_release_jars.py --version 0.4.0 --game 26.2      # one workspace's jars
"""
import argparse
import hashlib
import json
from pathlib import Path
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
MOD_ID = 'remnants_ballistics'
GAMES = ['1.21.1', '26.2', '26.3']
# Resource and data pack format each game loads; pack.mcmeta must accept both.
PACK_FORMATS = {'1.21.1': (34, 48), '26.2': (88, 107), '26.3': (97, 121)}
# Runtime requirements besides the loader, Minecraft and Java (keep in sync with release_metadata.mjs).
REQUIRED = {'neoforge': set(), 'fabric': {'fabric-api', 'forgeconfigapiport'}}
# Loom remaps the 1.21.1 access widener to intermediary names in the Fabric jar, so it is checked for presence only.
# 26.x is unobfuscated: nothing is remapped and mixin configs carry no refmap.
OBFUSCATED = {'1.21.1'}
REMAPPED = {'fabric': {f'{MOD_ID}.accesswidener'}, 'neoforge': set()}
# Recipe -> expected result id. Covers each recipe family (weapon, attachment, press, smelting, stations).
RECIPES = {
    'r9_sidearm': f'{MOD_ID}:r9_sidearm',
    'eclipse_railgun': f'{MOD_ID}:eclipse_railgun',
    'holo_reflex': f'{MOD_ID}:holo_reflex',
    'ammo_press/ammo_9mm': f'{MOD_ID}:ammo_9mm',
    'steel_ingot_from_blasting': f'{MOD_ID}:steel_ingot',
    'gunsmith_workbench': f'{MOD_ID}:gunsmith_workbench',
    'ammo_press': f'{MOD_ID}:ammo_press',
}
BLOCKS = ['gunsmith_workbench', 'ammo_press']
WEAPON_PARTS = ['body', 'action', 'magazine', 'stock', 'sights', 'muzzle']


def verify_neoforge(z, names, game, version):
    descriptor = tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
    assert descriptor['mods'][0]['modId'] == MOD_ID
    assert descriptor['mods'][0]['version'] == version
    icon = 'logoFile' if game in OBFUSCATED else 'iconFile'
    assert descriptor['mods'][0][icon] == 'logo.png' and 'logo.png' in names
    deps = descriptor['dependencies'][MOD_ID]
    required = {d['modId'] for d in deps if d.get('mandatory') or d.get('type') == 'required'}
    assert required == {'neoforge', 'minecraft'} | REQUIRED['neoforge'], f'unexpected required dependencies {required}'
    assert next(d['versionRange'] for d in deps if d['modId'] == 'minecraft') == f'[{game}]'
    assert 'fabric.mod.json' not in names, 'Fabric metadata in the NeoForge jar'


def verify_fabric(z, names, game, version):
    descriptor = json.loads(z.read('fabric.mod.json'))
    assert descriptor['id'] == MOD_ID
    assert descriptor['version'] == version
    assert descriptor['icon'] == 'logo.png' and 'logo.png' in names
    assert descriptor['accessWidener'] in names and descriptor['accessWidener'].startswith(MOD_ID)
    for config in descriptor['mixins']:
        assert config in names, f'mixin config {config} not packaged'
        refmap = json.loads(z.read(config)).get('refmap')
        if game in OBFUSCATED:
            assert refmap in names, f'{config}: refmap not packaged'
        else:
            assert refmap is None, f'{config}: refmap in an unobfuscated build'
    required = set(descriptor['depends']) - {'fabricloader', 'minecraft', 'java'}
    assert required == REQUIRED['fabric'], f'unexpected required dependencies {required}'
    assert descriptor['depends']['minecraft'] == game
    for entrypoint in ('main', 'client'):
        for cls in descriptor['entrypoints'][entrypoint]:
            assert cls.replace('.', '/') + '.class' in names, f'{entrypoint} entrypoint {cls} missing'
    assert 'META-INF/neoforge.mods.toml' not in names, 'NeoForge metadata in the Fabric jar'


def verify(jar, loader, game, version):
    resources = ROOT / game / 'common/src/main/resources'
    weapons = sorted(p.stem for p in (resources / f'assets/{MOD_ID}/gun_visuals').glob('*.json'))
    with zipfile.ZipFile(jar) as z:
        names = z.namelist()
        assert len(names) == len(set(names)), f'{jar}: duplicate entries'
        # Every generated resource is packaged byte for byte.
        for path in resources.rglob('*'):
            if path.is_file():
                entry = path.relative_to(resources).as_posix()
                assert entry in names, f'{jar}: missing {entry}'
                if game not in OBFUSCATED or entry not in REMAPPED[loader]:
                    assert z.read(entry) == path.read_bytes(), f'{jar}: stale {entry}'
        for name, output in RECIPES.items():
            recipe = json.loads(z.read(f'data/{MOD_ID}/recipe/{name}.json'))
            assert recipe['result']['id'] == output, f'{name}: result {recipe["result"]}'
        for name in BLOCKS:
            loot = json.loads(z.read(f'data/{MOD_ID}/loot_table/blocks/{name}.json'))
            assert loot['pools'][0]['entries'][0]['name'] == f'{MOD_ID}:{name}'
        assert not any(n.startswith(f'data/{MOD_ID}/recipes/') or n.startswith(f'data/{MOD_ID}/loot_tables/') for n in names), \
            'pre-1.21 data folder names'
        for weapon in weapons:
            for part in WEAPON_PARTS:
                assert f'assets/{MOD_ID}/models/gun/{weapon}/{part}.json' in names
            assert f'assets/{MOD_ID}/animations/gun/{weapon}.json' in names
        assert len(weapons) == 16, f'expected 16 weapons, found {len(weapons)}'
        pack = json.loads(z.read('pack.mcmeta'))['pack']
        if 'min_format' in pack:  # 26.x: [major, minor] or major
            low, high = ((f[0] if isinstance(f, list) else f) for f in (pack['min_format'], pack['max_format']))
        else:
            formats = pack.get('supported_formats', {'min_inclusive': pack['pack_format'], 'max_inclusive': pack['pack_format']})
            low, high = formats['min_inclusive'], formats['max_inclusive']
        for fmt in PACK_FORMATS[game]:
            assert low <= fmt <= high, f'pack.mcmeta does not accept format {fmt}'

        {'neoforge': verify_neoforge, 'fabric': verify_fabric}[loader](z, names, game, version)
        assert f'LICENSE_{MOD_ID}' in names and f'NOTICE_{MOD_ID}' in names, 'license files not packaged'
        # Unit tests and the development-only smoke-test mod never ship.
        assert not any(n.endswith('Test.class') for n in names)
        assert not any('/smoketest/' in n for n in names), 'smoke-test classes packaged'
    data = jar.read_bytes()
    return {'file': jar.name, 'loader': loader, 'game': game, 'version': version,
            'bytes': len(data), 'sha1': hashlib.sha1(data).hexdigest(),
            'sha256': hashlib.sha256(data).hexdigest(), 'sha512': hashlib.sha512(data).hexdigest(),
            'required_dependencies': sorted(REQUIRED[loader])}


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--version', required=True)
    parser.add_argument('--jar-dir', default='releases')
    parser.add_argument('--output')
    parser.add_argument('--game', action='append', choices=GAMES, help='verify only these workspaces (default: all)')
    args = parser.parse_args()
    manifests = [verify(Path(args.jar_dir) / f'{MOD_ID}-{loader}-{game}-{args.version}.jar', loader, game, args.version)
                 for game in args.game or GAMES for loader in ('neoforge', 'fabric')]
    output = json.dumps(manifests, indent=2) + '\n'
    if args.output:
        Path(args.output).write_text(output)
    print(output)
