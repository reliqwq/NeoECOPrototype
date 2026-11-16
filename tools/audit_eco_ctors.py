"""Constructor-descriptor ruler: every eco constructor our compiled bytecode invokes, checked against
an eco jar's declared constructors.

Why this exists separately from tools/audit_eco_abi.py: javap prints a constructor under its class name,
so `<init>` can never match there, and a record gaining one component (eco 21.2.15-beta's
StorageHostActionUI$Config, 13 -> 14) breaks an addon at runtime with NoSuchMethodError while the member
audit reports both sides identical. This ruler compares descriptors only, which is exactly the class of
change the member ruler is blind to.

Run from the repository root:
    python tools/audit_eco_ctors.py [path-to-eco.jar ...]
"""
import os
import re
import shutil
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

# javap is not on PATH on the author machine; resolve it the same way the repo does elsewhere:
# $JAVAP, then PATH, then the local JDK.
JAVAP = os.environ.get("JAVAP") or shutil.which("javap") or r"F:\java\bin\javap.exe"
CLASSES = Path("build/classes/java/main")
JAR_PREFIX = "cn/dancingsnow/neoecoae/"

CTOR_CALL = re.compile(r'// (?:Method|InterfaceMethod) (cn/dancingsnow/neoecoae/[\w/$/]+)'
                       r'."<init>":(\S+?)V')
CLASS_HEAD = re.compile(r"^(?:public |final |abstract |static |strictfp )*?"
                        r"(?:class|interface|record|enum) ([\w.$]+)")
CTOR_DECL = re.compile(r"^\s+(?:public |protected |private )?([\w.$]+)\((.*)\);\s*$")


def javap(args):
    return subprocess.run([JAVAP] + args, capture_output=True, text=True, errors="replace").stdout


def our_ctor_calls():
    """(owner, descriptor) for every eco constructor our own bytecode invokes."""
    calls = set()
    for class_file in sorted(CLASSES.rglob("*.class")):
        owner = str(class_file.relative_to(CLASSES).with_suffix("")).replace("\\", "/").replace("/", ".")
        out = javap(["-c", "-p", "-cp", str(CLASSES), owner])
        for match in CTOR_CALL.finditer(out):
            calls.add((match.group(1).replace("/", "."), match.group(2)))
    return calls


def jar_ctor_declarations(jar):
    """Every constructor an eco class declares in this jar, as {class: {descriptor}}.

    javap -p prints the parameters in source notation (dotted names, primitives by word), while our
    bytecode reference is a JVM descriptor - so both sides are put in descriptor form here or nothing
    ever compares.

    An unreadable jar is refused rather than reported as "everything missing": a path that does not
    exist yields an empty listing, and an empty listing once printed 56 missing against a jar that was
    simply not on this machine.
    """
    listing = subprocess.run(["unzip", "-Z1", jar], capture_output=True, text=True)
    if listing.returncode != 0:
        raise SystemExit(f"cannot read {jar}: {listing.stdout.strip() or listing.stderr.strip()}")
    entries = listing.stdout.split()
    if not entries:
        raise SystemExit(f"{jar} lists no entries at all")
    names = sorted(n[:-6].replace("/", ".") for n in entries
                   if n.endswith(".class") and n.startswith(JAR_PREFIX))
    if not names:
        raise SystemExit(f"{jar} contains no cn/dancingsnow/neoecoae classes - wrong artifact?")
    declared = defaultdict(set)
    current = None
    for chunk_start in range(0, len(names), 200):
        out = javap(["-p", "-cp", jar] + names[chunk_start:chunk_start + 200])
        for line in out.splitlines():
            head = CLASS_HEAD.match(line)
            if head:
                current = head.group(1)
                continue
            if current is None or not line.startswith(" "):
                continue
            decl = CTOR_DECL.match(line)
            if decl and decl.group(1).rsplit(".", 1)[-1] == current.rsplit(".", 1)[-1]:
                declared[current].add("(" + params_descriptor(decl.group(2)) + ")")
    return declared


PRIMITIVES = {"byte": "B", "char": "C", "double": "D", "float": "F", "int": "I",
              "long": "J", "short": "S", "boolean": "Z", "void": "V"}


def type_descriptor(name):
    name = name.strip()
    array = 0
    while name.endswith("[]"):
        array += 1
        name = name[:-2].strip()
    if name.endswith("..."):
        array += 1
        name = name[:-3].strip()
    if name in PRIMITIVES:
        base = PRIMITIVES[name]
    else:
        base = "L" + re.sub(r"<.*>", "", name).replace(".", "/") + ";"
    return "[" * array + base


def params_descriptor(params):
    params = params.strip()
    if not params:
        return ""
    parts = re.split(r",(?![^<]*>)", params)
    return "".join(type_descriptor(re.sub(r"<[^<>]*>", "", part)) for part in parts)


def main():
    jars = sys.argv[1:] or [JAR_PREFIX and "neoecobeta/neoecoae-21.2.15-beta.jar"]
    calls = our_ctor_calls()
    print(f"eco constructor descriptors our bytecode names: {len(calls)}")
    for jar in jars:
        declared = jar_ctor_declarations(jar)
        missing = sorted((owner, desc) for owner, desc in calls
                         if desc not in declared.get(owner, set()))
        print(f"  {jar}: {len(missing)} missing")
        for owner, desc in missing[:20]:
            print(f"    {owner} ({desc})V")


if __name__ == "__main__":
    main()
