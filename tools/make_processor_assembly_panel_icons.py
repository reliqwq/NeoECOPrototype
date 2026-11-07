"""Recolour eco's OTHER-column workstation sprites into our L1 tier green.

eco's large workstation panel draws a module's icon as a 15x15 sprite at (6,6) and its circuit chip
as a 28x28 sprite at (217,37); both come straight from the module record, and a null falls back to
eco's own widget atlas. A module from this addon has no builtin column, so the honest base is that
same fallback column: we take eco's OTHER cells out of the atlas and move only its two blue accents
onto our green ramp.

Only the accents: every one of eco's eight module icons keeps its grey chip body and greys stay grey
(the grey centre is the module's card body, not a slot to fill). Recolouring the neutrals too would
take the middle of the icon away from that family, which is exactly what the neighbouring columns do
not do.

The art is eco's, recoloured - not drawn here. These two files therefore sit in the "adapted from Neo
ECO AE Extension" group of THIRD_PARTY_NOTICES.md (GPL-3.0), unlike the card tile next to them.
Run from the repository root.
"""

import zipfile

from PIL import Image

# Same jar build.gradle compiles against; the sprite rectangles are eco's, read from
# WorkstationTextures.module/circuit: widget(column*15, 17, 15, 15) and widget(column*28, 34, 28, 28).
ECO_JAR = "neoecobeta/neoecoae-21.2.15.jar"
ATLAS = "assets/neoecoae/textures/guis/workstation/integrated_working_station_widget.png"
# WorkstationTextures.moduleColumn maps OTHER to column 7 (DATA_ENERGISTICS and APPLIED_GENERATORS
# take 5 and 6), so the atlas order is not the enum order.
OTHER_COLUMN = 7

OUT_ICON = "src/main/resources/assets/neoecoprototype/textures/gui/workstation/processor_assembly_icon.png"
OUT_CIRCUIT = "src/main/resources/assets/neoecoprototype/textures/gui/workstation/processor_assembly_circuit.png"

# eco's four chip-body greys, kept as they are, and its two blue accents, moved onto our ramp.
SRC_NEUTRALS = {(0x69, 0x6D, 0x88), (0x87, 0x8F, 0xA5), (0x9A, 0x9F, 0xB4), (0xCB, 0xCC, 0xD4)}
PLATE = (45, 128, 84)     # #2D8054, our DEEP-family plate green
L1 = (97, 211, 137)       # #61D389, NAME_THEME_TIER_L1
RECOLOUR = {(0x33, 0x7F, 0xF0): PLATE, (0x40, 0xC1, 0xFF): L1}


def read_atlas():
    try:
        with zipfile.ZipFile(ECO_JAR) as jar:
            with jar.open(ATLAS) as entry:
                return Image.open(entry).convert("RGBA")
    except FileNotFoundError:
        raise SystemExit("missing %s - put the eco jar named in build.gradle there first" % ECO_JAR)
    except KeyError:
        raise SystemExit("%s has no %s" % (ECO_JAR, ATLAS))


def recolour(image, name):
    colours = {p[:3] for p in image.getdata() if p[3] != 0}
    unexpected = sorted(colours - set(RECOLOUR) - SRC_NEUTRALS)
    if unexpected:
        raise SystemExit("%s: unmapped colours %r - eco's art changed, re-read the column"
                         % (name, ["#%02X%02X%02X" % c for c in unexpected]))

    out = Image.new("RGBA", image.size)
    out.putdata([(0, 0, 0, 0) if a == 0 else RECOLOUR.get((r, g, b), (r, g, b)) + (a,)
                 for r, g, b, a in image.getdata()])
    return out


def report(path, before, after):
    opaque = sum(1 for p in after.getdata() if p[3] != 0)
    colours = sorted({p[:3] for p in after.getdata() if p[3] != 0})
    changed = sum(1 for b, a in zip(before.getdata(), after.getdata()) if b[3] and b != a)
    accents = sum(1 for b in before.getdata() if b[3] and b[:3] in RECOLOUR)
    print("%-96s %dx%d  opaque=%d  accents=%d  changed=%d  colours=%d"
          % (path, after.size[0], after.size[1], opaque, accents, changed, len(colours)))
    for c in colours:
        marker = {L1: "  <- tier green", PLATE: "  <- plate"}.get(c, "  <- eco grey, kept")
        print("    #%02X%02X%02X%s" % (c[0], c[1], c[2], marker))
    assert opaque == sum(1 for p in before.getdata() if p[3] != 0)
    assert changed == accents, "%s: %d of %d accent pixels kept their blue" % (
        path, accents - changed, accents)


def main():
    atlas = read_atlas()
    cells = (
        ("other icon", atlas.crop((OTHER_COLUMN * 15, 17, OTHER_COLUMN * 15 + 15, 32)), OUT_ICON),
        ("other circuit", atlas.crop((OTHER_COLUMN * 28, 34, OTHER_COLUMN * 28 + 28, 62)), OUT_CIRCUIT),
    )
    for name, cell, path in cells:
        out = recolour(cell, name)
        out.save(path)
        report(path, cell, out)


if __name__ == "__main__":
    main()
