package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Describes an animation effect that can be applied to individual glyphs.
 */
public interface Effect {

    /**
     * Notifies the effect that a new frame has begun.
     *
     * @param ticks animation ticks elapsed
     */
    void beginFrame(int ticks);

    /**
     * Applies the effect to a glyph positioned at {@code basePos} with a {@code base} colour.
     * Implementations should populate the provided {@link Out} instance with the transformed state.
     *
     * @param index   glyph index within the rendered text
     * @param basePos base glyph position
     * @param base    base colour for the glyph
     * @param out     output container that should be populated with the transformed state
     */
    void applyGlyph(int index, Vec2 basePos, Color base, Out out);

    /**
     * Mutable container used to expose effect output values.
     */
    final class Out {

        private Vec2 position = new Vec2(0.0F, 0.0F);
        private Color color = Color.WHITE;

        /**
         * Resets the output to the provided defaults.
         *
         * @param basePos base glyph position
         * @param base    base glyph colour
         */
        public void reset(Vec2 basePos, Color base) {
            Objects.requireNonNull(basePos, "basePos");
            this.position = new Vec2(basePos.x, basePos.y);
            this.color = Objects.requireNonNull(base, "base");
        }

        /**
         * Applies the supplied positional offset relative to the current value.
         *
         * @param dx horizontal offset
         * @param dy vertical offset
         */
        public void offset(float dx, float dy) {
            this.position = new Vec2(position.x + dx, position.y + dy);
        }

        /**
         * Sets the glyph position.
         *
         * @param x horizontal coordinate
         * @param y vertical coordinate
         */
        public void setPosition(float x, float y) {
            this.position = new Vec2(x, y);
        }

        /**
         * Updates the glyph colour.
         *
         * @param color new colour value
         */
        public void setColor(Color color) {
            this.color = Objects.requireNonNull(color, "color");
        }

        /**
         * Multiplies the colour alpha channel by the provided factor.
         *
         * @param multiplier alpha multiplier
         */
        public void multiplyAlpha(float multiplier) {
            this.color = this.color.multiplyAlpha(multiplier);
        }

        /**
         * Retrieves the computed position.
         *
         * @return glyph position
         */
        public Vec2 getPosition() {
            return position;
        }

        /**
         * Retrieves the computed colour.
         *
         * @return glyph colour
         */
        public Color getColor() {
            return color;
        }
    }

    /**
     * Immutable RGBA colour representation used by effects.
     */
    final class Color {

        /** Constant representing white with full opacity. */
        public static final Color WHITE = fromInt(0xFFFFFFFF);

        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        private Color(float red, float green, float blue, float alpha) {
            this.red = clamp(red);
            this.green = clamp(green);
            this.blue = clamp(blue);
            this.alpha = clamp(alpha);
        }

        /**
         * Creates a colour from an ARGB integer.
         *
         * @param argb packed ARGB value
         * @return colour instance
         */
        public static Color fromInt(int argb) {
            int a = (argb >> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            return new Color(r / 255f, g / 255f, b / 255f, a / 255f);
        }

        /**
         * Parses a colour from a hexadecimal string.
         *
         * @param hex colour string prefixed with '#'
         * @return colour instance
         */
        public static Color fromHex(String hex) {
            Objects.requireNonNull(hex, "hex");
            String value = hex.startsWith("#") ? hex.substring(1) : hex;
            int argb;
            if (value.length() == 3) {
                int r = expandNibble(value.charAt(0));
                int g = expandNibble(value.charAt(1));
                int b = expandNibble(value.charAt(2));
                argb = 0xFF000000 | (r << 16) | (g << 8) | b;
            } else if (value.length() == 6) {
                argb = 0xFF000000 | Integer.parseInt(value, 16);
            } else if (value.length() == 8) {
                argb = (int) Long.parseLong(value, 16);
            } else {
                throw new IllegalArgumentException("Unsupported hex colour format: " + hex);
            }
            return fromInt(argb);
        }

        private static int expandNibble(char c) {
            int digit = Character.digit(c, 16);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid hex digit: " + c);
            }
            return (digit << 4) | digit;
        }

        private static float clamp(float value) {
            return Math.min(1.0f, Math.max(0.0f, value));
        }

        /**
         * Creates a colour from HSV components.
         *
         * @param hue        hue component in degrees
         * @param saturation saturation in the range [0, 1]
         * @param value      value in the range [0, 1]
         * @return colour instance
         */
        public static Color fromHsv(float hue, float saturation, float value) {
            float h = (hue % 360f + 360f) % 360f;
            float s = clamp(saturation);
            float v = clamp(value);
            float c = v * s;
            float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
            float m = v - c;
            float r;
            float g;
            float b;
            if (h < 60f) {
                r = c;
                g = x;
                b = 0f;
            } else if (h < 120f) {
                r = x;
                g = c;
                b = 0f;
            } else if (h < 180f) {
                r = 0f;
                g = c;
                b = x;
            } else if (h < 240f) {
                r = 0f;
                g = x;
                b = c;
            } else if (h < 300f) {
                r = x;
                g = 0f;
                b = c;
            } else {
                r = c;
                g = 0f;
                b = x;
            }
            return new Color(r + m, g + m, b + m, 1.0f);
        }

        /**
         * Linearly interpolates to the target colour.
         *
         * @param target target colour
         * @param t      interpolation factor
         * @return interpolated colour
         */
        public Color lerp(Color target, float t) {
            Objects.requireNonNull(target, "target");
            float clamped = clamp(t);
            float r = red + (target.red - red) * clamped;
            float g = green + (target.green - green) * clamped;
            float b = blue + (target.blue - blue) * clamped;
            float a = alpha + (target.alpha - alpha) * clamped;
            return new Color(r, g, b, a);
        }

        /**
         * Interpolates between two colours in HSV space.
         *
         * @param target target colour
         * @param t      interpolation factor
         * @return interpolated colour
         */
        public Color lerpHsv(Color target, float t) {
            Objects.requireNonNull(target, "target");
            float[] from = toHsv();
            float[] to = target.toHsv();
            float clamped = clamp(t);
            float hue = from[0] + (to[0] - from[0]) * clamped;
            float sat = from[1] + (to[1] - from[1]) * clamped;
            float val = from[2] + (to[2] - from[2]) * clamped;
            return fromHsv(hue, sat, val).withAlpha(alpha + (target.alpha - alpha) * clamped);
        }

        private float[] toHsv() {
            float max = Math.max(red, Math.max(green, blue));
            float min = Math.min(red, Math.min(green, blue));
            float delta = max - min;
            float hue;
            if (delta == 0f) {
                hue = 0f;
            } else if (max == red) {
                hue = 60f * (((green - blue) / delta) % 6f);
            } else if (max == green) {
                hue = 60f * (((blue - red) / delta) + 2f);
            } else {
                hue = 60f * (((red - green) / delta) + 4f);
            }
            if (hue < 0f) {
                hue += 360f;
            }
            float saturation = max == 0f ? 0f : delta / max;
            return new float[]{hue, saturation, max};
        }

        /**
         * Returns a new colour with the provided alpha value.
         *
         * @param alpha new alpha value
         * @return colour with updated alpha
         */
        public Color withAlpha(float alpha) {
            return new Color(red, green, blue, alpha);
        }

        /**
         * Scales the alpha component by the provided multiplier.
         *
         * @param multiplier alpha multiplier
         * @return colour with scaled alpha
         */
        public Color multiplyAlpha(float multiplier) {
            return withAlpha(clamp(alpha * multiplier));
        }

        /**
         * Retrieves the red component.
         *
         * @return red value in the range [0, 1]
         */
        public float getRed() {
            return red;
        }

        /**
         * Retrieves the green component.
         *
         * @return green value in the range [0, 1]
         */
        public float getGreen() {
            return green;
        }

        /**
         * Retrieves the blue component.
         *
         * @return blue value in the range [0, 1]
         */
        public float getBlue() {
            return blue;
        }

        /**
         * Retrieves the alpha component.
         *
         * @return alpha value in the range [0, 1]
         */
        public float getAlpha() {
            return alpha;
        }

        /**
         * Converts the colour to a packed ARGB integer.
         *
         * @return ARGB value
         */
        public int toArgb() {
            int a = Math.round(alpha * 255f) & 0xFF;
            int r = Math.round(red * 255f) & 0xFF;
            int g = Math.round(green * 255f) & 0xFF;
            int b = Math.round(blue * 255f) & 0xFF;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Color)) {
                return false;
            }
            Color color = (Color) o;
            return Float.compare(color.red, red) == 0
                    && Float.compare(color.green, green) == 0
                    && Float.compare(color.blue, blue) == 0
                    && Float.compare(color.alpha, alpha) == 0;
        }

        @Override
        public int hashCode() {
            int result = Float.hashCode(red);
            result = 31 * result + Float.hashCode(green);
            result = 31 * result + Float.hashCode(blue);
            result = 31 * result + Float.hashCode(alpha);
            return result;
        }
    }
}
