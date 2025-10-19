package net.tysontheember.emberstextapi.text;

import net.tysontheember.emberstextapi.text.effect.Effect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable collection of attributes applied to a {@link Span}.
 */
public final class AttributeSet {

    private final String color;
    private final Gradient gradient;
    private final Style style;
    private final Background background;
    private final Map<String, Object> effectParams;
    private final Map<String, Effect> effects;

    private AttributeSet(Builder builder) {
        this.color = builder.color;
        this.gradient = builder.gradient;
        this.style = builder.style;
        this.background = builder.background;
        this.effectParams = Collections.unmodifiableMap(new HashMap<>(builder.effectParams));
        this.effects = Collections.unmodifiableMap(new HashMap<>(builder.effects));
    }

    /**
     * Creates a new builder for {@link AttributeSet}.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Primary color assigned to the span.
     *
     * @return color value, or {@code null} when unset
     */
    public String getColor() {
        return color;
    }

    /**
     * Gradient configuration for the span.
     *
     * @return gradient configuration, or {@code null}
     */
    public Gradient getGradient() {
        return gradient;
    }

    /**
     * Style toggles for the span.
     *
     * @return immutable style information
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Background configuration for the span.
     *
     * @return immutable background information
     */
    public Background getBackground() {
        return background;
    }

    /**
     * Additional effect parameters by name.
     *
     * @return immutable parameter map
     */
    public Map<String, Object> getEffectParams() {
        return effectParams;
    }

    /**
     * Registered dynamic effects keyed by their tag name.
     *
     * @return immutable effect map
     */
    public Map<String, Effect> getEffects() {
        return effects;
    }

    /**
     * Builder for {@link AttributeSet}.
     */
    public static final class Builder {

        private String color;
        private Gradient gradient;
        private Style style = Style.builder().build();
        private Background background = Background.builder().build();
        private final Map<String, Object> effectParams = new HashMap<>();
        private final Map<String, Effect> effects = new HashMap<>();

        private Builder() {
        }

        /**
         * Sets the text color.
         *
         * @param color color representation
         * @return this builder for chaining
         */
        public Builder color(String color) {
            this.color = color;
            return this;
        }

        /**
         * Sets the gradient configuration.
         *
         * @param gradient gradient descriptor
         * @return this builder for chaining
         */
        public Builder gradient(Gradient gradient) {
            this.gradient = gradient;
            return this;
        }

        /**
         * Sets the style configuration.
         *
         * @param style style descriptor
         * @return this builder for chaining
         */
        public Builder style(Style style) {
            this.style = Objects.requireNonNull(style, "style");
            return this;
        }

        /**
         * Sets the background configuration.
         *
         * @param background background descriptor
         * @return this builder for chaining
         */
        public Builder background(Background background) {
            this.background = Objects.requireNonNull(background, "background");
            return this;
        }

        /**
         * Adds a named effect parameter.
         *
         * @param key   parameter key
         * @param value parameter value
         * @return this builder for chaining
         */
        public Builder effectParam(String key, Object value) {
            effectParams.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        /**
         * Adds every entry from the provided map.
         *
         * @param params additional parameter map
         * @return this builder for chaining
         */
        public Builder effectParams(Map<String, Object> params) {
            Objects.requireNonNull(params, "params");
            effectParams.putAll(params);
            return this;
        }

        /**
         * Adds a named effect instance.
         *
         * @param key    effect name
         * @param effect effect instance
         * @return this builder for chaining
         */
        public Builder effect(String key, Effect effect) {
            effects.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(effect, "effect"));
            return this;
        }

        /**
         * Adds every effect entry from the provided map.
         *
         * @param effects additional effect map
         * @return this builder for chaining
         */
        public Builder effects(Map<String, Effect> effects) {
            Objects.requireNonNull(effects, "effects");
            this.effects.putAll(effects);
            return this;
        }

        /**
         * Builds the immutable attribute set.
         *
         * @return attribute set instance
         */
        public AttributeSet build() {
            Objects.requireNonNull(style, "style");
            Objects.requireNonNull(background, "background");
            return new AttributeSet(this);
        }
    }

    /**
     * Immutable description of a gradient applied to text.
     */
    public static final class Gradient {

        private final String from;
        private final String to;
        private final boolean hsv;
        private final double flow;
        private final boolean span;
        private final boolean uni;

        private Gradient(Builder builder) {
            this.from = builder.from;
            this.to = builder.to;
            this.hsv = builder.hsv;
            this.flow = builder.flow;
            this.span = builder.span;
            this.uni = builder.uni;
        }

        /**
         * Creates a new gradient builder.
         *
         * @return gradient builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Color at the start of the gradient.
         *
         * @return starting color value
         */
        public String getFrom() {
            return from;
        }

        /**
         * Color at the end of the gradient.
         *
         * @return ending color value
         */
        public String getTo() {
            return to;
        }

        /**
         * Indicates whether the gradient interpolates in HSV color space.
         *
         * @return true for HSV interpolation
         */
        public boolean isHsv() {
            return hsv;
        }

        /**
         * Flow multiplier controlling gradient speed.
         *
         * @return flow value
         */
        public double getFlow() {
            return flow;
        }

        /**
         * Indicates whether the gradient spans the entire text instead of the span range.
         *
         * @return true when the gradient spans the full text
         */
        public boolean isSpan() {
            return span;
        }

        /**
         * Indicates whether the gradient should use a uniform distribution.
         *
         * @return true for uniform gradients
         */
        public boolean isUni() {
            return uni;
        }

        /**
         * Builder for {@link Gradient}.
         */
        public static final class Builder {

            private String from;
            private String to;
            private boolean hsv;
            private double flow;
            private boolean span;
            private boolean uni;

            private Builder() {
            }

            /**
             * Sets the starting color value.
             *
             * @param from starting color
             * @return this builder for chaining
             */
            public Builder from(String from) {
                this.from = from;
                return this;
            }

            /**
             * Sets the ending color value.
             *
             * @param to ending color
             * @return this builder for chaining
             */
            public Builder to(String to) {
                this.to = to;
                return this;
            }

            /**
             * Enables or disables HSV interpolation.
             *
             * @param hsv true to use HSV space
             * @return this builder for chaining
             */
            public Builder hsv(boolean hsv) {
                this.hsv = hsv;
                return this;
            }

            /**
             * Sets the flow multiplier.
             *
             * @param flow flow value
             * @return this builder for chaining
             */
            public Builder flow(double flow) {
                this.flow = flow;
                return this;
            }

            /**
             * Sets whether the gradient spans the entire text.
             *
             * @param span true for full text gradients
             * @return this builder for chaining
             */
            public Builder span(boolean span) {
                this.span = span;
                return this;
            }

            /**
             * Sets whether the gradient uses uniform distribution.
             *
             * @param uni true for uniform gradients
             * @return this builder for chaining
             */
            public Builder uni(boolean uni) {
                this.uni = uni;
                return this;
            }

            /**
             * Builds the gradient.
             *
             * @return immutable gradient
             */
            public Gradient build() {
                return new Gradient(this);
            }
        }
    }

    /**
     * Immutable description of font style toggles.
     */
    public static final class Style {

        private final boolean bold;
        private final boolean italic;
        private final boolean underlined;
        private final boolean strikethrough;
        private final boolean obfuscated;

        private Style(Builder builder) {
            this.bold = builder.bold;
            this.italic = builder.italic;
            this.underlined = builder.underlined;
            this.strikethrough = builder.strikethrough;
            this.obfuscated = builder.obfuscated;
        }

        /**
         * Creates a new style builder.
         *
         * @return style builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Indicates whether bold styling is enabled.
         *
         * @return true when bold
         */
        public boolean isBold() {
            return bold;
        }

        /**
         * Indicates whether italic styling is enabled.
         *
         * @return true when italic
         */
        public boolean isItalic() {
            return italic;
        }

        /**
         * Indicates whether underlining is enabled.
         *
         * @return true when underlined
         */
        public boolean isUnderlined() {
            return underlined;
        }

        /**
         * Indicates whether strikethrough is enabled.
         *
         * @return true when strikethrough
         */
        public boolean isStrikethrough() {
            return strikethrough;
        }

        /**
         * Indicates whether obfuscation is enabled.
         *
         * @return true when obfuscated
         */
        public boolean isObfuscated() {
            return obfuscated;
        }

        /**
         * Builder for {@link Style}.
         */
        public static final class Builder {

            private boolean bold;
            private boolean italic;
            private boolean underlined;
            private boolean strikethrough;
            private boolean obfuscated;

            private Builder() {
            }

            /**
             * Enables or disables bold styling.
             *
             * @param bold true for bold
             * @return this builder for chaining
             */
            public Builder bold(boolean bold) {
                this.bold = bold;
                return this;
            }

            /**
             * Enables or disables italic styling.
             *
             * @param italic true for italic
             * @return this builder for chaining
             */
            public Builder italic(boolean italic) {
                this.italic = italic;
                return this;
            }

            /**
             * Enables or disables underline styling.
             *
             * @param underlined true for underlined
             * @return this builder for chaining
             */
            public Builder underlined(boolean underlined) {
                this.underlined = underlined;
                return this;
            }

            /**
             * Enables or disables strikethrough styling.
             *
             * @param strikethrough true for strikethrough
             * @return this builder for chaining
             */
            public Builder strikethrough(boolean strikethrough) {
                this.strikethrough = strikethrough;
                return this;
            }

            /**
             * Enables or disables obfuscated styling.
             *
             * @param obfuscated true for obfuscated
             * @return this builder for chaining
             */
            public Builder obfuscated(boolean obfuscated) {
                this.obfuscated = obfuscated;
                return this;
            }

            /**
             * Builds the style descriptor.
             *
             * @return immutable style
             */
            public Style build() {
                return new Style(this);
            }
        }
    }

    /**
     * Immutable background styling configuration.
     */
    public static final class Background {

        private final boolean on;
        private final String color;
        private final String border;
        private final double alpha;

        private Background(Builder builder) {
            this.on = builder.on;
            this.color = builder.color;
            this.border = builder.border;
            this.alpha = builder.alpha;
        }

        /**
         * Creates a new background builder.
         *
         * @return background builder
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Indicates whether the background is enabled.
         *
         * @return true when enabled
         */
        public boolean isOn() {
            return on;
        }

        /**
         * Background color value.
         *
         * @return background color, or {@code null}
         */
        public String getColor() {
            return color;
        }

        /**
         * Background border color.
         *
         * @return border color, or {@code null}
         */
        public String getBorder() {
            return border;
        }

        /**
         * Background alpha multiplier.
         *
         * @return alpha value
         */
        public double getAlpha() {
            return alpha;
        }

        /**
         * Builder for {@link Background}.
         */
        public static final class Builder {

            private boolean on;
            private String color;
            private String border;
            private double alpha = 1.0;

            private Builder() {
            }

            /**
             * Enables or disables the background.
             *
             * @param on true when background should render
             * @return this builder for chaining
             */
            public Builder on(boolean on) {
                this.on = on;
                return this;
            }

            /**
             * Sets the background color.
             *
             * @param color color value
             * @return this builder for chaining
             */
            public Builder color(String color) {
                this.color = color;
                return this;
            }

            /**
             * Sets the border color.
             *
             * @param border border color value
             * @return this builder for chaining
             */
            public Builder border(String border) {
                this.border = border;
                return this;
            }

            /**
             * Sets the alpha multiplier.
             *
             * @param alpha alpha value
             * @return this builder for chaining
             */
            public Builder alpha(double alpha) {
                this.alpha = alpha;
                return this;
            }

            /**
             * Builds the background descriptor.
             *
             * @return immutable background
             */
            public Background build() {
                return new Background(this);
            }
        }
    }
}
