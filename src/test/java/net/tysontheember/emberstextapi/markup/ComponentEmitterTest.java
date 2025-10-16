package net.tysontheember.emberstextapi.markup;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentEmitterTest {
    @BeforeAll
    static void setup() {
        // Trigger static registration
        EmberMarkup.parse("");
    }

    @Test
    void appliesVanillaStyle() {
        MutableComponent component = EmberMarkup.toComponent("<bold><color from=#123456>Test</color></bold>");
        assertFalse(component.getSiblings().isEmpty());
        Style style = component.getSiblings().get(0).getStyle();
        assertEquals(TextColor.fromRgb(0x123456), style.getColor());
        assertTrue(style.isBold());
    }

    @Test
    void emitsMarkersForOverlay() {
        RSpan span = new RSpan("wave", java.util.Map.of("amp", "1.0"), java.util.List.of(new RNode.RText("Hi")));
        MutableComponent component = ComponentEmitter.toComponent(span, new AttributeContext(true, new ResourceLocation("emberstextapi", "test")));
        assertFalse(component.getSiblings().isEmpty());
        String marker = component.getSiblings().get(component.getSiblings().size() - 1).getString();
        assertTrue(marker.startsWith("\u200C{ember:attr:"));
    }
}
