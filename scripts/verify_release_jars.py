"""Verify the packaged release jars against the generated resources and record release hashes.

This is an artifact audit, separate from the unit tests (./gradlew :common:test), the in-game GameTests
(:neoforge:runGameTestServer, :fabric:runGametest), the client smoke tests and the source-level asset checks
(tools/assets/check_assets.py).

    python scripts/verify_release_jars.py --version 0.3.0 --jar-dir releases --output releases/manifest.json
    python scripts/verify_release_jars.py --version 0.3.0 --jar-dir releases --targets neoforge:1.21.1

Without --targets every supported target must be present.
"""
import argparse
import hashlib
import json
from pathlib import Path
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
MOD_ID = 'remnants_ballistics'
TARGETS = [('neoforge', '1.21.1'), ('fabric', '1.21.1')]
# Resource and data pack formats each Minecraft version must accept.
PACK_FORMATS = {'1.21.1': (34, 48)}
# Mods every jar must declare as required (besides the loader and Minecraft).
LOADER_DEPENDENCIES = {'neoforge': set(), 'forge': set(), 'fabric': {'fabric-api', 'forgeconfigapiport'}}
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
                if entry.endswith('.accesswidener') and loader == 'fabric':
                    verify_access_widener(z.read(entry).decode(), path.read_text(), jar)
                else:
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
        formats = pack.get('supported_formats', {'min_inclusive': pack['pack_format'], 'max_inclusive': pack['pack_format']})
        for fmt in PACK_FORMATS[game]:
            assert formats['min_inclusive'] <= fmt <= formats['max_inclusive'], f'{jar}: pack format {fmt} not supported'
        assert 'logo.png' in names
        required = verify_descriptor(z, names, loader, game, version)
        # Every loader module lists its platform services.
        for service in ('IPlatform', 'INetwork'):
            entry = f'META-INF/services/com.nightbeam.remnants.ballistics.platform.{service}'
            assert entry in names and f'.{loader}.' in z.read(entry).decode(), f'{jar}: {entry} missing or for another loader'
        assert f'LICENSE_{MOD_ID}' in names and f'NOTICE_{MOD_ID}' in names, 'license files not packaged'
        # Unit tests and the development-only smoke test never ship.
        assert not any(n.endswith('Test.class') for n in names)
        assert not any('/smoketest/' in n or n.startswith('remnants_ballistics_smoketest') for n in names), f'{jar}: smoke test packaged'
        assert not any(f'/{other}/' in n for n in names if n.endswith('.class') for other in ('neoforge', 'fabric', 'forge') if other != loader), \
            f'{jar}: classes of another loader packaged'

    data = jar.read_bytes()
    return {'file': jar.name, 'loader': loader, 'game': game, 'version': version,
            'bytes': len(data), 'sha1': hashlib.sha1(data).hexdigest(),
            'sha256': hashlib.sha256(data).hexdigest(), 'sha512': hashlib.sha512(data).hexdigest(),
            'required_dependencies': sorted(required)}


def verify_access_widener(packaged, source, jar):
    """Loom remaps the access widener to the runtime namespace: compare the rules, not the names."""
    def rules(text):
        lines = [l.split('#')[0].split() for l in text.splitlines()]
        return [l for l in lines if l]
    packaged_rules, source_rules = rules(packaged), rules(source)
    assert packaged_rules[0][:2] == source_rules[0][:2], f'{jar}: access widener header {packaged_rules[0]}'
    assert [r[:2] for r in packaged_rules[1:]] == [r[:2] for r in source_rules[1:]], f'{jar}: access widener rules differ'


def verify_descriptor(z, names, loader, game, version):
    """Checks the loader's mod descriptor; returns the required mods besides the loader and Minecraft."""
    if loader == 'fabric':
        descriptor = json.loads(z.read('fabric.mod.json'))
        assert descriptor['id'] == MOD_ID and descriptor['version'] == version
        assert descriptor['icon'] == 'logo.png'
        depends = descriptor['depends']
        assert depends['minecraft'] == game, f'fabric.mod.json targets {depends["minecraft"]}'
        required = set(depends) - {'fabricloader', 'minecraft', 'java'}
        for mixins in descriptor.get('mixins', []):
            config = json.loads(z.read(mixins))
            if 'refmap' in config:
                assert config['refmap'] in names, f'{mixins}: refmap {config["refmap"]} not packaged'
        for entrypoints in descriptor['entrypoints'].values():
            for entry in entrypoints:
                assert entry.replace('.', '/') + '.class' in names, f'entry point {entry} missing'
    else:
        path = 'META-INF/neoforge.mods.toml' if loader == 'neoforge' else 'META-INF/mods.toml'
        descriptor = tomllib.loads(z.read(path).decode())
        assert descriptor['mods'][0]['modId'] == MOD_ID
        assert descriptor['mods'][0]['version'] == version
        assert descriptor['mods'][0]['logoFile'] == 'logo.png'
        deps = descriptor['dependencies'][MOD_ID]
        required = {d['modId'] for d in deps if d.get('mandatory') or d.get('type') == 'required'} - {loader, 'minecraft'}
        assert next(d['versionRange'] for d in deps if d['modId'] == 'minecraft') == f'[{game}]'
    assert required == LOADER_DEPENDENCIES[loader], f'{loader}: unexpected required dependencies {required}'
    return required


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--version', required=True)
    parser.add_argument('--jar-dir', default='releases')
    parser.add_argument('--output')
    parser.add_argument('--targets', help='comma-separated loader:game pairs (default: every supported target)')
    args = parser.parse_args()
    targets = [tuple(t.split(':')) for t in args.targets.split(',')] if args.targets else TARGETS
    try:
        manifests = [verify(Path(args.jar_dir) / f'{MOD_ID}-{loader}-{game}-{args.version}.jar', loader, game, args.version)
                     for loader, game in targets]
    except (AssertionError, FileNotFoundError, KeyError) as e:
        # A GitHub annotation, so the reason is readable from the run summary.
        print(f'::error title=Jar verification::{type(e).__name__}: {e}')
        raise
    output = json.dumps(manifests, indent=2) + '\n'
    if args.output:
        Path(args.output).write_text(output)
    print(output)
