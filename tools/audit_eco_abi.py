"""ABI audit: eco members our compiled classes reference that the given eco jar does not declare.

A missing member is what produces NoSuchMethodError / NoSuchFieldError at runtime, but the printed
list is not a verdict on its own. Members declared on a supertype (Block.defaultBlockState,
Object.getClass) do not appear in javap of the named owner, and javap prints a constructor under the
class name, so `<init>` can never match. Auditing classes compiled against this same jar therefore
reports only false positives -- javac already proved those references resolve.

The check carries information when the two sides differ: compiled against one eco jar, audited
against another. Then compare against a jar you already ship green rather than reading it absolutely.

Run from the repository root:
    python tools/audit_eco_abi.py [path-to-eco.jar]

Without an argument it audits the jar build.gradle puts on the compile classpath, so the target
cannot silently drift behind an eco sync.
"""

import re
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

JAVAP = r"F:\java\bin\javap.exe"
CLASSES = Path("build/classes/java/main")
ECO_PREFIX = "cn/dancingsnow/neoecoae/"

# javap -c renders a resolved reference as:  // Method owner.name:(desc)ret
REF = re.compile(r"//\s*(?:Method|InterfaceMethod|Field)\s+(cn/dancingsnow/neoecoae/[^\s.]+)\.([^\s:(]+):(\S+)")


def jar_classpath(jar):
    libs = [str(p) for p in sorted(Path("libs").glob("*.jar"))]
    return ";".join([str(CLASSES), str(jar), *libs])


def collect_refs(cp):
    refs = set()
    class_files = sorted(CLASSES.rglob("*.class"))
    for path in class_files:
        out = subprocess.run(
            [JAVAP, "-c", "-p", "-cp", cp, str(path)],
            capture_output=True, text=True, errors="replace",
        ).stdout
        for owner, name, desc in REF.findall(out):
            refs.add((owner, name, desc))
    return refs, len(class_files)


def eco_members(cp, owner, cache):
    if owner in cache:
        return cache[owner]
    binary = owner.replace("/", ".")
    out = subprocess.run(
        [JAVAP, "-p", "-s", "-cp", cp, binary],
        capture_output=True, text=True, errors="replace",
    ).stdout
    members = set()
    pending = None
    for line in out.splitlines():
        stripped = line.strip()
        if stripped.startswith("descriptor:"):
            if pending:
                members.add(f"{pending}:{stripped.split(':', 1)[1].strip()}")
                pending = None
            continue
        m = re.match(r"^(?:[\w<>,\[\] .$?]+)\s+([\w$<>]+)\s*\(", stripped)
        if m:
            pending = m.group(1)
            continue
        m = re.match(r"^(?:[\w<>,\[\] .$?]+)\s+([\w$]+);?$", stripped)
        if m and not stripped.endswith("{"):
            pending = m.group(1)
    cache[owner] = members
    return members


def main():
    if len(sys.argv) > 1:
        jar = Path(sys.argv[1])
    else:
        declared = re.search(r'files\("([^"]*neoecoae[^"]*\.jar)"\)',
                             Path("build.gradle").read_text(encoding="utf-8"))
        if declared is None:
            raise SystemExit("no eco jar declared in build.gradle; pass its path as an argument")
        jar = Path(declared.group(1))
    if not jar.exists():
        raise SystemExit(f"eco jar not found: {jar}")
    cp = jar_classpath(jar)
    refs, class_count = collect_refs(cp)
    print(f"scanned {class_count} compiled classes; eco references: {len(refs)}")

    cache = {}
    missing = defaultdict(list)
    for owner, name, desc in sorted(refs):
        members = eco_members(cp, owner, cache)
        if f"{name}:{desc}" not in members:
            missing[owner].append(f"{name}{desc}")

    if not missing:
        print(f"OK: every referenced eco member exists in {jar.name}")
        return 0

    print(f"MISSING members in {jar.name}:")
    for owner, names in sorted(missing.items()):
        print(f"  {owner.replace('/', '.')}")
        for entry in sorted(set(names)):
            print(f"     {entry}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
