package net.tysontheember.emberstextapi.immersivemessages.api;

import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttribute;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public record ParseResult(@NotNull List<TextSpan> spans,
                          @NotNull List<MessageEffect> messageEffects,
                          @NotNull List<MessageAttribute> messageAttributes,
                          @NotNull String strippedMarkup) {

    public static ParseResult empty() {
        return new ParseResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "");
    }
}
