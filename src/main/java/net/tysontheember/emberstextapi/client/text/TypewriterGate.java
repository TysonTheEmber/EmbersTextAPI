package net.tysontheember.emberstextapi.client.text;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates typewriter playback across rendering surfaces.
 */
public final class TypewriterGate {
    private static final Map<String, TypewriterTrack> ACTIVE = new ConcurrentHashMap<>();
    private static final ThreadLocal<Deque<SurfaceContext>> CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);
    private static volatile long clientTicks;
    private static volatile boolean tooltipActive;
    private static volatile boolean tooltipWasActive;
    private static volatile String activeTooltipKey;
    private static volatile boolean typewriterEnabled = true;

    private TypewriterGate() {
    }

    public static void setEnabled(boolean enabled) {
        typewriterEnabled = enabled;
        if (!enabled) {
            ACTIVE.clear();
        }
    }

    public static boolean isEnabled() {
        return typewriterEnabled;
    }

    public static SurfaceContext currentContext() {
        Deque<SurfaceContext> stack = CONTEXT.get();
        return stack.isEmpty() ? SurfaceContext.DEFAULT : stack.peek();
    }

    public static void pushContext(Surface surface, String keyHint) {
        Deque<SurfaceContext> stack = CONTEXT.get();
        stack.push(new SurfaceContext(surface, keyHint));
    }

    public static void popContext() {
        Deque<SurfaceContext> stack = CONTEXT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public static void markTooltipActive(String key) {
        tooltipActive = true;
        if (!Objects.equals(activeTooltipKey, key)) {
            clearFor(Surface.TOOLTIP, null);
            activeTooltipKey = key;
        }
    }

    public static void clearTooltipSession() {
        tooltipActive = false;
        activeTooltipKey = null;
        clearFor(Surface.TOOLTIP, null);
        Deque<SurfaceContext> stack = CONTEXT.get();
        stack.removeIf(context -> context.surface() == Surface.TOOLTIP);
    }

    public static void tick() {
        clientTicks++;
        for (TypewriterTrack track : ACTIVE.values()) {
            track.tick(clientTicks);
        }
        if (!tooltipActive && tooltipWasActive) {
            clearFor(Surface.TOOLTIP, null);
            activeTooltipKey = null;
        }
        tooltipWasActive = tooltipActive;
        tooltipActive = false;
    }

    public static TypewriterTrack getOrCreate(Surface surface, String contextKey, String signature, SpanNode node,
            float speed, boolean wordMode, int targetLength) {
        if (!typewriterEnabled) {
            TypewriterTrack track = new TypewriterTrack("disabled", speed, wordMode, targetLength);
            track.setProgressComplete();
            return track;
        }
        String key = keyFor(surface, contextKey, signature, node);
        TypewriterTrack track = ACTIVE.compute(key, (ignored, existing) -> {
            if (existing == null) {
                TypewriterTrack created = new TypewriterTrack(key, speed, wordMode, targetLength);
                created.reset(clientTicks);
                return created;
            }
            existing.updateParameters(speed, wordMode, targetLength);
            return existing;
        });
        return track;
    }

    public static void clearFor(Surface surface, String signaturePrefix) {
        String prefix = surface.name() + ':';
        ACTIVE.entrySet().removeIf(entry -> {
            if (!entry.getKey().startsWith(prefix)) {
                return false;
            }
            if (signaturePrefix == null) {
                return true;
            }
            return entry.getKey().contains(signaturePrefix);
        });
    }

    private static String keyFor(Surface surface, String contextKey, String signature, SpanNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append(surface.name()).append(':');
        if (contextKey != null && !contextKey.isEmpty()) {
            builder.append(contextKey).append(':');
        }
        if (signature != null) {
            builder.append(signature);
        }
        builder.append('#').append(node.getStart()).append('-').append(node.getEnd());
        return builder.toString();
    }

    public static long getClientTicks() {
        return clientTicks;
    }

    public enum Surface {
        CHAT,
        TOOLTIP,
        TITLE,
        OTHER
    }

    public record SurfaceContext(Surface surface, String keyHint) {
        private static final SurfaceContext DEFAULT = new SurfaceContext(Surface.OTHER, "");
    }
}
