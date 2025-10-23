package net.tysontheember.emberstextapi.immersivemessages.api;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.client.TextLayoutCache;
import net.tysontheember.emberstextapi.immersivemessages.util.CaxtonCompat;
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
    private final Component text;
    private final float duration;
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
    private TextAnchor align = TextAnchor.TOP_CENTER;
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

    // Text gradient (multi-stop)
    private TextColor[] gradientStops;
    private TextColor[] gradientColors;

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

    // Shake (whole text)
    private boolean shake = false;
    private float shakeStrength = 0f;
    private ShakeType shakeType = ShakeType.RANDOM;
    private float shakeSpeed = 10f; // multiplier for shake animation speed
    private float shakeWavelength = 1f; // wavelength for WAVE shake type (in arbitrary units)

    // Per-character shake
    private boolean charShake = false;
    private float charShakeStrength = 0f;
    private ShakeType charShakeType = ShakeType.RANDOM;
    private float charShakeSpeed = 10f; // multiplier for char shake animation speed
    private float charShakeWavelength = 1f; // wavelength for WAVE char shake type

    // NEW: Span-based rendering (v2.0.0)
    private List<TextSpan> spans;
    private boolean spanMode = false;
    private int[] spanTypewriterIndices; // Per-span typewriter progress

    private OnRenderMessage onRender;
    private final Random random = new Random();

    public ImmersiveMessage(Component text, float duration) {
        this.text = text;
        this.duration = duration;
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
        // Initialize age to ensure proper fade-in from start
        this.age = 0f;
        this.previousAge = 0f;

        // Check if any spans have typewriter effects and enable global typewriter if so
        if (spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null)) {
            this.typewriter = true;
        }

        // Check if any spans have shake effects and enable global shake if so
        for (TextSpan span : spans) {
            if (span.getShakeType() != null && span.getShakeAmplitude() != null) {
                this.shake = true;
                this.shakeType = span.getShakeType();
                this.shakeStrength = span.getShakeAmplitude();
                if (span.getShakeSpeed() != null) {
                    this.shakeSpeed = span.getShakeSpeed();
                }
                if (span.getShakeWavelength() != null) {
                    this.shakeWavelength = span.getShakeWavelength();
                }
                break; // Use first shake effect found
            }
            if (span.getCharShakeType() != null && span.getCharShakeAmplitude() != null) {
                this.charShake = true;
                this.charShakeType = span.getCharShakeType();
                this.charShakeStrength = span.getCharShakeAmplitude();
                if (span.getCharShakeSpeed() != null) {
                    this.charShakeSpeed = span.getCharShakeSpeed();
                }
                if (span.getCharShakeWavelength() != null) {
                    this.charShakeWavelength = span.getCharShakeWavelength();
                }
                break; // Use first char shake effect found
            }
        }

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
        return new ImmersiveMessage(parsed, duration);
    }

    /** NEW: Create from TextSpan list. */
    public static ImmersiveMessage fromSpans(float duration, List<TextSpan> spans) {
        return new ImmersiveMessage(spans, duration);
    }

    // ----- Builder style setters -----
    public ImmersiveMessage shadow(boolean shadow) { this.shadow = shadow; return this; }
    public ImmersiveMessage anchor(TextAnchor anchor) { this.anchor = anchor; return this; }
    public ImmersiveMessage align(TextAnchor align) { this.align = align; return this; }
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
    private ImmersiveColor parseColour(String value) {
        if (value == null) return null;

        String v = value.trim();
        try {
            if (v.startsWith("#")) v = v.substring(1);
            if (v.startsWith("0x")) v = v.substring(2);
            if (v.length() == 8) {
                return new ImmersiveColor((int) Long.parseLong(v, 16));
            } else if (v.length() == 6) {
                return new ImmersiveColor(0xFF000000 | Integer.parseInt(v, 16));
            }
        } catch (NumberFormatException ignored) {
        }

        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null && fmt.getColor() != null) {
            return new ImmersiveColor(0xFF000000 | fmt.getColor());
        }
        TextColor parsed = TextColor.parseColor(value);
        if (parsed != null) {
            int c = parsed.getValue();
            if ((c & 0xFF000000) == 0) c |= 0xFF000000;
            return new ImmersiveColor(c);
        }
        return null;
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

    // ----- Text gradient convenience overloads -----
    public ImmersiveMessage gradient(int startRgb, int endRgb) {
        return gradient(TextColor.fromRgb(startRgb), TextColor.fromRgb(endRgb));
    }
    public ImmersiveMessage gradient(String start, String end) {
        TextColor s = parseColor(start);
        TextColor e = parseColor(end);
        if (s != null && e != null) return gradient(s, e);
        return this;
    }
    public ImmersiveMessage gradient(TextColor start, TextColor end) {
        if (start == null || end == null) return this;
        return gradient(new TextColor[]{start, end});
    }
    public ImmersiveMessage gradient(int... rgbs) {
        TextColor[] arr = new TextColor[rgbs.length];
        for (int i = 0; i < rgbs.length; i++) arr[i] = TextColor.fromRgb(rgbs[i]);
        return gradient(arr);
    }
    public ImmersiveMessage gradient(String... values) {
        TextColor[] arr = new TextColor[values.length];
        for (int i = 0; i < values.length; i++) arr[i] = parseColor(values[i]);
        return gradient(arr);
    }
    public ImmersiveMessage gradient(List<TextColor> colors) {
        return gradient(colors.toArray(new TextColor[0]));
    }
    public ImmersiveMessage gradient(TextColor... colors) {
        if (colors == null || colors.length < 2) return this;
        this.gradientStops = colors;
        buildGradientColors();
        rebuildGradientCurrent();
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
        rebuildGradientCurrent();
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
            rebuildGradientCurrent();
        }
        return this;
    }

    public ImmersiveMessage shake(ShakeType type, float strength) {
        if (type != null && strength > 0f) {
            this.shake = true;
            this.shakeType = type;
            this.shakeStrength = strength;
        } else {
            this.shake = false;
            this.shakeStrength = 0f;
            if (type != null) this.shakeType = type;
        }
        return this;
    }

    public ImmersiveMessage charShake(ShakeType type, float strength) {
        if (type != null && strength > 0f) {
            this.charShake = true;
            this.charShakeType = type;
            this.charShakeStrength = strength;
        } else {
            this.charShake = false;
            this.charShakeStrength = 0f;
            if (type != null) this.charShakeType = type;
        }
        return this;
    }

    private static TextColor parseColor(String value) {
        if (value == null) return null;
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) return TextColor.fromLegacyFormat(fmt);
        return TextColor.parseColor(value);
    }

    private static ImmersiveColor parseImmersiveColor(String value) {
        TextColor c = parseColor(value);
        return c != null ? new ImmersiveColor(0xFF000000 | c.getValue()) : null;
    }

    private void buildGradientColors() {
        String str = text.getString();
        gradientColors = new TextColor[str.length()];
        if (gradientStops == null || gradientStops.length < 2) return;

        int segments = gradientStops.length - 1;
        for (int i = 0; i < str.length(); i++) {
            float t = str.length() <= 1 ? 0f : i / (float) (str.length() - 1);
            float scaled = t * segments;
            int idx = Mth.clamp((int) Math.floor(scaled), 0, segments - 1);
            float local = scaled - idx;
            int start = gradientStops[idx].getValue();
            int end = gradientStops[idx + 1].getValue();
            int rgb = lerpColor(start, end, local);
            gradientColors[i] = TextColor.fromRgb(rgb);
        }
    }

    private static int lerpColor(int start, int end, float t) {
        int sr = (start >> 16) & 0xFF;
        int sg = (start >> 8) & 0xFF;
        int sb = start & 0xFF;
        int er = (end >> 16) & 0xFF;
        int eg = (end >> 8) & 0xFF;
        int eb = end & 0xFF;
        int r = (int) Mth.lerp(t, sr, er);
        int g = (int) Mth.lerp(t, sg, eg);
        int b = (int) Mth.lerp(t, sb, eb);
        return (r << 16) | (g << 8) | b;
    }

    private void rebuildGradientCurrent() {
        if (obfuscateMode != ObfuscateMode.NONE && baseText != null) {
            rebuildObfuscation();
        } else if (typewriter) {
            current = buildGradientComponent(typewriterIndex);
        } else {
            current = buildGradientComponent(text.getString().length());
        }
    }

    private MutableComponent buildGradientComponent(int limit) {
        MutableComponent result = Component.literal("");
        String str = text.getString();
        int end = Math.min(limit, str.length());
        for (int i = 0; i < end; i++) {
            char c = str.charAt(i);
            MutableComponent ch = Component.literal(String.valueOf(c)).withStyle(text.getStyle());
            if (gradientColors != null && i < gradientColors.length) {
                TextColor col = gradientColors[i];
                if (col != null) ch = ch.withStyle(s -> s.withColor(col));
            }
            result.append(ch);
        }
        return result;
    }

    private void initObfuscation() {
        baseText = text.getString();
        revealMask = new boolean[baseText.length()];
        revealOrder = new ArrayList<>(baseText.length());
        for (int i = 0; i < baseText.length(); i++) revealOrder.add(i);
        switch (obfuscateMode) {
            case RIGHT -> Collections.reverse(revealOrder);
            case CENTER -> {
                revealOrder.clear();
                int left = (baseText.length() - 1) / 2;
                int right = baseText.length() / 2;
                while (left >= 0 || right < baseText.length()) {
                    if (left >= 0) revealOrder.add(left--);
                    if (right < baseText.length()) revealOrder.add(right++);
                }
            }
            case RANDOM -> Collections.shuffle(revealOrder, random);
            default -> { /* LEFT default */ }
        }
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
            if (gradientColors != null && i < gradientColors.length) {
                TextColor col = gradientColors[i];
                if (col != null) ch = ch.withStyle(s -> s.withColor(col));
            }
            if (!revealMask[i]) ch = ch.withStyle(s -> s.withObfuscated(true));
            result.append(ch);
        }
        current = result;
    }

    // ----- Network codec -----
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

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
        if (gradientStops != null) {
            ListTag list = new ListTag();
            for (TextColor color : gradientStops) {
                if (color != null) {
                    list.add(IntTag.valueOf(color.getValue()));
                }
            }
            if (!list.isEmpty()) {
                tag.put("Gradient", list);
            }
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
        tag.putBoolean("Shake", shake);
        tag.putString("ShakeType", shakeType.name());
        tag.putFloat("ShakeStrength", shakeStrength);
        tag.putBoolean("CharShake", charShake);
        tag.putString("CharShakeType", charShakeType.name());
        tag.putFloat("CharShakeStrength", charShakeStrength);
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
                msg.align = TextAnchor.valueOf(tag.getString("Align"));
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
                msg.backgroundTexture = rl != null ? rl : ResourceLocation.fromNamespaceAndPath("minecraft", "missingno");
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
        if (tag.contains("Gradient")) {
            ListTag list = tag.getList("Gradient", Tag.TAG_INT);
            TextColor[] cols = new TextColor[list.size()];
            for (int i = 0; i < list.size(); i++) {
                cols[i] = TextColor.fromRgb(((IntTag) list.get(i)).getAsInt());
            }
            msg.gradient(cols);
        }
        if (tag.contains("BackgroundGradient")) {
            ListTag list = tag.getList("BackgroundGradient", Tag.TAG_INT);
            ImmersiveColor[] cols = new ImmersiveColor[list.size()];
            for (int i = 0; i < list.size(); i++) {
                cols[i] = new ImmersiveColor(((IntTag) list.get(i)).getAsInt());
            }
            msg.backgroundGradient(cols);
        }
        if (tag.contains("Shake")) msg.shake = tag.getBoolean("Shake");
        if (tag.contains("ShakeType")) {
            try {
                msg.shakeType = ShakeType.valueOf(tag.getString("ShakeType"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (tag.contains("ShakeStrength")) msg.shakeStrength = tag.getFloat("ShakeStrength");
        if (tag.contains("CharShake")) msg.charShake = tag.getBoolean("CharShake");
        if (tag.contains("CharShakeType")) {
            try {
                msg.charShakeType = ShakeType.valueOf(tag.getString("CharShakeType"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (tag.contains("CharShakeStrength")) msg.charShakeStrength = tag.getFloat("CharShakeStrength");
        if (tag.contains("WrapWidth")) msg.wrapMaxWidth = tag.getInt("WrapWidth");
        if (tag.contains("Delay")) msg.delay = tag.getFloat("Delay");
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

        // Text gradient stops
        buf.writeBoolean(gradientStops != null);
        if (gradientStops != null) {
            buf.writeVarInt(gradientStops.length);
            for (TextColor c : gradientStops) buf.writeInt(c.getValue());
        }

        // Background gradient stops
        buf.writeBoolean(backgroundGradientStops != null);
        if (backgroundGradientStops != null) {
            buf.writeVarInt(backgroundGradientStops.length);
            for (ImmersiveColor c : backgroundGradientStops) buf.writeInt(c.getARGB());
        }

        // Shake
        buf.writeBoolean(shake);
        buf.writeEnum(shakeType);
        buf.writeFloat(shakeStrength);
        buf.writeFloat(shakeSpeed);
        // Per-char shake
        buf.writeBoolean(charShake);
        buf.writeEnum(charShakeType);
        buf.writeFloat(charShakeStrength);
        buf.writeFloat(charShakeSpeed);
        buf.writeInt(wrapMaxWidth);
        buf.writeFloat(delay);
        buf.writeVarInt(fadeInTicks);
        buf.writeVarInt(fadeOutTicks);
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
        msg.align = buf.readEnum(TextAnchor.class);
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

        // Text gradient stops
        if (buf.readBoolean()) {
            int count = buf.readVarInt();
            TextColor[] cols = new TextColor[count];
            for (int i = 0; i < count; i++) cols[i] = TextColor.fromRgb(buf.readInt());
            msg.gradient(cols);
        }

        // Background gradient stops
        if (buf.readBoolean()) {
            int count = buf.readVarInt();
            ImmersiveColor[] cols = new ImmersiveColor[count];
            for (int i = 0; i < count; i++) cols[i] = new ImmersiveColor(buf.readInt());
            msg.backgroundGradient(cols);
        }

        // Shake
        msg.shake = buf.readBoolean();
        msg.shakeType = buf.readEnum(ShakeType.class);
        msg.shakeStrength = buf.readFloat();
        msg.shakeSpeed = buf.readFloat();
        // Per-char shake
        msg.charShake = buf.readBoolean();
        msg.charShakeType = buf.readEnum(ShakeType.class);
        msg.charShakeStrength = buf.readFloat();
        msg.charShakeSpeed = buf.readFloat();
        msg.wrapMaxWidth = buf.readInt();
        msg.delay = buf.readFloat();
        if (buf.isReadable()) {
            msg.fadeInTicks = Math.max(0, buf.readVarInt());
        }
        if (buf.isReadable()) {
            msg.fadeOutTicks = Math.max(0, buf.readVarInt());
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
        int alpha = Mth.clamp((int)(computeAlpha(age) * 255f), 0, 255);
        return (alpha << 24) | base;
    }

    /**
     * Computes the ARGB colour for rendering with interpolation between the previous and current age values.
     *
     * @param partialTick render partial tick, typically between {@code 0} and {@code 1}.
     * @return colour with fade alpha applied for the provided partial tick.
     */
    public int renderColour(float partialTick) {
        int base = text.getStyle().getColor() != null ? text.getStyle().getColor().getValue() : 0xFFFFFF;
        int alpha = Mth.clamp((int)(computeAlpha(sampleAge(partialTick)) * 255f), 0, 255);
        return (alpha << 24) | base;
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

            // Handle gradients by building character-by-character
            if (span.getGradientColors() != null && span.getGradientColors().length >= 2) {
                spanComponent = buildGradientComponent(span, content);
            } else {
                // Simple span with single styling
                spanComponent = Component.literal(content);
                applySpanStyling(spanComponent, span);
            }

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
     * Builds a gradient component for a span character by character.
     */
    private MutableComponent buildGradientComponent(TextSpan span) {
        return buildGradientComponent(span, span.getContent());
    }

    /**
     * Builds a gradient component for a span with custom content (for typewriter effects).
     */
    private MutableComponent buildGradientComponent(TextSpan span, String content) {
        TextColor[] gradientColors = span.getGradientColors();

        MutableComponent result = Component.literal("");

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            MutableComponent charComponent = Component.literal(String.valueOf(c));

            // Apply base styling (bold, italic, etc.)
            applySpanStyling(charComponent, span);

            // Apply gradient color for this character
            TextColor gradColor = computeGradientColor(gradientColors, i, content.length());
            if (gradColor != null) {
                charComponent = charComponent.withStyle(style -> style.withColor(gradColor));
            }

            result.append(charComponent);
        }

        return result;
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

    /**
     * Computes gradient color for a character at a specific index.
     */
    private TextColor computeGradientColor(TextColor[] gradientStops, int index, int totalLength) {
        if (gradientStops.length < 2 || totalLength <= 1) return gradientStops[0];

        float t = totalLength <= 1 ? 0f : index / (float) (totalLength - 1);
        int segments = gradientStops.length - 1;
        float scaled = t * segments;
        int segIndex = Mth.clamp((int) Math.floor(scaled), 0, segments - 1);
        float local = scaled - segIndex;

        int start = gradientStops[segIndex].getValue();
        int end = gradientStops[segIndex + 1].getValue();
        int rgb = lerpColor(start, end, local);
        return TextColor.fromRgb(rgb);
    }

    private float computeAlpha(float sampleAge) {
        float fadeIn = this.fadeInTicks;
        float fadeOut = this.fadeOutTicks;
        float visibleEnd = fadeIn + duration;
        float total = visibleEnd + fadeOut;

        float alpha;
        if (fadeIn > 0f && sampleAge <= fadeIn) {
            // Ensure we start at exactly 0 alpha when sampleAge is 0
            alpha = Math.max(0f, sampleAge) / Math.max(1f, fadeIn);
        } else if (sampleAge < visibleEnd || (duration <= 0f && fadeIn == 0f && fadeOut == 0f)) {
            alpha = 1f;
        } else if (fadeOut > 0f && sampleAge < total) {
            float fadeProgress = sampleAge - visibleEnd;
            alpha = 1f - (fadeProgress / Math.max(1f, fadeOut));
        } else {
            alpha = 0f;
        }
        return Mth.clamp(alpha, 0f, 1f);
    }

    /**
     * Computes alpha for a specific span with per-span fade effects.
     */
    private float computeSpanAlpha(TextSpan span, float sampleAge) {
        Integer spanFadeIn = span.getFadeInTicks();
        Integer spanFadeOut = span.getFadeOutTicks();

        // If span has no fade effects, use global alpha
        if (spanFadeIn == null && spanFadeOut == null) {
            return computeAlpha(sampleAge);
        }

        // Use span-specific fade timing
        float fadeIn = spanFadeIn != null ? spanFadeIn : 0f;
        float fadeOut = spanFadeOut != null ? spanFadeOut : 0f;
        float visibleEnd = fadeIn + duration;
        float total = visibleEnd + fadeOut;

        float alpha;
        if (fadeIn > 0f && sampleAge < fadeIn) {
            alpha = sampleAge / Math.max(1f, fadeIn);
        } else if (sampleAge < visibleEnd || (duration <= 0f && fadeIn == 0f && fadeOut == 0f)) {
            alpha = 1f;
        } else if (fadeOut > 0f && sampleAge < total) {
            float fadeProgress = sampleAge - visibleEnd;
            alpha = 1f - (fadeProgress / Math.max(1f, fadeOut));
        } else {
            alpha = 0f;
        }

        // Combine with global alpha (for overall message fade)
        float globalAlpha = computeAlpha(sampleAge);
        return Mth.clamp(alpha * globalAlpha, 0f, 1f);
    }

    private float sampleAge(float partialTick) {
        float clamped = Mth.clamp(partialTick, 0f, 1f);
        return Mth.lerp(clamped, previousAge, age);
    }

    public TextLayoutCache.Layout buildLayout(Component draw) {
        var font = Minecraft.getInstance().font;
        var caxtonHandler = CaxtonCompat.getHandler();
        List<FormattedCharSequence> lines = null;
        int baseWidth;
        int baseHeight;
        if (wrapMaxWidth > 0) {
            lines = font.split(draw, wrapMaxWidth);
            float maxWidth = 0f;
            for (FormattedCharSequence line : lines) {
                float width = font.getSplitter().stringWidth(line);
                if (caxtonHandler != null) {
                    float caxtonWidth = caxtonHandler.getWidth(line);
                    if (!Float.isNaN(caxtonWidth)) {
                        width = caxtonWidth;
                    }
                }
                maxWidth = Math.max(maxWidth, width);
            }
            baseWidth = Mth.ceil(maxWidth);
            baseHeight = lines.size() * font.lineHeight;
        } else {
            FormattedCharSequence sequence = draw.getVisualOrderText();
            float width = font.getSplitter().stringWidth(sequence);
            if (caxtonHandler != null) {
                float caxtonWidth = caxtonHandler.getWidth(sequence);
                if (!Float.isNaN(caxtonWidth)) {
                    width = caxtonWidth;
                }
            }
            baseWidth = Mth.ceil(width);
            baseHeight = font.lineHeight;
        }
        return new TextLayoutCache.Layout(lines, draw.getVisualOrderText(), baseWidth, baseHeight);
    }

    public void renderWithLayout(GuiGraphics graphics, Component draw, TextLayoutCache.Layout layout, int screenW, int screenH, float partialTick) {
        var font = Minecraft.getInstance().font;
        var caxtonHandler = CaxtonCompat.getHandler();
        List<FormattedCharSequence> lines = layout.lines();
        int baseWidth = layout.width();
        int baseHeight = layout.height();

        float charPadding = charShake ? charShakeStrength : 0f;
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
        float y = screenH * anchor.yFactor - baseHeight * textScale * align.yFactor + yOffset;

        float renderAge = sampleAge(partialTick);

        // Skip rendering entirely if we have fade-in and haven't started yet
        if (fadeInTicks > 0 && renderAge <= 0f) {
            graphics.pose().pushPose();
            graphics.pose().popPose();
            return;
        }

        if (shake) {
            float sx = 0f, sy = 0f;
            // Use tick-based timing for frame-rate independence (20 TPS = 0.05 seconds per tick)
            float shakeTime = renderAge * 0.05f * shakeSpeed;
            switch (shakeType) {
                case WAVE -> sy = (float) Math.sin(shakeTime * 2 * Math.PI / shakeWavelength) * shakeStrength;
                case CIRCLE -> {
                    sx = (float) Math.cos(shakeTime) * shakeStrength;
                    sy = (float) Math.sin(shakeTime) * shakeStrength;
                }
                case RANDOM -> {
                    sx = (random.nextFloat() - 0.5f) * 2f * shakeStrength;
                    sy = (random.nextFloat() - 0.5f) * 2f * shakeStrength;
                }
            }
            x += sx;
            y += sy;
        }

        float alpha = computeAlpha(renderAge);
        int colour = ((int)(alpha * 255) << 24) | (text.getStyle().getColor() != null ? text.getStyle().getColor().getValue() : 0xFFFFFF);

        if (typewriter && typewriterCenter && wrapMaxWidth <= 0) {
            float fullWidth = font.width(text);
            if (caxtonHandler != null) {
                float caxtonWidth = caxtonHandler.getWidth(text.getVisualOrderText());
                if (!Float.isNaN(caxtonWidth)) {
                    fullWidth = caxtonWidth;
                }
            }
            float currentWidth = font.width(draw);
            if (caxtonHandler != null) {
                float caxtonWidth = caxtonHandler.getWidth(draw.getVisualOrderText());
                if (!Float.isNaN(caxtonWidth)) {
                    currentWidth = caxtonWidth;
                }
            }
            x += (fullWidth - currentWidth) / 2f * textScale;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(x - textStartX * textScale, y - textStartY * textScale, 0);
        graphics.pose().scale(textScale, textScale, 1f);
        if (background) {
            int start = (Math.min(255, (int)(borderStart.getAlpha() * alpha)) << 24) | borderStart.getRGB();
            int end = (Math.min(255, (int)(borderEnd.getAlpha() * alpha)) << 24) | borderEnd.getRGB();
            int widthForBg = shake ? backgroundWidthInt + (int)(shakeStrength * 4f) : backgroundWidthInt;

            if (useTextureBackground && backgroundTexture != null) {
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                if (textureSizingMode == TextureSizingMode.STRETCH) {
                    graphics.blit(backgroundTexture, 0, 0, widthForBg, backgroundHeightInt, textureU, textureV, textureWidth, textureHeight, textureAtlasWidth, textureAtlasHeight);
                } else {
                    int drawWidth = Math.min(widthForBg, textureWidth);
                    int drawHeight = Math.min(backgroundHeightInt, textureHeight);
                    int destX = Math.max(0, (widthForBg - drawWidth) / 2);
                    int destY = Math.max(0, (backgroundHeightInt - drawHeight) / 2);
                    int uOffset = textureU;
                    int vOffset = textureV;
                    if (drawWidth < textureWidth) {
                        uOffset += (textureWidth - drawWidth) / 2;
                    }
                    if (drawHeight < textureHeight) {
                        vOffset += (textureHeight - drawHeight) / 2;
                    }
                    graphics.blit(backgroundTexture, destX, destY, drawWidth, drawHeight, uOffset, vOffset, drawWidth, drawHeight, textureAtlasWidth, textureAtlasHeight);
                }
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
            } else if (backgroundGradientStops != null) {
                int[] cols = new int[backgroundGradientStops.length];
                for (int i = 0; i < backgroundGradientStops.length; i++) {
                    ImmersiveColor c = backgroundGradientStops[i];
                    int a = Math.min(255, (int)(c.getAlpha() * alpha));
                    cols[i] = (a << 24) | c.getRGB();
                }
                RenderUtil.drawBackgroundGradient(graphics, 0, 0, widthForBg, backgroundHeightInt, cols, start, end);
            } else {
                int bg = (Math.min(255, (int)(backgroundColor.getAlpha() * alpha)) << 24) | backgroundColor.getRGB();
                RenderUtil.drawBackground(graphics, 0, 0, widthForBg, backgroundHeightInt, bg, start, end);
            }
        }
        if (onRender != null) {
            onRender.render(graphics, this, 0, 0, alpha);
        } else if (charShake) {
            renderCharShake(graphics, lines, draw, colour, textStartX, textStartY);
        } else if (spanMode && spans != null && hasItemSpans()) {
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
            int next = (int)(age * typewriterSpeed);
            if (next > typewriterIndex) {
                typewriterIndex = Math.min(next, text.getString().length());
                if (obfuscateMode != ObfuscateMode.NONE) {
                    rebuildObfuscation();
                } else if (gradientColors != null) {
                    current = buildGradientComponent(typewriterIndex);
                } else {
                    current = Component.literal(text.getString().substring(0, typewriterIndex))
                            .withStyle(text.getStyle());
                }
            }

            // Handle per-span typewriter (span mode)
            if (spanMode && spans != null && spanTypewriterIndices != null) {
                // Check if ANY span has its own typewriter speed (independent typewriter)
                boolean hasIndependentTypewriter = spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null);

                if (hasIndependentTypewriter) {
                    // Original behavior: Each span with typewriter animates independently
                    for (int i = 0; i < spans.size(); i++) {
                        TextSpan span = spans.get(i);
                        if (span.getTypewriterSpeed() != null) {
                            int spanNext = (int)(age * span.getTypewriterSpeed());
                            spanTypewriterIndices[i] = Math.min(spanNext, span.getContent().length());
                        } else {
                            spanTypewriterIndices[i] = span.getContent().length(); // Show all if no typewriter
                        }
                    }
                } else {
                    // NEW: Container-based typewriter - count chars across all spans
                    // Use global typewriter speed and reveal chars sequentially across spans
                    int totalCharsToShow = Math.min(next, getFullText().length());
                    int charsShown = 0;

                    for (int i = 0; i < spans.size(); i++) {
                        TextSpan span = spans.get(i);
                        int spanLength = span.getContent().length();

                        if (charsShown + spanLength <= totalCharsToShow) {
                            // Show entire span
                            spanTypewriterIndices[i] = spanLength;
                            charsShown += spanLength;
                        } else if (charsShown < totalCharsToShow) {
                            // Partially show this span
                            spanTypewriterIndices[i] = totalCharsToShow - charsShown;
                            charsShown = totalCharsToShow;
                        } else {
                            // Don't show this span yet
                            spanTypewriterIndices[i] = 0;
                        }
                    }
                }
            }
        }

        // Obfuscation progression
        if (obfuscateMode != ObfuscateMode.NONE) tickObfuscation(delta);
    }

    private void tickObfuscation(float delta) {
        if (baseText == null || revealIndex >= revealOrder.size()) return;

        obfuscateProgress += obfuscateSpeed * delta;
        int revealCount = Math.min((int) obfuscateProgress, revealOrder.size() - revealIndex);
        if (revealCount <= 0) return;

        int revealed = 0;
        for (int i = 0; i < revealCount; i++) {
            int idx = revealOrder.get(revealIndex);
            if (typewriter && idx >= typewriterIndex) break;
            revealMask[idx] = true;
            revealIndex++;
            revealed++;
        }
        if (revealed <= 0) return;
        obfuscateProgress -= revealed;
        rebuildObfuscation();
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
                        net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemLocation);
                        if (item != null) {
                            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, span.getItemCount() != null ? span.getItemCount() : 1);

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
                // Render entity (static; no animations)
                try {
                    ResourceLocation entityLocation = ResourceLocation.tryParse(span.getEntityId());
                    if (entityLocation != null) {
                        net.minecraft.world.entity.EntityType<?> entityType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(entityLocation);
                        if (entityType != null) {
                            // Create a dummy entity for rendering
                            net.minecraft.world.entity.Entity entity = entityType.create(mc.level);
                            if (entity != null) {
                                float entityScale = span.getEntityScale() != null ? span.getEntityScale() : 1.0f;
                                int entitySize = (int)(16 * entityScale); // Base size scaled

                                // Center entity vertically with text
                                float entityYOffset = yOffset - (entitySize - font.lineHeight) / 2.0f;

                                // Apply custom offsets if specified
                                float customOffsetX = span.getEntityOffsetX() != null ? span.getEntityOffsetX() : 0f;
                                float customOffsetY = span.getEntityOffsetY() != null ? span.getEntityOffsetY() : 0f;

                                // Get rotation values (defaults: yaw=45, pitch=0)
                                float yaw = span.getEntityYaw() != null ? span.getEntityYaw() : 45f;
                                float pitch = span.getEntityPitch() != null ? span.getEntityPitch() : 0f;

                                graphics.pose().pushPose();
                                graphics.pose().translate(xOffset + customOffsetX + entitySize / 2.0f, entityYOffset + customOffsetY + entitySize, 100); // Z=100 for depth
                                graphics.pose().scale(entityScale * 10, entityScale * 10, entityScale * 10);
                                graphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(180)); // Flip upright
                                graphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(180 + yaw)); // Yaw rotation (180 offset so 0=front)
                                graphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitch)); // Pitch rotation

                                // Render the entity
                                var entityRenderDispatcher = mc.getEntityRenderDispatcher();
                                entityRenderDispatcher.render(entity, 0, 0, 0, 0, 0, graphics.pose(), mc.renderBuffers().bufferSource(), 15728880);
                                mc.renderBuffers().bufferSource().endBatch();

                                graphics.pose().popPose();

                                xOffset += entitySize + 2; // Add spacing after entity
                            }
                        }
                    }
                } catch (Exception e) {
                    // If entity rendering fails, just skip it
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

                    // Apply gradient if needed
                    if (span.getGradientColors() != null && span.getGradientColors().length >= 2) {
                        spanComponent = buildGradientComponent(span, content);
                    }

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

    private void renderCharShake(GuiGraphics graphics, List<FormattedCharSequence> lines, Component draw, int colour, float baseX, float baseY) {
        var font = Minecraft.getInstance().font;
        var handler = CaxtonCompat.getHandler();
        int[] index = {0};

        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                final float lineBaseY = baseY + i * font.lineHeight;
                final float[] xAdvance = {baseX};
                FormattedCharSequence lineSeq = lines.get(i);
                lineSeq.accept((pos, style, codePoint) -> {
                    String ch = new String(Character.toChars(codePoint));
                    float sx = 0f, sy = 0f;
                    switch (charShakeType) {
                        // Use tick-based timing for frame-rate independence
                        case WAVE -> {
                            float charShakeTime = age * 0.05f * charShakeSpeed + index[0] * 0.1f;
                            sy = (float) Math.sin(charShakeTime * 2 * Math.PI / charShakeWavelength) * charShakeStrength;
                        }
                        case CIRCLE -> {
                            float charShakeTime = age * 0.05f * charShakeSpeed + index[0] * 0.1f;
                            sx = (float) Math.cos(charShakeTime) * charShakeStrength;
                            sy = (float) Math.sin(charShakeTime) * charShakeStrength;
                        }
                        case RANDOM -> {
                            sx = (random.nextFloat() - 0.5f) * 2f * charShakeStrength;
                            sy = (random.nextFloat() - 0.5f) * 2f * charShakeStrength;
                        }
                    }
                    Component comp = Component.literal(ch).withStyle(style);
                    FormattedCharSequence charSeq = comp.getVisualOrderText();
                    float cw = font.width(charSeq);
                    if (handler != null) {
                        float caxtonWidth = handler.getWidth(charSeq);
                        if (!Float.isNaN(caxtonWidth)) {
                            cw = caxtonWidth;
                        }
                    }
                    graphics.pose().pushPose();
                    graphics.pose().translate(xAdvance[0] + sx, lineBaseY + sy, 0);
                    graphics.drawString(font, charSeq, 0, 0, colour, shadow);
                    graphics.pose().popPose();
                    xAdvance[0] += cw;
                    index[0]++;
                    return true;
                });
            }
        } else {
            final float[] xAdvance = {baseX};
            draw.getVisualOrderText().accept((pos, style, codePoint) -> {
                String ch = new String(Character.toChars(codePoint));
                float sx = 0f, sy = 0f;
                switch (charShakeType) {
                    // Use tick-based timing for frame-rate independence
                    case WAVE -> {
                        float charShakeTime = age * 0.05f * charShakeSpeed + index[0] * 0.1f;
                        sy = (float) Math.sin(charShakeTime * 2 * Math.PI / charShakeWavelength) * charShakeStrength;
                    }
                    case CIRCLE -> {
                        float charShakeTime = age * 0.05f * charShakeSpeed + index[0] * 0.1f;
                        sx = (float) Math.cos(charShakeTime) * charShakeStrength;
                        sy = (float) Math.sin(charShakeTime) * charShakeStrength;
                    }
                    case RANDOM -> {
                        sx = (random.nextFloat() - 0.5f) * 2f * charShakeStrength;
                        sy = (random.nextFloat() - 0.5f) * 2f * charShakeStrength;
                    }
                }
                Component comp = Component.literal(ch).withStyle(style);
                FormattedCharSequence charSeq = comp.getVisualOrderText();
                float cw = font.width(charSeq);
                if (handler != null) {
                    float caxtonWidth = handler.getWidth(charSeq);
                    if (!Float.isNaN(caxtonWidth)) {
                        cw = caxtonWidth;
                    }
                }
                graphics.pose().pushPose();
                graphics.pose().translate(xAdvance[0] + sx, baseY + sy, 0);
                graphics.drawString(font, charSeq, 0, 0, colour, shadow);
                graphics.pose().popPose();
                xAdvance[0] += cw;
                index[0]++;
                return true;
            });
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
