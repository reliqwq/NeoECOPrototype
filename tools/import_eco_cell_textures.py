"""Import the eco compatibility cell textures this addon renders into our own assets.

Referencing `neoecoae:item/eco_cell_compat/...` at runtime couples our models to eco's internal
asset layout. Copying the few textures we actually draw keeps our models self-contained, matching
the existing `eco_item_cell_housing` / `eco_fluid_cell_housing` copies in the same folder.

Animated eco textures carry a `.png.mcmeta` sidecar; copying the PNG alone silently freezes them,
so the sidecar is copied too.

Run from the repository root:  python tools/import_eco_cell_textures.py
"""

import zipfile
from pathlib import Path

ECO_JAR = Path("libs/neoecoae-21.2.0-111a6fd6-local.jar")
OUTPUT_DIR = Path("src/main/resources/assets/neoecoprototype/textures/item/storage_recolor")
ECO_ITEM_DIR = "assets/neoecoae/textures/item/eco_cell_compat"

# eco texture name -> local file name
TEXTURES = {
    "omni_cell_housing.png": "eco_omni_cell_housing.png",
    "quantum_omni_cell_housing.png": "eco_quantum_omni_cell_housing.png",
    "quantum_omni_cell_layer.png": "eco_quantum_omni_cell_layer.png",
}


def main():
    jar = zipfile.ZipFile(ECO_JAR)
    names = set(jar.namelist())
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    for source_name, local_name in TEXTURES.items():
        entry = f"{ECO_ITEM_DIR}/{source_name}"
        data = jar.read(entry)
        (OUTPUT_DIR / local_name).write_bytes(data)
        print(f"imported {entry} -> {local_name} ({len(data)} bytes)")

        meta_entry = f"{entry}.mcmeta"
        if meta_entry in names:
            meta_data = jar.read(meta_entry)
            (OUTPUT_DIR / f"{local_name}.mcmeta").write_bytes(meta_data)
            print(f"imported {meta_entry} -> {local_name}.mcmeta ({len(meta_data)} bytes)")
        else:
            # A stale sidecar would animate a texture that eco ships as static.
            stale = OUTPUT_DIR / f"{local_name}.mcmeta"
            if stale.exists():
                stale.unlink()
                print(f"removed stale sidecar {stale.name}")


if __name__ == "__main__":
    main()
