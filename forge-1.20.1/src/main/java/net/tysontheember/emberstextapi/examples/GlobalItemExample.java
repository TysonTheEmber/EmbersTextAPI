package net.tysontheember.emberstextapi.examples;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.util.StyleUtil;

public class GlobalItemExample {

    public static Component createSimpleItemMessage() {
        Style diamondStyle = StyleUtil.withItem("minecraft:diamond");
        Component diamondIcon = Component.literal(" ").withStyle(diamondStyle);

        return Component.literal("You found a ")
                .append(diamondIcon)
                .append(Component.literal(" diamond!"));
    }

    public static Component createMultiItemMessage() {

        Style ironStyle = StyleUtil.withItem("minecraft:iron_ingot", 64);
        Style goldStyle = StyleUtil.withItem("minecraft:gold_ingot", 32);
        Style emeraldStyle = StyleUtil.withItem("minecraft:emerald", 16);

        return Component.literal("Inventory: ")
                .withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" ").withStyle(ironStyle))
                .append(Component.literal(" "))
                .append(Component.literal(" ").withStyle(goldStyle))
                .append(Component.literal(" "))
                .append(Component.literal(" ").withStyle(emeraldStyle));
    }

    public static Component createCustomPositionedItemMessage() {

        Style swordStyle = StyleUtil.withItem("minecraft:diamond_sword", 1, 0f, -2f);
        Component swordIcon = Component.literal(" ").withStyle(swordStyle);

        return Component.literal("Weapon: ")
                .append(swordIcon)
                .append(Component.literal(" Diamond Sword"));
    }

    public static Component createStyledItemMessage() {

        Style baseStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555));
        Style redItemStyle = StyleUtil.cloneAndAddItem(baseStyle, "minecraft:redstone", 64, 0f, 0f);

        return Component.literal("Redstone: ")
                .append(Component.literal(" ").withStyle(redItemStyle))
                .append(Component.literal(" x64").withStyle(baseStyle));
    }

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

    public static Component createTradeMessage() {
        Style emeraldStyle = StyleUtil.withItem("minecraft:emerald", 3);
        Style bookStyle = StyleUtil.withItem("minecraft:enchanted_book", 1);

        return Component.literal("Trade: ")
                .append(Component.literal(" ").withStyle(emeraldStyle))
                .append(Component.literal(" x3 → "))
                .append(Component.literal(" ").withStyle(bookStyle))
                .append(Component.literal(" Enchanted Book"));
    }

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
