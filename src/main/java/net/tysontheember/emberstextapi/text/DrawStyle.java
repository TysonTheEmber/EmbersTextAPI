package net.tysontheember.emberstextapi.text;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Rendering preferences supplied to {@link EmbersText#draw}.
 */
public final class DrawStyle {
    private final int baseColor;
    private final boolean shadow;
    private final float scale;
    private final float letterSpacing;
    private final float lineHeightMultiplier;
    private final float animationStartTime;
    private final long seed;
    private final BiConsumer<String, Throwable> warningSink;

    private DrawStyle(Builder builder) {
        this.baseColor = builder.baseColor;
        this.shadow = builder.shadow;
        this.scale = builder.scale;
        this.letterSpacing = builder.letterSpacing;
        this.lineHeightMultiplier = builder.lineHeightMultiplier;
        this.animationStartTime = builder.animationStartTime;
        this.seed = builder.seed;
        this.warningSink = builder.warningSink;
    }

    public static DrawStyle defaultStyle() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int baseColor() {
        return baseColor;
    }

    public boolean shadow() {
        return shadow;
    }

    public float scale() {
        return scale;
    }

    public float letterSpacing() {
        return letterSpacing;
    }

    public float lineHeightMultiplier() {
        return lineHeightMultiplier;
    }

    public float animationStartTime() {
        return animationStartTime;
    }

    public long seed() {
        return seed;
    }

    public BiConsumer<String, Throwable> warningSink() {
        return warningSink;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.baseColor = baseColor;
        builder.shadow = shadow;
        builder.scale = scale;
        builder.letterSpacing = letterSpacing;
        builder.lineHeightMultiplier = lineHeightMultiplier;
        builder.animationStartTime = animationStartTime;
        builder.seed = seed;
        builder.warningSink = warningSink;
        return builder;
    }

    public static final class Builder {
        private int baseColor = 0xFFFFFFFF;
        private boolean shadow = true;
        private float scale = 1f;
        private float letterSpacing = 0f;
        private float lineHeightMultiplier = 1f;
        private float animationStartTime = TextAnimationClock.now();
        private long seed = 0L;
        private BiConsumer<String, Throwable> warningSink = (message, throwable) -> {};

        private Builder() {
        }

        public Builder baseColor(int baseColor) {
            this.baseColor = baseColor;
            return this;
        }

        public Builder shadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        public Builder scale(float scale) {
            if (Float.isFinite(scale) && scale > 0f) {
                this.scale = scale;
            }
            return this;
        }

        public Builder letterSpacing(float letterSpacing) {
            if (Float.isFinite(letterSpacing)) {
                this.letterSpacing = letterSpacing;
            }
            return this;
        }

        public Builder lineHeightMultiplier(float multiplier) {
            if (Float.isFinite(multiplier) && multiplier > 0f) {
                this.lineHeightMultiplier = multiplier;
            }
            return this;
        }

        public Builder animationStartTime(float animationStartTime) {
            if (Float.isFinite(animationStartTime)) {
                this.animationStartTime = animationStartTime;
            }
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public Builder warningSink(BiConsumer<String, Throwable> warningSink) {
            this.warningSink = Objects.requireNonNullElse(warningSink, (message, throwable) -> {});
            return this;
        }

        public DrawStyle build() {
            return new DrawStyle(this);
        }
    }
}
