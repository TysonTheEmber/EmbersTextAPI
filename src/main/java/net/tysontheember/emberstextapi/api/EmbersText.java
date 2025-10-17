package net.tysontheember.emberstextapi.api;

import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.inline.AttributedText;
import net.tysontheember.emberstextapi.inline.TagParser;
import net.tysontheember.emberstextapi.render.AttributedComponentRenderer;

public final class EmbersText {
    private static final AttributedComponentRenderer RENDERER = new AttributedComponentRenderer();

    private EmbersText() {
    }

    public static AttributedText parse(String input) {
        return TagParser.parse(input);
    }

    public static Component render(String input, float timeSeconds) {
        return render(parse(input), timeSeconds);
    }

    public static Component render(AttributedText text, float timeSeconds) {
        return RENDERER.toComponent(text, timeSeconds);
    }
}
