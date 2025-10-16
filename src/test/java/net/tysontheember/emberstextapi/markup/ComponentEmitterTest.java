package net.tysontheember.emberstextapi.markup;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentEmitterTest {
    @Test
    void boldTagSetsStyle() {
        MutableComponent component = ComponentEmitter.emit(EmberParser.parse("<bold>Hi</bold>"));
        assertTrue(component.getStyle().isBold(), "Bold span should set style on emitted component");
        assertEquals("Hi", component.getString());
    }

    @Test
    void colorTagAppliesHexColor() {
        MutableComponent component = ComponentEmitter.emit(EmberParser.parse("<color value=#00ff00>Go</color>"));
        TextColor color = component.getStyle().getColor();
        assertNotNull(color);
        assertEquals(0x00FF00, color.getValue());
    }
}
