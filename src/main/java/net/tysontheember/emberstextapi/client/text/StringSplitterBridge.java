package net.tysontheember.emberstextapi.client.text;

import java.util.List;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * Bridge interface implemented via mixin to access original StringSplitter behaviour.
 */
public interface StringSplitterBridge {
    float emberstextapi$callStringWidthOriginal(FormattedCharSequence sequence);

    String emberstextapi$callPlainSubstrOriginal(String text, int maxWidth, Style style);

    List<FormattedCharSequence> emberstextapi$callSplitLinesOriginal(FormattedText text, int maxWidth, Style baseStyle);
}
