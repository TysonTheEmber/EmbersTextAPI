package net.tysontheember.emberstextapi.integrations;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "emberstextapi")
public final class TooltipInterceptor {
    private TooltipInterceptor() {
    }

    @SubscribeEvent
    public static void gather(RenderTooltipEvent.GatherComponents event) {
        // Placeholder: tooltip markup support will be added in a follow-up once
        // the overlay renderer can access tooltip layout data safely.
    }
}
