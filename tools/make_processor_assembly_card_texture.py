import colorsys
import collections
from PIL import Image

SRC = "src/main/resources/assets/neoecoprototype/textures/item/simplify_structure_expansion_card.png"
DST = "src/main/resources/assets/neoecoprototype/textures/item/processor_assembly_workstation_card.png"
# NAME_THEME_TIER_L1 = 0xFF61D389
TARGET = (97, 211, 137)
target_h = colorsys.rgb_to_hsv(*[c / 255 for c in TARGET])[0]
# Two of the source tile's 17 opaque colours have identical saturation and value (0.498 / 1.000) and differ
# only in hue, so snapping every hue to one target merges them and the tile loses an accent. Splitting them
# back apart needs a second axis; the one that costs nothing is their own original hue, which is exactly what
# distinguished them: the cyan band becomes a paler green, the azure band keeps the full saturation.
CYAN_BAND = 0.58
PALE_FACTOR = 0.72

im = Image.open(SRC).convert("RGBA")
src_px = list(im.getdata())
mapping = {}
merged = {}
out = []
for r, g, b, a in src_px:
    if a == 0:
        out.append((0, 0, 0, 0))
        continue
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    if h < CYAN_BAND:
        s *= PALE_FACTOR
    nr, ng, nb = colorsys.hsv_to_rgb(target_h, s, v)
    key = (r, g, b)
    val = (round(nr * 255), round(ng * 255), round(nb * 255))
    if key in mapping and mapping[key] != val:
        raise SystemExit("mapping is not a function: %r -> %r and %r" % (key, mapping[key], val))
    mapping.setdefault(key, val)
    merged.setdefault(val, set()).add(key)
    out.append((val[0], val[1], val[2], a))

collisions = {k: v for k, v in merged.items() if len(v) > 1}
src_colours = {p[:3] for p in src_px if p[3] != 0}
dst_colours = set(merged)
src_bright = max(max(p[:3]) for p in src_px if p[3] != 0)
dst_bright = max(max(c) for c in dst_colours)
src_dark = min(max(p[:3]) for p in src_px if p[3] != 0)
dst_dark = min(max(c) for c in dst_colours)

result = Image.new("RGBA", im.size)
result.putdata(out)
result.save(DST)

print("source          :", SRC, im.size)
print("opaque colours  : in %d -> out %d" % (len(src_colours), len(dst_colours)))
print("merged pairs    :", len(collisions), sorted(collisions)[:3])
print("opaque pixels   : in %d -> out %d" % (
    sum(1 for p in src_px if p[3] != 0), sum(1 for p in out if p[3] != 0)))
print("brightest value : %d -> %d" % (src_bright, dst_bright))
print("darkest value   : %d -> %d" % (src_dark, dst_dark))
print("hue             : %.1f -> %.1f deg (saturation and value untouched)" % (
    colorsys.rgb_to_hsv(*[c / 255 for c in (173, 176, 196)])[0] * 360, target_h * 360))
