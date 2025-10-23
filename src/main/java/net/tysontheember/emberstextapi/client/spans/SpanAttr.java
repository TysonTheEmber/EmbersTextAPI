package net.tysontheember.emberstextapi.client.spans;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable attribute bundle describing style, background, and effects for a span.
 */
public final class SpanAttr {
    private final StyleFlags style;
    private final Color color;
    private final Gradient gradient;
    private final Background bg;
    private final EffectSpec effect;

    private SpanAttr(StyleFlags style, Color color, Gradient gradient, Background bg, EffectSpec effect) {
        this.style = style;
        this.color = color;
        this.gradient = gradient;
        this.bg = bg;
        this.effect = effect;
    }

    public StyleFlags style() {
        return style;
    }

    public Color color() {
        return color;
    }

    public Gradient gradient() {
        return gradient;
    }

    public Background bg() {
        return bg;
    }

    public EffectSpec effect() {
        return effect;
    }

    /**
     * Builds a {@link SpanAttr} snapshot from an existing {@link TextSpan} instance.
     */
    public static SpanAttr fromTextSpan(TextSpan span) {
        Objects.requireNonNull(span, "span");

        StyleFlags styleFlags = new StyleFlags(
            Boolean.TRUE.equals(span.getBold()),
            Boolean.TRUE.equals(span.getItalic()),
            Boolean.TRUE.equals(span.getUnderline()),
            Boolean.TRUE.equals(span.getStrikethrough()),
            Boolean.TRUE.equals(span.getObfuscated()),
            span.getFont()
        );

        Color colour = span.getColor() != null ? new Color(span.getColor()) : null;
        Gradient gradient = span.getGradientColors() != null ? new Gradient(span.getGradientColors()) : null;

        Background background = null;
        if (Boolean.TRUE.equals(span.getHasBackground()) || span.getBackgroundColor() != null || span.getBackgroundGradient() != null) {
            background = new Background(
                Boolean.TRUE.equals(span.getHasBackground()),
                span.getBackgroundColor(),
                span.getBackgroundGradient(),
                null,
                null
            );
        }

        boolean hasBorderOverride = span.getGlobalBorderStart() != null || span.getGlobalBorderEnd() != null;
        if (background == null && hasBorderOverride) {
            background = new Background(false, null, null, span.getGlobalBorderStart(), span.getGlobalBorderEnd());
        }

        EffectSpec.Global global = null;
        if (span.hasGlobalAttributes()) {
            global = new EffectSpec.Global(
                span.getGlobalBackground(),
                span.getGlobalBackgroundColor(),
                span.getGlobalBackgroundGradient(),
                span.getGlobalBorderStart(),
                span.getGlobalBorderEnd(),
                span.getGlobalXOffset(),
                span.getGlobalYOffset(),
                span.getGlobalAnchor(),
                span.getGlobalAlign(),
                span.getGlobalScale(),
                span.getGlobalShadow(),
                span.getGlobalFadeInTicks(),
                span.getGlobalFadeOutTicks(),
                span.getGlobalTypewriterSpeed(),
                span.getGlobalTypewriterCenter()
            );
        }

        EffectSpec.Typewriter typewriter = span.getTypewriterSpeed() != null
            ? new EffectSpec.Typewriter(span.getTypewriterSpeed(), Boolean.TRUE.equals(span.getTypewriterCenter()))
            : null;

        EffectSpec.Shake shake = span.getShakeType() != null && span.getShakeAmplitude() != null
            ? new EffectSpec.Shake(span.getShakeType(), span.getShakeAmplitude(), span.getShakeSpeed(), span.getShakeWavelength())
            : null;

        EffectSpec.Shake charShake = span.getCharShakeType() != null && span.getCharShakeAmplitude() != null
            ? new EffectSpec.Shake(span.getCharShakeType(), span.getCharShakeAmplitude(), span.getCharShakeSpeed(), span.getCharShakeWavelength())
            : null;

        EffectSpec.Obfuscate obfuscate = span.getObfuscateMode() != null
            ? new EffectSpec.Obfuscate(span.getObfuscateMode(), span.getObfuscateSpeed())
            : null;

        EffectSpec.Fade fade = (span.getFadeInTicks() != null || span.getFadeOutTicks() != null)
            ? new EffectSpec.Fade(span.getFadeInTicks(), span.getFadeOutTicks())
            : null;

        EffectSpec.Item item = span.getItemId() != null
            ? new EffectSpec.Item(span.getItemId(), span.getItemCount(), span.getItemOffsetX(), span.getItemOffsetY())
            : null;

        EffectSpec.Entity entity = span.getEntityId() != null
            ? new EffectSpec.Entity(
                span.getEntityId(),
                span.getEntityScale(),
                span.getEntityOffsetX(),
                span.getEntityOffsetY(),
                span.getEntityYaw(),
                span.getEntityPitch(),
                span.getEntityAnimation()
            )
            : null;

        EffectSpec effect = (typewriter != null || shake != null || charShake != null ||
            obfuscate != null || fade != null || item != null || entity != null || global != null)
            ? new EffectSpec(typewriter, shake, charShake, obfuscate, fade, item, entity, global)
            : null;

        // Capture border overrides on the background bundle when available.
        if (background != null && hasBorderOverride) {
            background = new Background(
                background.enabled(),
                background.color(),
                background.gradient(),
                span.getGlobalBorderStart(),
                span.getGlobalBorderEnd()
            );
        }

        return new SpanAttr(styleFlags, colour, gradient, background, effect);
    }

    public record StyleFlags(boolean bold, boolean italic, boolean underline,
                             boolean strikethrough, boolean obfuscated, ResourceLocation font) {
    }

    public record Color(TextColor value) {
    }

    public record Gradient(TextColor[] colors) {
        public Gradient {
            if (colors != null) {
                colors = Arrays.copyOf(colors, colors.length);
            }
        }

        @Override
        public TextColor[] colors() {
            return this.colors == null ? null : Arrays.copyOf(this.colors, this.colors.length);
        }
    }

    public record Background(boolean enabled, ImmersiveColor color, ImmersiveColor[] gradient,
                              ImmersiveColor borderStart, ImmersiveColor borderEnd) {
        public Background {
            if (gradient != null) {
                gradient = Arrays.copyOf(gradient, gradient.length);
            }
        }

        @Override
        public ImmersiveColor[] gradient() {
            return this.gradient == null ? null : Arrays.copyOf(this.gradient, this.gradient.length);
        }
    }

    public record EffectSpec(Typewriter typewriter, Shake shake, Shake charShake, Obfuscate obfuscate,
                             Fade fade, Item item, Entity entity, Global global) {

        public record Typewriter(Float speed, boolean center) {
        }

        public record Shake(ShakeType type, Float amplitude, Float speed, Float wavelength) {
        }

        public record Obfuscate(ObfuscateMode mode, Float speed) {
        }

        public record Fade(Integer inTicks, Integer outTicks) {
        }

        public record Item(String id, Integer count, Float offsetX, Float offsetY) {
        }

        public record Entity(String id, Float scale, Float offsetX, Float offsetY, Float yaw, Float pitch,
                             String animation) {
        }

        public record Global(Boolean backgroundEnabled, ImmersiveColor backgroundColor, ImmersiveColor[] backgroundGradient,
                             ImmersiveColor borderStart, ImmersiveColor borderEnd, Float offsetX, Float offsetY,
                             TextAnchor anchor, TextAnchor align, Float scale, Boolean shadow,
                             Integer fadeInTicks, Integer fadeOutTicks, Float typewriterSpeed, Boolean typewriterCenter) {
            public Global {
                if (backgroundGradient != null) {
                    backgroundGradient = Arrays.copyOf(backgroundGradient, backgroundGradient.length);
                }
            }

            @Override
            public ImmersiveColor[] backgroundGradient() {
                return this.backgroundGradient == null ? null : Arrays.copyOf(this.backgroundGradient, this.backgroundGradient.length);
            }
        }
    }
}
