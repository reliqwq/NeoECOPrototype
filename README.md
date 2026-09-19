# Neo ECO Prototype

Neo ECO Prototype is an unofficial addon for Neo ECO AE Extension and Applied Energistics 2. Its Trinity system combines eco multiblock structure and storage with an AE2/eco network crafting entry and unified task UI for Minecraft 1.21.1 on NeoForge.

本项目是 Neo ECO AE Extension 和 Applied Energistics 2 的非官方附属模组。Trinity 将 eco 多方块形态与存储能力、AE2/eco 网络任务入口和统一 UI 组合起来，为 Minecraft 1.21.1 / NeoForge 提供统一的存储与合成终端。

## Features

- Trinity multiblock structure with dedicated storage, configuration, and crafting modules. Trinity remains experimental and is hidden from player-facing JEI in the release build.
- AE2/eco network crafting entry with unified task state, cancellation, timeout, and stale-output protection.
- Advisory inventory and pattern observations; recursive material planning, alternatives, CPU selection, and execution remain owned by AE2/eco.
- JEI multiblock build previews plus addon-owned storage cells, drives, energy cells, interfaces, and supporting blocks.
- Dedicated-server-safe crafting interface fallback for the Neo ECO AE Extension beta4 runtime.
- L1 small bulk storage matrices in item, fluid, and chemical variants (requires MegaCells): `3` types by default or `10` after AE2's NBT-preserving cell-upgrade recipe, each with long-integer capacity. The item variant reuses eco's MEGA long-bulk backend, so marked items store as compression chains.
- L1 drives accept native L1 cells and small bulk cells by default; pack makers may explicitly whitelist extra eco cells, including eco's MegaCells bulk cell.
- Addon-owned model, texture, translation, and recipe files organized under the `neoecoprototype` namespace; some visual resources are adapted from upstream Neo ECO AE Extension assets and retain their upstream licensing.

## Compatibility

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233 or compatible 21.1.x release |
| Applied Energistics 2 | 19.2.17 or compatible 19.2.x release |
| Neo ECO AE Extension | 21.2.0-beta4 or a compatible release |
| MegaCells (optional; required for the small bulk matrix family) | 4.11.0 or later |
| Java | 21 |

### Platform environment

This mod requires both sides: CurseForge should be configured as `Client: Required` and `Server: Required` for the project and each uploaded file. The NeoForge metadata declares all required dependencies on `side = "BOTH"`; CurseForge environment labels are publishing-platform metadata and are not stored in `neoforge.mods.toml`.

Neo ECO Prototype is not affiliated with or endorsed by Mojang, Microsoft, Applied Energistics 2, or Neo ECO AE Extension. Minecraft and related names are trademarks of their respective owners.

## Installation

1. Install Minecraft 1.21.1 with NeoForge.
2. Install the required runtime dependencies: Applied Energistics 2, GuideME, LowDragLib2, and Neo ECO AE Extension. MegaCells is optional and only enables the small bulk matrix family.
3. Put the Neo ECO Prototype release JAR in the `mods` directory.
4. Start the game and verify that the required dependency versions are installed.

The release JAR does not bundle the dependency JARs listed above. Obtain them from their respective maintainers and follow their licenses and distribution rules.

### L1 external storage-cell policy

L1 drives mount native L1 cells and the addon small bulk cells by default. Higher-tier eco cells, including `neoecoae:eco_mega_long_bulk_cell`, remain insertable but do **not** mount unless a server administrator explicitly opts in. To permit selected cells, edit the server config:

```toml
[l1_storage]
additional_storage_cells = ["neoecoae:eco_mega_long_bulk_cell"]
```

The small bulk cells start with three types (fluid and chemical variants included) and upgrade to ten types through the `ae2:storage_cell_upgrade` recipe; AE2 copies the source cell's NBT, so stored contents and cell settings are retained. The item variant reuses eco's MEGA long-bulk storage backend, so marked items store as compression chains; installing the MegaCells compression card in the cell workbench enables the chain variants.

## KubeJS Support

KubeJS is optional. Matrix texture recoloring is handled offline by `tools/generate_matrix_textures.py` with `tools/matrix_textures.json`; it preserves grayscale housing and only recolors configured source hues. The generated PNGs are ordinary runtime assets. The 4K chemical storage matrix is available as `neoecoprototype:simplify_chemical_storage_cell_4k` when Mekanism and Applied Mekanistics are installed. Use `ServerEvents.recipes` to add, remove, or replace addon recipes, including the Neo ECO AE Extension integrated working station recipe type. The addon also provides the `neoecoprototype:infinite_storage_matrix` startup builder for custom infinite item or fluid matrices. By default these matrices use the addon's unified infinite-matrix material for both the inventory item and the drive-mounted cell: a white housing, green level light, and gray type light. The builder also supports optional `.size('infinite')`, `.tier('l1')`, `.inventoryModel(...)`, and `.cellModel(...)` methods; the current infinite implementation validates the fixed `infinite`/`l1` combination. New Java matrices can use the `StorageMatrixDefinition` and `StorageMatrixRegistration` API without changing legacy registrations. See [docs/kubejs.md](docs/kubejs.md) for script examples. KubeJS scripts do not change multiblock structure definitions or controller behavior.

## Development Build

The current development setup uses the published Neo ECO AE Extension `21.2.0-beta4` JAR under `neoecobeta/` for offline compatibility testing. The local development copy is intentionally excluded from GitHub. The stable GitHub `v1.1.0` release remains a separate artifact from this development tree; this beta4-following development line is version `1.2.2`.

Required local development files are listed in `build.gradle`. Place lawful copies of the exact compatible dependencies in `libs/`, then run:

```powershell
.\gradlew.bat build --offline
```

The output is written to `build/libs/neoecoprototype-1.2.2.jar`.

Do not commit `libs/`, `run/`, `build/`, reference checkouts, or local world data. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for dependency licensing information.

## Source and Release Policy

The repository contains the source and resources for Neo ECO Prototype only. Local runtime artifacts, modified development copies of Neo ECO AE Extension, reference checkouts, and dependency binaries are not part of this repository.

A release should include:

- the mod JAR;
- the corresponding source revision;
- the required dependency versions;
- this repository's license and third-party notices.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. Bug reports should include the Minecraft, NeoForge, Neo ECO AE Extension, Java, and mod versions, plus a relevant log excerpt.

## License

Neo ECO Prototype code is released under the GNU General Public License version 3.0 only. See [LICENSE](LICENSE).

Textures and models are artwork assets and are **All Rights Reserved (ARR)** unless a specific upstream license is stated in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Artwork may not be extracted, reused, or redistributed separately without permission. Required attribution must be retained.

Copyright (C) 2026 reliqwq.

## Credits

- `reliqwq`: addon author and maintainer.
- `Neo-TiX`: original lead artist and artwork contributor; attribution retained with permission.
- `寒冰`: original artwork contributor; attribution retained with permission.
- `DancingSnow`: original author of Neo ECO AE Extension, the required upstream mod.
- `Yang120`: special thanks for substantial help from the original Neo ECO AE Extension team.
