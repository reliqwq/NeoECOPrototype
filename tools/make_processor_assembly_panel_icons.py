"""Draw eco's two workstation-panel sprites for our processor-assembly module.

eco's large workstation panel draws a 15x15 family icon beside the station name and a 28x28 circuit chip
beside the module slot. Both are plain textures named by ECOWorkstationModules.Module (icon / circuit) and
read as SpriteTexture(0, 0, w, h), so the file only has to be that size. Builtin modules pass null and eco
falls back to its own atlas column for OTHER; we draw our own instead so the panel reads as our family.

Everything here is generated from our own palette - no pixel is taken from eco's or AE2's art, which is also
why these two files count in the self-made group of THIRD_PARTY_NOTICES. Run it from the repository root.
"""

from PIL import Image

OUT_ICON = "src/main/resources/assets/neoecoprototype/textures/gui/workstation/processor_assembly_icon.png"
OUT_CIRCUIT = "src/main/resources/assets/neoecoprototype/textures/gui/workstation/processor_assembly_circuit.png"

CLEAR = (0, 0, 0, 0)
# NAME_THEME_TIER_L1 is 0xFF61D389; the rest of the ramp is built around it so the sprite belongs to the
# same family as the card the module is unlocked by.
INK = (16, 40, 30, 255)
DEEP = (26, 78, 52, 255)
PLATE = (45, 128, 84, 255)
L1 = (97, 211, 137, 255)
GLINT = (176, 240, 200, 255)
STEEL = (150, 158, 176, 255)


class Canvas:
    def __init__(self, width, height):
        self.w, self.h = width, height
        self.px = [[CLEAR for _ in range(width)] for _ in range(height)]

    def set(self, x, y, colour):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = colour

    def rect(self, x0, y0, x1, y1, colour):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, colour)

    def frame(self, x0, y0, x1, y1, colour):
        for x in range(x0, x1 + 1):
            self.set(x, y0, colour)
            self.set(x, y1, colour)
        for y in range(y0, y1 + 1):
            self.set(x0, y, colour)
            self.set(x1, y, colour)

    def line(self, x0, y0, x1, y1, colour):
        """1px Bresenham; the art is small enough that an exact diagonal reads cleanly."""
        dx, dy = abs(x1 - x0), abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx - dy
        while True:
            self.set(x0, y0, colour)
            if x0 == x1 and y0 == y1:
                return
            e2 = 2 * err
            if e2 > -dy:
                err -= dy
                x0 += sx
            if e2 < dx:
                err += dx
                y0 += sy

    def save(self, path):
        image = Image.new("RGBA", (self.w, self.h))
        image.putdata([p for row in self.px for p in row])
        image.save(path)
        opaque = sum(1 for row in self.px for p in row if p[3] != 0)
        colours = {p[:3] for row in self.px for p in row if p[3] != 0}
        print("%-100s %dx%d  opaque=%d  colours=%d" % (path, self.w, self.h, opaque, len(colours)))
        return image


def icon():
    """15x15 die with pins: pins carry the silhouette, one bright die carries the green."""
    c = Canvas(15, 15)
    # Body first, dark, so the pins outside it are the only steel in the tile.
    c.rect(3, 2, 11, 12, INK)
    c.rect(4, 3, 10, 11, DEEP)
    # Orientation notch: one dark corner, the same idiom as a real package.
    c.set(4, 3, INK)
    # Die and its four leads into the body.
    c.rect(6, 6, 8, 8, L1)
    c.set(7, 7, GLINT)
    for x, y in ((5, 7), (9, 7), (7, 5), (7, 9)):
        c.set(x, y, PLATE)
    # Three pins per side, in the clear 2px margin.
    for y in (4, 7, 10):
        c.set(1, y, STEEL)
        c.set(2, y, STEEL)
        c.set(12, y, STEEL)
        c.set(13, y, STEEL)
    return c.save(OUT_ICON)


def circuit():
    """28x28 chip with traces: one drop shadow under every run, drawn from our own ramp."""
    c = Canvas(28, 28)
    traces = set()
    # Four straight runs out of the die, ending in 3x3 pads near each edge.
    traces |= {(14, y) for y in range(9, 5, -1)}
    traces |= {(14, y) for y in range(18, 22)}
    traces |= {(x, 13) for x in range(9, 5, -1)}
    traces |= {(x, 14) for x in range(18, 22)}
    # Two stepped runs to the corners: contiguous cells, so they read as traces rather than a checker.
    traces |= {(17, 9), (18, 9), (18, 8), (19, 8), (19, 7), (20, 7), (20, 6), (21, 6), (21, 5)}
    traces |= {(10, 18), (9, 18), (9, 19), (8, 19), (8, 20), (7, 20), (7, 21), (6, 21), (6, 22)}
    # One shadow row below each run; a diagonal shifted by (1, 1) would sit *on* the next trace cell and
    # read as a dashed line instead of a thick one.
    for x, y in traces:
        if (x, y + 1) not in traces:
            c.set(x, y + 1, DEEP)
    for x, y in traces:
        c.set(x, y, PLATE)
    # Pads: 3x3 with a light centre, the same weight as the die so the eye reads endpoints as contacts.
    for cx, cy in ((14, 4), (14, 23), (4, 13), (23, 14), (23, 3), (4, 24)):
        c.rect(cx - 1, cy - 1, cx + 1, cy + 1, DEEP)
        c.set(cx, cy, GLINT)
    # Die: outline, body, and a 2x2 glint in the top-left corner of the body.
    c.rect(10, 10, 17, 17, INK)
    c.rect(11, 11, 16, 16, L1)
    c.rect(12, 12, 13, 13, GLINT)
    # Contacts from the die to each trace run.
    for x, y in ((14, 9), (14, 18), (9, 13), (18, 14)):
        c.set(x, y, PLATE)
    return c.save(OUT_CIRCUIT)


if __name__ == "__main__":
    icon()
    circuit()
