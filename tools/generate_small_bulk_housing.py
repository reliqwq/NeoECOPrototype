"""Generate the small-bulk (MEGA family) housings used by the L1 small bulk matrices.

The small bulk family belongs to eco's MEGA matrices, so every one of its housings comes from the
matching eco MEGA texture rather than from the generic item housing:

* base (3 types)      : eco MEGA housing lifted to a dark grey so it stays readable
* expanded (10 types) : the same eco MEGA housing as shipped ("原来的")

Outputs:
  item/.../small_bulk_cell_housing.png            <- eco mega_item_cell_housing (dark grey)
  item/.../small_bulk_cell_housing_expanded.png   <- eco mega_item_cell_housing (original)
  drive/.../small_bulk_cell_housing.png           <- eco drive/mega_cell_housing (dark grey)
  drive/.../small_bulk_cell_housing_expanded.png  <- eco drive/mega_cell_housing (original)

Run from the repository root:  python tools/generate_small_bulk_housing.py
"""

import io
import zipfile
from pathlib import Path

from PIL import Image

ECO_JAR = Path("libs/neoecoae-21.2.0-111a6fd6-local.jar")
ECO_MEGA_ITEM = "assets/neoecoae/textures/item/eco_cell_compat/mega_item_cell_housing.png"
ECO_MEGA_DRIVE = "assets/neoecoae/textures/block/storage/drive/mega_cell_housing.png"

ITEM_BASE = Path(
    "src/main/resources/assets/neoecoprototype/textures/item/storage_recolor/small_bulk_cell_housing.png"
)
ITEM_EXPANDED = Path(
    "src/main/resources/assets/neoecoprototype/textures/item/storage_recolor/"
    "small_bulk_cell_housing_expanded.png"
)
DRIVE_DIR = Path("src/main/resources/assets/neoecoprototype/textures/block/storage_recolor/drive")
DRIVE_BASE = DRIVE_DIR / "small_bulk_cell_housing.png"
DRIVE_EXPANDED = DRIVE_DIR / "small_bulk_cell_housing_expanded.png"

# Dark grey floor: below this luminance a pixel is treated as "black body" and lifted.
BLACK_THRESHOLD = 78
# Lifted body luminance range: keeps the original shading instead of flattening to one colour.
FLOOR = 58.0
SCALE = 1.35
GREY = (74, 78, 84)


def lift_black_to_dark_grey(image):
    """Keep alpha, keep colours above the threshold, lift the near-black body to dark grey."""
    result = []
    for red, green, blue, alpha in image.getdata():
        if alpha == 0:
            result.append((red, green, blue, alpha))
            continue
        luminance = 0.299 * red + 0.587 * green + 0.114 * blue
        if luminance >= BLACK_THRESHOLD:
            result.append((red, green, blue, alpha))
            continue
        lifted = min(255.0, FLOOR + luminance * SCALE)
        ratio = lifted / max(1.0, FLOOR + BLACK_THRESHOLD * SCALE)
        result.append((
            round(GREY[0] * ratio),
            round(GREY[1] * ratio),
            round(GREY[2] * ratio),
            alpha,
        ))
    output = Image.new("RGBA", image.size)
    output.putdata(result)
    return output


def average_colour(image):
    opaque = [p for p in image.getdata() if p[3] > 0]
    if not opaque:
        return None
    return tuple(sum(p[i] for p in opaque) // len(opaque) for i in range(3))


def main():
    jar = zipfile.ZipFile(ECO_JAR)

    # Item housings.
    item_bytes = jar.read(ECO_MEGA_ITEM)
    item_source = Image.open(io.BytesIO(item_bytes)).convert("RGBA")
    ITEM_BASE.parent.mkdir(parents=True, exist_ok=True)
    ITEM_EXPANDED.write_bytes(item_bytes)
    print("wrote", ITEM_EXPANDED, "avg=", average_colour(item_source))
    item_base = lift_black_to_dark_grey(item_source)
    item_base.save(ITEM_BASE)
    print("wrote", ITEM_BASE, "avg=", average_colour(item_base))

    # In-drive housings.
    drive_bytes = jar.read(ECO_MEGA_DRIVE)
    drive_source = Image.open(io.BytesIO(drive_bytes)).convert("RGBA")
    DRIVE_DIR.mkdir(parents=True, exist_ok=True)
    DRIVE_EXPANDED.write_bytes(drive_bytes)
    print("wrote", DRIVE_EXPANDED, "avg=", average_colour(drive_source))
    drive_base = lift_black_to_dark_grey(drive_source)
    drive_base.save(DRIVE_BASE)
    print("wrote", DRIVE_BASE, "avg=", average_colour(drive_base))


if __name__ == "__main__":
    main()
