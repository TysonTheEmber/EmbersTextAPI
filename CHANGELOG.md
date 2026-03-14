### Removed
- **`TextSpan.gradient()`** — 3 overloads removed. Use `span.effect("grad from=X to=Y")` or `<grad>` markup instead.
- **`TextSpan.shake()`** — 3 overloads removed. Use `span.effect("shake a=X")` or `<shake>` markup instead.
- **`TextSpan.charShake()`** — 3 overloads removed. Same replacement as above.
- **`ImmersiveMessage.shake()`** and **`ImmersiveMessage.charShake()`** — Use `<shake>` markup instead.
- **`ImmersiveMessage.gradient()`** — Use `<grad from=X to=Y>` markup instead.
- **`TypewriterConfig`** — Global defaults (20ms/char, always enabled) are now fixed constants in `TypewriterEffect`.
- **`ShakeType`** enum — No longer needed; effects system handles all shake types via `ShakeEffect`, `WaveEffect`, `CircleEffect`.
- **`EffectUtil.lerp()`** — Use `ColorMath.lerp()` directly.
- **`EffectUtil.hsvToRgb()`** — Use `ColorMath.hsvToRgbPacked()` directly.
- **`TooltipPacket.register()`** (Forge 1.20.1) — Was a no-op stub.

### Changed
- `TypewriterTrack` and `TypewriterTracks` moved from `net.tysontheember.emberstextapi.typewriter` to `net.tysontheember.emberstextapi.immersivemessages.effects.animation`. These are internal classes; most mod developers will not be affected.

### Internal
- Removed `ShakeCalculator` and `GradientCalculator` utility classes (only supported removed APIs).
- Removed all `[DIAG]` diagnostic logging from mixins and `ImmersiveMessage`.
- Cleaned up orphaned commented-out code in `MarkupParser` and `PlayerJoinEventHandler`.