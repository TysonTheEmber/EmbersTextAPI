# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ember's Text API is a Minecraft Forge mod (1.20.1) that provides an advanced text rendering API with rich visual effects, markup parsing, and network synchronization. The mod enables styled, animated text overlays ("immersive messages") with per-character effects like gradients, shake, typewriter animations, and more.

## Build Commands

```bash
# Build the mod JAR (outputs to build/libs/)
./gradlew build

# Run the Minecraft client with the mod
./gradlew runClient

# Run the dedicated server
./gradlew runServer

# Run data generators
./gradlew runData

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessageGradientTest"

# Clean build artifacts
./gradlew clean
```

## Architecture

### Core Components

**Entry Point**: `EmbersTextAPI.java` - Mod initialization, network registration, and client effect system setup.

**ImmersiveMessage** (`immersivemessages/api/ImmersiveMessage.java`) - The primary message object supporting:
- Span-based text rendering (v2.0.0+)
- Visual effects system (v2.1.0+)
- Anchor/align positioning, backgrounds, typewriter animations
- Per-character effects and reveal animations

**TextSpan** (`immersivemessages/api/TextSpan.java`) - Represents styled text segments with individual effects, colors, and rendering properties.

**MarkupParser** (`immersivemessages/api/MarkupParser.java`) - Parses XML-like markup into TextSpan lists. Supports tags like `<grad>`, `<shake>`, `<typewriter>`, `<rainbow>`, etc.

### Effect System (v2.1.0)

**Effect Interface** (`effects/Effect.java`) - Core interface for per-character rendering transformations (position, rotation, color, alpha).

**BaseEffect** (`effects/BaseEffect.java`) - Abstract base with parameter parsing and color utilities. Extend this for new effects.

**EffectRegistry** (`effects/EffectRegistry.java`) - Central registry for effect factories. Effects are registered at mod init in `ClientModEvents.onClientSetup()`.

**Visual Effects** (`effects/visual/`) - Built-in effects:
- Color: `RainbowEffect`, `GradientEffect`, `PulseEffect`, `FadeEffect`
- Motion: `WaveEffect`, `BounceEffect`, `SwingEffect`, `TurbulenceEffect`, `ShakeEffect`, `CircleEffect`, `WiggleEffect`, `PendulumEffect`, `ScrollEffect`
- Special: `GlitchEffect`, `NeonEffect`, `ShadowEffect`, `TypewriterEffect`

**EffectSettings** - Mutable state passed to effects containing character position, color channels, rotation, alpha, and sibling layers.

### Mixins

Mixins modify Minecraft's text rendering to support the API features:
- `StyleMixin` - Common style modifications
- `StringRenderOutputMixin` - Character-level rendering hooks
- `BakedGlyphMixin` / `FontAccess` - Glyph-level access
- `LiteralContentsMixin` / `TranslatableContentsMixin` - Component content access
- `StringSplitterMixin` - Text layout modifications
- `QuestScreenMixin` - FTB Quests integration

### Networking

**Network.java** - Forge SimpleChannel registration for all packets.

Packets (server-to-client):
- `S2C_OpenMessagePacket` - Display new message with UUID
- `S2C_UpdateMessagePacket` - Update existing message content
- `S2C_CloseMessagePacket` - Close specific message by UUID
- `S2C_CloseAllMessagesPacket` - Close all active messages
- `TooltipPacket` - Legacy tooltip message packet

### Client Managers

**ClientMessageManager** - Manages active messages by UUID, handles GUI rendering events and client ticks.

**ImmersiveMessagesManager** - Queue-based message manager with delay support for sequential message display.

## Creating New Effects

1. Create a class extending `BaseEffect` in `effects/visual/`
2. Parse parameters in constructor via `params.getDouble()`, `params.getString()`, etc.
3. Implement `apply(EffectSettings settings)` to modify rendering state
4. Implement `getName()` returning the effect tag name
5. Register in `EffectRegistry.initializeDefaultEffects()`:
   ```java
   register("myeffect", MyEffect::new);
   ```

## Markup Syntax

```
<grad from=#ff0000 to=#00ff00>gradient text</grad>
<shake a=2.0 f=1.5>shaking text</shake>
<typewriter s=20>typed text</typewriter>
<rainbow f=2.0>rainbow text</rainbow>
<bold><italic>styled</italic></bold>
```

Effect parameters use short keys: `a` (amplitude), `f` (frequency), `w` (wavelength), `s` (speed), etc.

## Key Specifications

See `TYPEWRITER_EFFECT_SPEC.md` for detailed typewriter effect behavior including per-context instance management, timing, and lifecycle rules.
