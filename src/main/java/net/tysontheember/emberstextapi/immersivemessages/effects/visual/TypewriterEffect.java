package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.typewriter.TypewriterController;
import net.tysontheember.emberstextapi.typewriter.TypewriterState;
import net.tysontheember.emberstextapi.util.ViewStateTracker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple typewriter effect - reveals text character by character.
 *
 * Parameters:
 * - speed: milliseconds per character (default: 20)
 * - s: (legacy) characters per second
 */
public class TypewriterEffect extends BaseEffect {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterEffect.class);

    private final int speedMs;

    public TypewriterEffect(@NotNull Params params) {
        super(params);

        // Parse speed (prefer "speed" in ms, fallback to legacy "s" in chars/sec)
        this.speedMs = params.getDouble("speed")
                .or(() -> params.getDouble("s").map(s -> 1000.0 / s))
                .map(Number::intValue)
                .orElse(20);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Determine context from ViewStateTracker
        String context = getContext(settings);

        // Get typewriter state for this context
        TypewriterState state = TypewriterController.getInstance().getState(context, speedMs);

        // Hide character if not yet revealed
        if (!state.isCharVisible(settings.index)) {
            settings.a = 0.0f;
        }
    }

    private String getContext(EffectSettings settings) {
        // Priority 1: Quest context
        String quest = ViewStateTracker.getCurrentQuestContext();
        if (quest != null) {
            return quest;
        }

        // Priority 2: Tooltip context
        String tooltip = ViewStateTracker.getCurrentTooltipContext();
        if (tooltip != null) {
            return tooltip;
        }

        // Priority 3: Screen context
        String screen = ViewStateTracker.getCurrentScreenContext();
        if (screen != null) {
            return screen;
        }

        // Fallback
        return "default";
    }

    @NotNull
    @Override
    public String getName() {
        return "typewriter";
    }
}
