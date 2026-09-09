# Neo ECO Prototype

Neo ECO Prototype is an unofficial addon for Neo ECO AE Extension and Applied Energistics 2. It adds lower-tier L1 storage, computation, and crafting multiblocks for Minecraft 1.21.1 on NeoForge.

本项目是 Neo ECO AE Extension 和 Applied Energistics 2 的非官方附属模组，为 Minecraft 1.21.1 / NeoForge 增加 L1 存储、计算和合成子系统。

## Features

- L1 storage, computation, and crafting multiblock definitions.
- JEI multiblock build previews for the three L1 structures.
- L1 storage cells, drives, energy cells, interfaces, and supporting blocks.
- Dedicated-server-safe crafting interface fallback for the preview12 runtime.
- Addon-owned model, texture, translation, and recipe files organized under the `neoecoprototype` namespace; some visual resources are adapted from upstream Neo ECO AE Extension assets and retain their upstream licensing.

## Compatibility

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233 or compatible 21.1.x release |
| Applied Energistics 2 | 19.2.17 or compatible 19.2.x release |
| Neo ECO AE Extension | 21.2.0-preview12 or a compatible release |
| Java | 21 |

Neo ECO Prototype is not affiliated with or endorsed by Mojang, Microsoft, Applied Energistics 2, or Neo ECO AE Extension. Minecraft and related names are trademarks of their respective owners.

## Installation

1. Install Minecraft 1.21.1 with NeoForge.
2. Install the required runtime dependencies: Applied Energistics 2, GuideME, LowDragLib2, and Neo ECO AE Extension.
3. Put the Neo ECO Prototype release JAR in the `mods` directory.
4. Start the game and verify that the required dependency versions are installed.

The release JAR does not bundle the dependency JARs listed above. Obtain them from their respective maintainers and follow their licenses and distribution rules.

## KubeJS Support

KubeJS is optional. Use `ServerEvents.recipes` to add, remove, or replace addon recipes, including the Neo ECO AE Extension integrated working station recipe type. See [docs/kubejs.md](docs/kubejs.md) for script examples. KubeJS scripts do not change multiblock structure definitions or controller behavior.

## Development Build

The current development setup uses a local Neo ECO AE Extension preview12 JAR under `libs/` because that release is not consumed from a public Maven coordinate in this project. Those files are intentionally excluded from GitHub.

Required local development files are listed in `build.gradle`. Place lawful copies of the exact compatible dependencies in `libs/`, then run:

```powershell
.\gradlew.bat build --offline
```

The output is written to `build/libs/neoecoprototype-1.0.3.jar`.

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

Neo ECO Prototype is released under the GNU General Public License version 3.0 only. See [LICENSE](LICENSE).

Copyright (C) 2026 reliqwq.

## Credits

- `reliqwq`: addon author and maintainer.
- `DancingSnow`: original author of Neo ECO AE Extension, the required upstream mod.
- `Yang120`: special thanks for substantial help from the original Neo ECO AE Extension team.
