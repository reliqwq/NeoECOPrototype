"""One-shot: give the L1 storage and crafting hosts a "communication interface" block-state axis.

Writes two placeholder sheets, four formed models, and rewrites the two controller blockstate files.
Prints what landed so a silent failure cannot pass as success.
"""

import json
from pathlib import Path

from PIL import Image

ASSETS = Path("src/main/resources/assets/neoecoprototype")
MODELS = ASSETS / "models/block"
TEXTURES = ASSETS / "textures/block"
BLOCKSTATES = ASSETS / "blockstates"

FACE_Y = {"north": 0, "east": 90, "south": 180, "west": 270}
GLYPH = [".XX.", "X..X", "X...", "X...", "X..X", ".XX."]  # 4x6 'C', white on the card colour


def card(path: Path, colour: str) -> None:
    image = Image.new("RGBA", (16, 16), (*Image.new("RGB", (1, 1), colour).getpixel((0, 0)), 255))
    for row, line in enumerate(GLYPH):
        for column, char in enumerate(line):
            if char == "X":
                image.putpixel((4 + column, 5 + row), (255, 255, 255, 255))
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def model(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=4), encoding="utf-8")


def formed_models() -> None:
    for family, parent, sheet, extra in (
        ("storage_controller", "storage_controller/controller_l4_formed_base",
         "storage_recolor/controller_formed/controller_formed_a_c", {}),
        ("storage_controller", "storage_controller/controller_l4_formed_base_mirrored",
         "storage_recolor/controller_formed/controller_formed_a_c", {}),
        ("crafting_controller", "crafting_controller/controller_formed_base",
         "crafting_recolor/controller_formed/controller_formed_a_c", {
             "screen": "neoecoprototype:block/crafting_recolor/controller/screen_on_a",
             "particle": "neoecoprototype:block/crafting_recolor/controller/controller_side_a",
         }),
        ("crafting_controller", "crafting_controller/controller_formed_base_mirrored",
         "crafting_recolor/controller_formed/controller_formed_a_c", {
             "screen": "neoecoprototype:block/crafting_recolor/controller/screen_on_a",
             "particle": "neoecoprototype:block/crafting_recolor/controller/controller_side_a",
         }),
    ):
        mirrored = parent.endswith("_mirrored")
        name = "controller_l4_formed_c" + ("_mirrored" if mirrored else "")
        textures = {
            "base": f"neoecoprototype:block/{sheet}",
            **extra,
        }
        model(MODELS / family / f"{name}.json", {
            "parent": f"neoecoprototype:block/{parent}",
            "textures": textures,
        })


def placeholders() -> None:
    card(TEXTURES / "storage_recolor/controller_formed/controller_formed_a_c.png", "#5ADC8C")
    card(TEXTURES / "crafting_recolor/controller_formed/controller_formed_a_c.png", "#DC5A8C")


def storage_blockstate() -> None:
    plain = "neoecoprototype:block/storage_controller/controller_l4_formed"
    communication = "neoecoprototype:block/storage_controller/controller_l4_formed_c"
    variants = {}
    for facing, y in FACE_Y.items():
        for formed in (False, True):
            for mirrored in (False, True):
                for communication_interface in (False, True):
                    key = (f"facing={facing},formed={str(formed).lower()},"
                           f"mirrored={str(mirrored).lower()},"
                           f"communication_interface={str(communication_interface).lower()}")
                    if not formed:
                        model_location = "neoecoprototype:block/storage_controller/controller_l4_off"
                    else:
                        model_location = communication if communication_interface else plain
                        if mirrored:
                            model_location += "_mirrored"
                    variants[key] = {"model": model_location} if y == 0 else {"model": model_location, "y": y}
    path = BLOCKSTATES / "simplify_storage_controller.json"
    path.write_text(json.dumps({"variants": variants}), encoding="utf-8")
    print(f"{path.name}: {len(variants)} variants")


def crafting_blockstate() -> None:
    path = BLOCKSTATES / "simplify_crafting_system.json"
    document = json.loads(path.read_text(encoding="utf-8"))
    terms = []
    for entry in document["multipart"]:
        when = entry["when"]
        if when.get("formed") == "true":
            base = dict(when)
            base["communication_interface"] = "false"
            terms.append({"when": base, "apply": entry["apply"]})
            mirrored = when["mirrored"]
            lifted = dict(base)
            lifted["communication_interface"] = "true"
            lifted["mirrored"] = mirrored
            apply_ = dict(entry["apply"])
            apply_["model"] = apply_["model"].replace("controller_l4_formed", "controller_l4_formed_c")
            terms.append({"when": lifted, "apply": apply_})
        else:
            terms.append(entry)
    path.write_text(json.dumps({"multipart": terms}, indent=2), encoding="utf-8")
    print(f"{path.name}: {len(terms)} multipart terms")


placeholders()
formed_models()
storage_blockstate()
crafting_blockstate()

for directory in sorted((MODELS / "storage_controller", MODELS / "crafting_controller")):
    print(directory.name, sorted(p.name for p in directory.glob("controller_l4_formed*.json")))
