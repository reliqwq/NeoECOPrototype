# Third-Party Notices

Neo ECO Prototype is an addon. It does not include the dependency JARs in this repository or in the source release. The following components are required for development or runtime and remain the property of their respective authors.

## Neo ECO AE Extension

- Mod ID: `neoecoae`
- Current development compatibility baseline: `21.2.1-beta2` (local JAR under `neoecobeta/`), which declares its own version as `21.2.1-beta2`
- Declared compatibility floor: `[21.2.1-beta2,)`, raised from `21.2.0` in `3.0.0-beta1` because addon blocks implement upstream's `gui.GuiTitleProvider`. The floor names a prerelease on purpose: `21.2.1-beta1` declared itself as plain `21.2.1` so `[21.2.1,)` accepted it, but `beta2` declares its real version and a prerelease compares *below* the release, so the loader refused to start with "requires neoecoae 21.2.1 or above"
- Earlier baselines this addon was built against: `21.2.0-beta2`, `21.2.0-beta4` (first build exposing `IECOBulkMarkableCellItem`), `21.2.0-beta6`, `21.2.0`, `21.2.1-beta1`
- License declared by the upstream mod: GNU GPLv3
- Upstream authors listed by the upstream metadata: DancingSnow, ZhuRuoLing, and Yang120231

Upstream has not published a `21.2.1` file anywhere public as of `2026-09-30`; the JAR used for this baseline came directly from the upstream author, so a fresh clone cannot obtain it from download sites and must ask for it. That is the reason the first addon build on this baseline is published as a prerelease rather than as a stable `3.0.0`.

Neo ECO Prototype is an independent addon and is not the Neo ECO AE Extension project. The local development JAR used during testing is intentionally not redistributed by this repository.

One asset is copied rather than referenced: `src/main/resources/assets/neoecoprototype/models/block/simplify_casing.json` carries the 13-element geometry of upstream `assets/neoecoae/models/block/casing_base.json` verbatim, with its own texture slots. It is inlined on purpose - this addon's CI rejects any asset that points into the `neoecoae` namespace, because an upstream rename would turn our casing into a missing model with nothing to warn us. It is geometry only; the surface artwork is our own.

Special thanks to `Yang120` for substantial help from the original Neo ECO AE Extension team.

## MegaCells (optional runtime compatibility)

- Mod ID: `megacells`
- Verified development baseline: `4.11.0` for NeoForge 1.21.1
- Required by Neo ECO AE Extension beta4 only when its MegaCells integration is used; it is not bundled or required by this addon.
- License declared by the MegaCells metadata: LGPLv3.0.

## Advanced AE / GeckoLib (local development runtime only)

- Mod IDs: `advanced_ae`, `geckolib`
- Present in the local development runtime because the upstream compat mixins expect them at runtime; not compile dependencies of this addon and never redistributed.
- Advanced AE 1.6.9 by pedroksl; GeckoLib 4.8.3 by BernieL.

## Extended AE (optional compile-time reference only)

- Mod ID: `extendedae`
- Version: `1.21-2.2.28` (compile-time reference dependency, `compileOnly`); the addon builds and runs without it and never redistributes it.
- ExtendedAE by GlodBlock, GNU LGPLv3.

## Player skins bundled for the named dolls

`src/main/resources/assets/neoecoprototype/textures/block/fumo/skins/{reliqwq,yang120,kouooki,tedxenon}.png` are the actual player skins of four named people, shipped inside this GPL-3 jar so their dolls show the right face without reaching the session service.

- reliqwq, yang120, kouooki: collected locally on 2026-09-19/2026-09-20 into `fumo_skins/` (not redistributed by this file) and copied byte-for-byte into the jar on 2026-09-30.
- tedxenon: fetched on 2026-09-30 from Mojang's public texture CDN for profile `82af1b07-f745-4c9e-8a1f-8836e084980f`. That profile publishes only a classic `SKIN` (no `SLIM`), which is why the file is named without the `_slim` suffix.
- A player skin is that player's own content and Mojang's usage terms do not license us to redistribute it, so each file here stands on the person's own say-so, recorded on 2026-09-30 by this addon's maintainer. Adding another doll's face requires asking first - not fetching.
- Arm width is decided by the file name, not by the profile: `<name>.png` is 4-pixel wrists, `<name>_slim.png` is 3-pixel. `FumoRenderer#skinOf` explains why the profile's uuid cannot answer that for a bundled skin.

## Artwork and model assets
- `Neo-TiX`: original lead artist and artwork contributor; attribution retained with permission.
- `寒冰`: original artwork contributor; attribution retained with permission.

Textures and models belonging to this project are **All Rights Reserved (ARR)** unless a specific upstream license is stated alongside the asset. They may not be extracted, reused, or redistributed separately without permission. Permission to use the artwork as part of NeoECOPrototype does not transfer ownership or grant a separate redistribution license.

Some visual resources are adapted from Neo ECO AE Extension assets. Those assets retain the upstream project's licensing and attribution requirements and are not relicensed by Neo ECO Prototype. The project code remains GPL-3.0-only; the GPL license does not apply to ARR artwork assets.

## Applied Energistics 2

- Mod ID: `ae2`
- Version tested: `19.2.18` (upstream Neo ECO AE Extension `21.2.1-beta1` required `19.2.18` from its own side and `21.2.1-beta2` dropped that back to `19.2.17`, which is the floor this addon has declared all along; the only AE2 class that differs between the two jars is `core.localization.Tooltips`, and what it lost in `19.2.18` are fields this addon never names)
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

## Local development JARs (not redistributed)

`build.gradle` resolves every dependency from local files under `libs/` and `neoecobeta/` so a build needs no network, and this repository intentionally contains none of them. A fresh clone therefore cannot compile until those files are placed, and the build says so explicitly: any Gradle task stops with the exact missing paths instead of failing later as unresolved symbols. The list itself is not copied into this file on purpose - the declarations in `build.gradle` are the only version of it, and a duplicate here would drift.

Where each one comes from is the section above named for that mod. Two exceptions are worth stating: the `neoecoae` baseline this addon compiles against is a build the upstream author handed over directly and is not downloadable anywhere yet, and the Mekanism / Applied Mekanistics / AE2-JEI-Integration jars are in the local runtime only so the client can be inspected with them - they are not compile dependencies of this addon.

## Distribution rule

Do not copy dependency JARs into this repository or bundle them into a release without checking each upstream project's current redistribution terms. Link users to the official project pages instead.
