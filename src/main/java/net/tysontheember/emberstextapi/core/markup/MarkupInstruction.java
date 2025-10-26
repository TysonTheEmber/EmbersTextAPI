package net.tysontheember.emberstextapi.core.markup;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single markup instruction emitted while tokenising a markup string.
 */
public final class MarkupInstruction {
    private final MarkupInstructionType type;
    private final String name;
    private final int position;
    private final Map<String, String> attributes;

    public MarkupInstruction(MarkupInstructionType type, String name, int position, Map<String, String> attributes) {
        this.type = Objects.requireNonNull(type, "type");
        this.name = Objects.requireNonNull(name, "name").toLowerCase(Locale.ROOT);
        this.position = Math.max(position, 0);
        if (attributes == null || attributes.isEmpty()) {
            this.attributes = Collections.emptyMap();
        } else {
            Map<String, String> copy = new LinkedHashMap<>();
            attributes.forEach((key, value) -> {
                if (key != null && !key.isEmpty()) {
                    copy.put(key.toLowerCase(Locale.ROOT), value);
                }
            });
            this.attributes = Collections.unmodifiableMap(copy);
        }
    }

    public MarkupInstructionType type() {
        return type;
    }

    public String name() {
        return name;
    }

    /**
     * Returns the zero-based glyph position at which this instruction should be applied.
     */
    public int position() {
        return position;
    }

    public Map<String, String> attributes() {
        return attributes;
    }
}
