package net.tysontheember.emberstextapi.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.client.cache.SpanCacheInvalidation;
import net.tysontheember.emberstextapi.config.ClientSettings;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SettingsScreenExt {
    private static final Component LABEL = Component.literal("Enable Styled Rendering");
    private static final Component TOOLTIP = Component.literal("Toggle global markup styling for chat, tooltips, and UI text.");
    private static final int TOGGLE_WIDTH = 310;
    private static final int TOGGLE_HEIGHT = 20;

    private SettingsScreenExt() {
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!shouldHandle(screen)) {
            return;
        }
        StyledRenderingCheckbox checkbox = createCheckbox(screen);
        if (checkbox == null) {
            return;
        }
        event.addListener(checkbox);
    }

    static boolean shouldHandle(Screen screen) {
        return screen instanceof AccessibilityOptionsScreen || screen instanceof VideoSettingsScreen;
    }

    private static StyledRenderingCheckbox createCheckbox(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.font == null) {
            return null;
        }
        int x = screen.width / 2 - TOGGLE_WIDTH / 2;
        int y = Math.max(40, screen.height - 53);
        StyledRenderingCheckbox checkbox = new StyledRenderingCheckbox(x, y);
        checkbox.setTooltip(Tooltip.create(TOOLTIP));
        return checkbox;
    }

    private static final class StyledRenderingCheckbox extends Checkbox {
        StyledRenderingCheckbox(int x, int y) {
            super(x, y, TOGGLE_WIDTH, TOGGLE_HEIGHT, LABEL, ClientSettings.isStyledRenderingEnabled(), true);
        }

        @Override
        public void onPress() {
            super.onPress();
            boolean enabled = this.selected();
            ClientSettings.setStyledRenderingEnabled(enabled);
            SpanCacheInvalidation.clearSpanCaches("styled rendering toggle -> " + enabled);
        }
    }
}
