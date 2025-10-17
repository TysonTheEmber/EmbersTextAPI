package net.tysontheember.emberstextapi.inline;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Provides parsing-time information for tag factories.
 */
public final class TagParserContext {
    private final Deque<TagAttribute> stack;

    TagParserContext(Deque<TagAttribute> stack) {
        this.stack = stack;
    }

    public Deque<TagAttribute> stack() {
        return new ArrayDeque<>(stack);
    }
}
