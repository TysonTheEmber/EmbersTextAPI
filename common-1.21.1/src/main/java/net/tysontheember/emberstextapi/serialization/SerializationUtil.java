package net.tysontheember.emberstextapi.serialization;

/**
 * Validation utilities for network packet serialization.
 * <p>
 * Provides constants and helper methods to ensure safe network data transmission,
 * preventing memory abuse and invalid values.
 * </p>
 */
public final class SerializationUtil {

    // ===== Network Packet Validation Constants =====

    /** Maximum allowed content string length (64KB should be more than enough for any text span) */
    public static final int MAX_CONTENT_LENGTH = 65536;

    /** Maximum allowed ID string length (ResourceLocation format: namespace:path) */
    public static final int MAX_ID_LENGTH = 256;

    /** Maximum allowed effect tag length */
    public static final int MAX_EFFECT_TAG_LENGTH = 512;

    /** Maximum allowed array size for colors/effects to prevent memory abuse */
    public static final int MAX_ARRAY_SIZE = 256;

    /** Maximum allowed item count */
    public static final int MAX_ITEM_COUNT = 64;

    /** Maximum allowed scale value */
    public static final float MAX_SCALE = 100.0f;

    /** Maximum allowed offset value (pixels) */
    public static final float MAX_OFFSET = 10000.0f;

    /**
     * Private constructor to prevent instantiation.
     */
    private SerializationUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Clamp a float value to a valid range, handling NaN and Infinity.
     *
     * @param value Value to clamp
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Clamped value, or min if value is NaN
     */
    public static float clampFloat(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
