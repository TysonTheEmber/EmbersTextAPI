# Changelog

## v3.0.0-alpha.2 — 2026-05-12

### Added

- `<click>` and `<hover>` markup tags for interactive text. `<click>` accepts
  `action=` (`open_url`, `run_command`, `suggest_command`, `copy_to_clipboard`)
  and `value=`. `<hover>` accepts `action=` (`show_text`, `show_item`,
  `show_entity`) and `value=`. Nested tags override parent click/hover values.
- `<lang:KEY>` markup tag resolves vanilla translation keys in each viewer's
  client locale. Shorthand `<lang:item.minecraft.diamond>` or attribute form
  `<lang key=KEY>` / `<lang key=KEY args=A,B,C>` for placeholder substitution.
  Missing keys render literally; translation file content is not re-parsed for
  ETA tags. Closes #2.
- `ImmersiveMessage.decode` re-parses `markupSource` on the client (1.20.1 and
  1.21.1) to ensure server-issued messages resolve markup in the receiving
  client's locale, matching the existing behavior on 26.1.
- Patchouli compatibility: `@Pseudo BookTextParserMixin` plus a
  `PatchouliBypass` thread-local that suppresses ETA markup rewriting in
  `LiteralContents`, `TranslatableContents`, and `StringSplitter` while
  Patchouli is parsing book text.

### Fixed

- SDF glyph pre-baking no longer repeats on every `FontManager` reload
  (initial load, F3+T, resource pack swap, datapack reload). Pre-baked MSDF
  data is now held in a process-lifetime cache keyed by (font fingerprint,
  SDF config), so reloads after the initial bake short-circuit the async pass.
- `SDFGlyphProvider.closed` is now `volatile`, so the background pre-bake
  loop reliably observes provider close.
- `<click>` and `<hover>` participate in the mixin render-gate, so
  click/hover-only spans no longer fall through to vanilla rendering on
  1.20.1, 1.21.1, and 26.1.
- Lang-only markup (`<lang>` with no other tags) renders through the ETA
  mixin pipeline instead of bypassing it.
- Immersive-message HUD layer no longer leaks `disableDepthTest` and
  `enableBlend` into world rendering. Previously, on 1.20.1 and 1.21.1, the
  layer wrapper toggled global GL state every frame without restoring it,
  causing the skybox to disappear on Fast graphics and the hotbar background
  to drop out on Fabulous. The wrapper now matches the 26.1 pattern: just
  push/translate/render/pop. Render types and `BackgroundRenderer` already
  set up their own blend state.