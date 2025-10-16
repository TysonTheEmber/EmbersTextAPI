package net.tysontheember.emberstextapi.overlay;

import net.minecraft.network.chat.Component;

/**
 * Represents a laid out component chunk. This is currently a lightweight
 * placeholder used by the attribute handlers when queuing overlay effects.
 */
public record LayoutRun(Component component, float x, float y, float width) {
}
