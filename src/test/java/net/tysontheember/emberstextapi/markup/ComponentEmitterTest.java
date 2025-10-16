package net.tysontheember.emberstextapi.markup;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    void markersLiveInStyleInsertionOnly() {
        MutableComponent component = ComponentEmitter.emit(EmberParser.parse("<gradient from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></gradient>"));
        assertEquals("EMBERCRAFT", component.getString());

        AtomicBoolean found = new AtomicBoolean(false);
        component.visit((style, text) -> {
            Optional.ofNullable(style.getInsertion())
                .filter(s -> s.startsWith("ember:attr:"))
                .ifPresent(s -> found.set(true));
            return Optional.empty();
        }, net.minecraft.network.chat.Style.EMPTY);

        assertTrue(found.get(), "Style insertion should contain overlay marker payload");
    }
}
