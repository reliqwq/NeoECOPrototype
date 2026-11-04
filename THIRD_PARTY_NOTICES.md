# Third-Party Notices

Neo ECO Prototype is an addon. No dependency JAR is included in this repository or in the source release; each project below stays the property of its authors, and users are pointed at the official download pages.

## Dependencies

| Component | Mod ID | Version this addon builds against | License as published upstream |
|---|---|---|---|
| Neo ECO AE Extension | `neoecoae` | `21.2.9` (also the declared floor) | GNU GPL-3.0 |
| Applied Energistics 2 | `ae2` | `19.2.17` | LGPL v3 / GPL v3 for code; **textures and models CC BY-NC-SA 3.0** |
| GuideME | `guideme` | `21.1.1` | see upstream |
| LDLib2 | `ldlib2` | `2.2.40` (hard dependency, inherited from upstream's floor) | LGPL-3.0 per mod metadata |
| Mega Cells | `megacells` | `4.11.0` (optional) | LGPL-3.0 per mod metadata |
| Extended AE | `extendedae` | `1.21-2.2.28` (compile-time reference only) | GNU LGPLv3 |
| AE2 OmniCells | `ae2omnicells` | `1.1.6` (optional) | see upstream |
| AppliedFlux | `appflux` | `1.21-2.1.5` (optional) | see upstream |
| Beyond Dimensions | `beyonddimensions` | `0.7.30` (optional) | see upstream |
| Just Enough Items | `jei` | `19.57.0.445` (compile-time only) | MIT |
| Jade | `jade` | Curse file `7428884` (compile-time only) | CC-BY-NC-SA-4.0 per mod metadata |
| Mekanism / Applied Mekanistics / AE2-JEI-Integration | — | local runtime only, never bundled | see upstream |

Development JARs are resolved from local files, so a fresh clone cannot compile until they are placed; the build then stops with the exact missing paths instead of failing later as unresolved symbols. The `21.2.9` build of Neo ECO AE Extension used as the baseline was handed over by its author and has no public download page yet - a packaging note, not a stability one.

The upstream mod's own metadata lists its authors as DancingSnow, ZhuRuoLing and Yang120231. Special thanks to `Yang120` for substantial help from the original Neo ECO AE Extension team.

One model, not a texture, is copied rather than referenced: `models/block/simplify_casing.json` carries the 13-element geometry of upstream `assets/neoecoae/models/block/casing_base.json` with its own texture slots. It is inlined on purpose - this addon's CI rejects any asset that points into the `neoecoae` namespace, because an upstream rename would otherwise turn our casing into a silent missing model. Geometry only; the surface artwork is our own.

## Artwork and model assets

- `Neo-TiX` - original lead artist and artwork contributor; attribution retained with permission.
- `kyln默寒寒冰` - original artwork contributor; attribution retained with permission.
- `ControlNet` - the contributor of PR #17, whose five candidate icons include the one we ship for the cryotheum ice cream (`textures/item/cryotheum_ice_cream/sundae_glass.png`); the PR names `Codex` and `Claude` as the generators.

Of the 325 PNGs this addon ships, in four groups:

1. **260 original** - drawn by the artists above, or produced inside this repository from their own files (the unlink-card icon is our own link-card tile with 12 pixels recoloured). **All Rights Reserved (ARR)**: not to be extracted, reused or redistributed separately without permission. Permission to use the artwork as part of NeoECOPrototype does not transfer ownership or grant a separate redistribution license.
2. **50 adapted from Neo ECO AE Extension** - 9 byte-for-byte copies of cell-art textures and 41 that keep upstream's pixel layout with a different palette. Upstream publishes under GPL-3.0 with no separate asset clause, so these carry upstream's licensing and attribution requirements and are not relicensed by this addon.
3. **14 adapted from Applied Energistics 2** - four pattern-provider faces, one interface face, three part backs, and since 2026-11-04 the four budding Grette mother rocks, the solid Grette crystal block and the Grette crystal dust (`{flawless,flawed,chipped,damaged}_budding_grette.png`, `grette_crystal_block.png`, `grette_crystal_dust.png`), each keeping AE2's pixel layout with a different palette. AE2's asset notice reads, verbatim: *"Textures and Models (c) 2020, Ridanisaurus Rid, (c) 2013 - 2020 AlgorithmX2 et al / License: CC BY-NC-SA 3.0"*. That license requires attribution, carries a non-commercial term, and requires derivatives to be shared under the same terms. **This addon makes no ARR claim over those fourteen files**, and anyone reusing them follows AE2's terms rather than ours.
4. **1 supplied as AI-generated pixel art** - `textures/item/cryotheum_ice_cream/sundae_glass.png`, from PR #17. What is checkable and what is not, stated separately. The tile has 13 colours: **7 of eco's 10** crystal-ramp colours (`cryotheum_crystal.png` is a 12-frame animation and all twelve frames use the same ten) and **6 of its own** - `(74,95,110)` slate, `(185,211,222)`, `(217,210,191)`, `(242,250,255)`, `(247,243,232)` and plain white. Five of those six appear in **none of the other 321 texture PNGs this addon ships**; white is in 19 of ours and 118 of the 1425 PNGs in the two jars we build against, and the slate appears in exactly one jar file - AE2's `ae2guide/assets/diagrams/quantum_bridge_demonstration.png`, a 1920×1057 guide illustration, not a pixel tile - so it is not borrowed from anyone's pixel art either. Per pixel against eco's crystal frame 0, **5 of its 126 opaque pixels share both position and colour**, so it is not a recolour and does not belong in group 2. No measurement here can establish how it was made, or what the generator was trained on; "original layout, no pixels copied" is the contributor's claim, and the numbers above are the check on the part of it we can check. Two deviations from the family are known and were accepted on purpose when this one was chosen over the four candidates that keep eco's outline: **the dominant outline is that slate `(74,95,110)` rather than eco's deep-sea blue `(12,63,104)`**, and the cream highlights `(247,243,232)` / `(217,210,191)` are new to the cryotheum family. We ship it under our own ARR notice because the contributor offered it that way, and anyone who takes a different view of AI-generated work should treat that one file as the first to remove.

The AE2-shaped tiles keep AE2's pixel layout and take their colours from our own `grette_cluster.png`, which is the family's colour authority: AE2's nine-colour ramp is first darkened (hue 210, saturation x1.55, value x0.72, saturation floor 0.12), then its hue and saturation are re-skinned along the cluster's own luminance curve, AE2's value ladder untouched. Measured with the mapping-break ruler, comparing only pixels opaque in both tiles: **0 of the comparable pixels break the colour mapping** for each tile against its own AE2 source - 0/256 for the mother rocks and the block, 0/88 for the dust, whose remaining pixels are AE2's colour-under-alpha. They replace an original drawing by our lead artist; the previous tiles are kept outside the repository for comparison. Adopting AE2's shape also gives up one property our own drawing had: our four conditions shared one stone body and changed only a 9x8 centre box (36-42 pixels, 0 outside), while these change 29-56 pixels spread over x3-12 / y2-12, leaving 195 of 256 pixels identical across all four.

The counts above were re-derived on 2026-11-04 by file content, not file name, against the two jars this addon actually builds against - `ae2-19.2.17.jar` (614 textures indexed) and `neoecoae-21.2.15-beta.jar` (480). A tile's signature is its size, its colour count and its per-pixel colour-equality map, which a pure recolour preserves exactly; matching signatures at equal size with at least three distinct colours is what "same layout, different palette" means here, so flat glow sheets cannot match by accident. The split came out **50 from eco + 14 from AE2 = 64**, and the 50 reproducing the earlier audit's eco figure against a different jar is the check that this is the same method. Two ways this measurement goes wrong are recorded because both were actually hit: an index that skips tiles taller than 64px drops precisely the animated cell art counted as byte-for-byte copies and reports 43 instead of 50; and a mapping-break ruler that counts transparent pixels reads AE2's colour-under-alpha as a broken mapping, scoring a pure palette map 29/256 where the honest figure is 0/88. That ruler is also lenient when the candidate has very few colours, which is why `sky_stone_dust.png` (3 colours) looked like a recolour of AE2's `sky_dust.png` (13 colours) under it and is not one under this rule - its equality structure differs, so it stays in group 1. The colours of the 2026-11-04 tiles come from our own `grette_cluster.png`: the crystal item's off-family colours snap onto the cluster's band by an injective, order-preserving assignment, and the AE2-shaped tiles take hue and saturation from that same band while keeping AE2's own value ladder. The project code remains GPL-3.0-only, and GPL does not apply to the ARR artwork in group 1.

## Player skins bundled for the named dolls

`textures/block/dolls/{reliqwq,yang120,kouooki,tedxenon}.png` are the actual player skins of four named people, shipped so their dolls show the right face without reaching the session service. A player skin is that player's own content, and Mojang's usage terms do not license us to redistribute it, so each of these four files stands on that person's own say-so. **Adding another doll's face requires asking first, not fetching.**

## Distribution rule

Do not copy dependency JARs into this repository or bundle them into a release without checking each upstream project's current redistribution terms. Link users to the official project pages instead.

Upstream projects: <https://github.com/DancingSnow0517/NeoECOAEExtension> · <https://github.com/AppliedEnergistics/Applied-Energistics-2> · <https://github.com/AppliedEnergistics/GuideME> · <https://github.com/Low-Drag-MC/LDLib2> · <https://github.com/mezz/JustEnoughItems> · <https://github.com/Snownee/Jade>
