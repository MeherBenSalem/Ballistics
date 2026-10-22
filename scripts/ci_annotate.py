"""Turn the important part of a Gradle log into GitHub annotations.

    python3 scripts/ci_annotate.py build-output.log [title]

Emits up to 10 per-line annotations for javac errors (file and line linked) and one summary annotation holding every
compiler error, failed test, failed GameTest and "What went wrong" block, so failures are readable from the run summary
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
    per_line = 0
    keep = []
    i = 0
    while i < len(lines):
        line = lines[i]
        m = JAVAC.match(line)
        if m:
            rel = m.group('path')
            if workspace and rel.startswith(workspace):
                rel = rel[len(workspace):].lstrip('/')
            if per_line < 10:
                print(f'::error file={escape_property(rel)},line={m.group("line")}::{escape(m.group("msg"))}')
                per_line += 1
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
    print(f'::error title={escape_property(title)}::{escape(excerpt)}')


if __name__ == '__main__':
    main()
