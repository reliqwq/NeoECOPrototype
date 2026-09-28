"""Re-cut the built-in fallback resource pack straight from a released tag.

The result lands in `src/main/resources/legacy_art/`, ships inside the jar, and is offered to the
player by `NeoECOPrototypeClient#onAddPackFinders` -- so "go back to the previous look" is a toggle in
the resource pack screen rather than something they have to download.

The pack carries blockstates as well as models and textures, which is what makes it need no
maintenance: a shipped blockstate only names the properties of its own era, and Minecraft matches
variant keys as subsets, so states this version added later fall through to the old wildcard keys
instead of resolving to nothing (measured: v1.2.7's 16 storage-host keys cover all 32 states the
current block can build, exactly once each). Dropping a property from a block would break that -- the
old key would name a property the block no longer has, get discarded, and really would hide the
block -- so re-run this script whenever a release removes one.

Usage:  python tools/legacy_pack.py v1.2.8
"""
import glob
import itertools
import json
import os
import re
import shutil
import subprocess
import sys
import zipfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
NS = "neoecoprototype"
SRC = f"src/main/resources/assets/{NS}"
PACK = os.path.join(REPO, "src", "main", "resources", "legacy_art")
PARTS = ("models", "textures", "blockstates")


def extract(tag):
    scratch = os.path.join(REPO, "build", "legacy-pack-tmp")
    if os.path.isdir(scratch):
        shutil.rmtree(scratch)
    os.makedirs(scratch)
    archive = subprocess.run(["git", "archive", "--format=zip", tag] + [f"{SRC}/{p}" for p in PARTS],
                             capture_output=True, cwd=REPO)
    if archive.returncode:
        sys.exit(archive.stderr.decode("utf-8", "replace"))
    blob = os.path.join(scratch, "shipped.zip")
    with open(blob, "wb") as fh:
        fh.write(archive.stdout)
    with zipfile.ZipFile(blob) as zh:
        zh.extractall(scratch)
    return os.path.join(scratch, *SRC.split("/"))


def build(source):
    if os.path.isdir(PACK):
        shutil.rmtree(PACK)
    os.makedirs(PACK)
    with open(os.path.join(PACK, "pack.mcmeta"), "w", encoding="utf-8") as fh:
        json.dump({"pack": {"description": "Neo ECO Prototype legacy art", "pack_format": 34}},
                  fh, ensure_ascii=False, indent=4)
    write_cover()
    for part in PARTS:
        src = os.path.join(source, part)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(PACK, "assets", NS, part))


def write_cover():
    """A pack without pack.png gets Minecraft's default stone tile as its icon, which reads as
    "broken texture" to everyone. The mod logo is not square, so centre-crop before downscaling."""
    logo = os.path.join(REPO, "src", "main", "resources", "logo.png")
    if not os.path.exists(logo):
        print("no src/main/resources/logo.png; the pack will show Minecraft's default stone icon")
        return
    from PIL import Image
    im = Image.open(logo).convert("RGBA")
    side = min(im.size)
    left = (im.width - side) // 2
    top = (im.height - side) // 2
    im.crop((left, top, left + side, top + side)).resize((64, 64), Image.LANCZOS).save(
        os.path.join(PACK, "pack.png"))


def check_self_contained():
    root = os.path.join(PACK, "assets", NS)
    models = {os.path.relpath(os.path.join(dp, f), os.path.join(root, "models")).replace(os.sep, "/")[:-5]
              for dp, _, fs in os.walk(os.path.join(root, "models")) for f in fs if f.endswith(".json")}
    broken = []
    for dp, _, fs in os.walk(os.path.join(root, "blockstates")):
        for f in fs:
            for ref in re.findall(r'"model"\s*:\s*"([^"]+)"', open(os.path.join(dp, f), encoding="utf-8").read()):
                if ref.split(":")[-1] not in models:
                    broken.append((f, ref))
    return models, broken


def check_states_still_resolve():
    """Every combination the shipped keys describe must be matched by one of them, so a reader can
    see the file is internally complete before trusting it against the block's real state set."""
    problems = []
    root = os.path.join(PACK, "assets", NS, "blockstates")
    for f in sorted(os.listdir(root)):
        keys = json.load(open(os.path.join(root, f), encoding="utf-8")).get("variants", {})
        pairs = {k: dict(p.split("=", 1) for p in k.split(",") if p) for k in keys}
        names = sorted({p.split("=")[0] for k in keys for p in k.split(",") if p})
        if not names:
            continue
        axes = {n: sorted({v for one in pairs.values() for k, v in one.items() if k == n}) or ["*"] for n in names}
        states = [dict(zip(axes, combo)) for combo in itertools.product(*axes.values())]
        unmatched = [s for s in states
                     if not any(all(s.get(a) == b for a, b in one.items()) for one in pairs.values())]
        if unmatched:
            problems.append(f"{f}: {len(unmatched)} of {len(states)} combinations match no key")
    return problems


def main():
    tag = sys.argv[1] if len(sys.argv) > 1 else "v1.2.8"
    build(extract(tag))
    models, broken = check_self_contained()
    counts = {p: sum(len(fs) for _, _, fs in os.walk(os.path.join(PACK, "assets", NS, p))) for p in PARTS}
    print(f"{tag} -> src/main/resources/legacy_art/  {counts}")
    print("dangling blockstate references:", len(broken), broken[:3])
    stale = check_states_still_resolve()
    print("internally incomplete blockstates:", stale if stale else "none")
    for leftover in glob.glob(os.path.join(REPO, "build", "legacy-pack-tmp*")):
        shutil.rmtree(leftover, ignore_errors=True)


if __name__ == "__main__":
    main()
