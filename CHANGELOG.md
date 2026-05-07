# Changelog

## v3.0.0-alpha.1 — [unreleased]

First public alpha of v3. Multi-loader release across Fabric, NeoForge, and Forge
on MC 1.20.1, 1.21.1, and 26.1.

### Highlights

- New bracket-markup syntax (`[]`) for message-level styling, backed by a
  registrable `MessageAttribute` system.
- New `MessageEffect` framework for animated message-level transforms — ships
  with `rock` and `breathe`.
- Gradient, pulse, and rainbow effects rewritten with multi-stop palettes and
  cleaner parameter names.
- Loader support extended to MC 26.1 (Fabric + NeoForge).

### Breaking

- Message-level markup tags moved from `<>` to `[]`. Removed: the angle-bracket
  forms of `bg`/`background`, `bggradient`/`backgroundgradient`, `scale`,
  `offset`, `anchor`, `align`, the global form of `shadow`, and the `in=/out=`
  form of `fade`. Migrate to `[bg color=red]`, `[scale 2]`,
  `[offset x=10 y=5]`, `[anchor TOP_CENTER]`, `[align LEFT]`, `[shadow]`,
  `[fade in=20 out=10]`.
- Removed the `<shake type=wave|circle>` compatibility shim and the
  `<charshake>` case. Use `<shake>`, `<wave>`, `<circle>` directly.
- Per-character forms `<shadow x= y= c=>` and `<fade a= f= w=>` are unchanged.
- `RainbowEffect` parameters renamed: `f` → `speed`, `w` → `phase`.

### Added

- `MessageAttribute` interface + `MessageAttributeRegistry` for registering
  custom message-level attributes that apply to the whole message.
- Built-in attributes: `bg` (solid and gradient via `from`/`to`), `scale`,
  `offset`, `anchor`, `align`, `shadow`, `fade`.
- `MessageEffect` framework + `MessageEffectRegistry` for animated whole-message
  transforms. Ships with `rock` and `breathe`.
- NBT and `FriendlyByteBuf` round-trip for both `messageAttributes` and
  `messageEffects` on `ImmersiveMessage`.
- `MarkupParser.parseFull` produces a separate `messageAttributes` list applied
  directly to `ImmersiveMessage`; per-character spans no longer carry
  message-level state.
- Positional shorthand for single-value tags: `[scale 2]`, `[anchor TOP_CENTER]`,
  `<color red>`, `<font norse>`. Numeric positional args supported in bracket
  tags.
- `ColorPalette` value type and `ColorParser.parseToRgbaFloats` (alpha-aware).
- `Palettes` parser for multi-stop color specs.
- Loader modules: `neoforge-26.1`, `fabric-26.1`.

### Changed

- `GradientEffect` rewritten: multi-stop palette, configurable angle, animates
  by default with smooth pingpong cycling. `timeOffset` bounded to preserve
  float precision.
- `PulseEffect` rewritten with an optional color-palette mode.
- `ColorEffect` accepts 8-digit hex (RGBA) and no longer guards on `isShadow`.
- Shadow inherits the wrapping effect's color on MC 1.20.1 and 1.21.1.
- Built-in presets updated to use the new `grad`/`pulse`/`rainbow` parameter
  names.
- Effect-tag param parser strips surrounding quotes from values.
- License metadata corrected across all loader poms and `mods.toml` to
  "Ember's Modding Licence v1.2" (was incorrectly reporting GPL-3.0).
- README support matrix and badges updated to include MC 26.1.

### Removed

- `TextSpan.globalXxx*` fields and their getters/setters (internal API).
- Empty `SoundEffect` enum stub. The real sound system is tracked in `TODO.md`
  and will land in a later release.

### Fixed

- Bracket-only markup is now routed through `fromMarkup` in test commands.
- Bracket tag parser accepts numeric positional arguments.
- Invalid `bg` gradient colors emit a warn-level log instead of failing
  silently.
- `emberstextapi:sdf` font providers are hidden from the vanilla `FontManager`
  parser to silence boot-time warnings.
- Forge 1.20.1 mod constructor no longer takes the removed
  `FMLJavaModLoadingContext` parameter.
- NeoForge 1.21.1 initializes effect registries on common setup so server-side
  parsing sees them.

### Build

- `forge-1.20.1` moved to a standalone Gradle 8.8 build (ForgeGradle 6 cannot
  run on Gradle 9). The root project delegates via `forge1201*` `Exec` tasks
  (`forge1201Build`, `forge1201RunClient`, etc.).
- NeoForge `moddev` plugin standardized at `2.0.141` across `neoforge-1.21.1`
  and `neoforge-26.1`.
- `fabric-1.20.1` and `fabric-1.21.1` stay on `fabric-loom-remap` (vanilla
  `fabric-loom` rejects Mojang mappings in this multi-loader workspace);
  documented inline.
- Publish workflow adds `forge-1.20.1` to the matrix (`*-all.jar` is the
  distributable, with LWJGL FreeType embedded to avoid a JPMS resolution
  failure on dedicated servers).
- `mc-publish` `version-type` is derived from the tag suffix
  (`-alpha*` / `-beta*` / release).
- `v3.0.0-alpha.0` is reserved as a dry-run tag — its workflow run skips
  Modrinth and CurseForge publishing while still updating Maven and creating a
  GitHub Release.
