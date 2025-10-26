package net.tysontheember.emberstextapi.core.markup;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.tysontheember.emberstextapi.core.style.EmbersStyle;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;
import net.tysontheember.emberstextapi.core.style.SpanStyleAdapter;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.mixin.StringDecomposerAccess;

/**
 * Central coordinator that wires markup parsing into the vanilla formatted text pipeline.
 */
public final class GlobalSpanProcessor {
    private GlobalSpanProcessor() {
    }

    public static boolean iterateFormatted(String original, int startIndex, Style style, Style plainStyle,
            FormattedCharSink sink) {
        Objects.requireNonNull(sink, "sink");
        Style currentStyle = style == null ? Style.EMPTY : style;
        Style resetStyle = plainStyle == null ? Style.EMPTY : plainStyle;

        MarkupStream stream = MarkupParser.stream(original);
        String text = stream.plainText();
        List<MarkupInstruction> instructions = stream.instructions();
        Deque<ActiveMarkup> stack = new ArrayDeque<>();
        int pointer = 0;

        while (pointer < instructions.size() && instructions.get(pointer).position() < startIndex) {
            currentStyle = applyInstruction(stack, currentStyle, instructions.get(pointer++));
        }

        int length = text.length();
        for (int index = Math.max(startIndex, 0); index < length; ++index) {
            while (pointer < instructions.size() && instructions.get(pointer).position() < index) {
                currentStyle = applyInstruction(stack, currentStyle, instructions.get(pointer++));
            }

            if (pointer < instructions.size() && instructions.get(pointer).position() == index) {
                int end = pointer;
                while (end < instructions.size() && instructions.get(end).position() == index) {
                    end++;
                }

                for (int i = pointer; i < end; ++i) {
                    MarkupInstruction instruction = instructions.get(i);
                    if (instruction.type() == MarkupInstructionType.CLOSE) {
                        currentStyle = closeTag(stack, instruction.name(), currentStyle);
                    }
                }

                for (int i = pointer; i < end; ++i) {
                    MarkupInstruction instruction = instructions.get(i);
                    if (instruction.type() == MarkupInstructionType.OPEN) {
                        currentStyle = openTag(stack, currentStyle, instruction);
                    }
                }

                pointer = end;
            }

            if (index >= text.length()) {
                break;
            }

            char ch = text.charAt(index);
            if (ch == '\u00a7') {
                if (index + 1 >= length) {
                    break;
                }
                char code = text.charAt(index + 1);
                ChatFormatting formatting = ChatFormatting.getByCode(code);
                if (formatting != null) {
                    if (formatting == ChatFormatting.RESET) {
                        stack.clear();
                        currentStyle = resetStyle;
                    } else {
                        currentStyle = currentStyle.applyLegacyFormat(formatting);
                    }
                }
                ++index;
                continue;
            }

            if (Character.isHighSurrogate(ch)) {
                if (index + 1 >= length) {
                    if (!sink.accept(index, currentStyle, 0xFFFD)) {
                        return false;
                    }
                    break;
                }
                char next = text.charAt(index + 1);
                if (Character.isLowSurrogate(next)) {
                    int codePoint = Character.toCodePoint(ch, next);
                    if (!sink.accept(index, currentStyle, codePoint)) {
                        return false;
                    }
                    ++index;
                    continue;
                }
                if (!sink.accept(index, currentStyle, 0xFFFD)) {
                    return false;
                }
                continue;
            }

            if (!StringDecomposerAccess.callFeedChar(currentStyle, sink, index, ch)) {
                return false;
            }
        }

        return true;
    }

    private static Style applyInstruction(Deque<ActiveMarkup> stack, Style currentStyle, MarkupInstruction instruction) {
        return instruction.type() == MarkupInstructionType.OPEN
                ? openTag(stack, currentStyle, instruction)
                : closeTag(stack, instruction.name(), currentStyle);
    }

    private static Style openTag(Deque<ActiveMarkup> stack, Style currentStyle, MarkupInstruction instruction) {
        Style previous = currentStyle;
        Style nextStyle = previous;
        SpanEffectRegistry.SpanEffectFactory factory = SpanEffectRegistry.getFactory(instruction.name());
        SpanEffectRegistry.ActiveSpanEffect effect = null;
        if (factory != null) {
            effect = factory.create(new SpanEffectRegistry.TagContext(instruction.name(), instruction.attributes()));
            if (effect != null) {
                nextStyle = applyEffect(previous, effect);
            }
        }
        stack.addLast(new ActiveMarkup(instruction.name(), previous, effect));
        return nextStyle;
    }

    private static Style closeTag(Deque<ActiveMarkup> stack, String name, Style fallback) {
        if (stack.isEmpty()) {
            return fallback;
        }

        Iterator<ActiveMarkup> descending = stack.descendingIterator();
        while (descending.hasNext()) {
            ActiveMarkup entry = descending.next();
            if (!entry.name().equals(name)) {
                continue;
            }
            while (!stack.isEmpty()) {
                ActiveMarkup removed = stack.removeLast();
                if (removed == entry) {
                    return removed.previousStyle();
                }
            }
            break;
        }
        return fallback;
    }

    private static Style applyEffect(Style baseStyle, SpanEffectRegistry.ActiveSpanEffect effect) {
        Style working = cloneStyle(baseStyle);
        TextSpan span = new TextSpan("");
        effect.applyToTextSpan(span);
        working = SpanStyleAdapter.applyTextSpan(working, span);
        SpanEffectState state = ((EmbersStyle) (Object) working).emberstextapi$getOrCreateSpanEffectState();
        effect.applyToEffectState(state);
        return working;
    }

    private static Style cloneStyle(Style style) {
        Style source = style == null ? Style.EMPTY : style;
        return source.withClickEvent(source.getClickEvent());
    }

    private record ActiveMarkup(String name, Style previousStyle, SpanEffectRegistry.ActiveSpanEffect effect) {
    }
}
