package net.tysontheember.emberstextapi.network;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.NoOpEffect;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies server-side config limits to messages before they are sent to clients.
 * Enforces maxServerMessageDuration, maxServerActiveMessages, maxQueueSize, and allowedEffects.
 */
public final class ServerMessageLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/ServerLimiter");

    private ServerMessageLimiter() {
    }

    /**
     * Apply server-side limits to a message before sending.
     * Caps duration and filters disallowed effects.
     */
    public static void sanitize(ImmersiveMessage message) {
        if (message == null) return;

        try {
            ConfigHelper config = ConfigHelper.getInstance();

            // Cap duration
            int maxDuration = config.getMaxServerMessageDuration();
            if (maxDuration > 0) {
                int msgDuration = message.durationTicks();
                if (msgDuration <= 0 || msgDuration > maxDuration) {
                    message.setDuration(maxDuration);
                    LOGGER.debug("Capped message duration to {} ticks (server limit)", maxDuration);
                }
            }

            // Filter effects via allowlist
            List<String> allowed = config.getAllowedEffects();
            if (!allowed.isEmpty()) {
                filterEffects(message, allowed);
            }
        } catch (Exception e) {
            LOGGER.trace("Could not apply server limits: {}", e.getMessage());
        }
    }

    /**
     * Apply server-side limits to all messages in a queue.
     * Also enforces maxQueueSize by truncating the steps list.
     *
     * @return the (potentially truncated) steps list
     */
    public static List<List<ImmersiveMessage>> sanitizeQueue(List<List<ImmersiveMessage>> steps) {
        if (steps == null || steps.isEmpty()) return steps;

        try {
            ConfigHelper config = ConfigHelper.getInstance();

            // Enforce maxQueueSize
            int maxQueueSize = config.getMaxQueueSize();
            if (maxQueueSize > 0 && steps.size() > maxQueueSize) {
                steps = new ArrayList<>(steps.subList(0, maxQueueSize));
                LOGGER.debug("Truncated queue to {} steps (server limit)", maxQueueSize);
            }

            // Sanitize each message in each step
            for (List<ImmersiveMessage> step : steps) {
                for (ImmersiveMessage msg : step) {
                    sanitize(msg);
                }
            }
        } catch (Exception e) {
            LOGGER.trace("Could not apply server queue limits: {}", e.getMessage());
        }

        return steps;
    }

    private static void filterEffects(ImmersiveMessage message, List<String> allowed) {
        List<String> lowerAllowed = allowed.stream()
                .map(String::toLowerCase)
                .toList();

        for (TextSpan span : message.getSpans()) {
            List<Effect> effects = span.getEffects();
            if (effects == null || effects.isEmpty()) continue;
            for (int i = 0; i < effects.size(); i++) {
                Effect effect = effects.get(i);
                if (!lowerAllowed.contains(effect.getName().toLowerCase())) {
                    effects.set(i, new NoOpEffect(effect.getName()));
                }
            }
        }
    }
}
