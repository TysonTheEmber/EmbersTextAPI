package net.tysontheember.emberstextapi.immersivemessages.api;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectContext;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.component.CustomData;
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
import net.tysontheember.emberstextapi.serialization.SerializationUtil;
import net.tysontheember.emberstextapi.immersivemessages.util.RenderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ImmersiveMessage {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Component text;
    private float duration;
    private float age;
    private float previousAge;

    private int fadeInTicks = 0;

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

    private ImmersiveColor[] backgroundGradientStops;

    private boolean typewriter = false;
    private float typewriterSpeed = 0.5f;
    private boolean typewriterCenter = false;
    private int typewriterIndex = 0;

    private MutableComponent current = Component.literal("");

    private ObfuscateMode obfuscateMode = ObfuscateMode.NONE;
    private float obfuscateSpeed = 0.00005f;
    private float obfuscateProgress = 0f;

    private String baseText;
    private boolean[] revealMask;
    private List<Integer> revealOrder;
    private int revealIndex = 0;

    private int wrapMaxWidth = -1;
    private float delay = 0f;

    private boolean spanCharShake = false;
    private float spanCharShakeMaxStrength = 0f;
    private List<CharShakeSegment> spanCharShakeSegments = Collections.emptyList();

    private List<TextSpan> spans;
    private boolean spanMode = false;
    private int[] spanTypewriterIndices;

    private String markupSource;
    private Component markupComponent;

    private List<Effect> globalEffects;
    private List<MessageEffect> messageEffects;
    private List<MessageAttribute> messageAttributes;
    private List<EffectSegment> spanEffectSegments = Collections.emptyList();

    private final String messageContextId;

    private OnRenderMessage onRender;
    private final Random random = new Random();

    public ImmersiveMessage(Component text, float duration) {
        this.text = text;
        this.duration = duration;
        this.messageContextId = "message:" + System.currentTimeMillis() + ":" + System.identityHashCode(this);

        net.tysontheember.emberstextapi.client.ViewStateTracker.markViewStarted(this.messageContextId);

        this.age = 0f;
        this.previousAge = 0f;
    }

    public ImmersiveMessage(List<TextSpan> spans, float duration) {
        this.spans = new ArrayList<>(spans);
        this.text = Component.literal(MarkupParser.toPlainText(spans));
        this.duration = duration;
        this.spanMode = true;
        this.spanTypewriterIndices = new int[spans.size()];

        this.messageContextId = "message:" + System.currentTimeMillis() + ":" + System.identityHashCode(this);
        net.tysontheember.emberstextapi.client.ViewStateTracker.markViewStarted(this.messageContextId);

        this.age = 0f;
        this.previousAge = 0f;

        if (spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null)) {
            this.typewriter = true;
        }

        evaluateSpanCharShake();
        buildEffectSegments();
    }

    public static ImmersiveMessage builder(float duration, String text) {
        return new ImmersiveMessage(Component.literal(text), duration);
    }

    public static ImmersiveMessage fromMarkup(float duration, String markup) {
        ParseResult parsed = MarkupParser.parseFull(markup);
        ImmersiveMessage msg = new ImmersiveMessage(parsed.spans(), duration);
        if (!parsed.messageAttributes().isEmpty()) {
            msg.messageAttributes = new ArrayList<>(parsed.messageAttributes());
            for (MessageAttribute attr : parsed.messageAttributes()) {
                attr.apply(msg);
            }
        }
        if (!parsed.messageEffects().isEmpty()) {
            msg.messageEffects = new ArrayList<>(parsed.messageEffects());
        }
        msg.markupSource = parsed.strippedMarkup();
        msg.markupComponent = Component.literal(parsed.strippedMarkup());
        return msg;
    }

    public static ImmersiveMessage fromSpans(float duration, List<TextSpan> spans) {
        return new ImmersiveMessage(spans, duration);
    }

    public ImmersiveMessage shadow(boolean shadow) { this.shadow = shadow; return this; }
    public ImmersiveMessage anchor(TextAnchor anchor) { this.anchor = anchor; return this; }
    public ImmersiveMessage align(TextAlign align) { this.align = align; return this; }
    public ImmersiveMessage offset(float x, float y) { this.xOffset = x; this.yOffset = y; return this; }

    public ImmersiveMessage fadeInTicks(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("fadeInTicks must be non-negative");
        }
        this.fadeInTicks = ticks;
        return this;
    }

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
        TextColor parsed = TextColor.parseColor(value).result().orElse(null);
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
    public ImmersiveMessage bgColor(ImmersiveColor color) { this.backgroundColor = color; this.background = true; return this; }
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

    public ImmersiveMessage addEffect(Effect effect) {
        if (this.globalEffects == null) {
            this.globalEffects = new ArrayList<>();
        }
        this.globalEffects.add(effect);
        return this;
    }

    public ImmersiveMessage effect(String tagContent) {
        if (tagContent != null && !tagContent.isEmpty()) {
            try {
                Effect effect = EffectRegistry.parseTag(tagContent);
                addEffect(effect);
            } catch (IllegalArgumentException e) {

            }
        }
        return this;
    }

    public ImmersiveMessage clearEffects() {
        if (this.globalEffects != null) {
            this.globalEffects.clear();
        }
        return this;
    }

    public List<Effect> getGlobalEffects() {
        return globalEffects;
    }

    public ImmersiveMessage messageEffect(MessageEffect effect) {
        if (this.messageEffects == null) {
            this.messageEffects = new ArrayList<>();
        }
        this.messageEffects.add(effect);
        return this;
    }

    public ImmersiveMessage clearMessageEffects() {
        if (this.messageEffects != null) {
            this.messageEffects.clear();
        }
        return this;
    }

    public List<MessageEffect> getMessageEffects() {
        return messageEffects;
    }

    private static ImmersiveColor parseImmersiveColor(String value) {
        if (value == null) return null;
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) {
            TextColor c = TextColor.fromLegacyFormat(fmt);
            if (c != null) {
                return new ImmersiveColor(0xFF000000 | c.getValue());
            }
        }
        TextColor c = TextColor.parseColor(value).result().orElse(null);
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

    public CompoundTag toNbt() {
        return toNbt(null);
    }

    public CompoundTag toNbt(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        if (markupSource != null) {
            tag.putString("Markup", markupSource);
        }

        if (provider != null) {
            try {
                tag.putString("TextJson", Component.Serializer.toJson(text, provider));
            } catch (Exception e) {
                LOGGER.warn("Failed to serialize component with provider, falling back to getString()", e);
                tag.putString("TextJson", text.getString());
            }
        } else {
            tag.putString("TextJson", text.getString());
        }

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
        if (messageEffects != null && !messageEffects.isEmpty()) {
            ListTag list = new ListTag();
            for (MessageEffect me : messageEffects) {
                list.add(StringTag.valueOf(me.serialize()));
            }
            tag.put("MessageEffects", list);
        }
        if (messageAttributes != null && !messageAttributes.isEmpty()) {
            ListTag list = new ListTag();
            for (MessageAttribute a : messageAttributes) {
                list.add(StringTag.valueOf(a.serialize()));
            }
            tag.put("MessageAttributes", list);
        }
        return tag;
    }

    public static ImmersiveMessage fromNbt(CompoundTag tag) {
        return fromNbt(tag, null);
    }

    public static ImmersiveMessage fromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        Component text = Component.literal("");

        if (tag.contains("TextJson", Tag.TAG_STRING)) {
            String json = tag.getString("TextJson");
            if (provider != null) {
                try {
                    MutableComponent parsed = Component.Serializer.fromJson(json, provider);
                    if (parsed != null) {
                        text = parsed;
                    } else {
                        text = Component.literal(json);
                    }
                } catch (Exception e) {

                    text = Component.literal(json);
                }
            } else {
                text = Component.literal(json);
            }
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

        if (tag.contains("Markup", Tag.TAG_STRING)) {
            msg.markupSource = tag.getString("Markup");
            msg.markupComponent = Component.literal(msg.markupSource);

            List<TextSpan> parsed = MarkupParser.parse(msg.markupSource);
            if (parsed != null && !parsed.isEmpty()) {
                msg.spans = new ArrayList<>(parsed);
                msg.spanMode = true;
                msg.spanTypewriterIndices = new int[parsed.size()];
                msg.evaluateSpanCharShake();
                msg.buildEffectSegments();
            }

            msg.typewriter = false;
        }

        if (tag.contains("MessageEffects", Tag.TAG_LIST)) {
            ListTag list = tag.getList("MessageEffects", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                String serialized = list.getString(i);
                try {
                    msg.messageEffect(MessageEffectRegistry.parseTag(serialized));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to decode message effect: {}", serialized, e);
                }
            }
        }

        if (tag.contains("MessageAttributes", Tag.TAG_LIST)) {
            ListTag list = tag.getList("MessageAttributes", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                String serialized = list.getString(i);
                try {
                    MessageAttribute attr = MessageAttributeRegistry.parseTag(serialized);
                    if (msg.messageAttributes == null) msg.messageAttributes = new ArrayList<>();
                    msg.messageAttributes.add(attr);
                    attr.apply(msg);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to decode message attribute: {}", serialized, e);
                }
            }
        }

        if (msg.obfuscateMode != ObfuscateMode.NONE) msg.initObfuscation();
        return msg;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(spanMode);

        if (spanMode) {

            buf.writeVarInt(spans != null ? spans.size() : 0);
            if (spans != null) {
                for (TextSpan span : spans) {
                    span.encode(buf);
                }
            }
        }

        buf.writeUtf(net.minecraft.network.chat.ComponentSerialization.CODEC
                .encodeStart(com.mojang.serialization.JsonOps.INSTANCE, text)
                .result()
                .map(Object::toString)
                .orElse("\"" + text.getString().replace("\"", "\\\"") + "\""));
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

        buf.writeBoolean(backgroundGradientStops != null);
        if (backgroundGradientStops != null) {
            buf.writeVarInt(backgroundGradientStops.length);
            for (ImmersiveColor c : backgroundGradientStops) buf.writeInt(c.getARGB());
        }

        buf.writeInt(wrapMaxWidth);
        buf.writeFloat(delay);
        buf.writeVarInt(fadeInTicks);
        buf.writeVarInt(fadeOutTicks);

        buf.writeVarInt(globalEffects == null ? 0 : globalEffects.size());
        if (globalEffects != null && !globalEffects.isEmpty()) {
            for (Effect effect : globalEffects) {
                buf.writeUtf(effect.serialize());
            }
        }

        buf.writeVarInt(messageAttributes == null ? 0 : messageAttributes.size());
        if (messageAttributes != null && !messageAttributes.isEmpty()) {
            for (MessageAttribute a : messageAttributes) {
                buf.writeUtf(a.serialize());
            }
        }
    }

    public static ImmersiveMessage decode(FriendlyByteBuf buf) {
        boolean spanMode = buf.readBoolean();

        ImmersiveMessage msg;
        if (spanMode) {

            int spanCount = buf.readVarInt();
            java.util.List<TextSpan> spans = new java.util.ArrayList<>();
            for (int i = 0; i < spanCount; i++) {
                spans.add(TextSpan.decode(buf));
            }

            String textJson = buf.readUtf();
            Component text = net.minecraft.network.chat.ComponentSerialization.CODEC
                    .parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(textJson))
                    .result()
                    .map(c -> (Component) c)
                    .orElse(Component.literal(textJson));
            float duration = buf.readFloat();

            msg = new ImmersiveMessage(text, duration);
            msg.spanMode = true;
            msg.spans = spans;
            msg.spanTypewriterIndices = new int[spans.size()];
            msg.evaluateSpanCharShake();
            msg.buildEffectSegments();
        } else {

            String textJson = buf.readUtf();
            Component text = net.minecraft.network.chat.ComponentSerialization.CODEC
                    .parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(textJson))
                    .result()
                    .map(c -> (Component) c)
                    .orElse(Component.literal(textJson));
            float duration = buf.readFloat();
            msg = new ImmersiveMessage(text, duration);
        }
        msg.xOffset = buf.readFloat();
        msg.yOffset = buf.readFloat();
        msg.shadow = buf.readBoolean();
        msg.anchor = SerializationUtil.readEnumSafe(buf, TextAnchor.class);
        msg.align = SerializationUtil.readEnumSafe(buf, TextAlign.class);
        msg.background = buf.readBoolean();
        msg.backgroundColor = new ImmersiveColor(buf.readInt());
        msg.borderStart = new ImmersiveColor(buf.readInt());
        msg.borderEnd = new ImmersiveColor(buf.readInt());
        msg.textScale = buf.readFloat();
        msg.typewriter = buf.readBoolean();
        msg.typewriterSpeed = buf.readFloat();
        msg.typewriterCenter = buf.readBoolean();
        msg.obfuscateMode = SerializationUtil.readEnumSafe(buf, ObfuscateMode.class);
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
            msg.textureSizingMode = SerializationUtil.readEnumSafe(buf, TextureSizingMode.class);
            msg.useTextureBackground = true;
            msg.background = true;
        }

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

        if (buf.isReadable()) {
            int effectCount = buf.readVarInt();
            if (effectCount > 0) {
                for (int i = 0; i < effectCount; i++) {
                    String effectTag = buf.readUtf();
                    try {
                        Effect effect = EffectRegistry.parseTag(effectTag);
                        msg.addEffect(effect);
                    } catch (IllegalArgumentException e) {

                        LOGGER.warn("Failed to decode global effect: {}", effectTag, e);
                    }
                }
            }
        }

        int attrCount = buf.readVarInt();
        for (int i = 0; i < attrCount; i++) {
            String serialized = buf.readUtf();
            try {
                MessageAttribute attr = MessageAttributeRegistry.parseTag(serialized);
                if (msg.messageAttributes == null) msg.messageAttributes = new ArrayList<>();
                msg.messageAttributes.add(attr);
                attr.apply(msg);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Failed to decode message attribute: {}", serialized, e);
            }
        }

        if (msg.obfuscateMode != ObfuscateMode.NONE) msg.initObfuscation();
        return msg;
    }

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

    public int getFadeInTicks() {
        return fadeInTicks;
    }

    public int getFadeOutTicks() {
        return fadeOutTicks;
    }

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

        ResourceLocation font = text.getStyle().getFont();
        return font != null ? font.toString() : "minecraft:default";
    }

    private Component getDrawComponent() {
        if (spanMode && spans != null) {
            return buildComponentFromSpans();
        }

        if (markupComponent != null) {
            return markupComponent;
        }

        if (typewriter) {
            return current;
        }
        return current.getString().isEmpty() ? text : current;
    }

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

            if (content.isEmpty() && span.getItemId() == null) continue;

            MutableComponent spanComponent;

            if (typewriter && spanTypewriterIndices != null && i < spanTypewriterIndices.length) {

                int spanTypewriterIndex = spanTypewriterIndices[i];
                if (spanTypewriterIndex < content.length()) {
                    content = content.substring(0, Math.max(0, spanTypewriterIndex));
                }
            } else if (!typewriter && span.getTypewriterSpeed() != null && spanTypewriterIndices != null) {

                int spanTypewriterIndex = spanTypewriterIndices[i];
                if (spanTypewriterIndex < content.length()) {
                    content = content.substring(0, Math.max(0, spanTypewriterIndex));
                }
            }

            if (span.getItemId() != null) {

                spanComponent = Component.literal(" ");
                result.append(spanComponent);
                currentCharIndex += span.getContent().length();
                continue;
            }

            if (span.getEntityId() != null) {

                spanComponent = Component.literal("  ");
                result.append(spanComponent);
                currentCharIndex += span.getContent().length();
                continue;
            }

            if (content.isEmpty()) {
                currentCharIndex += span.getContent().length();
                continue;
            }

            spanComponent = Component.literal(content);
            applySpanStyling(spanComponent, span);

            result.append(spanComponent);
            currentCharIndex += span.getContent().length();
        }

        return result;
    }

    private boolean hasAnyTypewriterSpans() {
        if (spans == null) return false;
        return spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null);
    }

    private void applySpanStyling(MutableComponent component, TextSpan span) {
        applySpanStyling(component, span, 1.0f);
    }

    private void applySpanStyling(MutableComponent component, TextSpan span, float alpha) {
        component.withStyle(style -> {
            if (span.getBold() != null && span.getBold()) style = style.withBold(true);
            if (span.getItalic() != null && span.getItalic()) style = style.withItalic(true);
            if (span.getUnderline() != null && span.getUnderline()) style = style.withUnderlined(true);
            if (span.getStrikethrough() != null && span.getStrikethrough()) style = style.withStrikethrough(true);
            if (span.getObfuscated() != null && span.getObfuscated()) style = style.withObfuscated(true);
            if (span.getFont() != null) style = style.withFont(span.getFont());

            if (span.getColor() != null) {
                int originalColor = span.getColor().getValue();
                int alphaComponent = (int)(alpha * 255) << 24;
                int colorWithAlpha = alphaComponent | (originalColor & 0x00FFFFFF);
                style = style.withColor(TextColor.fromRgb(colorWithAlpha));
            } else if (alpha < 1.0f) {

                int alphaComponent = (int)(alpha * 255) << 24;
                int colorWithAlpha = alphaComponent | 0x00FFFFFF;
                style = style.withColor(TextColor.fromRgb(colorWithAlpha));
            }

            return style;
        });
    }

    private float computeAlpha(float sampleAge) {
        return FadeCalculator.computeFadeAlpha(sampleAge, fadeInTicks, duration, fadeOutTicks);
    }

    private float computeSpanAlpha(TextSpan span, float sampleAge) {
        return FadeCalculator.computeSpanFadeAlpha(sampleAge,
                span.getFadeInTicks(), span.getFadeOutTicks(),
                duration, fadeInTicks, fadeOutTicks);
    }

    private float sampleAge(float partialTick) {
        float clamped = Mth.clamp(partialTick, 0f, 1f);
        return Mth.lerp(clamped, previousAge, age);
    }

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

        float charPadding = hasCharShakeSpans() ? spanCharShakeMaxStrength : 0f;
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

        float margin = 4f;
        float scaledW = baseWidth * textScale;
        float scaledH = baseHeight * textScale;
        x = Mth.clamp(x, margin, screenW - scaledW - margin);
        y = Mth.clamp(y, margin, screenH - scaledH - margin);

        float renderAge = sampleAge(partialTick);

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

        boolean hasGlobalEffects = globalEffects != null && !globalEffects.isEmpty();
        boolean hasSpanEffects = spanMode && spans != null && spans.stream().anyMatch(s -> s.getEffects() != null && !s.getEffects().isEmpty());

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

        boolean hasMessageEffects = messageEffects != null && !messageEffects.isEmpty();
        if (hasMessageEffects) {
            graphics.pose().pushPose();
            float renderTimeSeconds = sampleAge(partialTick) / 20f;
            MessageEffectContext meCtx = new MessageEffectContext(
                    graphics, renderTimeSeconds, partialTick,
                    baseWidth, baseHeight, textScale
            );
            for (MessageEffect me : messageEffects) {
                me.apply(meCtx);
            }
        }

        if (onRender != null) {
            onRender.render(graphics, this, 0, 0, alpha);
        } else if (!textVisible) {

        } else if (hasGlobalEffects || hasSpanEffects) {

            LOGGER.debug("Calling renderWithEffects");
            renderWithEffects(graphics, lines, draw, colour, textStartX, textStartY);
        } else if (hasCharShakeSpans() && !hasInlineItems) {
            renderCharShake(graphics, lines, draw, colour, textStartX, textStartY);
        } else if (hasInlineItems) {

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
        if (hasMessageEffects) {
            graphics.pose().popPose();
        }
        graphics.pose().popPose();
    }

    public void tick(float delta) {
        previousAge = age;
        age += delta;

        if (typewriter) {

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

            if (spanMode && spans != null && spanTypewriterIndices != null) {

                boolean hasIndependentTypewriter = TypewriterAnimator.hasIndependentTypewriter(spans);

                if (hasIndependentTypewriter) {

                    TypewriterAnimator.updateIndependentSpanTypewriter(age, spans, spanTypewriterIndices);
                } else {

                    int totalCharsToShow = Math.min(next, getFullText().length());
                    TypewriterAnimator.updateContainerTypewriter(totalCharsToShow, spans, spanTypewriterIndices);
                }
            }
        }

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

    private boolean hasItemSpans() {
        if (spans == null) return false;
        return spans.stream().anyMatch(span -> span.getItemId() != null || span.getEntityId() != null);
    }

    private boolean hasCharShakeSpans() {
        return spanCharShake;
    }

    private void evaluateSpanCharShake() {
        spanCharShake = false;
        spanCharShakeMaxStrength = 0f;
        spanCharShakeSegments = Collections.emptyList();
    }

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

    private static class CharShakeSegment {
        final int startIndex;
        final int endIndex;
        final ShakeType type;
        final float amplitude;
        final float speed;
        final float wavelength;

        CharShakeSegment(int startIndex, int endIndex, ShakeType type, float amplitude, float speed, float wavelength) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.type = type;
            this.amplitude = amplitude;
            this.speed = speed;
            this.wavelength = wavelength;
        }
    }

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

    private void renderSpansWithItems(GuiGraphics graphics, float startX, float startY, int colour, float alpha) {
        var font = Minecraft.getInstance().font;
        var mc = Minecraft.getInstance();
        float xOffset = startX;
        float yOffset = startY;

        for (int i = 0; i < spans.size(); i++) {
            TextSpan span = spans.get(i);

            if (span.getItemId() != null) {

                try {
                    ResourceLocation itemLocation = ResourceLocation.tryParse(span.getItemId());
                    if (itemLocation != null) {
                        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(itemLocation);
                        if (item != null) {
                            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, span.getItemCount() != null ? span.getItemCount() : 1);

                            if (span.getItemNbt() != null) {
                                try {
                                    CompoundTag nbtTag = TagParser.parseTag(span.getItemNbt());
                                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbtTag));
                                } catch (Exception e) {
                                    LOGGER.debug("Failed to parse item NBT '{}': {}", span.getItemNbt(), e.getMessage());
                                }
                            }

                            int itemSize = 16;

                            float itemYOffset = yOffset - (itemSize - font.lineHeight) / 2.0f;

                            float customOffsetX = span.getItemOffsetX() != null ? span.getItemOffsetX() : 0f;
                            float customOffsetY = span.getItemOffsetY() != null ? span.getItemOffsetY() : 0f;

                            graphics.pose().pushPose();
                            graphics.pose().translate(xOffset + customOffsetX, itemYOffset + customOffsetY, 0);
                            graphics.renderItem(stack, 0, 0);
                            graphics.pose().popPose();

                            xOffset += itemSize + 2;
                        }
                    }
                } catch (Exception e) {

                }
            } else if (span.getEntityId() != null) {

                float entityScale = span.getEntityScale() != null ? span.getEntityScale() : 1.0f;
                int entitySize = (int)(16 * entityScale);

                float entityYOffset = yOffset - (entitySize - font.lineHeight) / 2.0f;

                float customOffsetX = span.getEntityOffsetX() != null ? span.getEntityOffsetX() : 0f;
                float customOffsetY = span.getEntityOffsetY() != null ? span.getEntityOffsetY() : 0f;
                float yaw = span.getEntityYaw() != null ? span.getEntityYaw() : 45f;
                float pitch = span.getEntityPitch() != null ? span.getEntityPitch() : 0f;
                float roll = span.getEntityRoll() != null ? span.getEntityRoll() : 0f;
                int lighting = span.getEntityLighting() != null ? span.getEntityLighting() : 15;
                Float spin = span.getEntitySpin();

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
                    xOffset += renderedWidth + 2;
                }
            } else {

                String content = span.getContent();

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

    private void renderCharShake(GuiGraphics graphics, List<FormattedCharSequence> lines, Component draw, int colour, float baseX, float baseY) {
        var font = Minecraft.getInstance().font;
        int[] index = {0};
        boolean useSegmentShake = !spanCharShakeSegments.isEmpty();
        int[] segmentCursor = {0};
        CharShakeSegment[] activeSegment = {useSegmentShake ? spanCharShakeSegments.get(0) : null};

        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                final float lineBaseY = baseY + i * font.lineHeight;
                final float[] xAdvance = {baseX};
                FormattedCharSequence lineSeq = lines.get(i);
                lineSeq.accept((pos, style, codePoint) -> {
                    String ch = new String(Character.toChars(codePoint));
                    float sx = 0f, sy = 0f;
                    ShakeType shakeTypeToUse = ShakeType.RANDOM;
                    float amplitude = 0f;
                    float speed = 1f;
                    float wavelength = 1f;
                    boolean applyShake = false;

                    if (useSegmentShake) {
                        CharShakeSegment segment = activeSegment[0];
                        while (segment != null && index[0] >= segment.endIndex) {
                            segmentCursor[0]++;
                            segment = segmentCursor[0] < spanCharShakeSegments.size() ? spanCharShakeSegments.get(segmentCursor[0]) : null;
                            activeSegment[0] = segment;
                        }
                        if (segment != null && index[0] >= segment.startIndex) {
                            shakeTypeToUse = segment.type;
                            amplitude = segment.amplitude;
                            speed = segment.speed;
                            wavelength = segment.wavelength;
                            applyShake = amplitude > 0f;
                        }
                    }

                    if (applyShake) {
                        float charShakeTime = age * 0.05f * speed + index[0] * 0.1f;
                        switch (shakeTypeToUse) {
                            case WAVE -> {
                                float safeWavelength = Math.max(0.0001f, wavelength);
                                sy = (float) Math.sin(charShakeTime * 2 * Math.PI / safeWavelength) * amplitude;
                            }
                            case CIRCLE -> {
                                sx = (float) Math.cos(charShakeTime) * amplitude;
                                sy = (float) Math.sin(charShakeTime) * amplitude;
                            }
                            case RANDOM -> {
                                sx = (random.nextFloat() - 0.5f) * 2f * amplitude;
                                sy = (random.nextFloat() - 0.5f) * 2f * amplitude;
                            }
                        }
                    }
                    Component comp = Component.literal(ch).withStyle(style);
                    FormattedCharSequence charSeq = comp.getVisualOrderText();
                    float cw = font.getSplitter().stringWidth(charSeq);
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
                ShakeType shakeTypeToUse = ShakeType.RANDOM;
                float amplitude = 0f;
                float speed = 1f;
                float wavelength = 1f;
                boolean applyShake = false;

                if (useSegmentShake) {
                    CharShakeSegment segment = activeSegment[0];
                    while (segment != null && index[0] >= segment.endIndex) {
                        segmentCursor[0]++;
                        segment = segmentCursor[0] < spanCharShakeSegments.size() ? spanCharShakeSegments.get(segmentCursor[0]) : null;
                        activeSegment[0] = segment;
                    }
                    if (segment != null && index[0] >= segment.startIndex) {
                        shakeTypeToUse = segment.type;
                        amplitude = segment.amplitude;
                        speed = segment.speed;
                        wavelength = segment.wavelength;
                        applyShake = amplitude > 0f;
                    }
                }

                if (applyShake) {
                    float charShakeTime = age * 0.05f * speed + index[0] * 0.1f;
                    switch (shakeTypeToUse) {
                        case WAVE -> {
                            float safeWavelength = Math.max(0.0001f, wavelength);
                            sy = (float) Math.sin(charShakeTime * 2 * Math.PI / safeWavelength) * amplitude;
                        }
                        case CIRCLE -> {
                            sx = (float) Math.cos(charShakeTime) * amplitude;
                            sy = (float) Math.sin(charShakeTime) * amplitude;
                        }
                        case RANDOM -> {
                            sx = (random.nextFloat() - 0.5f) * 2f * amplitude;
                            sy = (random.nextFloat() - 0.5f) * 2f * amplitude;
                        }
                    }
                }
                Component comp = Component.literal(ch).withStyle(style);
                FormattedCharSequence charSeq = comp.getVisualOrderText();
                float cw = font.getSplitter().stringWidth(charSeq);
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

    private void renderWithEffects(GuiGraphics graphics, List<FormattedCharSequence> lines, Component draw, int baseColour, float baseX, float baseY) {
        var font = Minecraft.getInstance().font;
        int[] index = {0};

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

    private void renderCharWithEffects(GuiGraphics graphics, net.minecraft.client.gui.Font font,
                                        int codePoint, net.minecraft.network.chat.Style style,
                                        float baseX, float baseY,
                                        float baseR, float baseG, float baseB, float baseA,
                                        int charIndex, boolean isShadow, float[] xAdvanceOut) {

        float effR = baseR, effG = baseG, effB = baseB;
        if (style.getColor() != null) {
            int styleColor = style.getColor().getValue();
            effR = ((styleColor >> 16) & 0xFF) / 255f;
            effG = ((styleColor >> 8) & 0xFF) / 255f;
            effB = (styleColor & 0xFF) / 255f;
        }

        var settings = new net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings(
                0f, 0f,
                effR, effG, effB, baseA,
                charIndex, codePoint, isShadow
        );

        EffectSegment obfSegment = findEffectSegmentForChar(charIndex);
        if (obfSegment != null) {
            settings.obfuscateSpanStart = obfSegment.startIndex;
            settings.obfuscateSpanLength = obfSegment.endIndex - obfSegment.startIndex;
            settings.obfuscateKey = new ObfKey(this.messageContextId, obfSegment.spanIndex);
            settings.obfuscateStableKey = settings.obfuscateKey;
        } else {

            settings.obfuscateKey = this;
        }

        if (globalEffects != null && !globalEffects.isEmpty()) {
            net.tysontheember.emberstextapi.immersivemessages.effects.EffectContext.applyEffects(globalEffects, settings);
        }

        if (!spanEffectSegments.isEmpty()) {

            for (EffectSegment segment : spanEffectSegments) {
                if (charIndex >= segment.startIndex && charIndex < segment.endIndex) {

                    int spanLocalIndex = charIndex - segment.startIndex;
                    settings.index = spanLocalIndex;
                    settings.absoluteIndex = spanLocalIndex;

                    net.tysontheember.emberstextapi.immersivemessages.effects.EffectContext.applyEffects(segment.effects, settings);

                    settings.index = charIndex;
                    settings.absoluteIndex = charIndex;
                    break;
                }
            }
        }

        settings.clampColors();

        int effectiveCp = settings.codepoint;
        net.minecraft.network.chat.Style effectiveStyle = style;
        if (settings.useRandomGlyph) {

            effectiveStyle = style.applyFormat(net.minecraft.ChatFormatting.OBFUSCATED);
        }

        renderSingleChar(graphics, font, effectiveCp, effectiveStyle,
                baseX + settings.x, baseY + settings.y,
                settings.rot, settings.getPackedColor(), xAdvanceOut);

        for (var sibling : settings.getSiblingsOrEmpty()) {
            sibling.clampColors();
            renderSingleChar(graphics, font, codePoint, style,
                    baseX + sibling.x, baseY + sibling.y,
                    sibling.rot, sibling.getPackedColor(), null);
        }
    }

    private void renderSingleChar(GuiGraphics graphics, net.minecraft.client.gui.Font font,
                                   int codePoint, net.minecraft.network.chat.Style style,
                                   float x, float y, float rotation, int color,
                                   float[] xAdvanceOut) {
        String ch = new String(Character.toChars(codePoint));

        Component comp = Component.literal(ch).withStyle(style.withColor((TextColor) null));
        FormattedCharSequence charSeq = comp.getVisualOrderText();

        float cw = font.getSplitter().stringWidth(charSeq);

        if (((color >>> 24) & 0xFF) <= 3) {
            if (xAdvanceOut != null) {
                xAdvanceOut[0] += cw;
            }
            return;
        }

        graphics.pose().pushPose();

        if (rotation != 0f) {

            graphics.pose().translate(x + cw / 2f, y + font.lineHeight / 2f, 0);
            graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation(rotation));
            graphics.pose().translate(-(x + cw / 2f), -(y + font.lineHeight / 2f), 0);
        }

        graphics.pose().translate(x, y, 0);
        graphics.drawString(font, charSeq, 0, 0, color, shadow);
        graphics.pose().popPose();

        if (xAdvanceOut != null) {
            xAdvanceOut[0] += cw;
        }
    }

    public boolean isSpanMode() {
        return spanMode;
    }

    public List<TextSpan> getSpans() {
        return spanMode && spans != null ? new ArrayList<>(spans) : Collections.emptyList();
    }

    public ImmersiveMessage addSpan(TextSpan span) {
        if (!spanMode) {

            List<TextSpan> plainSpans = MarkupParser.fromPlainText(text.getString());
            spans = new ArrayList<>(plainSpans);
            spanMode = true;
        }
        spans.add(span);

        if (spanTypewriterIndices == null) {
            spanTypewriterIndices = new int[spans.size()];
        } else {
            int[] newIndices = new int[spans.size()];
            System.arraycopy(spanTypewriterIndices, 0, newIndices, 0, Math.min(spanTypewriterIndices.length, spans.size()));
            spanTypewriterIndices = newIndices;
        }
        evaluateSpanCharShake();
        buildEffectSegments();
        return this;
    }

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
