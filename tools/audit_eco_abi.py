"""ABI audit: every eco member our compiled classes reference must exist in the target eco jar.

A missing member is exactly what produces NoSuchMethodError / NoSuchFieldError at runtime, so this
checks the whole addon instead of the one call site a player happened to hit.

Run from the repository root:
    python tools/audit_eco_abi.py neoecobeta/neoecoae-21.2.0-beta3.jar
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
    jar = Path(sys.argv[1] if len(sys.argv) > 1 else "neoecobeta/neoecoae-21.2.0-beta3.jar")
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
