package net.tysontheember.emberstextapi.debug;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import org.lwjgl.glfw.GLFW;

/**
 * Handles debug hotkeys for overlay toggles and level cycling. The actual state changes are
 * delegated to {@link DebugCommand} so tests and command handlers reuse the same logic.
 */
public final class DebugHotkeys {
    private static final String SOURCE = "hotkey";
    private static final String CATEGORY = "key.categories.emberstextapi";

    private static final KeyMapping OVERLAY_TOGGLE = new KeyMapping(
            "key.emberstextapi.debug.overlayToggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY);

    private static final KeyMapping OVERLAY_LEVEL = new KeyMapping(
            "key.emberstextapi.debug.overlayLevel",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY);

    private DebugHotkeys() {
    }

    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OVERLAY_TOGGLE);
        event.register(OVERLAY_LEVEL);
    }

    static void handleClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        if (minecraft.options == null || minecraft.options.keyDebugMenu == null) {
            return;
        }

        boolean debugHeld = minecraft.options.keyDebugMenu.isDown();
        if (!debugHeld) {
            // Consume pending clicks so the next debug press does not trigger immediately.
            OVERLAY_TOGGLE.consumeClick();
            OVERLAY_LEVEL.consumeClick();
            return;
        }

        while (OVERLAY_TOGGLE.consumeClick()) {
            triggerOverlayToggle();
        }

        while (OVERLAY_LEVEL.consumeClick()) {
            triggerOverlayLevelCycle();
        }
    }

    static void triggerOverlayToggle() {
        DebugCommand.applyOverlayToggle(!DebugFlags.isOverlayFlagEnabled(), SOURCE);
    }

    static void triggerOverlayLevelCycle() {
        int nextLevel = (DebugFlags.getOverlayLevel() + 1) % (DebugOverlay.MAX_LEVEL + 1);
        DebugCommand.applyOverlayLevel(nextLevel, SOURCE);
    }

    @Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            DebugHotkeys.registerKeyMappings(event);
        }
    }

    @Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT)
    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            DebugHotkeys.handleClientTick(event);
        }
    }
}
