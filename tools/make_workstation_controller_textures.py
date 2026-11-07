"""Recolour eco's formed controller into our L1 tier green, and ship it as a module appearance.

A custom workstation module cannot override eco's models (the CI gate forbids writing into
`assets/neoecoae/`), but `ECOWorkstationModules.Module` takes `idleModel`/`workingModel`, which
`WorkstationModuleBakedModel` bakes standalone and draws instead of the blockstate model once a
module is installed. So the appearance is ours: eco's geometry, copied, with our textures.

The two compatible-module models (`large_integrated_working_station_compat/other_*`) are eco's
*muted* variant - the module slot reads as "some addon's module". We use the vivid base art
(`controller_formed_light*`, `controller_formed_screen*`) instead, because the point of shipping an
appearance is that it should read as a real module.

Both light strips animate at eco's own rhythm: 128x256, two frames, `interpolate: true`,
`frametime: 15` - the same `.mcmeta` eco ships beside `controller_formed_light_on.png`. The working
strip's frames are eco's own two, both retinted inside the same front/top regions, and frame 0 is
byte-identical to the static light. The idle strip is ours: eco's static sheet as frame 0, frame 1
flips both greens on the front and top faces. Neither introduces a third colour.

The idle strip's flip moves every lit pixel, which is the point. Its two faces carry 344 opaque
pixels, and eco's own light_on pair differs in exactly those 344 positions upstream - but that
pair's four blues collapse to 173 differing pixels once quantised onto our two greens, and the rest
is upstream's rhythm rather than ours to re-time. An earlier draft of ours lifted only the 68
base-green pixels on the north face, and the pair read as a still image both in the editor and in
the world (reliqwq, 2026-11-07): two greens this close in luminance need the whole lit area to move.

The working screen keeps all of eco's animation: `controller_formed_screen_on.png` is 16x352 / 22
frames that shuffle a two-tone scan, animated at `frametime: 5` with no interpolation, and we retint
every frame and ship eco's own `.mcmeta`. Flattening it to frame 0 (an earlier draft) left the one
face a player actually stares at motionless - that static green panel is what reliqwq was looking at
on 2026-11-07. The idle screen is eco's static `controller_formed_screen.png`, unchanged.

The body and the off screen carry no blue accents (the body's only saturated pixels are eco's
purple mark, which we keep), so they are straight copies.

Only the light sheet's front and top faces are retinted. eco's own eight family variants
(`controller_formed_light_compat/<family>.png`) do exactly that: measured on 2026-11-07, the
north/up regions differ from the base sheet while west/south/east are byte-identical - the south
and east faces are the ones that sit next to the working station's input hatch and interface, and
they keep eco's blue. Recolouring the whole sheet (an earlier draft) turned those blue too, which
is not this addon's art to repaint.

The art and the model geometry are eco's, recoloured / re-pointed - not drawn here. They belong to
the "adapted from Neo ECO AE Extension" group of THIRD_PARTY_NOTICES.md (GPL-3.0).
Run from the repository root.
"""

import json
import zipfile

from PIL import Image

ECO_JAR = "neoecobeta/neoecoae-21.2.15.jar"
TEX_DIR = "assets/neoecoae/textures/block/working_station"
MODEL_DIR = "assets/neoecoae/models/block"

OUT_TEX = "src/main/resources/assets/neoecoprototype/textures/block/workstation"
OUT_MODEL = "src/main/resources/assets/neoecoprototype/models/block/workstation"
TEXTURE_ID = "neoecoprototype:block/workstation"

# The two greens our storage host controller's formed multiblock lights up with, measured off
# textures/block/storage_recolor/controller_formed/controller_formed_light_a.png on 2026-11-07:
# #61D389 (161 px, NAME_THEME_TIER_L1) and #ACF26F (207 px, its highlight). Quantising to exactly
# those two instead of ramping between them is the point: a ramp puts mint tones on the strip that
# read as ExtendedAE's green rather than ours.
L1 = (97, 211, 137)        # #61D389, NAME_THEME_TIER_L1
GLINT = (172, 242, 111)    # #ACF26F, the storage host light's highlight
# eco's darkest accent is #337FF0 (luminance 119.0); its three lighter blues (#3CABFF 153.5,
# #40C1FF 170.1, #6BCDFF 187.8) all land on the highlight, so the working frame stays brighter
# than the idle one the way eco's two frames are.
LUM_SPLIT = 135.0

# The light sheet's north (front) and up (top) faces in pixels: uv [0,0,6,4] and [6,8,0,4] at
# texture_size 128. Everything outside them - the west/south/east faces that face the machine's
# input hatch and interface - keeps eco's own colours, which is what eco's family sheets do.
FRONT_REGIONS = ((0, 0, 48, 32), (0, 32, 48, 64))

# The idle strip's second frame flips L1 and the highlight on both lit faces - the same 344 lit
# positions eco's own light_on pair moves (measured 2026-11-07). Both greens stay on the sheet, so
# the frame adds no colour and the strip's overall brightness does not jump; only the pattern
# inverts.
FLIP_REGIONS = FRONT_REGIONS

# eco's rhythm for the working light strip, copied from controller_formed_light_on.png.mcmeta.
ANIMATION = {"animation": {"interpolate": True, "frametime": 15}}
# And the screen's: 22 frames, 5 ticks each, no interpolation - controller_formed_screen_on.png.mcmeta.
SCREEN_ANIMATION = {"animation": {"frametime": 5}}


def is_accent(rgb):
    r, g, b = rgb
    return b - max(r, g) >= 30 and b >= 120


def tier_green(rgb):
    lum = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]
    return GLINT if lum > LUM_SPLIT else L1


def read_png(jar, entry):
    with jar.open("%s/%s" % (TEX_DIR, entry)) as stream:
        return Image.open(stream).convert("RGBA")


def recolour(image, name, regions=None):
    def tinted(x, y):
        return regions is None or any(x0 <= x < x1 and y0 <= y < y1 for x0, y0, x1, y1 in regions)

    out = Image.new("RGBA", image.size)
    changed = kept = 0
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = image.getpixel((x, y))
            if a and is_accent((r, g, b)):
                if tinted(x, y):
                    r, g, b = tier_green((r, g, b))
                    changed += 1
                else:
                    kept += 1
            out.putpixel((x, y), (r, g, b, a))
    if changed == 0:
        raise SystemExit("%s: no blue accents in the front regions - eco's art changed" % name)
    if regions is not None and kept == 0:
        raise SystemExit("%s: the side/back faces lost their accents - re-read the light sheet"
                         % name)
    left = sum(1 for p in out.get_flattened_data() if p[3] and is_accent(p[:3]))
    if left != kept:
        raise SystemExit("%s: %d accent pixels survived, expected the %d outside the front regions"
                         % (name, left, kept))
    assert sum(1 for p in out.get_flattened_data() if p[3]) == \
           sum(1 for p in image.get_flattened_data() if p[3]), "%s: opacity changed" % name
    print("  %s: retinted=%d kept eco blue=%d" % (name, changed, kept))
    return out


def recolour_frames(image, name, regions, side):
    """Vertical frame strips repeat the pixel grid per frame, so the regions are frame-local."""
    assert image.width == side and image.height % side == 0, \
        "expected a %d-wide vertical frame strip, got %r" % (side, image.size)
    out = Image.new("RGBA", image.size)
    for index in range(image.height // side):
        recoloured = recolour(image.crop((0, index * side, side, (index + 1) * side)),
                              "%s frame %d" % (name, index), regions)
        out.paste(recoloured, (0, index * side))
    return out


def flip_greens(image, name, regions):
    """Exchange the two greens inside the regions, like eco's own second light frame does."""
    out = image.copy()
    changed = 0
    for x0, y0, x1, y1 in regions:
        for y in range(y0, y1):
            for x in range(x0, x1):
                r, g, b, a = out.getpixel((x, y))
                if not a:
                    continue
                if (r, g, b) == L1:
                    out.putpixel((x, y), GLINT + (a,))
                    changed += 1
                elif (r, g, b) == GLINT:
                    out.putpixel((x, y), L1 + (a,))
                    changed += 1
    opaque = sum(1 for x0, y0, x1, y1 in regions
                 for y in range(y0, y1) for x in range(x0, x1) if image.getpixel((x, y))[3])
    if changed != opaque:
        raise SystemExit("%s: flipped %d of the %d lit pixels in %r, so part of the lit area would"
                         " hold still between frames" % (name, changed, opaque, regions))
    print("  %s: flipped=%d" % (name, changed))
    return out


def strip(frames):
    out = Image.new("RGBA", (128, 128 * len(frames)))
    for index, image in enumerate(frames):
        assert image.size == (128, 128), "expected a square frame, got %r" % (image.size,)
        out.paste(image, (0, index * 128))
    return out


def copy(image):
    return image.copy()


def model(jar, source, textures):
    with jar.open("%s/%s.json" % (MODEL_DIR, source)) as stream:
        data = json.loads(stream.read().decode("utf-8"))
    assert data["parent"] == "block/block", "parent moved: %r" % data["parent"]
    assert set(data["textures"]) == set(textures), "slots moved: %r" % sorted(data["textures"])
    data["textures"] = {key: "%s/%s" % (TEXTURE_ID, name) for key, name in textures.items()}
    return data


def write_model(path, data):
    text = json.dumps(data, indent=2) + "\n"
    assert "neoecoae:" not in text, "%s still points into eco's namespace" % path
    for value in data["textures"].values():
        assert value.startswith(TEXTURE_ID + "/"), "%s: stray texture %r" % (path, value)
    json.loads(text)
    with open(path, "w", encoding="utf-8", newline="\n") as stream:
        stream.write(text)
    print("%-88s %d elements, textures %s" % (path, len(data["elements"]),
                                              sorted(k for k in data["textures"] if k != "particle")))


def main():
    with zipfile.ZipFile(ECO_JAR) as jar:
        body = copy(read_png(jar, "controller_formed/controller_formed.png"))
        light = recolour(read_png(jar, "controller_formed/controller_formed_light.png"), "light",
                         FRONT_REGIONS)
        light_strip = strip([light,
                             flip_greens(light, "light frame 1", FLIP_REGIONS)])
        light_on = recolour_frames(read_png(jar, "controller_formed/controller_formed_light_on.png"),
                                   "light_on", FRONT_REGIONS, 128)
        screen = copy(read_png(jar, "controller_formed/controller_formed_screen.png"))
        screen_on = recolour_frames(read_png(jar, "controller_formed/controller_formed_screen_on.png"),
                                    "screen_on", None, 16)
        particle = copy(read_png(jar, "casing.png"))

        expected = {
            "controller_body.png": (body, (128, 128)),
            "controller_light.png": (light_strip, (128, 256)),
            "controller_light_on.png": (light_on, (128, 256)),
            "controller_screen.png": (screen, (16, 16)),
            "controller_screen_on.png": (screen_on, (16, 352)),
            "controller_particle.png": (particle, (16, 16)),
        }
        for name, (image, size) in expected.items():
            assert image.size == size, "%s: %r, expected %r" % (name, image.size, size)
            path = "%s/%s" % (OUT_TEX, name)
            image.save(path)
            opaque = sum(1 for p in image.get_flattened_data() if p[3])
            print("%-88s %dx%d opaque=%d" % (path, image.width, image.height, opaque))

        with jar.open("%s/controller_formed/controller_formed_light_on.png.mcmeta" % TEX_DIR) as stream:
            assert json.loads(stream.read().decode("utf-8")) == ANIMATION, \
                "eco's animation rhythm moved - re-read controller_formed_light_on.png.mcmeta"
        with jar.open("%s/controller_formed/controller_formed_screen_on.png.mcmeta"
                      % TEX_DIR) as stream:
            assert json.loads(stream.read().decode("utf-8")) == SCREEN_ANIMATION, \
                "eco's screen rhythm moved - re-read controller_formed_screen_on.png.mcmeta"
        for name, meta, description in (("controller_light", ANIMATION, "frametime=15, interpolated"),
                                        ("controller_light_on", ANIMATION, "frametime=15, interpolated"),
                                        ("controller_screen_on", SCREEN_ANIMATION,
                                         "22 frames, frametime=5")):
            meta_path = "%s/%s.png.mcmeta" % (OUT_TEX, name)
            with open(meta_path, "w", encoding="utf-8", newline="\n") as stream:
                stream.write(json.dumps(meta, indent=2) + "\n")
            print("%-88s animation %s" % (meta_path, description))

        write_model("%s/controller_off.json" % OUT_MODEL,
                    model(jar, "large_integrated_working_station_off", {
                        "body": "controller_body",
                        "light": "controller_light",
                        "screen": "controller_screen",
                        "particle": "controller_particle",
                    }))
        write_model("%s/controller_on.json" % OUT_MODEL,
                    model(jar, "large_integrated_working_station_on", {
                        "body": "controller_body",
                        "light": "controller_light_on",
                        "screen": "controller_screen_on",
                        "particle": "controller_particle",
                    }))

    for name in ("controller_off.json", "controller_on.json"):
        with open("%s/%s" % (OUT_MODEL, name), encoding="utf-8") as stream:
            for value in json.load(stream)["textures"].values():
                png = "src/main/resources/assets/neoecoprototype/textures/%s.png" % value.split(":", 1)[1]
                Image.open(png).verify()


if __name__ == "__main__":
    main()
