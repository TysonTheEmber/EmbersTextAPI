package net.tysontheember.emberstextapi.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbersTextTest {

    @Test
    void parseReturnsEmptyWhenNull() {
        AttributedText text = EmbersText.parse(null);
        assertNotNull(text);
        assertEquals("", text.getText());
        assertTrue(text.getSpans().isEmpty());
    }

    @Test
    void toComponentAppliesStyles() {
        AttributeSet attributes = AttributeSet.builder()
                .color("#3366FF")
                .style(AttributeSet.Style.builder().bold(true).italic(true).build())
                .build();
        AttributedText text = AttributedText.builder()
                .text("Hi")
                .addSpan(Span.builder().start(0).end(2).attributes(attributes).build())
                .build();

        Component component = EmbersText.toComponent(text);
        assertEquals("Hi", component.getString());
        assertFalse(component.getSiblings().isEmpty());
        Style style = component.getSiblings().get(0).getStyle();
        assertTrue(style.isBold());
        assertTrue(style.isItalic());
        TextColor colour = style.getColor();
        assertNotNull(colour);
        assertEquals(TextColor.parseColor("#3366FF").getValue(), colour.getValue());
    }

    @Test
    void toComponentHandlesNull() {
        Component component = EmbersText.toComponent(null);
        assertEquals("", component.getString());
    }
}
