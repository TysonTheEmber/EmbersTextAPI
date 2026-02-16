package net.tysontheember.emberstextapi.immersivemessages.effects.position;

import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;

import java.util.Random;

/**
 * Utility class for calculating shake offsets based on shake type and parameters.
 * Extracts shake logic from ImmersiveMessage to provide focused, reusable shake calculations.
 */
public class ShakeCalculator {

    /**
     * Calculates the X and Y offset for a shake effect.
     *
     * @param type The type of shake (WAVE, CIRCLE, RANDOM)
     * @param time The current shake time (typically renderAge * 0.05f * speed)
     * @param strength The amplitude of the shake
     * @param wavelength The wavelength for wave-based shakes (ignored for RANDOM)
     * @param random Random instance for RANDOM shake type
     * @return A float array [dx, dy] representing the X and Y offsets
     */
    public static float[] calculateShakeOffset(ShakeType type, float time, float strength, float wavelength, Random random) {
        float sx = 0f;
        float sy = 0f;

        switch (type) {
            case WAVE -> {
                // Sinusoidal vertical motion
                sy = (float) Math.sin(time * 2 * Math.PI / wavelength) * strength;
            }
            case CIRCLE -> {
                // Circular orbital motion
                sx = (float) Math.cos(time) * strength;
                sy = (float) Math.sin(time) * strength;
            }
            case RANDOM -> {
                // Random jitter each frame
                sx = (random.nextFloat() - 0.5f) * 2f * strength;
                sy = (random.nextFloat() - 0.5f) * 2f * strength;
            }
        }

        return new float[]{sx, sy};
    }

    /**
     * Calculates the X and Y offset for a per-character shake effect.
     * Includes a character index offset to create a wave propagation effect.
     *
     * @param type The type of shake (WAVE, CIRCLE, RANDOM)
     * @param age The current message age in ticks
     * @param speed The speed multiplier for the shake animation
     * @param strength The amplitude of the shake
     * @param wavelength The wavelength for wave-based shakes (ignored for RANDOM)
     * @param charIndex The index of the character being shaken (creates phase offset)
     * @param random Random instance for RANDOM shake type
     * @return A float array [dx, dy] representing the X and Y offsets
     */
    public static float[] calculateCharShakeOffset(ShakeType type, float age, float speed, float strength,
                                                     float wavelength, int charIndex, Random random) {
        float sx = 0f;
        float sy = 0f;

        // Character-specific time offset creates wave propagation effect
        float charShakeTime = age * 0.05f * speed + charIndex * 0.1f;

        switch (type) {
            case WAVE -> {
                // Prevent division by zero with safe wavelength
                float safeWavelength = Math.max(0.0001f, wavelength);
                sy = (float) Math.sin(charShakeTime * 2 * Math.PI / safeWavelength) * strength;
            }
            case CIRCLE -> {
                sx = (float) Math.cos(charShakeTime) * strength;
                sy = (float) Math.sin(charShakeTime) * strength;
            }
            case RANDOM -> {
                sx = (random.nextFloat() - 0.5f) * 2f * strength;
                sy = (random.nextFloat() - 0.5f) * 2f * strength;
            }
        }

        return new float[]{sx, sy};
    }
}
