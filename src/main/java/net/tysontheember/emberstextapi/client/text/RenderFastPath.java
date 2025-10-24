package net.tysontheember.emberstextapi.client.text;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.duck.ETAStyle;

/**
 * Utility methods for skipping custom rendering when not required.
 */
public final class RenderFastPath {
    private RenderFastPath() {
    }

    public static boolean shouldBypass(Style style) {
        if (!EffectContext.areAnimationsEnabled()) {
            return true;
        }
        if (!(style instanceof ETAStyle etaStyle)) {
            return true;
        }
        return etaStyle.eta$getEffects().isEmpty();
    }
}
