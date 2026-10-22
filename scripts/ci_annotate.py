"""Turn the important part of a Gradle log into GitHub annotations.

    python3 scripts/ci_annotate.py build-output.log [title]

Emits one summary annotation holding every
compiler error, failed test, failed GameTest and "What went wrong" block, then up to 8 per-line annotations for javac
errors (file and line linked), so failures are readable from the run summary
page and the checks API without downloading the raw log.
"""
import os
import re
import sys

JAVAC = re.compile(r'^(?P<path>/\S+\.java):(?P<line>\d+): error: (?P<msg>.*)$')
INTERESTING = re.compile(r'(error:|warning: \[removal\]|FAILED|What went wrong|Caused by|Exception|failed|> Task .* FAILED|Test .* failed|'
                         r'GameTest|required|^\s+\^|^\s*symbol:|^\s*location:)', re.IGNORECASE)
MAX_SUMMARY = 60000


def escape(text):
    return text.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')


def escape_property(text):
    return escape(text).replace(':', '%3A').replace(',', '%2C')


def main():
    path = sys.argv[1]
    title = sys.argv[2] if len(sys.argv) > 2 else 'Build log excerpt'
    workspace = os.environ.get('GITHUB_WORKSPACE', '')
    try:
        with open(path, encoding='utf-8', errors='replace') as f:
            lines = [l.rstrip('\n') for l in f]
    except FileNotFoundError:
        print(f'::error title={escape_property(title)}::{escape(path)} was not written')
        return
    per_line = []
    keep = []
    i = 0
    while i < len(lines):
        line = lines[i]
        m = JAVAC.match(line)
        if m:
            rel = m.group('path')
            if workspace and rel.startswith(workspace):
                rel = rel[len(workspace):].lstrip('/')
            per_line.append(f'::error file={escape_property(rel)},line={m.group("line")}::{escape(m.group("msg"))}')
            keep.extend(lines[i:i + 5])
            i += 5
            continue
        if 'What went wrong' in line:
            keep.extend(lines[i:i + 12])
            i += 12
            continue
        if INTERESTING.search(line):
            keep.append(line)
        i += 1
    excerpt = '\n'.join(dict.fromkeys(keep)) if keep else '\n'.join(lines[-80:])
    if len(excerpt) > MAX_SUMMARY:
        excerpt = excerpt[:MAX_SUMMARY] + '\n[truncated]'
    # GitHub keeps 10 error annotations per step: the summary goes first, then up to 8 linked lines.
    print(f'::error title={escape_property(title)}::{escape(excerpt)}')
    for annotation in list(dict.fromkeys(per_line))[:8]:
        print(annotation)


if __name__ == '__main__':
    main()
