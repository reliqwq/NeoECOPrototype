"""Face atlas tool for the fumo doll.

The doll is one 64x64 player skin plus a UV table derived from each cube's nominal size, so "editing
a face" means editing that rectangle in the skin. This script collects skins, unpacks one into
per-face crops plus a labelled contact sheet, and packs edited crops back into a skin.

    python tools/fumo_faces.py fetch reliqwq          # download + unpack into fumo_skins/
    python tools/fumo_faces.py unpack fumo_skins/reliqwq.png
    # edit fumo_skins/reliqwq/<code>.png (codes are on the sheet, rects in manifest.json)
    python tools/fumo_faces.py pack fumo_skins/reliqwq # -> the resource pack, then F3+T in game

Collected skins live in `fumo_skins/` (gitignored): they are other players' artwork, so they are
never committed and never shipped in the jar. The applied override is read from a resource pack for
the same reason - see FumoRenderer.localSkin().

Cube table below must stay in sync with FumoModel.createBodyLayer(): (code, texU, texV, X, Y, Z).
"""
import base64
import json
import os
import struct
import sys
import urllib.request
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SKIN_DIR = os.path.join(ROOT, "fumo_skins")
PACK_SKIN_DIR = os.path.join(ROOT, "run", "resourcepacks", "fumo_edited", "assets",
                             "neoecoprototype", "textures", "block", "fumo", "skins")
USERCACHE = os.path.join(ROOT, "run", "usercache.json")

# (code, name, texU, texV, nominalX, nominalY, nominalZ) - the same cubes as FumoModel.
CUBES = [
    ("1", "头", 0, 0, 8, 8, 8),
    ("2", "头发层", 32, 0, 8, 8, 8),
    ("3", "躯干", 16, 16, 8, 8, 4),
    ("4", "右臂", 40, 16, 4, 5, 4),
    ("5", "左臂", 32, 48, 4, 5, 4),
    ("6", "右大腿", 0, 16, 4, 2.5, 4),
    ("7", "右脚", 0, 22, 4, 2.5, 4),
    ("8", "左大腿", 16, 48, 4, 2.5, 4),
    ("9", "左脚", 16, 54, 4, 2.5, 4),
]
# Face code -> (label, rect builder) using the standard skin unwrap.
FACES = {
    "A": ("前", lambda u, v, x, y, z: (u + z, v + z, u + z + x, v + z + y)),
    "B": ("后", lambda u, v, x, y, z: (u + z + x, v + z, u + z + 2 * x, v + z + y)),
    "C": ("右", lambda u, v, x, y, z: (u, v + z, u + z, v + z + y)),
    "D": ("左", lambda u, v, x, y, z: (u + z + x, v + z, u + z + x + z, v + z + y)),
    "E": ("上", lambda u, v, x, y, z: (u + z, v, u + z + x, v + z)),
    "F": ("下", lambda u, v, x, y, z: (u + z + x, v, u + z + 2 * x, v + z)),
}


def read_png(path):
    """Decode a non-interlaced RGBA/RGB 8-bit PNG into (w, h, rows of RGBA tuples)."""
    data = open(path, "rb").read()
    assert data[:8] == b"\x89PNG\r\n\x1a\n", path
    pos, idat, w, h, ct, bd = 8, b"", 0, 0, 0, 0
    while pos < len(data):
        (ln,) = struct.unpack(">I", data[pos:pos + 4])
        typ, body = data[pos + 4:pos + 8], data[pos + 8:pos + 8 + ln]
        pos += 12 + ln
        if typ == b"IHDR":
            w, h, bd, ct = struct.unpack(">IIBB", body[:10])
        elif typ == b"IDAT":
            idat += body
    assert bd == 8 and ct in (2, 6), f"{path}: only 8-bit RGB/RGBA supported (ct={ct})"
    ch = 3 if ct == 2 else 4
    raw = zlib.decompress(idat)
    stride = w * ch
    rows, prev = [], bytearray(stride)
    o = 0
    for _ in range(h):
        f = raw[o]
        o += 1
        line = bytearray(raw[o:o + stride])
        o += stride
        for i in range(stride):
            a = line[i - ch] if i >= ch else 0
            b = prev[i]
            c = prev[i - ch] if i >= ch else 0
            if f == 1:
                line[i] = (line[i] + a) & 255
            elif f == 2:
                line[i] = (line[i] + b) & 255
            elif f == 3:
                line[i] = (line[i] + (a + b) // 2) & 255
            elif f == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 255
        rows.append([tuple(line[i * ch:i * ch + 4]) for i in range(w)])
        prev = line
    return w, h, rows


def write_png(path, w, h, px):
    """Encode RGBA rows (list of lists of 4-tuples) as a PNG."""
    raw = bytearray()
    for row in px:
        raw.append(0)
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))

    def chunk(typ, body):
        return (struct.pack(">I", len(body)) + typ + body
                + struct.pack(">I", zlib.crc32(typ + body) & 0xFFFFFFFF))

    with open(path, "wb") as fh:
        fh.write(b"\x89PNG\r\n\x1a\n")
        fh.write(chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)))
        fh.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        fh.write(chunk(b"IEND", b""))


def rects():
    out = []
    for code, name, u, v, x, y, z in CUBES:
        for f, (label, build) in FACES.items():
            r = [int(round(s)) for s in build(u, v, x, y, z)]
            out.append((code + "-" + f, name + label, r))
    return out


def fetch(name):
    """Download a player's skin using the UUID the dev client already cached in usercache.json."""
    cache = json.load(open(USERCACHE, encoding="utf-8"))
    entry = next((e for e in cache if e["name"].lower() == name.lower()), None)
    if entry is None:
        sys.exit("no cached profile for %r - run /prototypefumo %s in game first" % (name, name))
    profile = json.loads(_get("https://sessionserver.mojang.com/session/minecraft/profile/"
                              + entry["uuid"].replace("-", "")))
    textures = json.loads(base64.b64decode(profile["properties"][0]["value"]))["textures"]
    if "SKIN" not in textures:
        sys.exit("%s has no skin texture" % name)
    url = textures["SKIN"]["url"]
    dest = os.path.join(SKIN_DIR, name.lower() + ".png")
    os.makedirs(SKIN_DIR, exist_ok=True)
    with open(dest, "wb") as fh:
        fh.write(_get(url))
    print("fetched", name, textures["SKIN"].get("metadata", {}).get("model", "classic"), "->", dest)
    unpack(dest)


def _get(url, attempts=4):
    for i in range(attempts):
        try:
            return urllib.request.urlopen(url, timeout=25).read()
        except Exception as error:
            if i == attempts - 1:
                sys.exit("fetch failed %s: %s" % (url[:60], error))
    return b""


def unpack(src, out_dir=None):
    w, h, px = read_png(src)
    stem = os.path.splitext(os.path.basename(src))[0]
    out_dir = out_dir or os.path.join(SKIN_DIR, stem)
    os.makedirs(out_dir, exist_ok=True)
    manifest = {"source": os.path.abspath(src), "faces": {}}
    for code, label, (x0, y0, x1, y1) in rects():
        crop = [[px[y][x] for x in range(x0, x1)] for y in range(y0, y1)]
        write_png(os.path.join(out_dir, code + ".png"), x1 - x0, y1 - y0, crop)
        manifest["faces"][code] = {"label": label, "rect": [x0, y0, x1, y1]}
    with open(os.path.join(out_dir, "manifest.json"), "w", encoding="utf-8") as fh:
        json.dump(manifest, fh, ensure_ascii=False, indent=1)
    sheet(out_dir, w, h, px)
    print("unpacked", len(manifest["faces"]), "faces from", src, "->", out_dir)


def sheet(out_dir, w, h, px):
    """Contact sheet: every face scaled x8 with a 1px border, laid out in rows of 6."""
    scale, gap = 8, 4
    items = rects()
    tiles = []
    for code, label, (x0, y0, x1, y1) in items:
        tw, th = (x1 - x0) * scale, (y1 - y0) * scale
        tile = [[(0, 0, 0, 255)] * (tw + 2 * gap) for _ in range(th + 2 * gap)]
        for y in range(y0, y1):
            for x in range(x0, x1):
                c = px[y][x]
                for dy in range(scale):
                    for dx in range(scale):
                        tile[gap + (y - y0) * scale + dy][gap + (x - x0) * scale + dx] = c
        tiles.append((code, tile))
    cols = 6
    rows = (len(tiles) + cols - 1) // cols
    cw = max(len(t[1][0]) for t in tiles) + 8
    chh = max(len(t[1]) for t in tiles) + 8
    canvas = [[(40, 40, 46, 255)] * (cols * cw) for _ in range(rows * chh)]
    for i, (code, tile) in enumerate(tiles):
        ox, oy = (i % cols) * cw + 4, (i // cols) * chh + 4
        for y, row in enumerate(tile):
            for x, c in enumerate(row):
                canvas[oy + y][ox + x] = c
    write_png(os.path.join(out_dir, "sheet.png"), cols * cw, rows * chh, canvas)


def pack(face_dir, dst=None):
    """Rebuild a skin from edited face crops, on top of the original skin when it is still there."""
    face_dir = face_dir.rstrip("/\\")
    if dst is None:
        os.makedirs(PACK_SKIN_DIR, exist_ok=True)
        dst = os.path.join(PACK_SKIN_DIR, os.path.basename(face_dir) + ".png")
    manifest = json.load(open(os.path.join(face_dir, "manifest.json"), encoding="utf-8"))
    faces = manifest["faces"]
    base = manifest.get("source", "")
    if base and os.path.exists(base):
        w, h, canvas = read_png(base)
    else:
        w = h = 64
        canvas = [[(0, 0, 0, 0)] * w for _ in range(h)]
        print("no source skin recorded, packing onto a blank", w, "x", h, "canvas")
    for code, entry in faces.items():
        path = os.path.join(face_dir, code + ".png")
        if not os.path.exists(path):
            continue
        sw, sh, px = read_png(path)
        x0, y0, x1, y1 = entry["rect"]
        if (sw, sh) != (x1 - x0, y1 - y0):
            print("skip", code, "resized:", (sw, sh), "!=", (x1 - x0, y1 - y0))
            continue
        for y in range(sh):
            for x in range(sw):
                canvas[y0 + y][x0 + x] = px[y][x]
    write_png(dst, w, h, canvas)
    print("packed", len(faces), "faces ->", dst)


if __name__ == "__main__":
    mode, *args = sys.argv[1:]
    if mode == "fetch" and len(args) == 1:
        fetch(*args)
    elif mode == "unpack" and 1 <= len(args) <= 2:
        unpack(*args)
    elif mode == "pack" and 1 <= len(args) <= 2:
        pack(*args)
    else:
        print(__doc__)
