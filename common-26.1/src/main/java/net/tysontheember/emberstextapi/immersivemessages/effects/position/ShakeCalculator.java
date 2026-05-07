package net.tysontheember.emberstextapi.immersivemessages.effects.position;

import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;

import java.util.Random;

public class ShakeCalculator {

    public static float[] calculateShakeOffset(ShakeType type, float time, float strength, float wavelength, Random random) {
        float sx = 0f;
        float sy = 0f;

        switch (type) {
            case WAVE -> {

                sy = (float) Math.sin(time * 2 * Math.PI / wavelength) * strength;
            }
            case CIRCLE -> {

                sx = (float) Math.cos(time) * strength;
                sy = (float) Math.sin(time) * strength;
            }
            case RANDOM -> {

                sx = (random.nextFloat() - 0.5f) * 2f * strength;
                sy = (random.nextFloat() - 0.5f) * 2f * strength;
            }
        }

        return new float[]{sx, sy};
    }

    public static float[] calculateCharShakeOffset(ShakeType type, float age, float speed, float strength,
                                                     float wavelength, int charIndex, Random random) {
        float sx = 0f;
        float sy = 0f;

        float charShakeTime = age * 0.05f * speed + charIndex * 0.1f;

        switch (type) {
            case WAVE -> {

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
