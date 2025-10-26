package net.tysontheember.emberstextapi.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.tysontheember.emberstextapi.client.text.TypewriterGate;
import org.lwjgl.glfw.GLFW;

/**
 * Client key bindings for toggling EmbersTextAPI features.
 */
public final class ETAKeybinds {
    private static final String CATEGORY = "key.categories.emberstextapi";

    public static final KeyMapping TOGGLE_GLOBAL_SPANS = new KeyMapping(
        "key.emberstextapi.toggle_global_spans",
        GLFW.GLFW_KEY_F9,
        CATEGORY
    );

    public static final KeyMapping TOGGLE_TYPEWRITER = new KeyMapping(
        "key.emberstextapi.toggle_typewriter",
        GLFW.GLFW_KEY_F10,
        CATEGORY
    );

    private ETAKeybinds() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_GLOBAL_SPANS);
        event.register(TOGGLE_TYPEWRITER);
    }

    public static void handleClientTick(Minecraft mc) {
        if (mc == null) {
            return;
        }
        while (TOGGLE_GLOBAL_SPANS.consumeClick()) {
            boolean newValue = !GlobalSwitches.enabled();
            GlobalSwitches.setGlobalSpansEnabled(newValue);
            showToast(mc, Component.literal("Embers Text API"),
                Component.literal("Global spans: " + onOff(newValue)));
        }
        while (TOGGLE_TYPEWRITER.consumeClick()) {
            boolean newValue = !GlobalSwitches.typewriterEnabled();
            GlobalSwitches.setTypewriterEnabled(newValue);
            TypewriterGate.setEnabled(newValue);
            showToast(mc, Component.literal("Embers Text API"),
                Component.literal("Typewriter: " + onOff(newValue)));
        }
    }

    private static void showToast(Minecraft mc, Component title, Component body) {
        mc.getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, title, body));
    }

    private static String onOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }
}
