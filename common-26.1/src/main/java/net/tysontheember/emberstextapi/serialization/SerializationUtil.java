package net.tysontheember.emberstextapi.serialization;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public final class SerializationUtil {

    public static final int MAX_CONTENT_LENGTH = 65536;
    public static final int MAX_ID_LENGTH = 256;
    public static final int MAX_EFFECT_TAG_LENGTH = 512;
    public static final int MAX_ARRAY_SIZE = 256;
    public static final int MAX_ITEM_COUNT = 64;
    public static final float MAX_SCALE = 100.0f;
    public static final float MAX_OFFSET = 10000.0f;

    private SerializationUtil() {
    }

    public static float clampFloat(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    public static <E extends Enum<E>> E readEnumSafe(FriendlyByteBuf buf, Class<E> enumClass) {
        int ordinal = buf.readVarInt();
        E[] values = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new DecoderException("Invalid " + enumClass.getSimpleName() + " ordinal: " + ordinal);
        }
        return values[ordinal];
    }
}
