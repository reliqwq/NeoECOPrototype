"""Generate the brown in-drive housing used by script-created (KubeJS) matrices.

The stock drive housing is a near-neutral grey shell, so a hue shift does nothing. Instead the
grey is tinted by multiplying it with the target brown, which keeps the original shading and
alpha while making script matrices read as brown inside a drive.

Run from the repository root:  python tools/generate_script_matrix_housing.py
"""

from pathlib import Path

from PIL import Image

SOURCE = Path(
    "src/main/resources/assets/neoecoprototype/textures/block/storage_recolor/drive/cell_housing.png"
)
OUTPUT = Path(
    "src/main/resources/assets/neoecoprototype/textures/block/storage_recolor/drive/"
    "brown_cell_housing.png"
)

# Medium brown (#8B5A2B).
TARGET = (139, 90, 43)


def tint(image, target):
    opaque = [p for p in image.getdata() if p[3] > 0]
    if not opaque:
        return image.copy()
    # Normalise against the source's mean channel values so the tint lands on `target` on average.
    means = [sum(p[channel] for p in opaque) / len(opaque) for channel in range(3)]
    factors = [target[channel] / max(1.0, means[channel]) for channel in range(3)]
    result = []
    for red, green, blue, alpha in image.getdata():
        result.append((
            min(255, round(red * factors[0])),
            min(255, round(green * factors[1])),
            min(255, round(blue * factors[2])),
            alpha,
        ))
    output = Image.new("RGBA", image.size)
    output.putdata(result)
    return output


def main():
    source = Image.open(SOURCE).convert("RGBA")
    output = tint(source, TARGET)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    output.save(OUTPUT)
    opaque = [p for p in output.getdata() if p[3] > 0]
    average = tuple(sum(p[i] for p in opaque) // len(opaque) for i in range(3))
    print("generated", OUTPUT, "avg=", average)


if __name__ == "__main__":
    main()
