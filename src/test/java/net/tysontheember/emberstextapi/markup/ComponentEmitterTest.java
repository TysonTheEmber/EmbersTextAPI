package net.tysontheember.emberstextapi.markup;

import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.attributes.BuiltinAttributes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentEmitterTest {
    @BeforeAll
    static void initAttributes() {
        BuiltinAttributes.init();
    }

    @Test
    void emitsVanillaStyles() {
        RSpan ast = EmberParser.parse("<bold><color value=#00ff00>Hi</color></bold>");
        MutableComponent component = ComponentEmitter.emit(ast);
        assertEquals("Hi", component.getString());
        assertTrue(component.getStyle().isBold());
        assertEquals(0x00FF00, component.getStyle().getColor().getValue());
    }
}
