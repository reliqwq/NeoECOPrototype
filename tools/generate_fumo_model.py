# Generates the fumo plushie block model from a player skin.
# Path A design: the model's UVs point straight into the standard 64x64 player
# skin layout, so the texture is the skin itself - drop in a different skin and
# you get that player's fumo.
#
# Usage: python tools/generate_fumo_model.py <skin.png> [output.json]
#   1. copies the skin to assets/neoecoprototype/textures/block/fumo/<name>_skin.png
#   2. writes the block model with chibi sitting geometry + skin-layout UVs

import io
import json
import os
import shutil
import sys

MOD_ID = "neoecoprototype"
TEX_BASE = f"{MOD_ID}:block/fumo"

# (name, from, to, faces) - faces: dir -> (u1, v1, u2, v2)
# The character faces north (-z); skin regions follow the standard 64x64 layout.
PARTS = [
    ("head", [4, 7, 4], [12, 15, 12], {
        "north": (8, 8, 16, 16),
        "south": (24, 8, 32, 16),
        "east": (0, 8, 8, 16),
        "west": (16, 8, 24, 16),
        "up": (8, 0, 16, 8),
        "down": (16, 0, 24, 8),
    }),
    ("body", [4, 2, 5], [12, 8, 11], {
        "north": (20, 20, 28, 26),
        "south": (32, 20, 40, 26),
        "east": (16, 20, 20, 26),
        "west": (28, 20, 32, 26),
        "up": (20, 16, 28, 20),
        "down": (28, 16, 36, 20),
    }),
    ("right_arm", [12, 3, 6], [15, 8, 10], {
        "north": (44, 20, 47, 25),
        "south": (52, 20, 55, 25),
        "east": (40, 20, 43, 25),
        "west": (48, 20, 51, 25),
        "up": (44, 16, 47, 19),
        "down": (48, 16, 51, 19),
    }),
    ("left_arm", [1, 3, 6], [4, 8, 10], {
        "north": (36, 52, 39, 57),
        "south": (44, 52, 47, 57),
        "east": (40, 52, 43, 57),
        "west": (32, 52, 35, 57),
        "up": (36, 48, 39, 51),
        "down": (40, 48, 43, 51),
    }),
    ("right_leg", [8, 0, 1], [12, 4, 5], {
        "north": (4, 20, 8, 24),
        "east": (8, 20, 12, 24),
        "west": (0, 20, 4, 24),
        "up": (4, 16, 8, 20),
    }),
    ("left_leg", [4, 0, 1], [8, 4, 5], {
        "north": (4, 52, 8, 56),
        "east": (8, 52, 12, 56),
        "west": (0, 52, 4, 56),
        "up": (0, 48, 4, 52),
    }),
]


def build_model(texture_key):
    texture_ref = f"{TEX_BASE}/{texture_key}"
    textures = {"particle": f"#{texture_key}", texture_key: texture_ref}
    elements = []
    for name, frm, to, faces in PARTS:
        faces_json = {}
        for direction, (u1, v1, u2, v2) in faces.items():
            faces_json[direction] = {
                "uv": [u1, v1, u2, v2],
                "texture": f"#{texture_key}",
            }
        elements.append({
            "name": name,
            "from": frm,
            "to": to,
            "faces": faces_json,
        })
    return {
        "credit": "Fumo plushie textured after a player skin",
        "render_type": "cutout",
        "texture_size": [64, 64],
        "textures": textures,
        "elements": elements,
    }


def main():
    if len(sys.argv) < 2:
        sys.exit("usage: python tools/generate_fumo_model.py <skin.png> [player_name]")
    skin_src = sys.argv[1]
    player = sys.argv[2] if len(sys.argv) > 2 else "player"

    root = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "src", "main", "resources", "assets", MOD_ID)
    tex_dir = os.path.join(root, "textures", "block", "fumo")
    model_dir = os.path.join(root, "models", "block")
    os.makedirs(tex_dir, exist_ok=True)
    os.makedirs(model_dir, exist_ok=True)

    tex_key = f"{player.lower()}_skin"
    tex_dst = os.path.join(tex_dir, f"{tex_key}.png")
    shutil.copyfile(skin_src, tex_dst)
    print("texture:", tex_dst)

    model = build_model(tex_key)
    model_path = os.path.join(model_dir, f"fumo_{player.lower()}.json")
    with io.open(model_path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(model, fh, indent=2, ensure_ascii=False)
        fh.write("\n")
    print("model:", model_path)


if __name__ == "__main__":
    main()
