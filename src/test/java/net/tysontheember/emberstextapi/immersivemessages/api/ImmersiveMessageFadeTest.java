package net.tysontheember.emberstextapi.immersivemessages.api;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImmersiveMessageFadeTest {
    private static final float ALPHA_EPSILON = 1f / 255f;
    private static void setAge(ImmersiveMessage message, float age) throws Exception {
        setAges(message, age, age);
    }

    private static void setAges(ImmersiveMessage message, float previous, float current) throws Exception {
        Field ageField = ImmersiveMessage.class.getDeclaredField("age");
        ageField.setAccessible(true);
        ageField.setFloat(message, current);
        Field prevAgeField = ImmersiveMessage.class.getDeclaredField("previousAge");
        prevAgeField.setAccessible(true);
        prevAgeField.setFloat(message, previous);
    }

    private static float alphaOf(ImmersiveMessage message) {
        int colour = message.renderColour();
        int alpha = (colour >>> 24) & 0xFF;
        return alpha / 255f;
    }

    private static float alphaOf(ImmersiveMessage message, float partialTick) {
        int colour = message.renderColour(partialTick);
        int alpha = (colour >>> 24) & 0xFF;
        return alpha / 255f;
    }

    @Test
    public void nbtRoundTripPreservesFadeValues() {
        ImmersiveMessage message = ImmersiveMessage.builder(60f, "Fade test")
                .fadeInTicks(10)
                .fadeOutTicks(20);
        CompoundTag tag = message.toNbt();
        ImmersiveMessage decoded = ImmersiveMessage.fromNbt(tag);
        assertEquals(10, decoded.getFadeInTicks());
        assertEquals(20, decoded.getFadeOutTicks());
    }

    @Test
    public void missingFadeKeysDefaultToZero() {
        ImmersiveMessage message = ImmersiveMessage.builder(40f, "No fades");
        CompoundTag tag = message.toNbt();
        tag.remove("fadeIn");
        tag.remove("fadeOut");
        ImmersiveMessage decoded = ImmersiveMessage.fromNbt(tag);
        assertEquals(0, decoded.getFadeInTicks());
        assertEquals(0, decoded.getFadeOutTicks());
    }

    @Test
    public void networkRoundTripPreservesFadeValues() {
        ImmersiveMessage message = ImmersiveMessage.builder(80f, "Network")
                .fadeInTicks(5)
                .fadeOutTicks(15);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        message.encode(buf);
        ImmersiveMessage decoded = ImmersiveMessage.decode(buf);
        assertEquals(5, decoded.getFadeInTicks());
        assertEquals(15, decoded.getFadeOutTicks());
    }

    @Test
    public void alphaCurveHandlesAllFadeCombinations() throws Exception {
        ImmersiveMessage none = ImmersiveMessage.builder(60f, "None");
        setAge(none, 0f);
        assertEquals(1f, alphaOf(none), ALPHA_EPSILON);
        setAge(none, 60f);
        assertEquals(0f, alphaOf(none), ALPHA_EPSILON);

        ImmersiveMessage fadeInOnly = ImmersiveMessage.builder(60f, "Fade in")
                .fadeInTicks(10);
        setAge(fadeInOnly, 5f);
        assertEquals(0.5f, alphaOf(fadeInOnly), ALPHA_EPSILON);
        setAge(fadeInOnly, 20f);
        assertEquals(1f, alphaOf(fadeInOnly), ALPHA_EPSILON);

        ImmersiveMessage fadeOutOnly = ImmersiveMessage.builder(60f, "Fade out")
                .fadeOutTicks(10);
        setAge(fadeOutOnly, 65f);
        assertEquals(0.5f, alphaOf(fadeOutOnly), ALPHA_EPSILON);
        setAge(fadeOutOnly, 70f);
        assertEquals(0f, alphaOf(fadeOutOnly), ALPHA_EPSILON);

        ImmersiveMessage both = ImmersiveMessage.builder(60f, "Both")
                .fadeInTicks(10)
                .fadeOutTicks(10);
        setAge(both, 5f);
        assertEquals(0.5f, alphaOf(both), ALPHA_EPSILON);
        setAge(both, 60f);
        assertEquals(1f, alphaOf(both), ALPHA_EPSILON);
        setAge(both, 75f);
        assertEquals(0.5f, alphaOf(both), ALPHA_EPSILON);
    }

    @Test
    public void partialTickInterpolationSmoothsAlpha() throws Exception {
        ImmersiveMessage message = ImmersiveMessage.builder(40f, "Interp")
                .fadeInTicks(10);
        setAges(message, 2f, 3f);
        assertEquals(0.25f, alphaOf(message, 0.5f), ALPHA_EPSILON);
    }

    @Test
    public void totalLifetimeIncludesFades() throws Exception {
        ImmersiveMessage message = ImmersiveMessage.builder(30f, "Lifetime")
                .fadeInTicks(10)
                .fadeOutTicks(10);
        assertTrue(message.hasDuration());
        assertEquals(50, message.totalLifetimeTicks());
        setAge(message, 40f);
        assertFalse(message.isFinished());
        setAge(message, 50f);
        assertTrue(message.isFinished());
    }
}
