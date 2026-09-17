"""Release audit: every registered item should have an item model.

Extracts item ids from the DeferredRegister calls in Java and checks that
assets/neoecoprototype/models/item/<name>.json exists.

Run from the repository root:  python tools/audit_item_models.py
"""

import re
from pathlib import Path

JAVA_ROOT = Path("src/main/java")
ITEM_MODELS = Path("src/main/resources/assets/neoecoprototype/models/item")

REGISTER = re.compile(r'\.register\(\s*"([a-z0-9_]+)"')


def registered_item_names():
    names = set()
    for path in JAVA_ROOT.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        for match in REGISTER.finditer(text):
            name = match.group(1)
            # Item registers are the ones passed an Item/BlockItem supplier; block registers use the
            # same helper shape, so filter by the argument that follows.
            tail = text[match.end(): match.end() + 200]
            if "Item(" in tail or "Item::new" in tail or "items.register" in text[max(0, match.start() - 80):match.start()]:
                names.add(name)
    return names


def main():
    names = registered_item_names()
    missing = sorted(n for n in names if not (ITEM_MODELS / f"{n}.json").exists())
    print(f"registered item-like names: {len(names)}")
    print("=== names without models/item/<name>.json ===")
    for name in missing:
        print("  " + name)
    print(f"  total: {len(missing)}")


if __name__ == "__main__":
    main()
