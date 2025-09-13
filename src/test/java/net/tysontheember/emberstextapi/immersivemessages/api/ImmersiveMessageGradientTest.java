package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class ImmersiveMessageGradientTest {
    private static MutableComponent getCurrent(ImmersiveMessage msg) throws Exception {
        Field f = ImmersiveMessage.class.getDeclaredField("current");
        f.setAccessible(true);
        return (MutableComponent) f.get(msg);
    }

    private static TextColor colorOfChar(MutableComponent comp, int index) {
        return ((MutableComponent) comp.getSiblings().get(index)).getStyle().getColor();
    }

    @Test
    public void simpleGradientAppliesColors() throws Exception {
        ImmersiveMessage msg = ImmersiveMessage.builder(20f, "ab")
                .gradient(0xFF0000, 0x00FF00);
        MutableComponent current = getCurrent(msg);
        assertEquals("ab", current.getString());
        assertEquals(0xFF0000, colorOfChar(current, 0).getValue());
        assertEquals(0x00FF00, colorOfChar(current, 1).getValue());
    }

    @Test
    public void gradientWithTypewriterProgresses() throws Exception {
        ImmersiveMessage msg = ImmersiveMessage.builder(20f, "ab")
                .typewriter(1f)
                .gradient(0x000000, 0xFFFFFF);
        msg.tick(1f);
        MutableComponent current = getCurrent(msg);
        assertEquals(1, current.getString().length());
        assertEquals(0x000000, colorOfChar(current, 0).getValue());
        msg.tick(1f);
        current = getCurrent(msg);
        assertEquals(2, current.getString().length());
        assertEquals(0xFFFFFF, colorOfChar(current, 1).getValue());
    }

    @Test
    public void gradientWithObfuscationUsesColors() throws Exception {
        ImmersiveMessage msg = ImmersiveMessage.builder(20f, "ab")
                .gradient(0x0000FF, 0x00FF00)
                .obfuscate(ObfuscateMode.LEFT, 1f);
        MutableComponent current = getCurrent(msg);
        assertEquals(2, current.getSiblings().size());
        assertTrue(((MutableComponent) current.getSiblings().get(0)).getStyle().isObfuscated());
        assertEquals(0x0000FF, colorOfChar(current, 0).getValue());
        msg.tick(1f);
        current = getCurrent(msg);
        assertFalse(((MutableComponent) current.getSiblings().get(0)).getStyle().isObfuscated());
        assertEquals(0x0000FF, colorOfChar(current, 0).getValue());
    }

    @Test
    public void gradientListAppliesMultipleStops() throws Exception {
        ImmersiveMessage msg = ImmersiveMessage.builder(20f, "abc")
                .gradient(0xFF0000, 0x00FF00, 0x0000FF);
        MutableComponent current = getCurrent(msg);
        assertEquals(3, current.getString().length());
        assertEquals(0xFF0000, colorOfChar(current, 0).getValue());
        assertEquals(0x00FF00, colorOfChar(current, 1).getValue());
        assertEquals(0x0000FF, colorOfChar(current, 2).getValue());
    }
}
