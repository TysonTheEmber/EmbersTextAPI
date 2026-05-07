package net.tysontheember.emberstextapi.util;

import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import org.joml.Matrix4f;

public class EffectUtil {

    public static Matrix4f rotate(Matrix4f pose, EffectSettings settings, float radians, float originX, float originY) {

        Matrix4f result = new Matrix4f(pose);

        result.translate(settings.x + originX, settings.y + originY, 0f);

        result.rotateZ(radians);

        result.translate(-originX, -originY, 0f);

        settings.x = 0f;
        settings.y = 0f;

        return result;
    }

    public static Matrix4f scale(Matrix4f pose, EffectSettings settings, float scale, float originX, float originY) {

        Matrix4f result = new Matrix4f(pose);

        result.translate(settings.x + originX, settings.y + originY, 0f);

        result.scale(scale, scale, 1.0f);

        result.translate(-originX, -originY, 0f);

        settings.x = 0f;
        settings.y = 0f;

        return result;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float noise(float x, float y, float seed) {

        float n = (float) Math.sin(x * 12.9898 + y * 78.233 + seed * 43.144) * 43758.5453f;
        return n - (float) Math.floor(n);
    }
}
