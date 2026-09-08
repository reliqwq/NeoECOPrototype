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

By submitting a contribution, you agree that it may be distributed under the repository's GPLv3-only license.
