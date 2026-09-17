"""Bump the addon version in build.gradle and neoforge.mods.toml."""

from pathlib import Path

OLD = "1.1.1"
NEW = "1.2.0"

gradle = Path("build.gradle")
text = gradle.read_text(encoding="utf-8")
target = "version = '%s'" % OLD
if target not in text:
    raise SystemExit("build.gradle: %r not found" % target)
gradle.write_text(text.replace(target, "version = '%s'" % NEW, 1), encoding="utf-8", newline="")
print("build.gradle ->", NEW)

toml = Path("src/main/resources/META-INF/neoforge.mods.toml")
text = toml.read_text(encoding="utf-8")
target = 'version = "%s"' % OLD
if target not in text:
    raise SystemExit("mods.toml: %r not found" % target)
toml.write_text(text.replace(target, 'version = "%s"' % NEW, 1), encoding="utf-8", newline="")
print("neoforge.mods.toml ->", NEW)
