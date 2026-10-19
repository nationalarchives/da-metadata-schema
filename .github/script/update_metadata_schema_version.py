#!/usr/bin/env python3
"""
Update the da-metadata-schema dependency version in an sbt project.
"""
import os
import pathlib
import re
import sys

ARTIFACT = r'"da-metadata-schema(?:_[0-9.]+)?"'
LITERAL_RE = re.compile(r'(' + ARTIFACT + r'\s*%\s*")[^"]+(")')
IDENT_RE = re.compile(ARTIFACT + r'\s*%\s*([A-Za-z_][A-Za-z0-9_.]*)')
EXCLUDED_DIRS = {".git", "target"}


def find_build_files(root: pathlib.Path):
    return [
        p for p in root.rglob("*")
        if p.is_file() and p.suffix in (".scala", ".sbt")
        and not any(part in EXCLUDED_DIRS for part in p.parts)
    ]


def main() -> int:
    version = os.environ.get("VERSION")
    if not version:
        print("VERSION environment variable is required", file=sys.stderr)
        return 1
    repo = os.environ.get("REPOSITORY", "repository")

    contents = {p: p.read_text() for p in find_build_files(pathlib.Path("."))}

    found = False
    idents = set()
    for text in contents.values():
        if LITERAL_RE.search(text):
            found = True
        for m in IDENT_RE.finditer(text):
            found = True
            idents.add(m.group(1).split(".")[-1])

    if not found:
        print(f"No da-metadata-schema dependency found in {repo}", file=sys.stderr)
        return 1

    val_res = {
        i: re.compile(r'(\bva[lr]\s+' + re.escape(i) + r'\s*(?::\s*String\s*)?=\s*")[^"]+(")')
        for i in idents
    }

    def replace(m):
        return m.group(1) + version + m.group(2)

    resolved = set()
    for path, text in contents.items():
        new = LITERAL_RE.sub(replace, text)
        for ident, regex in val_res.items():
            if regex.search(new):
                resolved.add(ident)
                new = regex.sub(replace, new)
        if new != text:
            path.write_text(new)
            print(f"Updated {path}")

    missing = idents - resolved
    if missing:
        print(
            f"Could not find version definition for: {', '.join(sorted(missing))} in {repo}",
            file=sys.stderr,
        )
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
