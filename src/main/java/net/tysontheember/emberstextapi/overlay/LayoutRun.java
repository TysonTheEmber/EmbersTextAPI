package net.tysontheember.emberstextapi.overlay;

import net.minecraft.network.chat.Component;

/**
 * Represents a laid out segment of text scheduled for overlay rendering.
 */
public record LayoutRun(Component component, float x, float y, float scale) {
}
