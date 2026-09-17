"""Release audit: orphan textures, missing item models, and cross-namespace references.

Run from the repository root:  python tools/audit_assets.py
"""

import json
import re
from pathlib import Path

ASSETS = Path("src/main/resources/assets/neoecoprototype")
MODELS = ASSETS / "models"
TEXTURES = ASSETS / "textures"

TEXTURE_REF = re.compile(r'"textures"\s*:\s*\{(.*?)\}', re.S)
VALUE_REF = re.compile(r'"([^"]+)"\s*:\s*"([^"]+)"')


def model_texture_refs():
    refs = []
    for path in MODELS.rglob("*.json"):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as error:
            print(f"BAD JSON {path}: {error}")
            continue
        for _, value in (data.get("textures") or {}).items():
            refs.append((path, value))
    return refs


def main():
    refs = model_texture_refs()

    missing = []
    foreign = []
    referenced = set()
    for path, value in refs:
        if ":" not in value:
            continue
        namespace, rest = value.split(":", 1)
        referenced.add(value)
        if namespace != "neoecoprototype":
            foreign.append((path, value))
            continue
        if not (TEXTURES / f"{rest}.png").exists():
            missing.append((path, value))

    print("=== missing textures referenced by models ===")
    for path, value in missing:
        print(f"  {path} -> {value}")
    print(f"  total: {len(missing)}")

    print("=== cross-namespace texture references ===")
    for path, value in foreign:
        print(f"  {path} -> {value}")
    print(f"  total: {len(foreign)}")

    orphans = []
    for png in TEXTURES.rglob("*.png"):
        rel = png.relative_to(TEXTURES).with_suffix("").as_posix()
        if f"neoecoprototype:{rel}" not in referenced:
            orphans.append(png)
    print("=== textures no model references ===")
    for png in sorted(orphans):
        print(f"  {png.relative_to(ASSETS).as_posix()}")
    print(f"  total: {len(orphans)}")

    print("=== orphan .mcmeta (no sibling png) ===")
    count = 0
    for meta in TEXTURES.rglob("*.png.mcmeta"):
        png = meta.with_name(meta.name[: -len(".mcmeta")])
        if not png.exists():
            print(f"  {meta.relative_to(ASSETS).as_posix()}")
            count += 1
    print(f"  total: {count}")


if __name__ == "__main__":
    main()
