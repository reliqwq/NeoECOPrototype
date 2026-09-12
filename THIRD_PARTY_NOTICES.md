# Third-Party Notices

Neo ECO Prototype is an addon. It does not include the dependency JARs in this repository or in the source release. The following components are required for development or runtime and remain the property of their respective authors.

## Neo ECO AE Extension

- Mod ID: `neoecoae`
- Version used by Neo ECO Prototype 1.0.5: local build of upstream commit `111a6fd6` (upstream version string `21.2.0-preview14`)
- Previous published compatibility baseline: `21.2.0-preview14`
- License declared by the upstream mod: GNU GPLv3
- Upstream authors listed by the upstream metadata: DancingSnow, ZhuRuoLing, and Yang120231

Neo ECO Prototype is an independent addon and is not the Neo ECO AE Extension project. The local development JAR used during testing is intentionally not redistributed by this repository.

Special thanks to `Yang120` for substantial help from the original Neo ECO AE Extension team.

## Advanced AE / GeckoLib (local development runtime only)

- Mod IDs: `advanced_ae`, `geckolib`
- Present in the local development runtime because the upstream compat mixins expect them at runtime; not compile dependencies of this addon and never redistributed.
- Advanced AE 1.6.9 by pedroksl; GeckoLib 4.8.3 by BernieL.

## Extended AE (optional compile-time reference only)

- Mod ID: `extendedae`
- Version: `1.21-2.2.28` (compile-time reference dependency, `compileOnly`); the addon builds and runs without it and never redistributes it.
- ExtendedAE by GlodBlock, GNU LGPLv3.

## Artwork and model assets

- `Neo-TiX`: original lead artist and artwork contributor; attribution retained with permission.
- `寒冰`: original artwork contributor; attribution retained with permission.

Textures and models belonging to this project are **All Rights Reserved (ARR)** unless a specific upstream license is stated alongside the asset. They may not be extracted, reused, or redistributed separately without permission. Permission to use the artwork as part of NeoECOPrototype does not transfer ownership or grant a separate redistribution license.

Some visual resources are adapted from Neo ECO AE Extension assets. Those assets retain the upstream project's licensing and attribution requirements and are not relicensed by Neo ECO Prototype. The project code remains GPL-3.0-only; the GPL license does not apply to ARR artwork assets.

## Applied Energistics 2

- Mod ID: `ae2`
- Version tested: `19.2.17`
- License: see the upstream project and the `NOTICE` file distributed with AE2
- Project: <https://github.com/AppliedEnergistics/Applied-Energistics-2>

## GuideME

- Mod ID: `guideme`
- Version tested: `21.1.1`
- License: see the upstream project and the license information distributed with GuideME
- Project: <https://github.com/AppliedEnergistics/GuideME>

## LowDragLib2

- Mod ID: `ldlib2`
- Version tested: `2.2.8`
- License declared by the mod metadata: LGPL-3.0
- Project: <https://github.com/Low-Drag-MC/LDLib2>

## Just Enough Items

- Mod ID: `jei`
- Version tested: `19.27.0.340`
- License declared by the mod metadata: MIT
- Project: <https://github.com/mezz/JustEnoughItems>

## Jade

- Mod ID: `jade`
- Version tested: `15.10.4`
- License declared by the mod metadata: CC-BY-NC-SA-4.0
- Project: <https://github.com/Snownee/Jade>

## Minecraft and NeoForge

Minecraft, Minecraft Forge/NeoForge, and their names and trademarks belong to their respective owners. Neo ECO Prototype is an unofficial community mod and is not endorsed by Mojang or Microsoft.

- NeoForge: <https://neoforged.net/>
- Minecraft usage guidelines: <https://www.minecraft.net/en-us/usage-guidelines>

## Distribution rule

Do not copy dependency JARs into this repository or bundle them into a release without checking each upstream project's current redistribution terms. Link users to the official project pages instead.
