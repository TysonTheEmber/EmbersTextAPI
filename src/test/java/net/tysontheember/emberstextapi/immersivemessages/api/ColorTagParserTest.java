package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColorTagParserTest {

    @Test
    public void parsesBareHexColor() {
        List<TextSpan> spans = MarkupParser.parse("<color value=FFD700>Gold</color>");
        assertEquals(1, spans.size(), "Should produce one span");
        TextSpan span = spans.get(0);
        assertNotNull(span.getColor(), "Color should be parsed");
        assertEquals(TextColor.fromRgb(0xFFD700), span.getColor());
    }

    @Test
    public void parsesShortHexColor() {
        List<TextSpan> spans = MarkupParser.parse("<color value=#f80>Hi</color>");
        assertEquals(1, spans.size(), "Should produce one span");
        assertEquals(TextColor.fromRgb(0xFF8800), spans.get(0).getColor());
    }

    @Test
    public void parses0xPrefixedHexColor() {
        List<TextSpan> spans = MarkupParser.parse("<color value=0x00ff00>Green</color>");
        assertEquals(1, spans.size(), "Should produce one span");
        assertEquals(TextColor.fromRgb(0x00FF00), spans.get(0).getColor());
    }
}
