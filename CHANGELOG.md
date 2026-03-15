### Added
- **Emojiful compatibility layer** — ETA effects now work alongside Emojiful's emoji rendering. Regular characters get full effects (rainbow, wave, shake, etc.) while emoji sprites render normally through Emojiful's pipeline. Supported on all loaders and versions (Forge 1.20.1 with Emojiful 4.x, NeoForge 1.21.1 with Emojiful 5.x, Fabric).
- **`EffectApplicator` utility class** — Shared effect-application logic extracted from `StringRenderOutputMixin`, used by both the vanilla rendering path and the Emojiful compatibility mixin.

### Changed
- Emojiful is no longer declared as incompatible. The previous hard block has been replaced with automatic compatibility mode detection.

### Internal
- Added `@Pseudo` mixin `EmojiCharacterRendererMixin` targeting both `EmojiFontHelper$EmojiCharacterRenderer` (NeoForge/Emojiful 5.x) and `EmojiFontRenderer$EmojiCharacterRenderer` (Forge/Emojiful 4.x) with `require = 0` for graceful absence.
- Refactored `StringRenderOutputMixin` to delegate effect building, application, and character rendering to `EffectApplicator`.
