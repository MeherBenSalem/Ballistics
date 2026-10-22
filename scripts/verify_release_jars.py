"""Verify the packaged release jar against the generated resources and record release hashes.

This is an artifact audit, separate from the unit tests (./gradlew test), the in-game GameTests
(./gradlew runGameTestServer) and the source-level asset checks (tools/assets/check_assets.py).

    python scripts/verify_release_jars.py --version 0.3.0 --jar-dir releases --output releases/manifest.json
"""
import argparse
import hashlib
import json
from pathlib import Path
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
MOD_ID = 'remnants_ballistics'
TARGETS = [('neoforge', '1.21.1')]
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
    resources = ROOT / game / 'src/main/resources'
    weapons = sorted(p.stem for p in (resources / f'assets/{MOD_ID}/gun_visuals').glob('*.json'))
    with zipfile.ZipFile(jar) as z:
        names = z.namelist()
        assert len(names) == len(set(names)), f'{jar}: duplicate entries'
        # Every generated resource is packaged byte for byte.
        for path in resources.rglob('*'):
            if path.is_file():
                entry = path.relative_to(resources).as_posix()
                assert entry in names, f'{jar}: missing {entry}'
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
        for fmt in (34, 48):  # 1.21.1 resource and data pack formats
            assert formats['min_inclusive'] <= fmt <= formats['max_inclusive']
        descriptor = tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
        assert descriptor['mods'][0]['modId'] == MOD_ID
        assert descriptor['mods'][0]['version'] == version
        assert descriptor['mods'][0]['logoFile'] == 'logo.png' and 'logo.png' in names
        deps = descriptor['dependencies'][MOD_ID]
        required = {d['modId'] for d in deps if d.get('mandatory') or d.get('type') == 'required'}
        assert required == {loader, 'minecraft'}, f'unexpected required dependencies {required}'
        assert next(d['versionRange'] for d in deps if d['modId'] == 'minecraft') == f'[{game}]'
        assert f'LICENSE_{MOD_ID}' in names and f'NOTICE_{MOD_ID}' in names, 'license files not packaged'
        # Unit tests never ship.
        assert not any(n.endswith('Test.class') for n in names)
    data = jar.read_bytes()
    return {'file': jar.name, 'loader': loader, 'game': game, 'version': version,
            'bytes': len(data), 'sha1': hashlib.sha1(data).hexdigest(),
            'sha256': hashlib.sha256(data).hexdigest(), 'sha512': hashlib.sha512(data).hexdigest(),
            'required_dependencies': []}


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--version', required=True)
    parser.add_argument('--jar-dir', default='releases')
    parser.add_argument('--output')
    args = parser.parse_args()
    manifests = [verify(Path(args.jar_dir) / f'{MOD_ID}-{loader}-{game}-{args.version}.jar', loader, game, args.version)
                 for loader, game in TARGETS]
    output = json.dumps(manifests, indent=2) + '\n'
    if args.output:
        Path(args.output).write_text(output)
    print(output)
