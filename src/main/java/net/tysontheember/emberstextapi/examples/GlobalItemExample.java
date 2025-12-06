package net.tysontheember.emberstextapi.examples;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.util.StyleUtil;

/**
 * Example demonstrating global item tag usage with the Style system.
 * <p>
 * This class shows various ways to use the global item rendering functionality
 * that's now available through mixins on the Style class.
 * </p>
 */
public class GlobalItemExample {

    /**
     * Create a simple message with an inline item icon.
     */
    public static Component createSimpleItemMessage() {
        Style diamondStyle = StyleUtil.withItem("minecraft:diamond");
        Component diamondIcon = Component.literal(" ").withStyle(diamondStyle);

        return Component.literal("You found a ")
                .append(diamondIcon)
                .append(Component.literal(" diamond!"));
    }

    /**
     * Create a message with multiple items and styling.
     */
    public static Component createMultiItemMessage() {
        // Create item styles
        Style ironStyle = StyleUtil.withItem("minecraft:iron_ingot", 64);
        Style goldStyle = StyleUtil.withItem("minecraft:gold_ingot", 32);
        Style emeraldStyle = StyleUtil.withItem("minecraft:emerald", 16);

        // Build message
        return Component.literal("Inventory: ")
                .withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" ").withStyle(ironStyle))
                .append(Component.literal(" "))
                .append(Component.literal(" ").withStyle(goldStyle))
                .append(Component.literal(" "))
                .append(Component.literal(" ").withStyle(emeraldStyle));
    }

    /**
     * Create a message with items and custom positioning.
     */
    public static Component createCustomPositionedItemMessage() {
        // Offset the item slightly to align better with text
        Style swordStyle = StyleUtil.withItem("minecraft:diamond_sword", 1, 0f, -2f);
        Component swordIcon = Component.literal(" ").withStyle(swordStyle);

        return Component.literal("Weapon: ")
                .append(swordIcon)
                .append(Component.literal(" Diamond Sword"));
    }

    /**
     * Create a message combining items with other style properties.
     */
    public static Component createStyledItemMessage() {
        // Create a red-colored style and add item to it
        Style baseStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555));
        Style redItemStyle = StyleUtil.cloneAndAddItem(baseStyle, "minecraft:redstone", 64, 0f, 0f);

        return Component.literal("Redstone: ")
                .append(Component.literal(" ").withStyle(redItemStyle))
                .append(Component.literal(" x64").withStyle(baseStyle));
    }

    /**
     * Create a recipe-like message showing ingredients.
     */
    public static Component createRecipeMessage() {
        Style plankStyle = StyleUtil.withItem("minecraft:oak_planks", 4);
        Style stickStyle = StyleUtil.withItem("minecraft:stick", 2);
        Style craftingTableStyle = StyleUtil.withItem("minecraft:crafting_table", 1);

        MutableComponent recipe = Component.literal("Recipe: ").withStyle(Style.EMPTY.withBold(true));
        recipe.append(Component.literal("\n"));
        recipe.append(Component.literal(" ").withStyle(plankStyle));
        recipe.append(Component.literal(" + "));
        recipe.append(Component.literal(" ").withStyle(stickStyle));
        recipe.append(Component.literal(" = "));
        recipe.append(Component.literal(" ").withStyle(craftingTableStyle));

        return recipe;
    }

    /**
     * Create a trading-style message.
     */
    public static Component createTradeMessage() {
        Style emeraldStyle = StyleUtil.withItem("minecraft:emerald", 3);
        Style bookStyle = StyleUtil.withItem("minecraft:enchanted_book", 1);

        return Component.literal("Trade: ")
                .append(Component.literal(" ").withStyle(emeraldStyle))
                .append(Component.literal(" x3 → "))
                .append(Component.literal(" ").withStyle(bookStyle))
                .append(Component.literal(" Enchanted Book"));
    }

    /**
     * Example of using items in a list or menu.
     */
    public static Component createItemList() {
        MutableComponent list = Component.literal("Available Items:").withStyle(Style.EMPTY.withBold(true));
        list.append(Component.literal("\n"));

        String[] items = {
                "minecraft:diamond",
                "minecraft:emerald",
                "minecraft:gold_ingot",
                "minecraft:iron_ingot",
                "minecraft:netherite_ingot"
        };

        for (String itemId : items) {
            Style itemStyle = StyleUtil.withItem(itemId, 1);
            list.append(Component.literal("  • "));
            list.append(Component.literal(" ").withStyle(itemStyle));
            list.append(Component.literal(" " + itemId.split(":")[1]));
            list.append(Component.literal("\n"));
        }

        return list;
    }

    /**
     * Example showing conditional item display based on game state.
     */
    public static Component createConditionalItemMessage(boolean hasItem, String itemId) {
        if (hasItem) {
            Style itemStyle = StyleUtil.withItem(itemId, 1);
            return Component.literal("You have: ")
                    .append(Component.literal(" ").withStyle(itemStyle))
                    .append(Component.literal(" " + itemId));
        } else {
            return Component.literal("You don't have this item yet!")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA)));
        }
    }
}
