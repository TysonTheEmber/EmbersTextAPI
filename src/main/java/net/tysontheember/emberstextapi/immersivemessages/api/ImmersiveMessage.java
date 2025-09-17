package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.ModList;
import net.tysontheember.emberstextapi.immersivemessages.util.CaxtonCompat;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import net.tysontheember.emberstextapi.immersivemessages.util.RenderUtil;
import xyz.flirora.caxton.render.CaxtonTextRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * More feature rich port of the original Immersive Messages class.
 * It supports anchor/align positioning, backgrounds, typewriter
 * animations and progressive de-obfuscation. Audio cues have been
 * intentionally omitted so the API focuses purely on text rendering.
 */
public class ImmersiveMessage {
    private final Component text;
    private final float duration;
    private float age;
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

    // Per-character shake
    private boolean charShake = false;
    private float charShakeStrength = 0f;
    private ShakeType charShakeType = ShakeType.RANDOM;

    private OnRenderMessage onRender;
    private final Random random = new Random();

    public ImmersiveMessage(Component text, float duration) {
        this.text = text;
        this.duration = duration;
    }

    /** Builder entry point. */
    public static ImmersiveMessage builder(float duration, String text) {
        return new ImmersiveMessage(Component.literal(text), duration);
    }

    // ----- Builder style setters -----
    public ImmersiveMessage shadow(boolean shadow) { this.shadow = shadow; return this; }
    public ImmersiveMessage anchor(TextAnchor anchor) { this.anchor = anchor; return this; }
    public ImmersiveMessage align(TextAnchor align) { this.align = align; return this; }
    public ImmersiveMessage offset(float x, float y) { this.xOffset = x; this.yOffset = y; return this; }

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
    public void encode(FriendlyByteBuf buf) {
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
        // Per-char shake
        buf.writeBoolean(charShake);
        buf.writeEnum(charShakeType);
        buf.writeFloat(charShakeStrength);
        buf.writeInt(wrapMaxWidth);
        buf.writeFloat(delay);
    }

    public static ImmersiveMessage decode(FriendlyByteBuf buf) {
        Component text = buf.readComponent();
        float duration = buf.readFloat();
        ImmersiveMessage msg = new ImmersiveMessage(text, duration);
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
        // Per-char shake
        msg.charShake = buf.readBoolean();
        msg.charShakeType = buf.readEnum(ShakeType.class);
        msg.charShakeStrength = buf.readFloat();
        msg.wrapMaxWidth = buf.readInt();
        msg.delay = buf.readFloat();
        if (msg.obfuscateMode != ObfuscateMode.NONE) msg.initObfuscation();
        return msg;
    }

    // ----- Runtime behaviour -----
    public void tick(float delta) {
        age += delta;

        // Typewriter progression
        if (typewriter) {
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

    public boolean isFinished() { return age >= duration; }

    public float getDelay() { return delay; }

    public void render(GuiGraphics graphics) {
        Component draw = typewriter ? current : (current.getString().isEmpty() ? text : current);
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
//            float width = caxtonHandler != null ? caxtonHandler.getWidth(draw.getVisualOrderText()) : font.getSplitter().stringWidth(draw.getVisualOrderText());
            float width = font.getSplitter().stringWidth(draw.getVisualOrderText());
            if (caxtonHandler != null) {
                float caxtonWidth = caxtonHandler.getWidth(draw.getVisualOrderText());
                if (!Float.isNaN(caxtonWidth)) {
                    width = caxtonWidth;
                }
            }
            baseWidth = Mth.ceil(width);
            baseHeight = font.lineHeight;
        }

        float padding = charShake ? charShakeStrength : 0f;
        int textWidth = baseWidth + (int) Math.ceil(padding * 2f);
        int textHeight = baseHeight + (int) Math.ceil(padding * 2f);

        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        float x = screenW * anchor.xFactor - baseWidth * textScale * align.xFactor + xOffset;
        float y = screenH * anchor.yFactor - baseHeight * textScale * align.yFactor + yOffset;

        if (shake) {
            float sx = 0f, sy = 0f;
            switch (shakeType) {
                case WAVE -> sy = (float) Math.sin(age * 10f) * shakeStrength;
                case CIRCLE -> {
                    sx = (float) Math.cos(age * 10f) * shakeStrength;
                    sy = (float) Math.sin(age * 10f) * shakeStrength;
                }
                case RANDOM -> {
                    sx = (random.nextFloat() - 0.5f) * 2f * shakeStrength;
                    sy = (random.nextFloat() - 0.5f) * 2f * shakeStrength;
                }
            }
            x += sx;
            y += sy;
        }

        float fadeTime = Math.min(10f, duration * 0.25f);
        float alpha = 1f;
        if (age < fadeTime) alpha = age / fadeTime;
        else if (age > duration - fadeTime) alpha = (duration - age) / fadeTime;
        alpha = Mth.clamp(alpha, 0f, 1f);
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
        graphics.pose().translate(x - padding * textScale, y - padding * textScale, 0);
        graphics.pose().scale(textScale, textScale, 1f);
        if (background) {
            int start = (Math.min(255, (int)(borderStart.getAlpha() * alpha)) << 24) | borderStart.getRGB();
            int end = (Math.min(255, (int)(borderEnd.getAlpha() * alpha)) << 24) | borderEnd.getRGB();
            int widthForBg = shake ? textWidth + 200 : textWidth;

            if (useTextureBackground && backgroundTexture != null) {
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                graphics.blit(backgroundTexture, 0, 0, widthForBg, textHeight, textureU, textureV, textureWidth, textureHeight, textureAtlasWidth, textureAtlasHeight);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
            } else if (backgroundGradientStops != null) {
                int[] cols = new int[backgroundGradientStops.length];
                for (int i = 0; i < backgroundGradientStops.length; i++) {
                    ImmersiveColor c = backgroundGradientStops[i];
                    int a = Math.min(255, (int)(c.getAlpha() * alpha));
                    cols[i] = (a << 24) | c.getRGB();
                }
                RenderUtil.drawBackgroundGradient(graphics, 0, 0, widthForBg, textHeight, cols, start, end);
            } else {
                int bg = (Math.min(255, (int)(backgroundColor.getAlpha() * alpha)) << 24) | backgroundColor.getRGB();
                RenderUtil.drawBackground(graphics, 0, 0, widthForBg, textHeight, bg, start, end);
            }
        }
        if (onRender != null) {
            onRender.render(graphics, this, 0, 0, alpha);
        } else if (charShake) {
            renderCharShake(graphics, lines, draw, colour, padding);
        } else if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                graphics.drawString(font, lines.get(i), 0, i * font.lineHeight, colour, shadow);
            }
        } else {
            graphics.drawString(font, draw, 0, 0, colour, shadow);
        }
        graphics.pose().popPose();
    }

    private void renderCharShake(GuiGraphics graphics, List<FormattedCharSequence> lines, Component draw, int colour, float padding) {
        var font = Minecraft.getInstance().font;
        var handler = CaxtonCompat.getHandler();
        int[] index = {0};

        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                final float baseY = padding + i * font.lineHeight;
                final float[] xAdvance = {padding};
                FormattedCharSequence lineSeq = lines.get(i);
                lineSeq.accept((pos, style, codePoint) -> {
                    String ch = new String(Character.toChars(codePoint));
                    float sx = 0f, sy = 0f;
                    switch (charShakeType) {
                        case WAVE -> sy = (float) Math.sin(age * 10f + index[0]) * charShakeStrength;
                        case CIRCLE -> {
                            sx = (float) Math.cos(age * 10f + index[0]) * charShakeStrength;
                            sy = (float) Math.sin(age * 10f + index[0]) * charShakeStrength;
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
        } else {
            final float[] xAdvance = {padding};
            draw.getVisualOrderText().accept((pos, style, codePoint) -> {
                String ch = new String(Character.toChars(codePoint));
                float sx = 0f, sy = 0f;
                switch (charShakeType) {
                    case WAVE -> sy = (float) Math.sin(age * 10f + index[0]) * charShakeStrength;
                    case CIRCLE -> {
                        sx = (float) Math.cos(age * 10f + index[0]) * charShakeStrength;
                        sy = (float) Math.sin(age * 10f + index[0]) * charShakeStrength;
                    }
                    case RANDOM -> {
                        sx = (random.nextFloat() - 0.5f) * 2f * charShakeStrength;
                        sy = (random.nextFloat() - 0.5f) * 2f * charShakeStrength;
                    }
                }
                Component comp = Component.literal(ch).withStyle(style);
                FormattedCharSequence charSeq = comp.getVisualOrderText();
                float cw = handler != null ? handler.getWidth(charSeq) : font.width(charSeq);
                graphics.pose().pushPose();
                graphics.pose().translate(xAdvance[0] + sx, padding + sy, 0);
                graphics.drawString(font, charSeq, 0, 0, colour, shadow);
                graphics.pose().popPose();
                xAdvance[0] += cw;
                index[0]++;
                return true;
            });
        }
    }
}
