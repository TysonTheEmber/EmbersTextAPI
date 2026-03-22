package net.tysontheember.emberstextapi.immersivemessages.api;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import net.tysontheember.emberstextapi.client.TextLayoutCache;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateAnimator;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfKey;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterAnimator;
import net.tysontheember.emberstextapi.immersivemessages.effects.color.FadeCalculator;
import net.tysontheember.emberstextapi.immersivemessages.effects.rendering.BackgroundRenderer;
import net.tysontheember.emberstextapi.immersivemessages.effects.util.ColorUtil;
import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import net.tysontheember.emberstextapi.immersivemessages.util.RenderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * More feature rich port of the original Immersive Messages class.
 * It supports anchor/align positioning, backgrounds, typewriter
 * animations and progressive de-obfuscation. Audio cues have been
 * intentionally omitted so the API focuses purely on text rendering.
 *
 * Version 2.0.0: Added span-based text rendering with markup parser support.
 */
public class ImmersiveMessage {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Component text;
    private float duration;
    private float age;
    private float previousAge;
    /** Number of ticks spent fading in before reaching full opacity. */
    private int fadeInTicks = 0;
    /** Number of ticks spent fading out after the visible duration completes. */
    private int fadeOutTicks = 0;
    private float xOffset;
    private float yOffset = 55f;
    private boolean shadow = true;
    private TextAnchor anchor = TextAnchor.TOP_CENTER;
    private TextAlign align = TextAlign.CENTER;
    private float textScale = 1f;
    private boolean background = false;
    private ImmersiveColor backgroundColor = new ImmersiveColor(0xAA000000);
    private ImmersiveColor borderStart = new ImmersiveColor(0xAAFFFFFF);
    private ImmersiveColor borderEnd = new ImmersiveColor(0xAA000000);
    private boolean useTextureBackground = false;
    private ResourceLocation backgroundTexture;
    private int textureU = 0;
    private int textureV = 0;
    private int textureWidth = 256;
    private int textureHeight = 256;
    private int textureAtlasWidth = 256;
    private int textureAtlasHeight = 256;
    private float texturePaddingX = 0f;
    private float texturePaddingY = 0f;
    private float textureScaleX = 1f;
    private float textureScaleY = 1f;
    private float textureOverrideWidth = -1f;
    private float textureOverrideHeight = -1f;
    private TextureSizingMode textureSizingMode = TextureSizingMode.STRETCH;

    // Background gradient (multi-stop ARGB ImmersiveColor)
    private ImmersiveColor[] backgroundGradientStops;

    // Typewriter
    private boolean typewriter = false;
    private float typewriterSpeed = 0.5f; // characters per tick
    private boolean typewriterCenter = false;
    private int typewriterIndex = 0;

    // Current rendered text (may be typewritten or obfuscated)
    private MutableComponent current = Component.literal("");

    // Obfuscation
    private ObfuscateMode obfuscateMode = ObfuscateMode.NONE;
    private float obfuscateSpeed = 0.00005f; // characters per tick
    private float obfuscateProgress = 0f;

    // Per-character reveal tracking (mask-based, not §k injection)
    private String baseText;
    private boolean[] revealMask;          // true = revealed (de-obfuscated)
    private List<Integer> revealOrder;     // order to flip bits in revealMask
    private int revealIndex = 0;

    // Layout
    private int wrapMaxWidth = -1;
    private float delay = 0f;


    // NEW: Span-based rendering (v2.0.0)
    private List<TextSpan> spans;
    private boolean spanMode = false;
    private int[] spanTypewriterIndices; // Per-span typewriter progress

    // Original markup source string for NBT round-trip (v2.0.0)
    // When present, the client will use this to re-parse markup effects via LiteralContentsMixin
    private String markupSource;
    private Component markupComponent; // Cached Component.literal(markupSource) for layout cache hits

    // NEW: Visual effects system (v2.0.0)
    private List<Effect> globalEffects;
    private List<EffectSegment> spanEffectSegments = Collections.emptyList();

    // Unique context ID for this message (prevents tooltip hovers from resetting chat typewriter)
    private final String messageContextId;

    private OnRenderMessage onRender;
    private final Random random = new Random();

    public ImmersiveMessage(Component text, float duration) {
        this.text = text;
        this.duration = duration;
        // Create a unique context ID for this message instance
        // Using creation timestamp + object hash ensures uniqueness
        this.messageContextId = "message:" + System.currentTimeMillis() + ":" + System.identityHashCode(this);

        // Mark this message context as started NOW (when message is created)
        // This ensures typewriter effects start immediately when sent, not when first rendered
        // And prevents tooltip hovers from resetting the animation
        net.tysontheember.emberstextapi.client.ViewStateTracker.markViewStarted(this.messageContextId);

        // Initialize age to ensure proper fade-in from start
        this.age = 0f;
        this.previousAge = 0f;
    }

    // NEW: Span-based constructor
    public ImmersiveMessage(List<TextSpan> spans, float duration) {
        this.spans = new ArrayList<>(spans);
        this.text = Component.literal(MarkupParser.toPlainText(spans)); // For compatibility
        this.duration = duration;
        this.spanMode = true;
        this.spanTypewriterIndices = new int[spans.size()];

        // Create unique context ID and mark as started (same as other constructor)
        this.messageContextId = "message:" + System.currentTimeMillis() + ":" + System.identityHashCode(this);
        net.tysontheember.emberstextapi.client.ViewStateTracker.markViewStarted(this.messageContextId);

        // Initialize age to ensure proper fade-in from start
        this.age = 0f;
        this.previousAge = 0f;

        // Check if any spans have typewriter effects and enable global typewriter if so
        if (spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null)) {
            this.typewriter = true;
        }

        buildEffectSegments();

        // Extract global message attributes from any span that has them (typically the first one)
        for (TextSpan span : spans) {
            if (span.hasGlobalAttributes()) {
                if (span.getGlobalBackground() != null) {
                    this.background = span.getGlobalBackground();
                }
                if (span.getGlobalBackgroundColor() != null) {
                    this.backgroundColor = span.getGlobalBackgroundColor();
                }
                if (span.getGlobalBackgroundGradient() != null) {
                    this.backgroundGradientStops = span.getGlobalBackgroundGradient();
                }
                if (span.getGlobalBorderStart() != null) {
                    this.borderStart = span.getGlobalBorderStart();
                }
                if (span.getGlobalBorderEnd() != null) {
                    this.borderEnd = span.getGlobalBorderEnd();
                }
                if (span.getGlobalXOffset() != null) {
                    this.xOffset = span.getGlobalXOffset();
                }
                if (span.getGlobalYOffset() != null) {
                    this.yOffset = span.getGlobalYOffset();
                }
                if (span.getGlobalAnchor() != null) {
                    this.anchor = span.getGlobalAnchor();
                }
                if (span.getGlobalAlign() != null) {
                    this.align = span.getGlobalAlign();
                }
                if (span.getGlobalScale() != null) {
                    this.textScale = span.getGlobalScale();
                }
                if (span.getGlobalShadow() != null) {
                    this.shadow = span.getGlobalShadow();
                }
                if (span.getGlobalFadeInTicks() != null) {
                    this.fadeInTicks = span.getGlobalFadeInTicks();
                }
                if (span.getGlobalFadeOutTicks() != null) {
                    this.fadeOutTicks = span.getGlobalFadeOutTicks();
                }
                if (span.getGlobalTypewriterSpeed() != null) {
                    this.typewriter = true;
                    this.typewriterSpeed = span.getGlobalTypewriterSpeed();
                    this.typewriterCenter = span.getGlobalTypewriterCenter() != null ? span.getGlobalTypewriterCenter() : false;
                }
                break; // Use first span with global attributes
            }
        }
    }

    /** Builder entry point. */
    public static ImmersiveMessage builder(float duration, String text) {
        return new ImmersiveMessage(Component.literal(text), duration);
    }

    /** NEW: Create from markup text with span-based rendering. */
    public static ImmersiveMessage fromMarkup(float duration, String markup) {
        List<TextSpan> parsed = MarkupParser.parse(markup);
        ImmersiveMessage msg = new ImmersiveMessage(parsed, duration);
        msg.markupSource = markup;
        msg.markupComponent = Component.literal(markup);
        return msg;
    }

    /** NEW: Create from TextSpan list. */
    public static ImmersiveMessage fromSpans(float duration, List<TextSpan> spans) {
        return new ImmersiveMessage(spans, duration);
    }

    // ----- Builder style setters -----
    public ImmersiveMessage shadow(boolean shadow) { this.shadow = shadow; return this; }
    public ImmersiveMessage anchor(TextAnchor anchor) { this.anchor = anchor; return this; }
    public ImmersiveMessage align(TextAlign align) { this.align = align; return this; }
    public ImmersiveMessage offset(float x, float y) { this.xOffset = x; this.yOffset = y; return this; }

    /**
     * Sets the number of ticks to fade the message in before it reaches full opacity.
     *
     * @param ticks fade-in length in ticks, must be non-negative.
     * @return this builder instance for chaining.
     * @throws IllegalArgumentException if {@code ticks} is negative.
     */
    public ImmersiveMessage fadeInTicks(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("fadeInTicks must be non-negative");
        }
        this.fadeInTicks = ticks;
        return this;
    }

    /**
     * Sets the number of ticks to fade the message out after the visible duration completes.
     *
     * @param ticks fade-out length in ticks, must be non-negative.
     * @return this builder instance for chaining.
     * @throws IllegalArgumentException if {@code ticks} is negative.
     */
    public ImmersiveMessage fadeOutTicks(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("fadeOutTicks must be non-negative");
        }
        this.fadeOutTicks = ticks;
        return this;
    }

    public ImmersiveMessage color(ChatFormatting vanilla) {
        if (text instanceof MutableComponent mutable && vanilla != null) {
            mutable.withStyle(s -> s.withColor(vanilla));
        }
        return this;
    }
    public ImmersiveMessage color(String value) {
        if (value == null) return this;
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) {
            return color(fmt);
        }
        TextColor parsed = TextColor.parseColor(value);
        if (parsed != null && text instanceof MutableComponent mutable) {
            mutable.withStyle(s -> s.withColor(parsed));
        }
        return this;
    }
    public ImmersiveMessage color(int rgb) {
        if (text instanceof MutableComponent mutable) {
            mutable.withStyle(s -> s.withColor(rgb));
        }
        return this;
    }

    // ----- Background color/border customization -----
    /**
     * Parse a color string to ImmersiveColor.
     * Delegates to centralized {@link ColorParser} utility.
     *
     * @param value Color string to parse
     * @return ImmersiveColor or null if parsing fails
     */
    private ImmersiveColor parseColour(String value) {
        return ColorParser.parseImmersiveColor(value);
    }

    public ImmersiveMessage bgAlpha(float alpha) {
        int a = Mth.clamp((int) (alpha * 255f), 0, 255);
        this.backgroundColor = new ImmersiveColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), a);
        this.background = true;
        return this;
    }
    public ImmersiveMessage background(boolean enabled) {
        this.background = enabled;
        if (!enabled) {
            this.useTextureBackground = false;
            this.backgroundTexture = null;
        }
        return this;
    }
    public ImmersiveMessage bgColor(ChatFormatting vanilla) {
        if (vanilla != null && vanilla.getColor() != null) {
            this.backgroundColor = new ImmersiveColor(0xFF000000 | vanilla.getColor());
            this.background = true;
        }
        return this;
    }
    public ImmersiveMessage bgColor(String value) {
        ImmersiveColor parsed = parseColour(value);
        if (parsed != null) {
            this.backgroundColor = parsed;
            this.background = true;
        }
        return this;
    }
    public ImmersiveMessage bgColor(int argb) { this.backgroundColor = new ImmersiveColor(argb); this.background = true; return this; }
    public ImmersiveMessage borderGradient(ImmersiveColor start, ImmersiveColor end) { this.borderStart = start; this.borderEnd = end; this.background = true; return this; }
    public ImmersiveMessage borderGradient(ChatFormatting start, ChatFormatting end) {
        if (start != null && end != null && start.getColor() != null && end.getColor() != null) {
            return borderGradient(new ImmersiveColor(0xFF000000 | start.getColor()), new ImmersiveColor(0xFF000000 | end.getColor()));
        }
        return this;
    }
    public ImmersiveMessage borderGradient(String start, String end) {
        ImmersiveColor s = parseColour(start);
        ImmersiveColor e = parseColour(end);
        if (s != null && e != null) {
            return borderGradient(s, e);
        }
        return this;
    }

    public ImmersiveMessage backgroundColors(ImmersiveColor bg, ImmersiveColor borderStart, ImmersiveColor borderEnd) {
        if (bg != null) {
            this.backgroundColor = bg;
        }
        if (borderStart != null) {
            this.borderStart = borderStart;
        }
        if (borderEnd != null) {
            this.borderEnd = borderEnd;
        }
        if (bg != null || borderStart != null || borderEnd != null) {
            this.background = true;
        }
        return this;
    }

    public ImmersiveMessage textureBackground(ResourceLocation texture) {
        return textureBackground(texture, 0, 0, 256, 256, 256, 256);
    }

    public ImmersiveMessage textureBackground(ResourceLocation texture, int width, int height) {
        return textureBackground(texture, 0, 0, width, height, width, height);
    }

    public ImmersiveMessage textureBackground(ResourceLocation texture, int u, int v, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight) {
        if (texture != null) {
            this.backgroundTexture = texture;
            this.textureU = u;
            this.textureV = v;
            this.textureWidth = Math.max(1, regionWidth);
            this.textureHeight = Math.max(1, regionHeight);
            this.textureAtlasWidth = Math.max(1, atlasWidth);
            this.textureAtlasHeight = Math.max(1, atlasHeight);
            this.useTextureBackground = true;
            this.background = true;
        } else {
            this.backgroundTexture = null;
            this.useTextureBackground = false;
        }
        return this;
    }

    public ImmersiveMessage textureBackgroundScale(float scale) {
        return textureBackgroundScale(scale, scale);
    }

    public ImmersiveMessage textureBackgroundScale(float scaleX, float scaleY) {
        this.textureScaleX = Float.isFinite(scaleX) ? Math.max(0f, scaleX) : this.textureScaleX;
        this.textureScaleY = Float.isFinite(scaleY) ? Math.max(0f, scaleY) : this.textureScaleY;
        return this;
    }

    public ImmersiveMessage textureBackgroundPadding(float padding) {
        return textureBackgroundPadding(padding, padding);
    }

    public ImmersiveMessage textureBackgroundPadding(float paddingX, float paddingY) {
        if (Float.isFinite(paddingX)) {
            this.texturePaddingX = Math.max(0f, paddingX);
        }
        if (Float.isFinite(paddingY)) {
            this.texturePaddingY = Math.max(0f, paddingY);
        }
        return this;
    }

    public ImmersiveMessage textureBackgroundSize(float width, float height) {
        textureBackgroundWidth(width);
        textureBackgroundHeight(height);
        return this;
    }

    public ImmersiveMessage textureBackgroundWidth(float width) {
        if (Float.isFinite(width) && width > 0f) {
            this.textureOverrideWidth = width;
        } else {
            this.textureOverrideWidth = -1f;
        }
        return this;
    }

    public ImmersiveMessage textureBackgroundHeight(float height) {
        if (Float.isFinite(height) && height > 0f) {
            this.textureOverrideHeight = height;
        } else {
            this.textureOverrideHeight = -1f;
        }
        return this;
    }

    public ImmersiveMessage textureBackgroundMode(TextureSizingMode mode) {
        if (mode != null) {
            this.textureSizingMode = mode;
        }
        return this;
    }

    // ----- Background gradient convenience overloads (ARGB) -----
    public ImmersiveMessage backgroundGradient(int startArgb, int endArgb) {
        return backgroundGradient(new ImmersiveColor(startArgb), new ImmersiveColor(endArgb));
    }
    public ImmersiveMessage backgroundGradient(String start, String end) {
        ImmersiveColor s = parseImmersiveColor(start);
        ImmersiveColor e = parseImmersiveColor(end);
        if (s != null && e != null) return backgroundGradient(s, e);
        return this;
    }
    public ImmersiveMessage backgroundGradient(int... argbs) {
        ImmersiveColor[] arr = new ImmersiveColor[argbs.length];
        for (int i = 0; i < argbs.length; i++) arr[i] = new ImmersiveColor(argbs[i]);
        return backgroundGradient(arr);
    }
    public ImmersiveMessage backgroundGradient(String... values) {
        ImmersiveColor[] arr = new ImmersiveColor[values.length];
        for (int i = 0; i < values.length; i++) arr[i] = parseImmersiveColor(values[i]);
        return backgroundGradient(arr);
    }
    public ImmersiveMessage backgroundGradient(ImmersiveColor... colors) {
        if (colors == null || colors.length < 2) return this;
        this.backgroundGradientStops = colors;
        this.background = true;
        return this;
    }
    public ImmersiveMessage backgroundGradient(List<ImmersiveColor> colors) {
        return backgroundGradient(colors.toArray(new ImmersiveColor[0]));
    }

    public ImmersiveMessage scale(float size) {
        this.textScale = size;
        return this;
    }

    public ImmersiveMessage typewriter(float speed) {
        return typewriter(speed, false);
    }

    public ImmersiveMessage typewriter(float speed, boolean center) {
        this.typewriter = true;
        this.typewriterSpeed = speed;
        this.typewriterCenter = center;
        this.typewriterIndex = 0;
        current = Component.literal("").withStyle(text.getStyle());
        return this;
    }

    public ImmersiveMessage wrap(int maxWidth) {
        this.wrapMaxWidth = maxWidth;
        return this;
    }

    public ImmersiveMessage obfuscate(ObfuscateMode mode, float speed) {
        this.obfuscateMode = mode != null ? mode : ObfuscateMode.NONE;
        this.obfuscateSpeed = speed;
        if (this.obfuscateMode != ObfuscateMode.NONE) {
            initObfuscation();
        } else {
            this.baseText = null;
            this.revealMask = null;
            this.revealOrder = null;
            this.revealIndex = 0;
            current = Component.literal(text.getString()).withStyle(text.getStyle());
        }
        return this;
    }

    // NEW: Visual effects system (v2.0.0)
    /**
     * Add a global visual effect to this message.
     * Effects are applied to all characters in the message.
     *
     * @param effect The effect to add
     * @return This message for chaining
     */
    public ImmersiveMessage addEffect(Effect effect) {
        if (this.globalEffects == null) {
            this.globalEffects = new ArrayList<>();
        }
        this.globalEffects.add(effect);
        return this;
    }

    /**
     * Add a global effect by parsing a tag content string.
     * Example: effect("rainbow f=2.0 w=0.5")
     *
     * @param tagContent Effect name and parameters
     * @return This message for chaining
     */
    public ImmersiveMessage effect(String tagContent) {
        if (tagContent != null && !tagContent.isEmpty()) {
            try {
                Effect effect = EffectRegistry.parseTag(tagContent);
                addEffect(effect);
            } catch (IllegalArgumentException e) {
                // Silently ignore unknown effects
            }
        }
        return this;
    }

    /**
     * Clear all global effects.
     *
     * @return This message for chaining
     */
    public ImmersiveMessage clearEffects() {
        if (this.globalEffects != null) {
            this.globalEffects.clear();
        }
        return this;
    }

    /**
     * Get the list of global effects (may be null).
     *
     * @return List of effects or null
     */
    public List<Effect> getGlobalEffects() {
        return globalEffects;
    }

    private static ImmersiveColor parseImmersiveColor(String value) {
        if (value == null) return null;
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) {
            TextColor c = TextColor.fromLegacyFormat(fmt);
            return new ImmersiveColor(0xFF000000 | c.getValue());
        }
        TextColor c = TextColor.parseColor(value);
        return c != null ? new ImmersiveColor(0xFF000000 | c.getValue()) : null;
    }

    private void initObfuscation() {
        baseText = text.getString();
        revealMask = new boolean[baseText.length()];
        revealOrder = ObfuscateAnimator.createRevealOrder(obfuscateMode, baseText.length(), random);
        revealIndex = 0;
        obfuscateProgress = 0f;
        rebuildObfuscation();
    }

    private void rebuildObfuscation() {
        MutableComponent result = Component.literal("");
        int limit = typewriter ? typewriterIndex : (baseText == null ? 0 : baseText.length());
        for (int i = 0; i < limit; i++) {
            char c = baseText.charAt(i);
            MutableComponent ch = Component.literal(String.valueOf(c)).withStyle(text.getStyle());
            if (!revealMask[i]) ch = ch.withStyle(s -> s.withObfuscated(true));
            result.append(ch);
        }
        current = result;
    }

    // ----- Network codec -----
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        // Serialize original markup source so client can re-parse effects via LiteralContentsMixin
        if (markupSource != null) {
            tag.putString("Markup", markupSource);
        }

        // 1.20.1-safe: store text as JSON
        tag.putString("TextJson", Component.Serializer.toJson(text));

        tag.putFloat("Duration", duration);
        tag.putInt("fadeIn", fadeInTicks);
        tag.putInt("fadeOut", fadeOutTicks);
        tag.putFloat("XOffset", xOffset);
        tag.putFloat("YOffset", yOffset);
        tag.putBoolean("Shadow", shadow);
        tag.putString("Anchor", anchor.name());
        tag.putString("Align", align.name());
        tag.putFloat("Scale", textScale);
        tag.putBoolean("Background", background);
        tag.putInt("BackgroundColor", backgroundColor.getARGB());
        tag.putInt("BorderStart", borderStart.getARGB());
        tag.putInt("BorderEnd", borderEnd.getARGB());
        tag.putBoolean("Typewriter", typewriter);
        tag.putFloat("TypewriterSpeed", typewriterSpeed);
        tag.putBoolean("TypewriterCenter", typewriterCenter);
        tag.putString("ObfuscateMode", obfuscateMode.name());
        tag.putFloat("ObfuscateSpeed", obfuscateSpeed);
        tag.putBoolean("UseTextureBackground", useTextureBackground && backgroundTexture != null);
        if (useTextureBackground && backgroundTexture != null) {
            CompoundTag texture = new CompoundTag();
            texture.putString("Location", backgroundTexture.toString());
            texture.putInt("U", textureU);
            texture.putInt("V", textureV);
            texture.putInt("Width", textureWidth);
            texture.putInt("Height", textureHeight);
            texture.putInt("AtlasWidth", textureAtlasWidth);
            texture.putInt("AtlasHeight", textureAtlasHeight);
            texture.putFloat("PaddingX", texturePaddingX);
            texture.putFloat("PaddingY", texturePaddingY);
            texture.putFloat("ScaleX", textureScaleX);
            texture.putFloat("ScaleY", textureScaleY);
            if (textureOverrideWidth >= 0f) {
                texture.putFloat("OverrideWidth", textureOverrideWidth);
            }
            if (textureOverrideHeight >= 0f) {
                texture.putFloat("OverrideHeight", textureOverrideHeight);
            }
            texture.putString("SizingMode", textureSizingMode.name());
            tag.put("Texture", texture);
        }
        if (backgroundGradientStops != null) {
            ListTag list = new ListTag();
            for (ImmersiveColor color : backgroundGradientStops) {
                if (color != null) {
                    list.add(IntTag.valueOf(color.getARGB()));
                }
            }
            if (!list.isEmpty()) {
                tag.put("BackgroundGradient", list);
            }
        }
        tag.putInt("WrapWidth", wrapMaxWidth);
        tag.putFloat("Delay", delay);
        return tag;
    }

    public static ImmersiveMessage fromNbt(CompoundTag tag) {
        Component text = Component.literal("");

        // 1.20.1-safe: read JSON text
        if (tag.contains("TextJson", Tag.TAG_STRING)) {
            Component parsed = Component.Serializer.fromJson(tag.getString("TextJson"));
            if (parsed != null) text = parsed;
        }

        float duration = tag.contains("Duration") ? tag.getFloat("Duration") : 0f;
        int fadeIn = tag.contains("fadeIn") ? tag.getInt("fadeIn")
                : tag.contains("FadeIn") ? tag.getInt("FadeIn") : 0;
        int fadeOut = tag.contains("fadeOut") ? tag.getInt("fadeOut")
                : tag.contains("FadeOut") ? tag.getInt("FadeOut") : 0;
        ImmersiveMessage msg = new ImmersiveMessage(text, duration);
        msg.fadeInTicks = Math.max(0, fadeIn);
        msg.fadeOutTicks = Math.max(0, fadeOut);
        if (tag.contains("XOffset")) msg.xOffset = tag.getFloat("XOffset");
        if (tag.contains("YOffset")) msg.yOffset = tag.getFloat("YOffset");
        if (tag.contains("Shadow")) msg.shadow = tag.getBoolean("Shadow");
        if (tag.contains("Anchor")) {
            try {
                msg.anchor = TextAnchor.valueOf(tag.getString("Anchor"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (tag.contains("Align")) {
            try {
                msg.align = TextAlign.valueOf(tag.getString("Align"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (tag.contains("Scale")) msg.textScale = tag.getFloat("Scale");
        if (tag.contains("Background")) msg.background = tag.getBoolean("Background");
        if (tag.contains("BackgroundColor")) msg.backgroundColor = new ImmersiveColor(tag.getInt("BackgroundColor"));
        if (tag.contains("BorderStart")) msg.borderStart = new ImmersiveColor(tag.getInt("BorderStart"));
        if (tag.contains("BorderEnd")) msg.borderEnd = new ImmersiveColor(tag.getInt("BorderEnd"));
        if (tag.contains("Typewriter")) msg.typewriter = tag.getBoolean("Typewriter");
        if (tag.contains("TypewriterSpeed")) msg.typewriterSpeed = tag.getFloat("TypewriterSpeed");
        if (tag.contains("TypewriterCenter")) msg.typewriterCenter = tag.getBoolean("TypewriterCenter");
        if (tag.contains("ObfuscateMode")) {
            try {
                msg.obfuscateMode = ObfuscateMode.valueOf(tag.getString("ObfuscateMode"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (tag.contains("ObfuscateSpeed")) msg.obfuscateSpeed = tag.getFloat("ObfuscateSpeed");
        if (tag.contains("Texture")) {
            CompoundTag texture = tag.getCompound("Texture");
            if (texture.contains("Location")) {
                ResourceLocation rl = ResourceLocation.tryParse(texture.getString("Location"));
                msg.backgroundTexture = rl != null ? rl : new ResourceLocation("minecraft", "missingno");
                msg.useTextureBackground = true;
                msg.background = true;
            }
            if (texture.contains("U")) msg.textureU = texture.getInt("U");
            if (texture.contains("V")) msg.textureV = texture.getInt("V");
            if (texture.contains("Width")) msg.textureWidth = Math.max(1, texture.getInt("Width"));
            if (texture.contains("Height")) msg.textureHeight = Math.max(1, texture.getInt("Height"));
            if (texture.contains("AtlasWidth")) msg.textureAtlasWidth = Math.max(1, texture.getInt("AtlasWidth"));
            if (texture.contains("AtlasHeight")) msg.textureAtlasHeight = Math.max(1, texture.getInt("AtlasHeight"));
            if (texture.contains("PaddingX")) msg.texturePaddingX = texture.getFloat("PaddingX");
            if (texture.contains("PaddingY")) msg.texturePaddingY = texture.getFloat("PaddingY");
            if (texture.contains("ScaleX")) msg.textureScaleX = texture.getFloat("ScaleX");
            if (texture.contains("ScaleY")) msg.textureScaleY = texture.getFloat("ScaleY");
            msg.textureOverrideWidth = texture.contains("OverrideWidth") ? texture.getFloat("OverrideWidth") : -1f;
            msg.textureOverrideHeight = texture.contains("OverrideHeight") ? texture.getFloat("OverrideHeight") : -1f;
            if (texture.contains("SizingMode")) {
                try {
                    msg.textureSizingMode = TextureSizingMode.valueOf(texture.getString("SizingMode"));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } else if (tag.getBoolean("UseTextureBackground")) {
            msg.useTextureBackground = true;
            msg.background = true;
        }
        if (tag.contains("BackgroundGradient")) {
            ListTag list = tag.getList("BackgroundGradient", Tag.TAG_INT);
            ImmersiveColor[] cols = new ImmersiveColor[list.size()];
            for (int i = 0; i < list.size(); i++) {
                cols[i] = new ImmersiveColor(((IntTag) list.get(i)).getAsInt());
            }
            msg.backgroundGradient(cols);
        }
        if (tag.contains("WrapWidth")) msg.wrapMaxWidth = tag.getInt("WrapWidth");
        if (tag.contains("Delay")) msg.delay = tag.getFloat("Delay");

        // Restore markup source so client can re-parse effects via LiteralContentsMixin
        if (tag.contains("Markup", Tag.TAG_STRING)) {
            msg.markupSource = tag.getString("Markup");
            msg.markupComponent = Component.literal(msg.markupSource);
            // Disable legacy typewriter since the markup-based TypewriterEffect will handle it
            msg.typewriter = false;
        }

        if (msg.obfuscateMode != ObfuscateMode.NONE) msg.initObfuscation();
        return msg;
    }

    public void encode(FriendlyByteBuf buf) {
        // NEW: Write span mode flag first
        buf.writeBoolean(spanMode);

        if (spanMode) {
            // Serialize spans data
            buf.writeVarInt(spans != null ? spans.size() : 0);
            if (spans != null) {
                for (TextSpan span : spans) {
                    span.encode(buf);
                }
            }
        }

        buf.writeComponent(text);
        buf.writeFloat(duration);
        buf.writeFloat(xOffset);
        buf.writeFloat(yOffset);
        buf.writeBoolean(shadow);
        buf.writeEnum(anchor);
        buf.writeEnum(align);
        buf.writeBoolean(background);
        buf.writeInt(backgroundColor.getARGB());
        buf.writeInt(borderStart.getARGB());
        buf.writeInt(borderEnd.getARGB());
        buf.writeFloat(textScale);
        buf.writeBoolean(typewriter);
        buf.writeFloat(typewriterSpeed);
        buf.writeBoolean(typewriterCenter);
        buf.writeEnum(obfuscateMode);
        buf.writeFloat(obfuscateSpeed);

        buf.writeBoolean(useTextureBackground && backgroundTexture != null);
        if (useTextureBackground && backgroundTexture != null) {
            buf.writeResourceLocation(backgroundTexture);
            buf.writeVarInt(textureU);
            buf.writeVarInt(textureV);
            buf.writeVarInt(textureWidth);
            buf.writeVarInt(textureHeight);
            buf.writeVarInt(textureAtlasWidth);
            buf.writeVarInt(textureAtlasHeight);
            buf.writeFloat(texturePaddingX);
            buf.writeFloat(texturePaddingY);
            buf.writeFloat(textureScaleX);
            buf.writeFloat(textureScaleY);
            buf.writeBoolean(textureOverrideWidth >= 0f);
            if (textureOverrideWidth >= 0f) {
                buf.writeFloat(textureOverrideWidth);
            }
            buf.writeBoolean(textureOverrideHeight >= 0f);
            if (textureOverrideHeight >= 0f) {
                buf.writeFloat(textureOverrideHeight);
            }
            buf.writeEnum(textureSizingMode);
        }

        // Background gradient stops
        buf.writeBoolean(backgroundGradientStops != null);
        if (backgroundGradientStops != null) {
            buf.writeVarInt(backgroundGradientStops.length);
            for (ImmersiveColor c : backgroundGradientStops) buf.writeInt(c.getARGB());
        }

        buf.writeInt(wrapMaxWidth);
        buf.writeFloat(delay);
        buf.writeVarInt(fadeInTicks);
        buf.writeVarInt(fadeOutTicks);

        // NEW: Encode global effects (v2.0.0)
        buf.writeVarInt(globalEffects == null ? 0 : globalEffects.size());
        if (globalEffects != null && !globalEffects.isEmpty()) {
            for (Effect effect : globalEffects) {
                buf.writeUtf(effect.serialize());
            }
        }
    }

    public static ImmersiveMessage decode(FriendlyByteBuf buf) {
        // NEW: Read span mode flag first
        boolean spanMode = buf.readBoolean();

        ImmersiveMessage msg;
        if (spanMode) {
            // Deserialize spans data
            int spanCount = buf.readVarInt();
            java.util.List<TextSpan> spans = new java.util.ArrayList<>();
            for (int i = 0; i < spanCount; i++) {
                spans.add(TextSpan.decode(buf));
            }

            // Read component and duration
            Component text = buf.readComponent();
            float duration = buf.readFloat();

            // Create span-based message
            msg = new ImmersiveMessage(text, duration);
            msg.spanMode = true;
            msg.spans = spans;
            msg.spanTypewriterIndices = new int[spans.size()];
            msg.buildEffectSegments();
        } else {
            // Legacy deserialization
            Component text = buf.readComponent();
            float duration = buf.readFloat();
            msg = new ImmersiveMessage(text, duration);
        }
        msg.xOffset = buf.readFloat();
        msg.yOffset = buf.readFloat();
        msg.shadow = buf.readBoolean();
        msg.anchor = buf.readEnum(TextAnchor.class);
        msg.align = buf.readEnum(TextAlign.class);
        msg.background = buf.readBoolean();
        msg.backgroundColor = new ImmersiveColor(buf.readInt());
        msg.borderStart = new ImmersiveColor(buf.readInt());
        msg.borderEnd = new ImmersiveColor(buf.readInt());
        msg.textScale = buf.readFloat();
        msg.typewriter = buf.readBoolean();
        msg.typewriterSpeed = buf.readFloat();
        msg.typewriterCenter = buf.readBoolean();
        msg.obfuscateMode = buf.readEnum(ObfuscateMode.class);
        msg.obfuscateSpeed = buf.readFloat();

        if (buf.readBoolean()) {
            msg.backgroundTexture = buf.readResourceLocation();
            msg.textureU = buf.readVarInt();
            msg.textureV = buf.readVarInt();
            msg.textureWidth = Math.max(1, buf.readVarInt());
            msg.textureHeight = Math.max(1, buf.readVarInt());
            msg.textureAtlasWidth = Math.max(1, buf.readVarInt());
            msg.textureAtlasHeight = Math.max(1, buf.readVarInt());
            msg.texturePaddingX = buf.readFloat();
            msg.texturePaddingY = buf.readFloat();
            msg.textureScaleX = buf.readFloat();
            msg.textureScaleY = buf.readFloat();
            msg.textureOverrideWidth = buf.readBoolean() ? buf.readFloat() : -1f;
            msg.textureOverrideHeight = buf.readBoolean() ? buf.readFloat() : -1f;
            msg.textureSizingMode = buf.readEnum(TextureSizingMode.class);
            msg.useTextureBackground = true;
            msg.background = true;
        }

        // Background gradient stops
        if (buf.readBoolean()) {
            int count = buf.readVarInt();
            ImmersiveColor[] cols = new ImmersiveColor[count];
            for (int i = 0; i < count; i++) cols[i] = new ImmersiveColor(buf.readInt());
            msg.backgroundGradient(cols);
        }

        msg.wrapMaxWidth = buf.readInt();
        msg.delay = buf.readFloat();
        if (buf.isReadable()) {
            msg.fadeInTicks = Math.max(0, buf.readVarInt());
        }
        if (buf.isReadable()) {
            msg.fadeOutTicks = Math.max(0, buf.readVarInt());
        }

        // NEW: Decode global effects (v2.0.0)
        if (buf.isReadable()) {
            int effectCount = buf.readVarInt();
            if (effectCount > 0) {
                for (int i = 0; i < effectCount; i++) {
                    String effectTag = buf.readUtf();
                    try {
                        Effect effect = EffectRegistry.parseTag(effectTag);
                        msg.addEffect(effect);
                    } catch (IllegalArgumentException e) {
                        // Skip unknown effects (forward compatibility)
                        LOGGER.warn("Failed to decode global effect: {}", effectTag, e);
                    }
                }
            }
        }

        if (msg.obfuscateMode != ObfuscateMode.NONE) msg.initObfuscation();
        return msg;
    }

    // ----- Runtime behaviour -----
    public void tickEffects() {
        tick(1f);
    }

    public boolean hasDuration() {
        return totalLifetime() > 0f;
    }

    public int durationTicks() {
        return Mth.ceil(duration);
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    /**
     * @return configured fade-in length in ticks.
     */
    public int getFadeInTicks() {
        return fadeInTicks;
    }

    /**
     * @return configured fade-out length in ticks.
     */
    public int getFadeOutTicks() {
        return fadeOutTicks;
    }

    /**
     * Total lifetime in ticks including fade in/out wrappers.
     */
    public float totalLifetime() {
        return fadeInTicks + duration + fadeOutTicks;
    }

    public int totalLifetimeTicks() {
        return Mth.ceil(totalLifetime());
    }

    public Component component() {
        return getDrawComponent();
    }

    public int renderColour() {
        int base = text.getStyle().getColor() != null ? text.getStyle().getColor().getValue() : 0xFFFFFF;
        int alpha = normalizeFontAlpha(Mth.clamp(Math.round(computeAlpha(age) * 255f), 0, 255));
        return (alpha << 24) | (base & 0x00FFFFFF);
    }

    /**
     * Computes the ARGB colour for rendering with interpolation between the previous and current age values.
     *
     * @param partialTick render partial tick, typically between {@code 0} and {@code 1}.
     * @return colour with fade alpha applied for the provided partial tick.
     */
    public int renderColour(float partialTick) {
        int base = text.getStyle().getColor() != null ? text.getStyle().getColor().getValue() : 0xFFFFFF;
        int alpha = normalizeFontAlpha(Mth.clamp(Math.round(computeAlpha(sampleAge(partialTick)) * 255f), 0, 255));
        return (alpha << 24) | (base & 0x00FFFFFF);
    }

    public float getTextScale() {
        return textScale;
    }

    public int getWrapWidth() {
        return wrapMaxWidth;
    }

    public String fontKey() {
        // 1.20.1: Style#getFont() returns a ResourceLocation (not Optional)
        ResourceLocation font = text.getStyle().getFont();
        return font != null ? font.toString() : "minecraft:default";
    }

    private Component getDrawComponent() {
        // NEW: Handle span-based rendering
        if (spanMode && spans != null) {
            return buildComponentFromSpans();
        }

        // Markup source: return the original markup string as a literal component
        // so LiteralContentsMixin can re-parse tags (<typewriter>, <rainbow>, etc.)
        if (markupComponent != null) {
            return markupComponent;
        }

        // Legacy rendering
        if (typewriter) {
            return current;
        }
        return current.getString().isEmpty() ? text : current;
    }

    /**
     * Builds a styled Component from the current spans.
     */
    private Component buildComponentFromSpans() {
        if (spans == null || spans.isEmpty()) {
            return Component.literal("");
        }

        MutableComponent result = Component.literal("");
        boolean hasAnyTypewriter = hasAnyTypewriterSpans();
        int currentCharIndex = 0;

        for (int i = 0; i < spans.size(); i++) {
            TextSpan span = spans.get(i);
            String content = span.getContent();
            // Skip only if empty AND not an item span
            if (content.isEmpty() && span.getItemId() == null) continue;

            MutableComponent spanComponent;

            // Handle typewriter effect
            if (typewriter && spanTypewriterIndices != null && i < spanTypewriterIndices.length) {
                // Use the pre-calculated typewriter index (works for both container and per-span typewriter)
                int spanTypewriterIndex = spanTypewriterIndices[i];
                if (spanTypewriterIndex < content.length()) {
                    content = content.substring(0, Math.max(0, spanTypewriterIndex));
                }
            } else if (!typewriter && span.getTypewriterSpeed() != null && spanTypewriterIndices != null) {
                // Handle case where global typewriter is off but span has its own typewriter
                int spanTypewriterIndex = spanTypewriterIndices[i];
                if (spanTypewriterIndex < content.length()) {
                    content = content.substring(0, Math.max(0, spanTypewriterIndex));
                }
            }

            // Handle item spans specially
            if (span.getItemId() != null) {
                // Add a placeholder space for items - they'll be rendered custom in renderSpansWithItems
                spanComponent = Component.literal(" ");
                result.append(spanComponent);
                currentCharIndex += span.getContent().length();
                continue;
            }

            // Handle entity spans specially
            if (span.getEntityId() != null) {
                // Add a placeholder space for entities - they'll be rendered custom in renderSpansWithItems
                spanComponent = Component.literal("  ");
                result.append(spanComponent);
                currentCharIndex += span.getContent().length();
                continue;
            }

            if (content.isEmpty()) {
                currentCharIndex += span.getContent().length();
                continue;
            }

            // Simple span with single styling
            spanComponent = Component.literal(content);
            applySpanStyling(spanComponent, span);

            result.append(spanComponent);
            currentCharIndex += span.getContent().length();
        }

        return result;
    }

    /**
     * Checks if any spans have their own typewriter effects.
     */
    private boolean hasAnyTypewriterSpans() {
        if (spans == null) return false;
        return spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null);
    }

    /**
     * Applies span styling to a component.
     */
    private void applySpanStyling(MutableComponent component, TextSpan span) {
        applySpanStyling(component, span, 1.0f);
    }

    /**
     * Applies span styling to a component with alpha modulation.
     */
    private void applySpanStyling(MutableComponent component, TextSpan span, float alpha) {
        component.withStyle(style -> {
            if (span.getBold() != null && span.getBold()) style = style.withBold(true);
            if (span.getItalic() != null && span.getItalic()) style = style.withItalic(true);
            if (span.getUnderline() != null && span.getUnderline()) style = style.withUnderlined(true);
            if (span.getStrikethrough() != null && span.getStrikethrough()) style = style.withStrikethrough(true);
            if (span.getObfuscated() != null && span.getObfuscated()) style = style.withObfuscated(true);
            if (span.getFont() != null) style = style.withFont(span.getFont());

            // Apply color with alpha modulation
            if (span.getColor() != null) {
                int originalColor = span.getColor().getValue();
                int alphaComponent = (int)(alpha * 255) << 24;
                int colorWithAlpha = alphaComponent | (originalColor & 0x00FFFFFF);
                style = style.withColor(TextColor.fromRgb(colorWithAlpha));
            } else if (alpha < 1.0f) {
                // Apply alpha to default color
                int alphaComponent = (int)(alpha * 255) << 24;
                int colorWithAlpha = alphaComponent | 0x00FFFFFF; // White with alpha
                style = style.withColor(TextColor.fromRgb(colorWithAlpha));
            }

            return style;
        });
    }

    private float computeAlpha(float sampleAge) {
        return FadeCalculator.computeFadeAlpha(sampleAge, fadeInTicks, duration, fadeOutTicks);
    }

    /**
     * Computes alpha for a specific span with per-span fade effects.
     */
    private float computeSpanAlpha(TextSpan span, float sampleAge) {
        return FadeCalculator.computeSpanFadeAlpha(sampleAge,
                span.getFadeInTicks(), span.getFadeOutTicks(),
                duration, fadeInTicks, fadeOutTicks);
    }

    private float sampleAge(float partialTick) {
        float clamped = Mth.clamp(partialTick, 0f, 1f);
        return Mth.lerp(clamped, previousAge, age);
    }

    /**
     * Vanilla Font#drawInBatch forces colors with alpha 0..3 to full opacity.
     * Collapse that range to 0 so fade edges do not flash for one frame.
     */
    private static int normalizeFontAlpha(int alpha) {
        return alpha <= 3 ? 0 : alpha;
    }

    public TextLayoutCache.Layout buildLayout(Component draw) {
        var font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = null;
        int baseWidth;
        int baseHeight;
        if (wrapMaxWidth > 0) {
            lines = font.split(draw, wrapMaxWidth);
            float maxWidth = 0f;
            for (FormattedCharSequence line : lines) {
                float width = font.getSplitter().stringWidth(line);
                maxWidth = Math.max(maxWidth, width);
            }
            baseWidth = Mth.ceil(maxWidth);
            baseHeight = lines.size() * font.lineHeight;
        } else {
            FormattedCharSequence sequence = draw.getVisualOrderText();
            float width = font.getSplitter().stringWidth(sequence);
            baseWidth = Mth.ceil(width);
            baseHeight = font.lineHeight;
        }
        return new TextLayoutCache.Layout(lines, draw.getVisualOrderText(), baseWidth, baseHeight);
    }

    public void renderWithLayout(GuiGraphics graphics, Component draw, TextLayoutCache.Layout layout, int screenW, int screenH, float partialTick) {
        var font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = layout.lines();
        int baseWidth = layout.width();
        int baseHeight = layout.height();

        float charPadding = 0f;
        float textAreaWidth = baseWidth + charPadding * 2f;
        float textAreaHeight = baseHeight + charPadding * 2f;

        float scaledWidth = textureOverrideWidth >= 0f ? textureOverrideWidth : textAreaWidth * Math.max(textureScaleX, 0f);
        float scaledHeight = textureOverrideHeight >= 0f ? textureOverrideHeight : textAreaHeight * Math.max(textureScaleY, 0f);
        scaledWidth = Math.max(scaledWidth, textAreaWidth);
        scaledHeight = Math.max(scaledHeight, textAreaHeight);

        float backgroundWidth = scaledWidth + texturePaddingX * 2f;
        float backgroundHeight = scaledHeight + texturePaddingY * 2f;

        float extraX = (backgroundWidth - textAreaWidth) / 2f;
        float extraY = (backgroundHeight - textAreaHeight) / 2f;
        float textStartX = extraX + charPadding;
        float textStartY = extraY + charPadding;

        int backgroundWidthInt = Mth.ceil(backgroundWidth);
        int backgroundHeightInt = Mth.ceil(backgroundHeight);

        float x = screenW * anchor.xFactor - baseWidth * textScale * align.xFactor + xOffset;
        float y = screenH * anchor.yFactor - baseHeight * textScale * anchor.yFactor + yOffset;

        // Clamp so text stays on screen with a small margin
        float margin = 4f;
        float scaledW = baseWidth * textScale;
        float scaledH = baseHeight * textScale;
        x = Mth.clamp(x, margin, screenW - scaledW - margin);
        y = Mth.clamp(y, margin, screenH - scaledH - margin);

        float renderAge = sampleAge(partialTick);

        // Skip rendering entirely if we have fade-in and haven't started yet
        if (fadeInTicks > 0 && renderAge <= 0f) {
            graphics.pose().pushPose();
            graphics.pose().popPose();
            return;
        }

        float alpha = computeAlpha(renderAge);
        int alphaByte = normalizeFontAlpha(Mth.clamp(Math.round(alpha * 255f), 0, 255));
        int colour = (alphaByte << 24) | ((text.getStyle().getColor() != null ? text.getStyle().getColor().getValue() : 0xFFFFFF) & 0x00FFFFFF);
        boolean textVisible = alphaByte > 0;

        if (typewriter && typewriterCenter && wrapMaxWidth <= 0) {
            float fullWidth = font.width(text);
            float currentWidth = font.width(draw);
            x += (fullWidth - currentWidth) / 2f * textScale;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(x - textStartX * textScale, y - textStartY * textScale, 0);
        graphics.pose().scale(textScale, textScale, 1f);
        if (background) {
            int widthForBg = backgroundWidthInt;

            if (useTextureBackground && backgroundTexture != null) {
                BackgroundRenderer.renderTextureBackground(graphics, 0, 0, widthForBg, backgroundHeightInt,
                        backgroundTexture, textureU, textureV, textureWidth, textureHeight,
                        textureAtlasWidth, textureAtlasHeight, textureSizingMode, alpha);
            } else if (backgroundGradientStops != null) {
                BackgroundRenderer.renderGradientBackground(graphics, 0, 0, widthForBg, backgroundHeightInt,
                        backgroundGradientStops, borderStart, borderEnd, alpha);
            } else {
                BackgroundRenderer.renderSolidBackground(graphics, 0, 0, widthForBg, backgroundHeightInt,
                        backgroundColor, borderStart, borderEnd, alpha);
            }
        }
        boolean hasInlineItems = spanMode && spans != null && hasItemSpans();

        // NEW: Check for effects (v2.0.0)
        boolean hasGlobalEffects = globalEffects != null && !globalEffects.isEmpty();
        boolean hasSpanEffects = spanMode && spans != null && spans.stream().anyMatch(s -> s.getEffects() != null && !s.getEffects().isEmpty());

        // Debug: Log effect detection
        if (LOGGER.isDebugEnabled() && (hasGlobalEffects || hasSpanEffects)) {
            LOGGER.debug("Effect rendering: hasGlobalEffects={}, hasSpanEffects={}, spanMode={}, spans={}",
                hasGlobalEffects, hasSpanEffects, spanMode, spans != null ? spans.size() : 0);
            if (hasGlobalEffects && globalEffects != null) {
                LOGGER.debug("Global effects count: {}", globalEffects.size());
            }
            if (hasSpanEffects && spans != null) {
                long effectSpanCount = spans.stream()
                        .filter(s -> s.getEffects() != null && !s.getEffects().isEmpty())
                        .count();
                LOGGER.debug("Spans with effects: {}", effectSpanCount);
            }
        }

        if (onRender != null) {
            onRender.render(graphics, this, 0, 0, alpha);
        } else if (!textVisible) {
            // Skip font rendering when alpha is in vanilla's unsafe 0..3 range.
        } else if (hasGlobalEffects || hasSpanEffects) {
            // Use new effect rendering system (v2.0.0)
            LOGGER.debug("Calling renderWithEffects");
            renderWithEffects(graphics, lines, draw, colour, textStartX, textStartY);
        } else if (hasInlineItems) {
            // Render spans with items/entities inline (entities static; no animations)
            renderSpansWithItems(graphics, textStartX, textStartY, colour, alpha);
        } else if (lines != null) {
            int drawStartX = Mth.floor(textStartX);
            for (int i = 0; i < lines.size(); i++) {
                int drawStartY = Mth.floor(textStartY + i * font.lineHeight);
                graphics.drawString(font, lines.get(i), drawStartX, drawStartY, colour, shadow);
            }
        } else {
            int drawStartX = Mth.floor(textStartX);
            int drawStartY = Mth.floor(textStartY);
            graphics.drawString(font, draw, drawStartX, drawStartY, colour, shadow);
        }
        graphics.pose().popPose();
    }

    public void tick(float delta) {
        previousAge = age;
        age += delta;

        // Typewriter progression
        if (typewriter) {
            // Handle global typewriter (legacy mode)
            int next = TypewriterAnimator.calculateTypewriterIndex(age, typewriterSpeed, text.getString().length());
            if (next > typewriterIndex) {
                typewriterIndex = next;
                if (obfuscateMode != ObfuscateMode.NONE) {
                    rebuildObfuscation();
                } else {
                    current = Component.literal(text.getString().substring(0, typewriterIndex))
                            .withStyle(text.getStyle());
                }
            }

            // Handle per-span typewriter (span mode)
            if (spanMode && spans != null && spanTypewriterIndices != null) {
                // Check if ANY span has its own typewriter speed (independent typewriter)
                boolean hasIndependentTypewriter = TypewriterAnimator.hasIndependentTypewriter(spans);

                if (hasIndependentTypewriter) {
                    // Original behavior: Each span with typewriter animates independently
                    TypewriterAnimator.updateIndependentSpanTypewriter(age, spans, spanTypewriterIndices);
                } else {
                    // NEW: Container-based typewriter - count chars across all spans
                    // Use global typewriter speed and reveal chars sequentially across spans
                    int totalCharsToShow = Math.min(next, getFullText().length());
                    TypewriterAnimator.updateContainerTypewriter(totalCharsToShow, spans, spanTypewriterIndices);
                }
            }
        }

        // Obfuscation progression
        if (obfuscateMode != ObfuscateMode.NONE) tickObfuscation(delta);
    }

    private void tickObfuscation(float delta) {
        if (baseText == null || revealIndex >= revealOrder.size()) return;

        int[] revealIndexRef = {revealIndex};
        float[] progressRef = {obfuscateProgress};
        int revealed = ObfuscateAnimator.updateRevealMask(revealMask, revealOrder,
                revealIndexRef, progressRef,
                delta, obfuscateSpeed,
                typewriter, typewriterIndex);
        revealIndex = revealIndexRef[0];
        obfuscateProgress = progressRef[0];

        if (revealed > 0) {
            rebuildObfuscation();
        }
    }

    public boolean isFinished() {
        return hasDuration() && age >= totalLifetime();
    }

    public float getDelay() { return delay; }

    public void render(GuiGraphics graphics) {
        Component draw = component();
        TextLayoutCache.Layout layout = buildLayout(draw);
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        renderWithLayout(graphics, draw, layout, screenW, screenH, 0f);
    }

    /**
     * Checks if any spans contain items or entities to render.
     */
    private boolean hasItemSpans() {
        if (spans == null) return false;
        return spans.stream().anyMatch(span -> span.getItemId() != null || span.getEntityId() != null);
    }

    /**
     * Build effect segments from spans with effects (v2.0.0).
     * This creates a mapping of character indices to their associated effects.
     */
    private void buildEffectSegments() {
        spanEffectSegments = Collections.emptyList();
        if (spans == null) return;

        List<EffectSegment> segments = new ArrayList<>();
        int charIndex = 0;
        for (int spanIdx = 0; spanIdx < spans.size(); spanIdx++) {
            TextSpan span = spans.get(spanIdx);
            String content = span.getContent();
            int length = content != null ? content.length() : 0;
            if (length > 0 && span.getEffects() != null && !span.getEffects().isEmpty()) {
                segments.add(new EffectSegment(
                    charIndex,
                    charIndex + length,
                    span.getEffects(),
                    spanIdx
                ));
            }
            charIndex += length;
        }
        if (!segments.isEmpty()) {
            spanEffectSegments = segments;
        }
    }

    private EffectSegment findEffectSegmentForChar(int charIndex) {
        if (spanEffectSegments == null || spanEffectSegments.isEmpty()) {
            return null;
        }
        for (EffectSegment segment : spanEffectSegments) {
            if (charIndex >= segment.startIndex && charIndex < segment.endIndex) {
                return segment;
            }
        }
        return null;
    }

    /**
     * Segment mapping for visual effects (v2.0.0).
     * Maps character ranges to their associated effects.
     */
    private static class EffectSegment {
        final int startIndex;
        final int endIndex;
        final List<Effect> effects;
        final int spanIndex;

        EffectSegment(int startIndex, int endIndex, List<Effect> effects, int spanIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.effects = effects;
            this.spanIndex = spanIndex;
        }
    }

    /**
     * Renders spans with inline items/entities. Entities are rendered statically (no animations).
     */
    private void renderSpansWithItems(GuiGraphics graphics, float startX, float startY, int colour, float alpha) {
        var font = Minecraft.getInstance().font;
        var mc = Minecraft.getInstance();
        float xOffset = startX;
        float yOffset = startY;

        for (int i = 0; i < spans.size(); i++) {
            TextSpan span = spans.get(i);

            // Check if this is an item span
            if (span.getItemId() != null) {
                // Render item icon
                try {
                    ResourceLocation itemLocation = ResourceLocation.tryParse(span.getItemId());
                    if (itemLocation != null) {
                        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(itemLocation);
                        if (item != null) {
                            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, span.getItemCount() != null ? span.getItemCount() : 1);

                            // Apply NBT data if specified
                            if (span.getItemNbt() != null) {
                                try {
                                    CompoundTag nbtTag = TagParser.parseTag(span.getItemNbt());
                                    stack.setTag(nbtTag);
                                } catch (Exception e) {
                                    LOGGER.debug("Failed to parse item NBT '{}': {}", span.getItemNbt(), e.getMessage());
                                }
                            }

                            // Render the item at 16x16 size (standard Minecraft item size)
                            int itemSize = 16;
                            // Center item vertically with text (font height is 9, item is 16)
                            float itemYOffset = yOffset - (itemSize - font.lineHeight) / 2.0f;

                            // Apply custom offsets if specified
                            float customOffsetX = span.getItemOffsetX() != null ? span.getItemOffsetX() : 0f;
                            float customOffsetY = span.getItemOffsetY() != null ? span.getItemOffsetY() : 0f;

                            graphics.pose().pushPose();
                            graphics.pose().translate(xOffset + customOffsetX, itemYOffset + customOffsetY, 0);
                            graphics.renderItem(stack, 0, 0);
                            graphics.pose().popPose();

                            xOffset += itemSize + 2; // Add spacing after item
                        }
                    }
                } catch (Exception e) {
                    // If item rendering fails, just skip it
                }
            } else if (span.getEntityId() != null) {
                // Render entity using EntityRenderer utility
                float entityScale = span.getEntityScale() != null ? span.getEntityScale() : 1.0f;
                int entitySize = (int)(16 * entityScale);

                // Center entity vertically with text
                float entityYOffset = yOffset - (entitySize - font.lineHeight) / 2.0f;

                // Get parameters with defaults
                float customOffsetX = span.getEntityOffsetX() != null ? span.getEntityOffsetX() : 0f;
                float customOffsetY = span.getEntityOffsetY() != null ? span.getEntityOffsetY() : 0f;
                float yaw = span.getEntityYaw() != null ? span.getEntityYaw() : 45f;
                float pitch = span.getEntityPitch() != null ? span.getEntityPitch() : 0f;
                float roll = span.getEntityRoll() != null ? span.getEntityRoll() : 0f;
                int lighting = span.getEntityLighting() != null ? span.getEntityLighting() : 15;
                Float spin = span.getEntitySpin(); // null if not set

                int renderedWidth = net.tysontheember.emberstextapi.immersivemessages.util.EntityRenderer.render(
                        graphics,
                        span.getEntityId(),
                        xOffset,
                        entityYOffset,
                        entityScale,
                        customOffsetX,
                        customOffsetY,
                        yaw,
                        pitch,
                        roll,
                        lighting,
                        spin,
                        span.getEntityNbt()
                );

                if (renderedWidth > 0) {
                    xOffset += renderedWidth + 2; // Add spacing after entity
                }
            } else {
                // Render text span
                String content = span.getContent();

                // Handle typewriter effect
                if (typewriter && spanTypewriterIndices != null && i < spanTypewriterIndices.length) {
                    int spanTypewriterIndex = spanTypewriterIndices[i];
                    if (spanTypewriterIndex < content.length()) {
                        content = content.substring(0, Math.max(0, spanTypewriterIndex));
                    }
                }

                if (!content.isEmpty()) {
                    Component spanComponent = Component.literal(content);
                    applySpanStyling((MutableComponent) spanComponent, span, alpha);

                    int spanColor = colour;
                    if (span.getColor() != null) {
                        spanColor = ((int)(alpha * 255) << 24) | span.getColor().getValue();
                    }

                    graphics.drawString(font, spanComponent, (int)xOffset, (int)yOffset, spanColor, shadow);
                    xOffset += font.width(spanComponent);
                }
            }
        }
    }

    /**
     * Render with effects applied per-character (v2.0.0).
     * This method applies both span-level and global effects to each character.
     */
    private void renderWithEffects(GuiGraphics graphics, List<FormattedCharSequence> lines, Component draw, int baseColour, float baseX, float baseY) {
        var font = Minecraft.getInstance().font;
        int[] index = {0};

        // Extract base color components from baseColour
        float baseAlpha = ((baseColour >> 24) & 0xFF) / 255f;
        float baseRed = ((baseColour >> 16) & 0xFF) / 255f;
        float baseGreen = ((baseColour >> 8) & 0xFF) / 255f;
        float baseBlue = (baseColour & 0xFF) / 255f;

        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                final float lineBaseY = baseY + i * font.lineHeight;
                final float[] xAdvance = {baseX};
                FormattedCharSequence lineSeq = lines.get(i);

                lineSeq.accept((pos, style, codePoint) -> {
                    renderCharWithEffects(graphics, font, codePoint, style,
                            xAdvance[0], lineBaseY, baseRed, baseGreen, baseBlue, baseAlpha,
                            index[0], false, xAdvance);
                    index[0]++;
                    return true;
                });
            }
        } else {
            final float[] xAdvance = {baseX};
            draw.getVisualOrderText().accept((pos, style, codePoint) -> {
                renderCharWithEffects(graphics, font, codePoint, style,
                        xAdvance[0], baseY, baseRed, baseGreen, baseBlue, baseAlpha,
                        index[0], false, xAdvance);
                index[0]++;
                return true;
            });
        }
    }

    /**
     * Render a single character with effects applied.
     */
    private void renderCharWithEffects(GuiGraphics graphics, net.minecraft.client.gui.Font font,
                                        int codePoint, net.minecraft.network.chat.Style style,
                                        float baseX, float baseY,
                                        float baseR, float baseG, float baseB, float baseA,
                                        int charIndex, boolean isShadow, float[] xAdvanceOut) {
        // Use the character's style color (from span styling) as the base RGB,
        // falling back to the message-level color. This ensures effects like rainbow
        // compose correctly on top of span colors.
        float effR = baseR, effG = baseG, effB = baseB;
        if (style.getColor() != null) {
            int styleColor = style.getColor().getValue();
            effR = ((styleColor >> 16) & 0xFF) / 255f;
            effG = ((styleColor >> 8) & 0xFF) / 255f;
            effB = (styleColor & 0xFF) / 255f;
        }

        var settings = new net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings(
                0f, 0f,  // x, y offsets start at 0
                effR, effG, effB, baseA,
                charIndex, codePoint, isShadow
        );
        // Set obfuscate identity and span bounds when applicable
        EffectSegment obfSegment = findEffectSegmentForChar(charIndex);
        if (obfSegment != null) {
            settings.obfuscateSpanStart = obfSegment.startIndex;
            settings.obfuscateSpanLength = obfSegment.endIndex - obfSegment.startIndex;
            settings.obfuscateKey = new ObfKey(this.messageContextId, obfSegment.spanIndex);
            settings.obfuscateStableKey = settings.obfuscateKey;
        } else {
            // Fallback: persist across frames while this message is displayed
            settings.obfuscateKey = this;
        }

        // Apply global effects first
        if (globalEffects != null && !globalEffects.isEmpty()) {
            net.tysontheember.emberstextapi.immersivemessages.effects.EffectContext.applyEffects(globalEffects, settings);
        }

        // Apply span-level effects (v2.0.0)
        if (!spanEffectSegments.isEmpty()) {
            // Find the effect segment for this character index
            for (EffectSegment segment : spanEffectSegments) {
                if (charIndex >= segment.startIndex && charIndex < segment.endIndex) {
                    // Use span-relative index for animation effects (typewriter, obfuscate)
                    // so they operate on 0..spanLength-1 rather than the global char position
                    int spanLocalIndex = charIndex - segment.startIndex;
                    settings.index = spanLocalIndex;
                    settings.absoluteIndex = spanLocalIndex;
                    // Apply this span's effects
                    net.tysontheember.emberstextapi.immersivemessages.effects.EffectContext.applyEffects(segment.effects, settings);
                    // Restore global index for any subsequent logic
                    settings.index = charIndex;
                    settings.absoluteIndex = charIndex;
                    break;
                }
            }
        }

        // Clamp colors to valid range
        settings.clampColors();

        // Determine effective codepoint and style for rendering
        int effectiveCp = settings.codepoint;
        net.minecraft.network.chat.Style effectiveStyle = style;
        if (settings.useRandomGlyph) {
            // Use Minecraft's native §k obfuscation for authentic random glyph rendering
            effectiveStyle = style.applyFormat(net.minecraft.ChatFormatting.OBFUSCATED);
        }

        // Render main character with effect-modified settings
        renderSingleChar(graphics, font, effectiveCp, effectiveStyle,
                baseX + settings.x, baseY + settings.y,
                settings.rot, settings.getPackedColor(), xAdvanceOut);

        // Render sibling layers (for multi-layer effects like glitch, neon)
        // Use getSiblingsOrEmpty() to avoid NPE when no siblings exist
        for (var sibling : settings.getSiblingsOrEmpty()) {
            sibling.clampColors();
            renderSingleChar(graphics, font, codePoint, style,
                    baseX + sibling.x, baseY + sibling.y,
                    sibling.rot, sibling.getPackedColor(), null);
        }
    }

    /**
     * Render a single character at the specified position with optional rotation.
     */
    private void renderSingleChar(GuiGraphics graphics, net.minecraft.client.gui.Font font,
                                   int codePoint, net.minecraft.network.chat.Style style,
                                   float x, float y, float rotation, int color,
                                   float[] xAdvanceOut) {
        String ch = new String(Character.toChars(codePoint));
        // Strip the style's color so our explicit color parameter (with effect-computed ARGB)
        // is used by drawString. Minecraft's renderer ignores the color param when the style
        // has its own color, which breaks typewriter (alpha=0) and fade (partial alpha).
        Component comp = Component.literal(ch).withStyle(style.withColor((TextColor) null));
        FormattedCharSequence charSeq = comp.getVisualOrderText();

        // Calculate character width using float precision — font.width() returns int
        // (ceiling-rounded), which causes cumulative spacing drift in per-character rendering.
        float cw = font.getSplitter().stringWidth(charSeq);

        // Skip alpha values that vanilla Font can force to opaque (0..3).
        if (((color >>> 24) & 0xFF) <= 3) {
            if (xAdvanceOut != null) {
                xAdvanceOut[0] += cw;
            }
            return;
        }

        graphics.pose().pushPose();

        // Apply rotation if needed (for SwingEffect, etc.)
        if (rotation != 0f) {
            // Rotate around character center
            graphics.pose().translate(x + cw / 2f, y + font.lineHeight / 2f, 0);
            graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation(rotation));
            graphics.pose().translate(-(x + cw / 2f), -(y + font.lineHeight / 2f), 0);
        }

        graphics.pose().translate(x, y, 0);
        graphics.drawString(font, charSeq, 0, 0, color, shadow);
        graphics.pose().popPose();

        // Update x advance for next character
        if (xAdvanceOut != null) {
            xAdvanceOut[0] += cw;
        }
    }

    // NEW: Span-based API methods (v2.0.0)

    /**
     * Returns true if this message uses span-based rendering.
     */
    public boolean isSpanMode() {
        return spanMode;
    }

    /**
     * Gets the spans used for rendering. Only valid if isSpanMode() returns true.
     */
    public List<TextSpan> getSpans() {
        return spanMode && spans != null ? new ArrayList<>(spans) : Collections.emptyList();
    }

    /**
     * Adds a span to this message. Converts to span mode if not already.
     */
    public ImmersiveMessage addSpan(TextSpan span) {
        if (!spanMode) {
            // Convert to span mode
            List<TextSpan> plainSpans = MarkupParser.fromPlainText(text.getString());
            spans = new ArrayList<>(plainSpans); // Make it mutable
            spanMode = true;
        }
        spans.add(span);
        // Resize typewriter indices array
        if (spanTypewriterIndices == null) {
            spanTypewriterIndices = new int[spans.size()];
        } else {
            int[] newIndices = new int[spans.size()];
            System.arraycopy(spanTypewriterIndices, 0, newIndices, 0, Math.min(spanTypewriterIndices.length, spans.size()));
            spanTypewriterIndices = newIndices;
        }
        buildEffectSegments();
        return this;
    }

    /**
     * Gets the total text content across all spans.
     */
    public String getFullText() {
        if (spanMode && spans != null) {
            return MarkupParser.toPlainText(spans);
        }
        return text.getString();
    }

    public enum TextureSizingMode {
        STRETCH,
        CROP;

        public static TextureSizingMode fromString(String value) {
            if (value == null) {
                return STRETCH;
            }
            return switch (value.toLowerCase(java.util.Locale.ROOT)) {
                case "crop", "cut", "cover" -> CROP;
                default -> STRETCH;
            };
        }
    }
}
