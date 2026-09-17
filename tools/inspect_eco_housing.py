import io
import zipfile

from PIL import Image

jar = zipfile.ZipFile("libs/neoecoae-21.2.0-111a6fd6-local.jar")
names = [
    "assets/neoecoae/textures/item/eco_cell_compat/mega_item_cell_housing.png",
    "assets/neoecoae/textures/item/eco_cell_compat/omni_cell_housing.png",
    "assets/neoecoae/textures/item/eco_cell_compat/quantum_omni_cell_housing.png",
]
for name in names:
    image = Image.open(io.BytesIO(jar.read(name))).convert("RGBA")
    pixels = [p for p in image.getdata() if p[3] > 0]
    average = tuple(sum(c[i] for c in pixels) // len(pixels) for i in range(3))
    brightest = max(sum(c[:3]) // 3 for c in pixels)
    print("%-34s size=%s avg=%s max=%s px=%d"
          % (name.rsplit("/", 1)[-1], image.size, average, brightest, len(pixels)))
