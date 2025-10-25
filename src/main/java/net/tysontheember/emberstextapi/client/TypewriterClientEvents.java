package net.tysontheember.emberstextapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.client.text.TypewriterGate;

/**
 * Client event hooks supporting typewriter gating.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT)
public final class TypewriterClientEvents {
    private static Screen lastScreen;
    private static boolean tooltipContextPushed;

    private TypewriterClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        TypewriterGate.tick();
        Screen current = Minecraft.getInstance().screen;
        if (current != lastScreen) {
            TypewriterGate.clearTooltipSession();
            tooltipContextPushed = false;
            lastScreen = current;
        }
    }

    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        Screen screen = Minecraft.getInstance().screen;
        ItemStack stack = event.getItemStack();
        String screenKey = screen == null ? "null" : screen.getClass().getName();
        String stackKey = stack.isEmpty() ? "empty" : stack.getDescriptionId();
        String contextKey = screenKey + ':' + stackKey;
        TypewriterGate.markTooltipActive(contextKey);
        if (tooltipContextPushed) {
            TypewriterGate.popContext();
            tooltipContextPushed = false;
        }
        TypewriterGate.pushContext(TypewriterGate.Surface.TOOLTIP, contextKey);
        tooltipContextPushed = true;
    }

    // Context is cleared on the next tooltip render or via clearTooltipSession().
}
