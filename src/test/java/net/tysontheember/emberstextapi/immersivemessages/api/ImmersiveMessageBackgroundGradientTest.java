package net.tysontheember.emberstextapi.immersivemessages.api;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class ImmersiveMessageBackgroundGradientTest {
    @Test
    public void backgroundGradientEncodesAndDecodes() throws Exception {
        ImmersiveMessage msg = ImmersiveMessage.builder(20f, "test")
                .background(true)
                .backgroundGradient(0xFFFF0000, 0xFF00FF00);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        msg.encode(buf);
        ImmersiveMessage decoded = ImmersiveMessage.decode(buf);
        Field f = ImmersiveMessage.class.getDeclaredField("backgroundGradientStops");
        f.setAccessible(true);
        ImmersiveColor[] stops = (ImmersiveColor[]) f.get(decoded);
        assertNotNull(stops);
        assertEquals(2, stops.length);
        assertEquals(0xFFFF0000, stops[0].getARGB());
        assertEquals(0xFF00FF00, stops[1].getARGB());
    }
}
