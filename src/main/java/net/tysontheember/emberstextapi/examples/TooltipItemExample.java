package net.tysontheember.emberstextapi.examples;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Example demonstrating how to use {@code <item>} tags in item tooltips.
 * <p>
 * This class shows various tooltip patterns using inline item rendering
 * with the global markup system.
 * </p>
 * <p>
 * Register this class as an event handler to see the examples in action:
 * <pre>{@code
 * MinecraftForge.EVENT_BUS.register(TooltipItemExample.class);
 * }</pre>
 * </p>
 */
public class TooltipItemExample {

    /**
     * Example: Simple crafting recipe tooltip.
     * <p>
     * This is demonstration code only. To activate, register this class
     * as a Forge event handler in your mod's setup:
     * <pre>{@code MinecraftForge.EVENT_BUS.register(TooltipItemExample.class);}</pre>
     * </p>
     */
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Add recipe tooltip to crafting table
        if (stack.is(Items.CRAFTING_TABLE)) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("<bold>Recipe:</bold>"));
            event.getToolTip().add(Component.literal("  <item id=minecraft:oak_planks count=4/> x4 Planks"));
        }

        // Add smelting recipe to iron ingot
        else if (stack.is(Items.IRON_INGOT)) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("<bold>Smelting:</bold>"));
            event.getToolTip().add(Component.literal("  <item id=minecraft:raw_iron/> + <item id=minecraft:coal/> → <item id=minecraft:iron_ingot/>"));
        }

        // Add trading info to emerald
        else if (stack.is(Items.EMERALD)) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("<bold><color value=#00AA00>Villager Currency</color></bold>"));
            event.getToolTip().add(Component.literal("Trade with villagers using <item id=minecraft:emerald/>"));
        }

        // Add fuel info to coal
        else if (stack.is(Items.COAL)) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("<bold>Fuel</bold>"));
            event.getToolTip().add(Component.literal("Burns 8 items in a <item id=minecraft:furnace y=-1/>"));
        }

        // Add upgrade info to diamond gear
        else if (stack.is(Items.DIAMOND_SWORD) || stack.is(Items.DIAMOND_PICKAXE) ||
                 stack.is(Items.DIAMOND_AXE) || stack.is(Items.DIAMOND_SHOVEL)) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("<bold>Upgrade:</bold>"));
            event.getToolTip().add(Component.literal("  <item id=minecraft:netherite_ingot/> + <item id=minecraft:netherite_upgrade_smithing_template/> → <rainbow>Netherite</rainbow>"));
        }

        // Add beacon pyramid info
        else if (stack.is(Items.BEACON)) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("<bold>Pyramid Base Materials:</bold>"));
            event.getToolTip().add(Component.literal("  <item id=minecraft:iron_block/>  <item id=minecraft:gold_block/>  <item id=minecraft:emerald_block/>  <item id=minecraft:diamond_block/>"));
        }
    }

    /**
     * Example custom item with detailed tooltips using item icons.
     */
    public static class CraftingGuideItem extends net.minecraft.world.item.Item {
        public CraftingGuideItem(Properties properties) {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);

            tooltip.add(Component.empty());
            tooltip.add(Component.literal("<rainbow><bold>Crafting Guide</bold></rainbow>"));
            tooltip.add(Component.empty());

            // Common recipes section
            tooltip.add(Component.literal("<bold><color value=#FFD700>Common Recipes:</color></bold>"));
            tooltip.add(Component.literal("  <item id=minecraft:stick count=4/> + <item id=minecraft:oak_planks/> = <item id=minecraft:crafting_table/>"));
            tooltip.add(Component.literal("  <item id=minecraft:iron_ingot count=3/> + <item id=minecraft:stick count=2/> = <item id=minecraft:iron_pickaxe/>"));
            tooltip.add(Component.literal("  <item id=minecraft:cobblestone count=8/> = <item id=minecraft:furnace/>"));

            tooltip.add(Component.empty());

            // Smelting recipes
            tooltip.add(Component.literal("<bold><color value=#FF5555>Smelting:</color></bold>"));
            tooltip.add(Component.literal("  <item id=minecraft:raw_iron/> → <item id=minecraft:iron_ingot/>"));
            tooltip.add(Component.literal("  <item id=minecraft:raw_gold/> → <item id=minecraft:gold_ingot/>"));
            tooltip.add(Component.literal("  <item id=minecraft:sand/> → <item id=minecraft:glass/>"));

            tooltip.add(Component.empty());

            // Tips section
            tooltip.add(Component.literal("<italic><color value=#AAAAAA>Right-click to view full guide</color></italic>"));
        }
    }

    /**
     * Example quest book item showing progression requirements.
     */
    public static class QuestBookItem extends net.minecraft.world.item.Item {
        public QuestBookItem(Properties properties) {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);

            tooltip.add(Component.empty());
            tooltip.add(Component.literal("<gradient from=#FFD700 to=#FF8C00><bold>Quest Progress</bold></gradient>"));
            tooltip.add(Component.empty());

            // Active quests
            tooltip.add(Component.literal("<bold>Active Quests:</bold>"));
            tooltip.add(Component.literal("  <color value=#00FF00>✓</color> Gather <item id=minecraft:oak_log count=64/> x64"));
            tooltip.add(Component.literal("  <color value=#00FF00>✓</color> Craft <item id=minecraft:crafting_table/> x1"));
            tooltip.add(Component.literal("  <color value=#FFAA00>○</color> Mine <item id=minecraft:iron_ore count=32/> x32"));
            tooltip.add(Component.literal("  <color value=#AAAAAA>○</color> Build <item id=minecraft:furnace/> x1"));

            tooltip.add(Component.empty());

            // Next reward
            tooltip.add(Component.literal("<bold>Next Reward:</bold>"));
            tooltip.add(Component.literal("  <item id=minecraft:diamond_pickaxe/> <rainbow>Enchanted Diamond Pickaxe</rainbow>"));

            tooltip.add(Component.empty());

            // Stats
            tooltip.add(Component.literal("<italic>Quests Completed: <bold>12/50</bold></italic>"));
        }
    }

    /**
     * Example trader item showing available trades.
     */
    public static class TraderTokenItem extends net.minecraft.world.item.Item {
        public TraderTokenItem(Properties properties) {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);

            tooltip.add(Component.empty());
            tooltip.add(Component.literal("<bold><color value=#FFD700>Trader's Token</color></bold>"));
            tooltip.add(Component.literal("<italic>Exchange for valuable items</italic>"));
            tooltip.add(Component.empty());

            // Available trades
            tooltip.add(Component.literal("<bold>Available Trades:</bold>"));
            tooltip.add(Component.literal("  <item id=minecraft:emerald count=10/> x10 → <item id=minecraft:diamond/> x1"));
            tooltip.add(Component.literal("  <item id=minecraft:gold_ingot count=32/> x32 → <item id=minecraft:netherite_scrap/> x1"));
            tooltip.add(Component.literal("  <item id=minecraft:diamond count=5/> x5 → <item id=minecraft:enchanted_golden_apple/> x1"));

            tooltip.add(Component.empty());

            // Special trades
            tooltip.add(Component.literal("<bold><color value=#AA00FF>Special:</color></bold>"));
            tooltip.add(Component.literal("  <item id=minecraft:nether_star/> → <shake><rainbow>Mystery Box</rainbow></shake>"));
        }
    }
}
