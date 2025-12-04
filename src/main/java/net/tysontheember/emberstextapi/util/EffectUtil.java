package net.tysontheember.emberstextapi.util;

import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import org.joml.Matrix4f;

/**
 * Utility class for effect rendering operations.
 * <p>
 * Provides helper methods for common rendering transformations used by effects,
 * particularly matrix operations for rotation and scaling.
 * </p>
 */
public class EffectUtil {

    /**
     * Apply rotation to a pose matrix around a specific origin point.
     * <p>
     * This method creates a rotated copy of the pose matrix by:
     * <ol>
     *   <li>Translating to the rotation origin (originX, originY)</li>
     *   <li>Applying rotation around the Z axis</li>
     *   <li>Translating back to (0, 0)</li>
     * </ol>
     * </p>
     * <p>
     * The settings object's x and y coordinates are updated to (0, 0) since
     * the translation is now baked into the matrix. This prevents double-translation
     * during vertex rendering.
     * </p>
     *
     * <h3>Usage Pattern:</h3>
     * <pre>{@code
     * // Rotate character 45 degrees around its center
     * float centerX = glyphWidth / 2f;
     * float centerY = lineHeight / 2f;
     * Matrix4f rotatedPose = EffectUtil.rotate(pose, settings, 0.785f, centerX, centerY);
     * // Now settings.x and settings.y are 0, rotation is in the matrix
     * }</pre>
     *
     * <h3>Thread Safety:</h3>
     * <p>
     * This method is safe to call from the render thread. It creates a new matrix
     * and modifies the settings object, but does not share state between calls.
     * </p>
     *
     * @param pose Original pose matrix (not modified)
     * @param settings Effect settings to update (x and y will be set to 0)
     * @param radians Rotation angle in radians (positive = counter-clockwise)
     * @param originX X coordinate of rotation origin (relative to settings.x)
     * @param originY Y coordinate of rotation origin (relative to settings.y)
     * @return New matrix with rotation applied
     */
    public static Matrix4f rotate(Matrix4f pose, EffectSettings settings, float radians, float originX, float originY) {
        // Create a copy of the pose matrix to avoid modifying the original
        Matrix4f result = new Matrix4f(pose);

        // Step 1: Translate to rotation origin
        // We need to translate by (settings.x + originX, settings.y + originY)
        result.translate(settings.x + originX, settings.y + originY, 0f);

        // Step 2: Apply rotation around Z axis
        result.rotateZ(radians);

        // Step 3: Translate back by -origin (not -settings position)
        // This moves the rotation center back to 0,0 in the local space
        result.translate(-originX, -originY, 0f);

        // Step 4: Update settings to reflect that translation is now in matrix
        // The vertex positions will be added to (0, 0) instead of (settings.x, settings.y)
        settings.x = 0f;
        settings.y = 0f;

        return result;
    }

    /**
     * Apply scaling to a pose matrix around a specific origin point.
     * <p>
     * Similar to rotation, but applies uniform scaling instead.
     * This can be used for pulse effects or size variations.
     * </p>
     *
     * @param pose Original pose matrix (not modified)
     * @param settings Effect settings to update (x and y will be adjusted)
     * @param scale Scale factor (1.0 = no change, > 1.0 = larger, < 1.0 = smaller)
     * @param originX X coordinate of scaling origin
     * @param originY Y coordinate of scaling origin
     * @return New matrix with scaling applied
     */
    public static Matrix4f scale(Matrix4f pose, EffectSettings settings, float scale, float originX, float originY) {
        // Create a copy of the pose matrix
        Matrix4f result = new Matrix4f(pose);

        // Translate to origin
        result.translate(settings.x + originX, settings.y + originY, 0f);

        // Apply uniform scaling
        result.scale(scale, scale, 1.0f);

        // Translate back
        result.translate(-originX, -originY, 0f);

        // Update settings
        settings.x = 0f;
        settings.y = 0f;

        return result;
    }

    /**
     * Clamp a value between min and max.
     * <p>
     * Utility method for clamping values, commonly used for color/alpha clamping.
     * </p>
     *
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Linear interpolation between two values.
     * <p>
     * Commonly used for smooth transitions in effects.
     * </p>
     *
     * @param a Start value
     * @param b End value
     * @param t Interpolation factor (0.0 to 1.0)
     * @return Interpolated value
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Convert HSV color to RGB.
     * <p>
     * Alternative to Minecraft's Mth.hsvToRgb for effect implementations.
     * Returns packed RGB integer (no alpha).
     * </p>
     *
     * @param hue Hue (0.0 to 1.0)
     * @param saturation Saturation (0.0 to 1.0)
     * @param value Value/Brightness (0.0 to 1.0)
     * @return Packed RGB color (0xRRGGBB)
     */
    public static int hsvToRgb(float hue, float saturation, float value) {
        // Normalize hue to 0-360 range
        float h = hue * 360f;

        float c = value * saturation;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = value - c;

        float r, g, b;
        if (h < 60) {
            r = c; g = x; b = 0;
        } else if (h < 120) {
            r = x; g = c; b = 0;
        } else if (h < 180) {
            r = 0; g = c; b = x;
        } else if (h < 240) {
            r = 0; g = x; b = c;
        } else if (h < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }

        int ri = (int) ((r + m) * 255);
        int gi = (int) ((g + m) * 255);
        int bi = (int) ((b + m) * 255);

        return (ri << 16) | (gi << 8) | bi;
    }

    /**
     * Calculate Perlin-like noise for organic motion effects.
     * <p>
     * Simple noise function for effects like turbulence or shake.
     * Not true Perlin noise, but sufficient for visual effects.
     * </p>
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param seed Seed value for variation
     * @return Noise value (approximately -1.0 to 1.0)
     */
    public static float noise(float x, float y, float seed) {
        // Simple pseudo-random noise based on coordinates
        float n = (float) Math.sin(x * 12.9898 + y * 78.233 + seed * 43.144) * 43758.5453f;
        return n - (float) Math.floor(n); // Return fractional part
    }
}
