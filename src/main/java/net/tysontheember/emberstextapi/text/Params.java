package net.tysontheember.emberstextapi.text;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A strongly typed parameter map produced by {@link ParamSpec#validate(Map, java.util.function.BiConsumer)}.
 */
public final class Params {
    private final Map<String, Object> values;

    Params(Map<String, Object> values) {
        this.values = values;
    }

    public static Params of(Map<String, Object> values) {
        return new Params(Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }

    public Map<String, Object> raw() {
        return values;
    }

    public boolean contains(String key) {
        return values.containsKey(key.toLowerCase(java.util.Locale.ROOT));
    }

    public String getString(String key, String fallback) {
        Object value = values.get(key.toLowerCase(java.util.Locale.ROOT));
        return value instanceof String s ? s : fallback;
    }

    public boolean getBoolean(String key, boolean fallback) {
        Object value = values.get(key.toLowerCase(java.util.Locale.ROOT));
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        return fallback;
    }

    public int getInt(String key, int fallback) {
        Object value = values.get(key.toLowerCase(java.util.Locale.ROOT));
        if (value instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }

    public float getFloat(String key, float fallback) {
        Object value = values.get(key.toLowerCase(java.util.Locale.ROOT));
        if (value instanceof Number n) {
            return n.floatValue();
        }
        return fallback;
    }

    public double getDouble(String key, double fallback) {
        Object value = values.get(key.toLowerCase(java.util.Locale.ROOT));
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }

    public int getColor(String key, int fallback) {
        Object value = values.get(key.toLowerCase(java.util.Locale.ROOT));
        if (value instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }

    public ResourceLocation getResource(String key) {
        Object value = values.get(key.toLowerCase(java.util.Locale.ROOT));
        return value instanceof ResourceLocation rl ? rl : null;
    }

    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(values.size());
        values.forEach((key, value) -> {
            buf.writeUtf(key);
            if (value == null) {
                buf.writeByte(0);
            } else if (value instanceof String s) {
                buf.writeByte(1);
                buf.writeUtf(s);
            } else if (value instanceof Integer i) {
                buf.writeByte(2);
                buf.writeInt(i);
            } else if (value instanceof Float f) {
                buf.writeByte(3);
                buf.writeFloat(f);
            } else if (value instanceof Double d) {
                buf.writeByte(4);
                buf.writeDouble(d);
            } else if (value instanceof Boolean b) {
                buf.writeByte(5);
                buf.writeBoolean(b);
            } else if (value instanceof ResourceLocation rl) {
                buf.writeByte(6);
                buf.writeResourceLocation(rl);
            } else {
                throw new IllegalStateException("Unsupported parameter type: " + value.getClass());
            }
        });
    }

    public static Params fromBuffer(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            int type = buf.readByte();
            Object value;
            switch (type) {
                case 0 -> value = null;
                case 1 -> value = buf.readUtf();
                case 2 -> value = buf.readInt();
                case 3 -> value = buf.readFloat();
                case 4 -> value = buf.readDouble();
                case 5 -> value = buf.readBoolean();
                case 6 -> value = buf.readResourceLocation();
                default -> throw new IllegalStateException("Unknown parameter type id " + type);
            }
            values.put(key, value);
        }
        return new Params(Collections.unmodifiableMap(values));
    }

    @Override
    public String toString() {
        return "Params" + values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Params params)) return false;
        return Objects.equals(values, params.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
