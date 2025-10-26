package net.tysontheember.emberstextapi.core.markup;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable result describing the plain text and instructions extracted from a markup string.
 */
public final class MarkupStream {
    private final String plainText;
    private final List<MarkupInstruction> instructions;

    public MarkupStream(String plainText, List<MarkupInstruction> instructions) {
        this.plainText = plainText == null ? "" : plainText;
        if (instructions == null || instructions.isEmpty()) {
            this.instructions = Collections.emptyList();
        } else {
            this.instructions = Collections.unmodifiableList(List.copyOf(instructions));
        }
    }

    public String plainText() {
        return plainText;
    }

    public List<MarkupInstruction> instructions() {
        return instructions;
    }

    public boolean isEmpty() {
        return plainText.isEmpty() && instructions.isEmpty();
    }

    public MarkupStream withInstructions(List<MarkupInstruction> newInstructions) {
        return new MarkupStream(plainText, Objects.requireNonNull(newInstructions, "instructions"));
    }
}
