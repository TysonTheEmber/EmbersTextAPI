# Global Text Styling Implementation Plan

**Project**: EmbersTextAPI v2.x
**Target**: Incorporate TextAnimator-style global text rendering capabilities
**Date**: 2025-12-03
**Status**: Planning Phase

---

## 1. Overview and Goals

### Goal: Enable Global Text Effect Application

Transform EmbersTextAPI from a targeted overlay system into a **universal text styling framework** that can apply effects to ANY text rendered in Minecraft, including:

- Chat messages (player chat, system messages)
- GUI text (tooltips, buttons, menus, item names)
- HUD elements (actionbar, boss bar, titles, subtitles)
- In-game signs, books, nameplates
- FTB Quests and other mod GUIs

### Key Objectives

1. **Inject at Minecraft's core text rendering pipeline** to intercept ALL text before rendering
2. **Parse inline effect markup** in text content (e.g., `<rainbow>text</rainbow>`, `<wave>text</wave>`)
3. **Apply effects globally** without requiring explicit ImmersiveMessage creation
4. **Preserve existing ImmersiveMessage system** as a specialized high-level API
5. **Maintain compatibility** with existing effects and markup parser
6. **Minimize performance impact** through efficient caching and selective processing

### Constraints

- **Minecraft version**: 1.20.1
- **Mod loader**: Forge (NeoForge compatible patterns)
- **Client-only**: All rendering logic must be client-side only
- **Mixin-based**: Use Mixin injection for core integration (already configured)
- **Backward compatibility**: Don't break existing ImmersiveMessage API

---

## 2. Reference Analysis Summary: TextAnimator Architecture

### How TextAnimator Works

TextAnimator achieves global text styling through a **multi-layered Mixin injection strategy**:

#### Layer 1: Style Augmentation (Data Injection)
- **StyleMixin** adds custom fields to `net.minecraft.network.chat.Style`:
  - `List<Effect> textanimator$effects` - stores effects attached to a style
  - `TypewriterTrack textanimator$track` - tracks typewriter animation state
  - `int textanimator$typewriterIndex` - character index for typewriter
- **TAStyle** duck interface exposes getters/setters for these fields
- Effects propagate through style inheritance (all `withX()` methods)

#### Layer 2: Text Content Parsing (Markup Detection)
- **LiteralContentsMixin** + **TranslatableContentsMixin**:
  - Intercepts `Component.visit()` calls
  - Detects inline markup in text content (e.g., `<rainb>text</rainb>`)
  - Parses effects and attaches them to the Style object
  - Strips markup from displayed text
- **StringDecomposerMixin**:
  - Redirects all string iteration to custom logic
  - Processes effects during character decomposition

#### Layer 3: Rendering Interception (Effect Application)
- **StringRenderOutputMixin** (Font.StringRenderOutput):
  - Intercepts `accept(int index, Style style, int codepoint)`
  - Reads effects from `TAStyle` interface
  - Modifies glyph position, color, alpha, rotation per-character
  - Applies effects by mutating `EffectSettings` object
- **BakedGlyphMixin**:
  - Adds custom rendering method `textanimator$render()`
  - Applies per-glyph transformations (rotation, offset, etc.)

#### Key Design Patterns
1. **Duck typing** via Mixin interfaces (TAStyle, TABakedGlyph)
2. **Style piggyback** - effects ride on vanilla Style objects
3. **Character-level processing** - effects applied per-codepoint during render
4. **Side-safe** - all client mixins in separate config section

#### TextAnimator File Structure
```
snownee/textanimator/
├── TextAnimator.java              # Effect registration
├── TextAnimatorClient.java         # Client utilities (rotation, random dirs)
├── effect/
│   ├── Effect.java                 # Effect interface
│   ├── EffectFactory.java          # Effect registry
│   ├── EffectSettings.java         # Per-character render state
│   ├── BaseEffect.java             # Abstract base class
│   ├── RainbowEffect.java          # Example: color animation
│   ├── WaveEffect.java             # Example: positional wave
│   └── [12 more effects...]
├── mixin/
│   ├── StyleMixin.java             # Augment Style with effects
│   ├── StringDecomposerMixin.java  # Redirect string processing
│   └── client/
│       ├── FontMixin.java          # Modify rainbow in border rendering
│       ├── StringRenderOutputMixin.java  # CORE: Apply effects during render
│       ├── BakedGlyphMixin.java    # Custom glyph rendering
│       ├── LiteralContentsMixin.java     # Parse markup in literals
│       └── TranslatableContentsMixin.java # Parse markup in translatables
└── duck/
    ├── TAStyle.java                # Interface for Style augmentation
    └── TABakedGlyph.java           # Interface for glyph augmentation
```

---

## 3. Current State Analysis: EmbersTextAPI

### Architecture Overview

EmbersTextAPI v2.x uses a **span-based overlay rendering system**:

1. **ImmersiveMessage**: High-level API for creating styled messages
   - Wraps `List<TextSpan>` with global attributes (duration, position, background)
   - Sent via network packets (S2C_OpenMessagePacket) to clients
   - Rendered as HUD overlays (not integrated with vanilla text)

2. **TextSpan**: Per-span styling container
   - Holds text content + styling attributes (color, font, effects)
   - Supports inline item/entity rendering
   - Recently added: `List<Effect> effects` (v2.1.0)

3. **MarkupParser**: Parses `<tag>` markup into TextSpan objects
   - Handles nested tags via stack-based parsing
   - Maps tags to TextSpan attributes
   - Supports 15+ effect types (rainbow, wave, shake, etc.)

4. **Effect System** (v2.1.0):
   - `Effect` interface with `apply(EffectContext)` method
   - `EffectRegistry` for registration and tag parsing
   - 14 built-in effects (RainbowEffect, WaveEffect, etc.)
   - Effects stored in TextSpan, applied during custom overlay rendering

5. **ClientMessageManager**: Manages active overlay messages
   - Renders via `ClientEvents.onRenderOverlay()`
   - Custom text rendering using `Font.drawInBatch8xOutline()`
   - Completely separate from Minecraft's normal text pipeline

### Strengths
- ✅ Rich effect system already implemented
- ✅ Markup parser handles complex nested tags
- ✅ Effect architecture is extensible (Effect interface)
- ✅ Span-based design allows per-region styling

### Limitations
- ❌ **NOT global** - only applies to explicit ImmersiveMessage instances
- ❌ **Overlay-only** - doesn't integrate with vanilla text rendering
- ❌ Effects in chat require explicit commands (`/emberstextapi send`)
- ❌ Cannot style GUI elements, tooltips, or third-party mod text
- ❌ Markup only parsed in ImmersiveMessage context

### Technical Debt
- Mixins not configured (no `emberstextapi.mixins.json` found)
- Effect system separate from rendering (applied in custom renderer)
- No integration with Minecraft's `Style` or `FormattedCharSequence`

---

## 4. Target Architecture Design

### High-Level Design

**Hybrid Approach**: Combine TextAnimator's global injection with EmbersTextAPI's rich span system

```
┌─────────────────────────────────────────────────────────────┐
│  User Input: Chat, GUI, Commands, Components                │
└──────────────────────┬──────────────────────────────────────┘
                       │
       ┌───────────────┴────────────────┐
       │  Component System (Vanilla)     │
       │  - MutableComponent            │
       │  - Style objects               │
       └───────────────┬────────────────┘
                       │
          ┌────────────▼─────────────┐
          │  MIXIN INJECTION LAYER   │
          │  ┌──────────────────────┐│
          │  │ 1. StyleMixin        ││  <-- Augment Style with Effect list
          │  │    + emberstextapi$  ││
          │  │      effects: List   ││
          │  └──────────────────────┘│
          │  ┌──────────────────────┐│
          │  │ 2. ComponentMixins   ││  <-- Parse markup in text content
          │  │    - LiteralContents ││      Detect <rainbow>, <wave>, etc.
          │  │    - Translatable    ││      Attach effects to Style
          │  └──────────────────────┘│
          │  ┌──────────────────────┐│
          │  │ 3. StringDecomposer  ││  <-- (Optional) Custom iteration
          │  └──────────────────────┘│
          └────────────┬─────────────┘
                       │
       ┌───────────────▼────────────────┐
       │  StringDecomposer               │
       │  - iterateFormatted()          │
       │  - Emits (index, style, cp)    │
       └───────────────┬────────────────┘
                       │
          ┌────────────▼─────────────┐
          │  RENDERING LAYER         │
          │  ┌──────────────────────┐│
          │  │ 4. StringRenderOut   ││  <-- CORE: Apply effects per char
          │  │    accept(i,sty,cp)  ││      Read emberstextapi$effects
          │  │    → apply effects   ││      Modify position/color/alpha
          │  │    → render glyph    ││
          │  └──────────────────────┘│
          │  ┌──────────────────────┐│
          │  │ 5. BakedGlyphMixin   ││  <-- Custom glyph render method
          │  │    emberstextapi$    ││
          │  │    render()          ││
          │  └──────────────────────┘│
          └────────────┬─────────────┘
                       │
       ┌───────────────▼────────────────┐
       │  OpenGL / Vertex Buffers        │
       │  - Final rendered text          │
       └─────────────────────────────────┘
```

### Core Components to Create

#### 1. Mixin Configuration (`emberstextapi.mixins.json`)
- Define common and client mixin lists
- Set compatibility level (JAVA_17)
- Configure injection priority (1200 to run after most mods)

#### 2. Duck Interfaces (`net.tysontheember.emberstextapi.mixin.duck`)
- **ETAStyle**: Interface for Style augmentation
  ```java
  ImmutableList<Effect> emberstextapi$getEffects()
  void emberstextapi$setEffects(ImmutableList<Effect>)
  void emberstextapi$addEffect(Effect)
  ```
- **ETABakedGlyph**: Interface for custom glyph rendering
  ```java
  void emberstextapi$render(EffectContext, boolean italic, float boldOffset, ...)
  ```

#### 3. Mixin Classes

##### Common Mixins (Both Sides):
- **StyleMixin**:
  - Add `ImmutableList<Effect> emberstextapi$effects` field
  - Implement ETAStyle interface
  - Propagate effects in `applyTo()`, `withColor()`, etc.
  - Hook serialization (save effects to JSON with `eta$effects` key)

##### Client Mixins:
- **LiteralContentsMixin**:
  - Inject at `visit()` method (HEAD, cancellable)
  - Parse inline markup (delegate to MarkupParser)
  - Extract effects and attach to style
  - Strip markup from displayed text

- **TranslatableContentsMixin**:
  - Same pattern as LiteralContentsMixin
  - Handle translated strings with markup

- **StringRenderOutputMixin** (CRITICAL):
  - Target: `net.minecraft.client.gui.Font$StringRenderOutput.accept()`
  - Inject at HEAD, cancellable
  - Read `emberstextapi$effects` from style
  - Create EffectContext with character data
  - Apply effects in order (modify position, color, alpha)
  - Render glyph with transformed state
  - Handle strikethrough/underline with modified positions

- **BakedGlyphMixin**:
  - Add custom render method `emberstextapi$render()`
  - Accept EffectContext instead of raw coordinates
  - Apply rotation, scaling if effects modified context

- **FontMixin** (Optional):
  - Filter rainbow from border rendering (like TextAnimator)
  - Prevent double-rainbow on text outlines

#### 4. Effect Context Adapter
- **EffectContext**: Shared state object for rendering
  - Replaces TextAnimator's `EffectSettings`
  - Fields: `x, y, r, g, b, a, rot, scale, codepoint, index, shadowOffset`
  - Used by both global system AND ImmersiveMessage renderer

#### 5. Markup Detection & Parsing Pipeline
- **GlobalMarkupProcessor**:
  - Entry point for detecting markup in ANY Component
  - Cache parsed results (keyed by text content hash)
  - Returns `List<Effect>` for given text region
  - Integrates with existing MarkupParser

---

## 5. Implementation Plan

### Phase 1: Infrastructure Setup ✅ COMPLETE
**Goal**: Create mixin configuration and duck interfaces

#### Step 1.1: Create Mixin Configuration File ✅
- **File**: `src/main/resources/emberstextapi.mixins.json`
- **Content**:
  ```json
  {
    "required": true,
    "minVersion": "0.8",
    "package": "net.tysontheember.emberstextapi.mixin",
    "compatibilityLevel": "JAVA_17",
    "refmap": "emberstextapi.refmap.json",
    "mixins": [
      "StyleMixin"
    ],
    "client": [
      "client.BakedGlyphMixin",
      "client.FontMixin",
      "client.LiteralContentsMixin",
      "client.StringRenderOutputMixin",
      "client.TranslatableContentsMixin"
    ],
    "injectors": {
      "defaultRequire": 1
    }
  }
  ```
- **Registry**: Ensure `build.gradle` mixin config is correct (already done)

#### Step 1.2: Create Duck Interface Package ✅
- **Directory**: `src/main/java/net/tysontheember/emberstextapi/mixin/duck/`
- **File**: `ETAStyle.java`
  ```java
  package net.tysontheember.emberstextapi.mixin.duck;
  import com.google.common.collect.ImmutableList;
  import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;

  public interface ETAStyle {
      ImmutableList<Effect> emberstextapi$getEffects();
      void emberstextapi$setEffects(ImmutableList<Effect> effects);
      void emberstextapi$addEffect(Effect effect);
  }
  ```
- **File**: `ETABakedGlyph.java`
  ```java
  package net.tysontheember.emberstextapi.mixin.duck;
  import com.mojang.blaze3d.vertex.VertexConsumer;
  import org.joml.Matrix4f;
  import net.tysontheember.emberstextapi.immersivemessages.effects.EffectContext;

  public interface ETABakedGlyph {
      void emberstextapi$render(
          EffectContext ctx,
          boolean italic,
          float boldOffset,
          Matrix4f pose,
          VertexConsumer buffer,
          int packedLight
      );
  }
  ```

#### Step 1.3: Verify Mixin Dependencies ✅
- **Check**: `build.gradle` already has:
  - `implementation 'org.spongepowered:mixin:0.8.5'`
  - `implementation 'io.github.llamalad7:mixinextras-forge:0.3.5'`
- **JVM Args**: Ensure run configs have mixin debug flags (already configured)

---

### Phase 2: Style Augmentation ✅ COMPLETE
**Goal**: Add Effect storage to Minecraft's Style class

#### Step 2.1: Create StyleMixin ✅
- **File**: `src/main/java/net/tysontheember/emberstextapi/mixin/StyleMixin.java`
- **Target**: `net.minecraft.network.chat.Style`
- **Fields to add**:
  ```java
  @Unique
  private ImmutableList<Effect> emberstextapi$effects = ImmutableList.of();
  ```
- **Methods**:
  - Implement ETAStyle interface (getters/setters)
  - `@ModifyReturnValue` on all `withX()` methods to propagate effects
  - `@ModifyReturnValue` on `applyTo(Style)` to merge effects
  - `@Inject` into `equals()` to compare effects

#### Step 2.2: Style Serialization (Optional for v1)
- **Target**: `Style.Serializer`
- **Inner Mixin**: StyleMixin$SerializerMixin
- **Inject**:
  - `deserialize()`: Read `eta$effects` JSON array, parse effect tags
  - `serialize()`: Write effects as JSON array of strings

---

### Phase 3: Markup Detection in Text Content ✅ COMPLETE
**Goal**: Parse inline markup in Component text and attach effects to Style

#### Step 3.1: Create LiteralContentsMixin ✅
- **File**: `src/main/java/net/tysontheember/emberstextapi/mixin/client/LiteralContentsMixin.java`
- **Target**: `net.minecraft.network.chat.contents.LiteralContents`
- **Injection Point**: `visit(StyledContentConsumer, Style)` at HEAD, cancellable
- **Logic**:
  1. Check if `text` field contains `<` and `>`
  2. If yes, delegate to MarkupParser.parse(text)
  3. For each TextSpan, clone Style and add effects
  4. Strip markup tags from displayed content
  5. Invoke consumer with processed text
  6. Cancel original method
- **Pseudo-code**:
  ```java
  @Shadow @Final private String text;

  @Inject(method = "visit", at = @At("HEAD"), cancellable = true)
  private <T> void emberstextapi$visit(..., CallbackInfoReturnable<Optional<T>> cir) {
      if (!text.contains("<") || !text.contains(">")) return;

      List<TextSpan> spans = MarkupParser.parse(text);
      if (spans.isEmpty()) return;

      // Process each span
      for (TextSpan span : spans) {
          Style spanStyle = StyleUtil.cloneAndAddEffects(style, span.getEffects());
          String content = span.getContent();

          // Emit to consumer character by character
          for (int i = 0; i < content.length(); i++) {
              Optional<T> result = consumer.accept(spanStyle, String.valueOf(content.charAt(i)));
              if (result.isPresent()) {
                  cir.setReturnValue(result);
                  return;
              }
          }
      }

      cir.setReturnValue(Optional.empty());
  }
  ```

#### Step 3.2: Create TranslatableContentsMixin ✅
- **File**: `src/main/java/net/tysontheember/emberstextapi/mixin/client/TranslatableContentsMixin.java`
- **Target**: `net.minecraft.network.chat.contents.TranslatableContents`
- **Same pattern as LiteralContentsMixin**
- **Note**: Handle translation key resolution first, then parse result

#### Step 3.3: Create StyleUtil Helper ✅
- **File**: `src/main/java/net/tysontheember/emberstextapi/mixin/util/StyleUtil.java`
- **Methods**:
  - `Style cloneAndAddEffects(Style original, List<Effect> effects)`:
    - Create copy of style
    - Cast to ETAStyle interface
    - Call `emberstextapi$setEffects(effects)`
    - Return modified style

---

### Phase 4: Core Rendering Integration
**Goal**: Apply effects during Minecraft's character rendering loop

#### Step 4.1: Create EffectContext Class
- **File**: `src/main/java/net/tysontheember/emberstextapi/immersivemessages/effects/EffectContext.java`
- **Purpose**: Shared state object for character rendering (replaces EffectSettings)
- **Fields**:
  ```java
  public float x, y;           // Position
  public float r, g, b, a;     // Color/alpha
  public float rot;            // Rotation (radians)
  public float scale;          // Scale multiplier
  public int codepoint;        // Character code
  public int charIndex;        // Index in string
  public int absoluteIndex;    // Absolute index across all text
  public float shadowOffset;   // Shadow rendering offset
  public boolean isShadow;     // Is this a shadow pass?
  public List<EffectContext> siblings; // For multi-pass effects (neon, shadow)
  ```

#### Step 4.2: Update Effect Interface
- **File**: `src/main/java/net/tysontheember/emberstextapi/immersivemessages/effects/Effect.java`
- **Change**:
  - OLD: `void apply(EffectContext ctx)` (used in overlay renderer)
  - NEW: Keep same signature, but EffectContext now used globally
- **Update all 14 existing Effect implementations** to use new EffectContext fields

#### Step 4.3: Create StringRenderOutputMixin
- **File**: `src/main/java/net/tysontheember/emberstextapi/mixin/client/StringRenderOutputMixin.java`
- **Target**: `net.minecraft.client.gui.Font$StringRenderOutput` (inner class)
- **Priority**: 1200 (run after most mods)
- **Injection**: `accept(int index, Style style, int codepoint)` at HEAD, cancellable
- **Logic**:
  1. Read effects from `ETAStyle` interface
  2. If no effects, return (let vanilla handle it)
  3. Get font, glyph info, baked glyph from FontSet
  4. Extract color from style
  5. Create EffectContext with initial state
  6. Apply each effect in order
  7. Render glyph with modified context
  8. Handle strikethrough/underline
  9. Advance x position
  10. Cancel original method

- **Pseudo-code**:
  ```java
  @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
  private void emberstextapi$accept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
      ETAStyle etaStyle = (ETAStyle) style;
      ImmutableList<Effect> effects = etaStyle.emberstextapi$getEffects();

      if (effects.isEmpty()) return; // Let vanilla handle

      // Get glyph data (shadow fields from @Shadow)
      FontSet fontSet = ((FontAccess) this$0).callGetFontSet(style.getFont());
      GlyphInfo glyphInfo = fontSet.getGlyphInfo(codepoint, filterFishyGlyphs);
      BakedGlyph bakedGlyph = style.isObfuscated() && codepoint != 32
          ? fontSet.getRandomGlyph(glyphInfo)
          : fontSet.getGlyph(codepoint);

      // Extract color
      float r, g, b, a = this.a;
      TextColor textColor = style.getColor();
      if (textColor != null) {
          int k = textColor.getValue();
          r = ((k >> 16) & 0xFF) / 255f * dimFactor;
          g = ((k >> 8) & 0xFF) / 255f * dimFactor;
          b = (k & 0xFF) / 255f * dimFactor;
      } else {
          r = this.r; g = this.g; b = this.b;
      }

      float shadowOffset = dropShadow ? glyphInfo.getShadowOffset() : 0f;

      // Create effect context
      EffectContext ctx = new EffectContext();
      ctx.x = this.x + shadowOffset;
      ctx.y = this.y + shadowOffset;
      ctx.r = r; ctx.g = g; ctx.b = b; ctx.a = a;
      ctx.codepoint = codepoint;
      ctx.charIndex = index;
      ctx.absoluteIndex = index;
      ctx.shadowOffset = shadowOffset;
      ctx.isShadow = dropShadow;
      ctx.siblings = Lists.newArrayList(ctx);

      // Apply effects
      for (Effect effect : effects) {
          int size = ctx.siblings.size();
          for (int i = 0; i < size; i++) {
              effect.apply(ctx.siblings.get(i));
          }
      }

      // Render each sibling
      for (EffectContext sibling : ctx.siblings) {
          emberstextapi$renderChar(sibling, codepoint, style, fontSet, glyphInfo, bakedGlyph);
      }

      // Handle decorations (strikethrough, underline)
      float glyphWidth = glyphInfo.getAdvance(style.isBold());
      if (ctx.a != 0 && style.isStrikethrough()) {
          addEffect(new BakedGlyph.Effect(
              ctx.x - 1f, ctx.y + 4.5f,
              ctx.x + glyphWidth, ctx.y + 3.5f,
              0.01f, ctx.r, ctx.g, ctx.b, ctx.a
          ));
      }
      if (ctx.a != 0 && style.isUnderlined()) {
          addEffect(new BakedGlyph.Effect(
              ctx.x - 1f, ctx.y + 9f,
              ctx.x + glyphWidth, ctx.y + 8f,
              0.01f, ctx.r, ctx.g, ctx.b, ctx.a
          ));
      }

      // Advance
      this.x += glyphWidth;
      cir.setReturnValue(true);
  }

  @Unique
  private void emberstextapi$renderChar(EffectContext ctx, int oCodepoint, Style style,
                                        FontSet fontSet, GlyphInfo glyphInfo, BakedGlyph bakedGlyph) {
      if (ctx.a == 0) return;
      if (ctx.codepoint != oCodepoint) {
          bakedGlyph = fontSet.getGlyph(ctx.codepoint);
          if (bakedGlyph instanceof EmptyGlyph) return;
      }

      VertexConsumer vertexConsumer = bufferSource.getBuffer(bakedGlyph.renderType(mode));
      Matrix4f pose = this.pose;

      // Apply rotation if effect set it
      if (ctx.rot != 0) {
          float glyphWidth = glyphInfo.getAdvance(style.isBold());
          pose = EffectUtil.rotate(pose, ctx, ctx.rot, glyphWidth / 2f, this$0.lineHeight / 2f);
      }

      // Render via custom method
      ETABakedGlyph glyph = (ETABakedGlyph) bakedGlyph;
      glyph.emberstextapi$render(ctx, style.isItalic(), 0f, pose, vertexConsumer, packedLightCoords);
      if (style.isBold()) {
          glyph.emberstextapi$render(ctx, style.isItalic(), glyphInfo.getBoldOffset(), pose, vertexConsumer, packedLightCoords);
      }
  }
  ```

#### Step 4.4: Create BakedGlyphMixin
- **File**: `src/main/java/net/tysontheember/emberstextapi/mixin/client/BakedGlyphMixin.java`
- **Target**: `net.minecraft.client.gui.font.glyphs.BakedGlyph`
- **Add Method**:
  ```java
  @Override
  public void emberstextapi$render(EffectContext ctx, boolean italic, float boldOffset,
                                   Matrix4f pose, VertexConsumer buffer, int light) {
      // Shadow original render() method
      float minX = ctx.x + (italic ? this.left - boldOffset : this.left);
      float maxX = ctx.x + (italic ? this.right - boldOffset : this.right);
      // ... rest of vanilla render logic, but use ctx.x, ctx.y, ctx.r, ctx.g, ctx.b, ctx.a
  }
  ```

#### Step 4.5: Create EffectUtil Helper
- **File**: `src/main/java/net/tysontheember/emberstextapi/util/EffectUtil.java`
- **Methods**:
  - `Matrix4f rotate(Matrix4f pose, EffectContext ctx, float rad, float oX, float oY)`:
    - Copy pose matrix
    - Translate to origin, rotate, translate back
    - Update ctx.x and ctx.y to 0 (rotation absorbed into matrix)
    - Return modified pose

---

### Phase 5: Testing & Validation
**Goal**: Verify global effects work in all contexts

#### Step 5.1: Create Test Command
- **File**: Already exists at `MessageCommands.java`
- **Add method**: `testGlobalEffectsSubcommand()` (ALREADY DONE at line 541-624)
- **Tests**:
  - Rainbow effect in chat (`/emberstextapi testglobal`)
  - Wave effect in chat
  - Nested effects (rainbow + wave)
  - Effects with vanilla formatting (bold, underline)
  - Long text with effects

#### Step 5.2: Manual Testing Scenarios
- [ ] **Chat**: Send message with `<rainbow>text</rainbow>`
- [ ] **System message**: Use `player.sendSystemMessage(Component.literal("<wave>text</wave>"))`
- [ ] **Command feedback**: Use effect markup in command error messages
- [ ] **Tooltip**: Create item with lore containing `<bounce>text</bounce>`
- [ ] **FTB Quest**: Add quest with reward text containing effects
- [ ] **Sign**: Place sign with `<shake>text</shake>` (may require entity mixin)
- [ ] **Boss bar**: Create boss bar with styled text
- [ ] **Title/subtitle**: Use `/title @a title {"text":"<pulse>HELLO</pulse>"}`

#### Step 5.3: Performance Testing
- **Scenario 1**: Chat spam with 100 messages/sec containing effects
  - Monitor FPS drop
  - Check render thread CPU usage
  - Target: <5% FPS drop

- **Scenario 2**: Open GUI with 500+ text elements
  - Measure render time
  - Check for frame stutters

- **Optimization 1**: Cache parsed effects by text content hash
  - Implement `MarkupCache` with 256-entry LRU cache
  - Key: `text.hashCode() ^ style.hashCode()`

- **Optimization 2**: Skip effect processing if no markup detected
  - Quick scan for `<` and `>` characters
  - If absent, skip all markup parsing

#### Step 5.4: Compatibility Testing
- [ ] **Shader mods**: Test with Iris/Optifine
- [ ] **Font mods**: Test with Caxton (already a dependency)
- [ ] **Chat mods**: Test with BetterChat, ChatHeads
- [ ] **GUI mods**: Test with JEI, REI tooltips

---

### Phase 6: Integration with ImmersiveMessage
**Goal**: Make ImmersiveMessage use global rendering pipeline

#### Step 6.1: Add Conversion Method
- **File**: `src/main/java/net/tysontheember/emberstextapi/immersivemessages/api/ImmersiveMessage.java`
- **Method**: `Component toComponent()`
  - Convert TextSpan list to MutableComponent tree
  - Attach effects to Style objects using ETAStyle interface
  - Return root component

#### Step 6.2: Update ImmersiveMessageRenderer
- **File**: Likely in `ClientMessageManager.java` or `ClientEvents.java`
- **Change**: Use `message.toComponent()` and vanilla `Font.drawInBatch8xOutline()` instead of custom rendering
- **Benefit**: ImmersiveMessages now rendered with same pipeline as global effects

#### Step 6.3: Deprecation Notice
- **Document**: Old custom renderer is deprecated
- **Migration path**: Users should rely on global rendering
- **Keep**: Background, anchor, positioning logic (not part of global system)

---

## 6. Files to Create/Modify

### New Files to Create

```
src/main/java/net/tysontheember/emberstextapi/mixin/
├── duck/
│   ├── ETAStyle.java                      # Interface for Style augmentation
│   └── ETABakedGlyph.java                 # Interface for glyph rendering
├── util/
│   ├── StyleUtil.java                     # Style cloning and effect merging
│   └── EffectUtil.java                    # Rotation, transformation utilities
├── StyleMixin.java                        # Augment Style with effects
└── client/
    ├── BakedGlyphMixin.java               # Custom glyph rendering
    ├── FontMixin.java                     # Filter rainbow in borders (optional)
    ├── LiteralContentsMixin.java          # Parse markup in literal text
    ├── StringRenderOutputMixin.java       # Core effect application
    └── TranslatableContentsMixin.java     # Parse markup in translations

src/main/resources/
└── emberstextapi.mixins.json              # Mixin configuration
```

### Files to Modify

```
src/main/java/net/tysontheember/emberstextapi/immersivemessages/
├── effects/
│   ├── EffectContext.java                 # Unify with global rendering context
│   ├── Effect.java                        # Update docs to mention global usage
│   └── [All 14 effect classes]            # Ensure work with unified EffectContext
├── api/
│   ├── ImmersiveMessage.java              # Add toComponent() method
│   └── TextSpan.java                      # Add getEffects() public accessor (already exists)
└── client/
    └── ClientMessageManager.java          # (Optional) Use global renderer

src/main/java/net/tysontheember/emberstextapi/
└── EmbersTextAPI.java                     # Update init logging

build.gradle                                # Verify mixin configs (already correct)
```

---

## 7. Mixin Injection Points (Detailed Specifications)

### 1. StyleMixin
**Target**: `net.minecraft.network.chat.Style`
**Type**: Class augmentation (add fields/methods)

**Fields to Add**:
```java
@Unique
private ImmutableList<Effect> emberstextapi$effects = ImmutableList.of();
```

**Methods to Inject**:
- `@ModifyReturnValue` on ALL style modification methods:
  - `withColor(TextColor)`, `withBold(Boolean)`, `withItalic(Boolean)`, etc.
  - `applyTo(Style)` (merge effects from both styles)
  - Target: `@At("RETURN")`
  - Logic: Propagate `emberstextapi$effects` to returned style

- `@Inject` into `equals(Object)`:
  - Target: `@At("HEAD")`, cancellable
  - Logic: Compare effect lists, return false if different

**Obfuscation**: Use Parchment mappings (already configured)

---

### 2. LiteralContentsMixin
**Target**: `net.minecraft.network.chat.contents.LiteralContents`
**Method**: `visit(FormattedText.StyledContentConsumer<T>, Style)`

**Injection**:
```java
@Inject(
    method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
    at = @At("HEAD"),
    cancellable = true
)
```

**Logic**:
1. Check if `this.text` contains `<` and `>`
2. If no: return (let vanilla handle)
3. If yes: Parse markup via `MarkupParser.parse(text)`
4. For each TextSpan, clone style and attach effects
5. Iterate characters and invoke consumer
6. Cancel original method

**Data to Capture**:
- Shadow `@Final String text` field
- Method parameters: `consumer`, `style`

**Thread Safety**: Render thread only (client mixin)

---

### 3. StringRenderOutputMixin
**Target**: `net.minecraft.client.gui.Font$StringRenderOutput`
**Method**: `accept(int, Style, int)` (boolean return)

**Injection**:
```java
@Inject(
    method = "accept(ILnet/minecraft/network/chat/Style;I)Z",
    at = @At("HEAD"),
    cancellable = true,
    priority = 1200
)
```

**Fields to Shadow**:
```java
@Shadow @Final private Matrix4f pose;
@Shadow @Final MultiBufferSource bufferSource;
@Shadow float x;
@Shadow float y;
@Shadow @Final(remap = false) Font this$0; // Inner class reference
@Shadow @Final private boolean dropShadow;
@Shadow @Final private float dimFactor;
@Shadow @Final private float a;
@Shadow @Final private float r, g, b;
@Shadow @Final private Font.DisplayMode mode;
@Shadow @Final private int packedLightCoords;
```

**Logic**:
1. Cast style to ETAStyle, read effects
2. If effects empty, return (vanilla handles)
3. Get FontSet, GlyphInfo, BakedGlyph
4. Extract color from style
5. Build EffectContext
6. Apply each effect
7. Render glyph via custom method
8. Handle decorations
9. Cancel original

**Alternative Injection Points** (if accept() fails):
- `@At(value = "INVOKE", target = "BakedGlyph.render(...)")` - redirect rendering
- Trade-off: More fragile (vanilla refactors break it)

**Recommended**: HEAD injection with full cancellation (cleaner, more maintainable)

---

### 4. BakedGlyphMixin
**Target**: `net.minecraft.client.gui.font.glyphs.BakedGlyph`
**Type**: Method addition (duck interface implementation)

**New Method**:
```java
@Override
public void emberstextapi$render(
    EffectContext ctx,
    boolean italic,
    float boldOffset,
    Matrix4f pose,
    VertexConsumer buffer,
    int light
) {
    // Re-implement vanilla render() logic using ctx fields
}
```

**No Injection Required**: Pure method addition via interface

**Implementation**: Copy vanilla `render()` method body, replace parameters with `ctx.x`, `ctx.r`, etc.

---

## 8. Risk Assessment & Mitigation

### Risk 1: Mixin Conflicts with Other Mods
**Likelihood**: Medium
**Impact**: High (breaks text rendering for all mods)

**Mitigation**:
- Use high priority (1200) to run after most mods
- Check for `ETAStyle.emberstextapi$getEffects()` before assuming list is empty
- Add config option to disable global rendering (fallback to ImmersiveMessage-only mode)

**Detection**:
- Log warning if another mod also mixins into StringRenderOutput
- Test with popular mods (JEI, FTB, Quark, Mekanism)

---

### Risk 2: Performance Impact on Vanilla Text
**Likelihood**: Medium
**Impact**: Medium (FPS drop in chat-heavy scenarios)

**Mitigation**:
- **Early exit**: If no `<` or `>` in text, skip all parsing (99% of text)
- **Cache parsed effects**: Use `ConcurrentHashMap<Integer, List<Effect>>` with text hash as key
- **Limit effect application**: Cap at 5 effects per character
- **Profile rendering**: Add debug flag to log effect application time

**Testing**:
- Spam 1000 messages in chat
- Monitor with Spark profiler
- Target: <1ms per message

---

### Risk 3: Breaking ImmersiveMessage Compatibility
**Likelihood**: Low
**Impact**: High (breaks existing modpacks)

**Mitigation**:
- Keep ImmersiveMessage API unchanged
- Add deprecation notices, not removals
- Provide migration guide in docs
- Test all 33 existing test cases in MessageCommands

---

### Risk 4: Markup Injection Attacks
**Likelihood**: Low
**Impact**: Medium (malicious players crash clients with complex markup)

**Mitigation**:
- Limit nesting depth (max 8 levels)
- Cap effect count per text (max 16 effects)
- Sanitize user input in chat
- Add server-side validation for commands

---

### Risk 5: Unicode/RTL Text Handling
**Likelihood**: Medium
**Impact**: Low (effects misalign on non-Latin text)

**Mitigation**:
- Test with Arabic, Hebrew, Chinese, Japanese text
- Defer to vanilla text direction logic
- Apply effects after BiDi processing

---

## 9. Testing Strategy

### Unit Tests (Optional, for critical logic)
- **MarkupParser**: Test nested tag parsing
- **StyleUtil**: Test effect merging
- **EffectContext**: Test state mutations

### Integration Tests

#### Test 1: Chat Message with Rainbow
```java
player.sendSystemMessage(Component.literal("<rainbow>Hello World</rainbow>"));
```
**Expected**: Each character cycles through rainbow colors

#### Test 2: Nested Effects
```java
Component.literal("<rainbow><wave>Nested</wave></rainbow>")
```
**Expected**: Rainbow colors + wave motion

#### Test 3: Partial Markup
```java
Component.literal("Normal <shake>shaking</shake> normal")
```
**Expected**: Only "shaking" shakes, rest is static

#### Test 4: Invalid Markup
```java
Component.literal("<unknown>text</unknown>")
```
**Expected**: Renders as plain text (tags ignored), no crash

#### Test 5: Tooltip Effect
```java
ItemStack stack = new ItemStack(Items.DIAMOND);
stack.setHoverName(Component.literal("<pulse>Legendary Diamond</pulse>"));
```
**Expected**: Tooltip title pulses

#### Test 6: FTB Quest Reward
- Create quest with reward text: `<bounce>You won!</bounce>`
- **Expected**: Text bounces in quest GUI

---

## 10. Configuration Options (Future Enhancement)

### Client Config (`emberstextapi-client.toml`)
```toml
[global_effects]
  # Enable global text effects system
  enabled = true

  # Maximum effects per character (performance limit)
  max_effects_per_char = 5

  # Enable effect caching (improves performance)
  cache_enabled = true
  cache_size = 256

  # Animation speed multiplier (1.0 = normal, 0.5 = slow, 2.0 = fast)
  animation_speed = 1.0

  # Disable effects in specific contexts
  disable_in_chat = false
  disable_in_tooltips = false
  disable_in_guis = false
```

---

## 11. Documentation TODO

- [ ] Update README.md with global effects usage
- [ ] Create MIGRATION.md for ImmersiveMessage → Global transition
- [ ] Document all effect tags in EFFECTS.md
- [ ] Add examples for modpack developers
- [ ] Create video tutorial for markup syntax

---

## 12. Phase Summary & Milestones

| Phase | Milestone | Estimated Time | Validation | Status |
|-------|-----------|----------------|------------|--------|
| 1. Infrastructure | Mixin configs + duck interfaces | 2 hours | Mod loads without errors | ✅ COMPLETE |
| 2. Style Augmentation | StyleMixin working | 3 hours | Effects stored in Style objects | ✅ COMPLETE |
| 3. Markup Detection | Component mixins parse tags | 4 hours | Chat messages detect `<rainbow>` | ✅ COMPLETE |
| 4. Core Rendering | StringRenderOutput applies effects | 6 hours | Rainbow renders globally | Pending |
| 5. Testing | All scenarios pass | 4 hours | No crashes, FPS stable | Pending |
| 6. Integration | ImmersiveMessage uses global | 2 hours | Backward compatibility verified | Pending |
| **TOTAL** | **Full Implementation** | **21 hours** | **Production ready** | **Phase 3/6** |

---

## 13. Success Criteria

### Functional Requirements
- ✅ Text with `<rainbow>` markup renders with rainbow colors in chat
- ✅ Text with nested `<wave><shake>` renders with both effects
- ✅ Vanilla text without markup renders normally (no performance hit)
- ✅ Tooltips, GUIs, titles support effect markup
- ✅ Existing ImmersiveMessage commands still work
- ✅ FTB Quests text can use effects

### Performance Requirements
- ✅ <5% FPS drop with 100 concurrent chat messages
- ✅ <1ms per text render for markup parsing
- ✅ No stuttering on GUI open with 500+ text elements

### Compatibility Requirements
- ✅ Works with Caxton font renderer
- ✅ No conflicts with JEI tooltips
- ✅ Compatible with Iris shaders
- ✅ Existing modpacks don't break

---

## 14. Next Steps

**Immediate Actions** (for code-writing agent):
1. Create `emberstextapi.mixins.json` configuration file
2. Create duck interface package and files
3. Implement StyleMixin with effect storage
4. Test: Verify effects attach to Style objects

**Post-Implementation**:
1. Write comprehensive EFFECTS.md documentation
2. Create example commands for modpack developers
3. Submit CurseForge/Modrinth update with "Global Effects" feature
4. Create demo video showing effects in various contexts

---

## 15. Notes & Assumptions

- **Assumed**: Existing MarkupParser handles all tag types correctly
- **Assumed**: EffectRegistry.parseTag() returns valid Effect instances
- **Assumed**: All 14 existing effects are stateless (no shared state between chars)
- **Known Issue**: Typewriter effect needs special handling (tracks animation state per text instance)
- **Decision**: Keep ImmersiveMessage as high-level API for modpack developers
- **Decision**: Global system is opt-in via markup (doesn't affect unmarked text)

---

## 16. References

- TextAnimator source: `/home/ember/IdeaProjects/EmbersTextAPI/temp_reference/TextAnimator/`
- Mixin documentation: https://github.com/SpongePowered/Mixin/wiki
- Forge mixin guide: https://docs.minecraftforge.net/en/1.20.x/advanced/mixins/
- Parchment mappings: https://parchmentmc.org/docs/getting-started

---

**END OF PLAN**

This plan is ready for execution by the `minecraft-mod-dev` agent. Each step contains sufficient detail to implement without architectural decisions. The plan prioritizes maintainability, compatibility, and performance while achieving the goal of global text styling.
