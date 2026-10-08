# Neo ECO Prototype

An unofficial addon for Neo ECO AE Extension and Applied Energistics 2, for Minecraft 1.21.1 on NeoForge. It brings eco's multiblock machines down to an L1 tier that an ordinary survival network can reach early, and adds the interfaces, storage cells and pattern provider that tier needs around them.

[中文](README_ZH_CN.md)

## What it offers

Storage starts at L1 with drives, energy cells and the small bulk matrices. A small bulk matrix marks three resources and upgrades to ten on a crafting table; its capacity is counted as a long, and the byte ceiling is `Long.MAX_VALUE`. The family needs MegaCells; its item variant reuses eco's MEGA long-bulk backend, so marked items store as compression chains.

An L1 drive mounts native L1 cells and small bulk cells. It mounts anything else only when a server administrator has named it.

Crafting and computation reuse eco's structures at L1 numbers. The processor assembler keeps AE2's molecular assembler grid of nine slots and is extended to accept this addon's own processor recipes: a push carrying several complete sets of materials finishes them in one crafting cycle, with the output capped at one stack.

Any pattern provider feeds it, ours included. Ours holds 27 patterns where AE2's holds 9, and its recipe yields two per craft.

Two interfaces carry power and hold large stocks. The powered interface has 18 marker slots and injects 200 AE/t. The superconductive interface has the same layout, caches 8192 items or 512,000 mB of fluid per marker slot, and injects 4000 AE/t. AE2 runs only one passive generator per grid and always picks the strongest, so they do not add up.

The Trinity system folds storage, configuration and crafting into one structure. It is still experimental: it is left out of JEI and its items carry a not-implemented line until its design is finished.

## Environment

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.251 or a compatible 21.1.x release |
| Applied Energistics 2 | 19.2.17 or a compatible 19.2.x release |
| Neo ECO AE Extension | 21.2.1-beta2 or a compatible later release (the 21.2.1 release also asks for LDLib2 2.2.40 or newer) |
| Java | 21 |
| MegaCells | optional; required when the small bulk matrices are used |
| Mekanism and Applied Mekanistics | optional; required when chemical cells and chemical matrices are used |
| AppliedFlux | optional; required when the FE storage matrices are used — it brings its own requirements, GuideME and Glodium |

The addon is required on both sides.

## Installing

Install Minecraft 1.21.1 with NeoForge, then Applied Energistics 2, GuideME, LowDragLib2 and Neo ECO AE Extension at the versions above. Add MegaCells for the small bulk matrices, Mekanism with Applied Mekanistics for chemical storage, and AppliedFlux for FE storage. Put the release jar in `mods` and start the game.

The release jar does not bundle any of those dependencies; get them from their own maintainers and follow their licenses.

## Which cells an L1 drive mounts

Higher-tier eco cells can be inserted but will not mount until a server administrator names them. To permit selected cells, edit the server config:

```toml
[l1_storage]
additional_storage_cells = ["neoecoae:eco_mega_long_bulk_cell"]
```

## KubeJS

KubeJS is optional. Scripts register storage matrices and recipes through startup and recipe events, including eco's integrated working station recipe type, and the `neoecoprototype:infinite_storage_matrix` builder makes custom infinite item or fluid matrices. Scripts cannot change multiblock definitions or controller behaviour. Examples are in [docs/kubejs.md](docs/kubejs.md).

## Building from source

Place lawful copies of the exact compatible dependencies in `libs/`, as listed in `build.gradle`, then run:

```powershell
.\gradlew.bat build --offline
```

A build from a fresh clone stops with the names of the jars it is missing, because none of them are redistributed here. The output is `build/libs/neoecoprototype-0.3.1.jar`. Keep `libs/`, `run/`, `build/`, reference checkouts and local world data out of the repository.

Why the dependency floors are written the way they are, including the prerelease range for eco, is recorded in [docs/ae2-extension-playbook.md](docs/ae2-extension-playbook.md). What ships with a release, and how it is published, is in [docs/publishing.md](docs/publishing.md).

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. A bug report should give the Minecraft, NeoForge, Neo ECO AE Extension, Java and mod versions plus a relevant log excerpt.

## License

Neo ECO Prototype code is released under the GNU General Public License version 3.0 only. See [LICENSE](LICENSE).

Textures and models are artwork assets and are All Rights Reserved unless a specific upstream license is stated in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Artwork may not be extracted, reused or redistributed separately without permission, and required attribution must be retained.

Copyright (C) 2026 reliqwq.

## Credits

reliqwq wrote and maintains this addon. Neo-TiX is the original lead artist and artwork contributor, and 寒冰 also contributed artwork; both attributions are retained with permission. DancingSnow is the author of Neo ECO AE Extension, the upstream mod this addon requires, and Yang120 of the same team helped a great deal.

## Disclaimer

Neo ECO Prototype is not affiliated with or endorsed by Mojang, Microsoft, Applied Energistics 2, or Neo ECO AE Extension. Minecraft and related names are trademarks of their respective owners.
