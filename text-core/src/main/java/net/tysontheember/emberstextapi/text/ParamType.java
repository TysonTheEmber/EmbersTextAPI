package net.tysontheember.emberstextapi.text;

import java.util.Locale;
import java.util.Objects;

/**
 * Supported parameter types for {@link ParamSpec} definitions. The type is
 * responsible for converting the raw string tokens emitted by the
 * {@link TagParser} into strongly typed values.
 */
public enum ParamType {
    STRING {
        @Override
        public Object convert(Object value) {
            if (value == null) {
                return null;
            }
            return value.toString();
        }
    },
    BOOLEAN {
        @Override
        public Object convert(Object value) {
            if (value instanceof Boolean b) {
                return b;
            }
            if (value == null) {
                return null;
            }
            String stringValue = value.toString().trim().toLowerCase(Locale.ROOT);
            if (stringValue.isEmpty()) {
                return null;
            }
            return switch (stringValue) {
                case "1", "true", "yes", "on" -> Boolean.TRUE;
                case "0", "false", "no", "off" -> Boolean.FALSE;
                default -> throw new IllegalArgumentException("Unknown boolean value: " + value);
            };
        }
    },
    INTEGER {
        @Override
        public Object convert(Object value) {
            if (value instanceof Integer i) {
                return i;
            }
            if (value instanceof Number n) {
                return n.intValue();
            }
            if (value == null) {
                return null;
            }
            String s = value.toString().trim();
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Not an integer: " + value, ex);
            }
        }
    },
    FLOAT {
        @Override
        public Object convert(Object value) {
            if (value instanceof Float f) {
                return f;
            }
            if (value instanceof Number n) {
                return n.floatValue();
            }
            if (value == null) {
                return null;
            }
            String s = value.toString().trim();
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Not a float: " + value, ex);
            }
        }
    },
    DOUBLE {
        @Override
        public Object convert(Object value) {
            if (value instanceof Double d) {
                return d;
            }
            if (value instanceof Number n) {
                return n.doubleValue();
            }
            if (value == null) {
                return null;
            }
            String s = value.toString().trim();
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Not a double: " + value, ex);
            }
        }
    },
    COLOR {
        @Override
        public Object convert(Object value) {
            if (value instanceof Integer i) {
                return i;
            }
            if (value instanceof Number n) {
                return n.intValue();
            }
            if (value == null) {
                return null;
            }
            String s = value.toString().trim();
            if (s.isEmpty()) {
                return null;
            }
            return ColorUtil.parseColor(s);
        }
    },
    RESOURCE {
        @Override
        public Object convert(Object value) {
            if (value instanceof EmbersKey key) {
                return key;
            }
            if (value == null) {
                return null;
            }
            String s = value.toString().trim();
            if (s.isEmpty()) {
                return null;
            }
            return EmbersKey.parse(s);
        }
    };

    public abstract Object convert(Object value);

    public <T> T convert(Object value, Class<T> type) {
        Object converted = convert(value);
        if (converted == null) {
            return null;
        }
        if (!type.isInstance(converted)) {
            throw new IllegalStateException("Converted value " + converted + " is not of type " + type.getSimpleName());
        }
        return type.cast(converted);
    }

    public Number asNumber(Object value) {
        Object converted = convert(value);
        if (converted instanceof Number n) {
            return n;
        }
        throw new IllegalStateException("Value " + value + " is not numeric for type " + name());
    }

    static Object requireNotNull(Object value, String name) {
        return Objects.requireNonNull(value, name + " may not be null");
    }
}
