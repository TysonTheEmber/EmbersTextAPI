package net.tysontheember.emberstextapi.debug;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tysontheember.emberstextapi.core.markup.SpanText;

public class DebugTextItem extends Item {
    public DebugTextItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createSample(String key) {
        ItemStack stack = new ItemStack(ETAItems.DEBUG_TEXT_ITEM.get());
        stack.setHoverName(SpanText.parse(DebugSamples.require(key)));
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(DebugSamples.SAMPLE_NBT_KEY, key);
        return stack;
    }

    public static Optional<String> getSampleKey(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(DebugSamples.SAMPLE_NBT_KEY)) {
            return Optional.empty();
        }
        return Optional.of(tag.getString(DebugSamples.SAMPLE_NBT_KEY));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        getSampleKey(stack).ifPresent(key -> {
            tooltip.add(Component.literal("Sample: " + key).withStyle(ChatFormatting.GRAY));
            DebugSamples.get(key).ifPresent(sample -> {
                tooltip.add(SpanText.parse(sample));
                tooltip.add(SpanText.parse("<italic>/eta debug chat " + key + "</italic>"));
            });
        });
    }
}
