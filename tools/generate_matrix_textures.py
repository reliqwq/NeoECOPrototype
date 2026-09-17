"""Offline matrix texture recolor tool.

Recolors only pixels close to configured source colors while preserving alpha,
grayscale housing, borders, highlights, and per-pixel value/brightness.
Requires Pillow: python -m pip install pillow
"""

import argparse
import colorsys
import json
from pathlib import Path

from PIL import Image


def parse_rgb(value):
    value = value.strip().lstrip("#")
    if len(value) != 6:
        raise ValueError(f"Expected RRGGBB color, got {value!r}")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def recolor(source, output, source_colors, target, hue_tolerance, min_saturation, brightness):
    image = Image.open(source).convert("RGBA")
    target_h, target_s, _ = colorsys.rgb_to_hsv(*(channel / 255 for channel in target))
    refs = [colorsys.rgb_to_hsv(*(channel / 255 for channel in color)) for color in source_colors]
    result = []

    for red, green, blue, alpha in image.getdata():
        if alpha == 0:
            result.append((red, green, blue, alpha))
            continue
        hue, saturation, value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
        if saturation < min_saturation:
            result.append((red, green, blue, alpha))
            continue
        matched = any(
            min(abs(hue - ref_h), 1 - abs(hue - ref_h)) <= hue_tolerance
            and saturation >= ref_s * 0.35
            for ref_h, ref_s, _ in refs
        )
        if not matched:
            result.append((red, green, blue, alpha))
            continue
        new_value = max(0.0, min(1.0, value * brightness))
        new_red, new_green, new_blue = colorsys.hsv_to_rgb(target_h, target_s, new_value)
        result.append((round(new_red * 255), round(new_green * 255), round(new_blue * 255), alpha))

    image.putdata(result)
    Path(output).parent.mkdir(parents=True, exist_ok=True)
    image.save(output)


def main():
    parser = argparse.ArgumentParser(description="Generate recolored matrix textures offline")
    parser.add_argument("config", type=Path, help="JSON texture recolor configuration")
    args = parser.parse_args()
    config = json.loads(args.config.read_text(encoding="utf-8"))
    for entry in config["textures"]:
        source_colors = [parse_rgb(color) for color in entry["source_colors"]]
        target = parse_rgb(entry["target_color"])
        recolor(
            Path(entry["source"]),
            Path(entry["output"]),
            source_colors,
            target,
            float(entry.get("hue_tolerance", 0.08)),
            float(entry.get("min_saturation", 0.18)),
            float(entry.get("brightness", 1.0)),
        )
        print(f"generated {entry['output']}")


if __name__ == "__main__":
    main()
