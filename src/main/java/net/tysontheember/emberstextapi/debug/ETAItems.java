package net.tysontheember.emberstextapi.debug;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tysontheember.emberstextapi.EmbersTextAPI;

/**
 * Debug item registrations for the ETA harness.
 */
public final class ETAItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EmbersTextAPI.MODID);

    public static final RegistryObject<Item> DEBUG_TEXT_ITEM =
            ITEMS.register("debug_text", () -> new DebugTextItem(new Item.Properties().stacksTo(1)));

    private ETAItems() {
    }
}
