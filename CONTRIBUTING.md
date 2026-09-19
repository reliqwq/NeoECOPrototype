# Contributing

Thank you for helping improve Neo ECO Prototype.

## Before opening an issue

- Confirm the issue is reproducible on Minecraft 1.21.1 and NeoForge 21.1.x.
- Record the Neo ECO AE Extension, Applied Energistics 2, Java, and Neo ECO Prototype versions.
- Include the relevant `latest.log` or crash-report excerpt.
- Remove usernames, access tokens, local paths, world data, and other private information from logs.
- Do not attach dependency JARs or private development artifacts.

## Pull requests

- Keep changes focused and explain the player-facing behavior.
- Do not modify the upstream/reference checkouts kept outside the addon source tree.
- Do not use Mixin `@Overwrite` when an injection or accessor is sufficient.
- Keep resources in the `neoecoprototype` namespace and do not add `neoecoae:*` resource references for addon-owned assets.
- Add or update focused tests when changing shared behavior.
- Run `.\gradlew.bat test --offline` and, when resources or packaging change, `.\gradlew.bat build --offline`.
- Do not commit `build/`, `run/`, `.gradle/`, local dependency JARs, or reference source trees.

## Release checklist

- Bump `version` in `build.gradle` and `neoforge.mods.toml`, and the development-line note in `README.md`.
- Write the `CHANGELOG.md` entry and proof-read every mentioned block and item against its **in-game display name** (the lang entries), never internal class or registry names.
- Add `neoforge:conditions` to any recipe that references items registered only when an optional mod is present.
- When the optional-dependency surface changed, run the no-MEGA startup smoke: `tools/smoke_no_mega.ps1` (compiles with MegaCells first, then moves the jar aside and boots the client with `-x compileJava`; restore happens automatically on exit. The small bulk family must simply be absent, and the compile must NOT reference `gripe.*` outside the guarded auto-mark path).
- Run `.\gradlew.bat runGameTestServer` for the gametest suite.
- Run `.\gradlew.bat build --offline`, tag `v<version>`, push `main` and the tag, then publish the GitHub release with the JAR attached.

By submitting a contribution, you agree that it may be distributed under the repository's GPLv3-only license.
