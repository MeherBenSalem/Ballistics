"""Report a scripted client run (<version>/common/src/smoketest) as GitHub annotations and contact sheets.

    python3 scripts/smoketest_report.py --log smoketest-output.log --run-dir 1.21.1/neoforge/run/smoketest --out smoketest \
        --prefix 1.21.1-neoforge

Reads <run-dir>/smoketest/result.json written by the run. Emits an error annotation per failed check, a warning
per distinct log line our mod (or the model, sound and texture loaders about our files) logged at WARN or above,
and a summary notice. With Pillow installed, it also builds one JPEG contact sheet per weapon (hip, aim, fire,
reload, inspect, third person) plus the title and screen captures, in <out>/. Exits non-zero unless the run passed.
"""
import argparse
import json
import os
import re
import sys

LEVEL = re.compile(r'/(WARN|ERROR|FATAL)\]')
OURS = re.compile(r'remnants_ballistics|nightbeam\.remnants|BallisticsSmokeTest', re.IGNORECASE)
# Loader noise that mentions every mod and says nothing about ours.
IGNORED = re.compile(r'Failed to open OpenAL device|OpenAL|Missing sound for event: minecraft|realms|'
                     r'ALSA|Reported exception thrown|Narrator|joystick', re.IGNORECASE)


def escape(text):
    return text.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')


def annotate(level, title, message):
    print(f'::{level} title={escape(title).replace(":", "%3A").replace(",", "%2C")}::{escape(message)}')


def log_findings(path):
    if not os.path.exists(path):
        return [], []
    with open(path, encoding='utf-8', errors='replace') as f:
        lines = [l.rstrip('\n') for l in f]
    warnings = []
    crash = []
    for i, line in enumerate(lines):
        if LEVEL.search(line) and OURS.search(line) and not IGNORED.search(line):
            # Strip the timestamp so repeated messages collapse.
            warnings.append(re.sub(r'^\[[^\]]*\] ', '', line)[:400])
        if 'Exception' in line or 'Caused by' in line or '---- Minecraft Crash Report ----' in line:
            crash.extend(lines[i:i + 6])
    return list(dict.fromkeys(warnings)), list(dict.fromkeys(crash))


def contact_sheets(run_dir, shots, out, prefix):
    try:
        from PIL import Image, ImageDraw
    except ImportError:
        print('Pillow not installed; skipping contact sheets')
        return []
    shots_dir = os.path.join(run_dir, 'screenshots')
    groups = {}
    singles = []
    for name in shots:
        m = re.match(r'(\d\d_[a-z0-9_]+?)_(\d)_[a-z_]+\.png$', name)
        if m:
            groups.setdefault(m.group(1), []).append(name)
        else:
            singles.append(name)
    written = []
    tile_w, tile_h = 640, 360
    for key, names in sorted(groups.items()):
        names.sort()
        cols = 3
        rows = (len(names) + cols - 1) // cols
        sheet = Image.new('RGB', (tile_w * cols, tile_h * rows), (16, 16, 18))
        draw = ImageDraw.Draw(sheet)
        for i, name in enumerate(names):
            path = os.path.join(shots_dir, name)
            if not os.path.exists(path):
                continue
            img = Image.open(path).convert('RGB').resize((tile_w, tile_h), Image.LANCZOS)
            x, y = (i % cols) * tile_w, (i // cols) * tile_h
            sheet.paste(img, (x, y))
            label = re.sub(r'^\d\d_[a-z0-9_]+?_\d_', '', name[:-4]).replace('_', ' ')
            draw.rectangle([x, y, x + 8 + 7 * len(label), y + 16], fill=(0, 0, 0))
            draw.text((x + 4, y + 2), label, fill=(255, 255, 255))
        target = os.path.join(out, f'smoke-{prefix}{key}.jpg')
        sheet.save(target, quality=86)
        written.append(target)
    for name in singles:
        path = os.path.join(shots_dir, name)
        if os.path.exists(path):
            target = os.path.join(out, f'smoke-{prefix}' + name[:-4] + '.jpg')
            Image.open(path).convert('RGB').save(target, quality=88)
            written.append(target)
    return written


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--log', required=True)
    parser.add_argument('--run-dir', required=True)
    parser.add_argument('--out', required=True)
    parser.add_argument('--prefix', default='', help='target name for file names and titles, e.g. 1.21.1-fabric')
    args = parser.parse_args()
    os.makedirs(args.out, exist_ok=True)
    prefix = args.prefix + '-' if args.prefix else ''
    title = f'Client smoke test ({args.prefix})' if args.prefix else 'Client smoke test'

    warnings, crash = log_findings(args.log)
    for line in warnings[:30]:
        annotate('warning', f'Client log ({args.prefix or "smoke test"})', line)
    if len(warnings) > 30:
        annotate('warning', f'Client log ({args.prefix or "smoke test"})', f'{len(warnings) - 30} more warnings not shown')

    result_path = os.path.join(args.run_dir, 'smoketest', 'result.json')
    if not os.path.exists(result_path):
        excerpt = '\n'.join(crash[:120]) or 'no exception found in the log'
        annotate('error', title, 'The client exited without writing a result (crash or hang).\n' + excerpt)
        sys.exit(1)
    with open(result_path, encoding='utf-8') as f:
        result = json.load(f)
    for failure in result.get('failures', []):
        annotate('error', title, failure)
    sheets = contact_sheets(args.run_dir, result.get('screenshots', []), args.out, prefix)
    summary = (f"{result.get('status')} in {result.get('seconds')} s: {len(result.get('failures', []))} failed checks, "
               f"{len(result.get('screenshots', []))} screenshots, {len(sheets)} images, {len(warnings)} log warnings. "
               + '; '.join(result.get('notes', [])))
    annotate('notice', title, summary)
    with open(os.path.join(args.out, f'summary-{args.prefix or "smoke"}.txt'), 'w', encoding='utf-8') as f:
        f.write(summary + '\n')
    sys.exit(0 if result.get('status') == 'PASS' else 1)


if __name__ == '__main__':
    main()
