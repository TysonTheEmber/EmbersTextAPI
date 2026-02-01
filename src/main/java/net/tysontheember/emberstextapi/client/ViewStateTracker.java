package net.tysontheember.emberstextapi.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks when text becomes visible to enable view-based animation resets.
 * <p>
 * This is a client-only utility for managing UI state.
 * </p>
 *
 * @since 2.0.0
 * <p>
 * This utility class maintains timestamps for when different text contexts
 * become visible (tooltips appear, screens open, etc.). Effects like typewriter
 * use these timestamps to determine when to reset their animations.
 * </p>
 * <p>
 * The tracker is thread-safe and uses context identifiers to maintain independent
 * state for different text elements (different tooltips, different screens, etc.).
 * </p>
 *
 * <h3>Context Identification:</h3>
 * <p>
 * Contexts are identified by strings that represent:
 * <ul>
 *   <li>Tooltip item/entity IDs</li>
 *   <li>Screen class names</li>
 *   <li>Custom identifiers set by effects</li>
 *   <li>FTB Quest IDs</li>
 * </ul>
 * </p>
 *
 * <h3>Reset Triggers:</h3>
 * <p>
 * View start times are updated when:
 * <ul>
 *   <li>A tooltip is first rendered (item hover begins)</li>
 *   <li>A screen is opened or changed</li>
 *   <li>An FTB Quest description is viewed</li>
 *   <li>Explicit reset is called via API</li>
 * </ul>
 * </p>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * All operations are thread-safe using {@link ConcurrentHashMap}.
 * Safe to call from render thread and event handlers.
 * </p>
 *
 * @see net.tysontheember.emberstextapi.immersivemessages.effects.visual.TypewriterEffect
 */
@OnlyIn(Dist.CLIENT)
public class ViewStateTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewStateTracker.class);

    /** Minecraft's default font line height in pixels (at scale 1.0). */
    private static final float LINE_HEIGHT_PIXELS = 9.0f;

    /**
     * Map of context identifiers to view start timestamps (milliseconds).
     * Thread-safe for concurrent access from render and event threads.
     */
    private static final ConcurrentHashMap<String, Long> VIEW_START_TIMES = new ConcurrentHashMap<>();

    /**
     * Currently active tooltip context (item ID, entity type, etc.).
     * Null when no tooltip is being rendered.
     */
    private static volatile String currentTooltipContext = null;

    /**
     * Currently active screen context (screen class name).
     * Null when no custom screen is open.
     */
    private static volatile String currentScreenContext = null;

    /**
     * Currently active quest context (quest ID).
     * Null when no quest is being viewed.
     */
    private static volatile String currentQuestContext = null;

    /**
     * Per-context position-aware ordinal trackers used by the typewriter effect.
     * Tracks character positions (Y, X) and assigns ordinals based on visual order
     * (top-to-bottom, left-to-right) rather than rendering order.
     * This fixes multi-line typewriter animation to always animate top-to-bottom
     * regardless of the order lines are rendered.
     */
    private static final ConcurrentHashMap<String, PositionAwareOrdinalTracker> CHAR_ORDINALS = new ConcurrentHashMap<>();

    /**
     * Last frame timestamp (nanoseconds) observed when assigning ordinals.
     * Used to detect frame boundaries and clear per-frame state.
     */
    private static volatile long lastFrameTimeNs = -1;

    /**
     * Last frame's tooltip context for change detection.
     */
    private static volatile String lastTooltipContext = null;

    /**
     * Helper class that tracks character positions and assigns ordinals based on
     * visual position (Y, then X) rather than rendering order.
     * Uses a position-to-ordinal calculation to ensure stable ordinals that don't
     * change as more characters are rendered.
     */
    private static class PositionAwareOrdinalTracker {
        // Map from position key to assigned ordinal (for caching)
        private final Map<String, Integer> positionToOrdinal = new HashMap<>();
        // Track minimum Y value seen to normalize positions
        private float minY = Float.MAX_VALUE;
        // Track minimum X value seen per line
        private final Map<Integer, Float> minXPerLine = new HashMap<>();

        /**
         * Get the ordinal for a character at the given position.
         * Ordinals are calculated based on position: characters higher up (lower Y)
         * get lower ordinals, and within a line, characters further left (lower X)
         * get lower ordinals.
         *
         * @param y Y position of the character
         * @param x X position of the character
         * @param advance Whether to record this position (usually true)
         * @return The ordinal for this position
         */
        int getOrdinal(float y, float x, boolean advance) {
            String posKey = positionKey(y, x);

            // Return cached ordinal if we've seen this position before
            Integer cached = positionToOrdinal.get(posKey);
            if (cached != null) {
                return cached;
            }

            if (!advance) {
                // Just calculate without caching
                return calculateOrdinal(y, x);
            }

            // Track min Y for normalization
            if (y < minY) {
                minY = y;
            }

            // Track min X per line (using absolute Y to determine line)
            int absoluteLineIndex = getAbsoluteLineIndex(y);
            Float currentMinX = minXPerLine.get(absoluteLineIndex);
            if (currentMinX == null || x < currentMinX) {
                minXPerLine.put(absoluteLineIndex, x);
            }

            // Calculate and cache the ordinal
            int ordinal = calculateOrdinal(y, x);
            positionToOrdinal.put(posKey, ordinal);
            return ordinal;
        }

        /**
         * Calculate ordinal based on RELATIVE position from the first character seen.
         * Formula: (lineIndex * 10000) + characterIndexInLine
         * This ensures characters on earlier lines always have lower ordinals,
         * and within a line, characters to the left have lower ordinals.
         */
        private int calculateOrdinal(float y, float x) {
            // Use relative positions from the first character seen (minY, minX)
            float relativeY = (minY == Float.MAX_VALUE) ? 0 : (y - minY);

            // Determine which line this character is on RELATIVE to the first line
            int relativeLineIndex = getRelativeLineIndex(relativeY);

            // Get the minimum X for this line (using absolute line index for lookup)
            int absoluteLineIndex = getAbsoluteLineIndex(y);
            Float lineMinX = minXPerLine.get(absoluteLineIndex);
            float relativeX = (lineMinX == null) ? x : (x - lineMinX);

            // Determine character position within the line (assuming ~6 pixels per char)
            // Round to nearest character position to avoid floating point issues
            int charIndexInLine = Math.max(0, (int) Math.round(relativeX / 6.0f));

            // Combine: line * 10000 + char position
            // This gives us a stable ordinal where earlier lines have lower values
            return (relativeLineIndex * 10000) + charIndexInLine;
        }

        /**
         * Convert absolute Y position to absolute line index.
         * Used for tracking which characters belong to the same line.
         */
        private int getAbsoluteLineIndex(float absoluteY) {
            return (int) Math.round(absoluteY / LINE_HEIGHT_PIXELS);
        }

        /**
         * Convert RELATIVE Y position to relative line index (0-based from first line).
         * Used for ordinal calculation.
         */
        private int getRelativeLineIndex(float relativeY) {
            return Math.max(0, (int) Math.round(relativeY / LINE_HEIGHT_PIXELS));
        }

        private static String positionKey(float y, float x) {
            // Round to nearest 0.1 to handle floating point precision issues
            int yRounded = Math.round(y * 10);
            int xRounded = Math.round(x * 10);
            return yRounded + "," + xRounded;
        }
    }


    /**
     * Get the timestamp when a specific view context became visible.
     * <p>
     * If the context is not tracked, creates a new entry with the current time.
     * This ensures the context is tracked for future calls.
     * </p>
     *
     * @param contextId The context identifier
     * @return Timestamp in milliseconds when the context became visible
     */
    public static long getViewStartTime(String contextId) {
        return VIEW_START_TIMES.computeIfAbsent(contextId, key -> {
            long currentTime = System.currentTimeMillis();
            LOGGER.info("TRACKER: Auto-created view context: {} at {}", key, currentTime);
            return currentTime;
        });
    }

    /**
     * Mark a view context as becoming visible right now.
     * <p>
     * This resets the animation timer for any effects using this context.
     * </p>
     *
     * @param contextId The context identifier to mark as visible
     */
    public static void markViewStarted(String contextId) {
        if (contextId == null || contextId.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Long previousTime = VIEW_START_TIMES.put(contextId, currentTime);

        if (previousTime == null) {
            LOGGER.info("TRACKER: New view context started: {} at {}", contextId, currentTime);
        } else {
            LOGGER.info("TRACKER: View context reset: {} (was {}, now {})", contextId, previousTime, currentTime);
        }

        // Reset ordinal counters for this context so typing restarts at the top.
        resetOrdinals(contextId);

    }

    /**
     * Update tooltip context tracking.
     * <p>
     * Should be called every frame during tooltip rendering. Automatically detects
     * when tooltip changes (including from no tooltip to tooltip, or between different tooltips).
     * </p>
     *
     * @param tooltipContext The current tooltip context identifier (null if no tooltip)
     */
    public static void updateTooltipContext(String tooltipContext) {
        // Detect context change (including null -> non-null and vice versa)
        if (!java.util.Objects.equals(tooltipContext, lastTooltipContext)) {
            LOGGER.info("TRACKER: Tooltip context changed: {} -> {}", lastTooltipContext, tooltipContext);

            // If we have a new tooltip context, mark it as started
            if (tooltipContext != null) {
                markViewStarted(tooltipContext);
            }

            // Notify event handler when tooltip disappears (context becomes null)
            if (tooltipContext == null && lastTooltipContext != null) {
                onTooltipDisappeared();
            }

            lastTooltipContext = tooltipContext;
        }

        currentTooltipContext = tooltipContext;
    }

    /**
     * Called when a tooltip disappears.
     * Can be used by other systems to clean up state.
     */
    private static void onTooltipDisappeared() {
        LOGGER.debug("Tooltip disappeared - clearing empty tooltip cache");
        // Clear the cached empty tooltip context so next hover gets a fresh timestamp
        clearEmptyTooltipCache();
    }

    /**
     * Notify ViewStateEventHandler to clear empty tooltip cache.
     * This is a callback mechanism to avoid circular dependencies.
     */
    private static void clearEmptyTooltipCache() {
        try {
            // Call the EventHandler's public method via reflection to avoid circular dependency
            Class.forName("net.tysontheember.emberstextapi.client.ViewStateEventHandler")
                .getMethod("clearEmptyTooltipCache")
                .invoke(null);
        } catch (Exception e) {
            LOGGER.warn("Could not clear empty tooltip cache", e);
        }
    }

    /**
     * Mark a screen as opened.
     * <p>
     * Should be called when a new screen is opened (from screen open events).
     * </p>
     *
     * @param screenClass The screen's class name or identifier
     */
    public static void markScreenOpened(String screenClass) {
        if (screenClass == null || screenClass.isEmpty()) {
            return;
        }

        LOGGER.debug("Screen opened: {}", screenClass);
        currentScreenContext = screenClass;
        markViewStarted("screen:" + screenClass);
    }

    /**
     * Mark a screen as closed.
     * <p>
     * Should be called when a screen is closed.
     * </p>
     */
    public static void markScreenClosed() {
        if (currentScreenContext != null) {
            LOGGER.debug("Screen closed: {}", currentScreenContext);
            currentScreenContext = null;
        }
    }

    /**
     * Mark an FTB Quest as being viewed.
     * <p>
     * Should be called when an FTB Quest description is opened or changed.
     * </p>
     *
     * @param questId The quest identifier
     */
    public static void markQuestViewed(String questId) {
        if (questId == null || questId.isEmpty()) {
            return;
        }

        LOGGER.info("TRACKER: Quest viewed: {}", questId);
        currentQuestContext = "quest:" + questId;
        markViewStarted(currentQuestContext);
    }

    /**
     * Get the currently active tooltip context.
     *
     * @return Current tooltip context identifier, or null if no tooltip
     */
    public static String getCurrentTooltipContext() {
        return currentTooltipContext;
    }

    /**
     * Get the currently active screen context.
     *
     * @return Current screen context identifier, or null if no custom screen
     */
    public static String getCurrentScreenContext() {
        return currentScreenContext;
    }

    /**
     * Get the currently active quest context.
     *
     * @return Current quest context identifier, or null if no quest is being viewed
     */
    public static String getCurrentQuestContext() {
        return currentQuestContext;
    }

    /**
     * Clear all tracked view states.
     * <p>
     * Useful for cleanup or testing. Not typically needed during normal operation.
     * </p>
     */
    public static void clear() {
        VIEW_START_TIMES.clear();
        currentTooltipContext = null;
        currentScreenContext = null;
        currentQuestContext = null;
        lastTooltipContext = null;
        CHAR_ORDINALS.clear();
        lastFrameTimeNs = -1;
        LOGGER.debug("View state tracker cleared");
    }

    /**
     * Get the number of currently tracked view contexts.
     * <p>
     * Useful for debugging and monitoring.
     * </p>
     *
     * @return Number of tracked contexts
     */
    public static int getTrackedContextCount() {
        return VIEW_START_TIMES.size();
    }

    /**
     * Begin a new render frame for typewriter ordinal assignment.
     * Clears per-frame ordinal counters when the frame timestamp advances.
     *
     * @param frameTimeNs Current frame timestamp in nanoseconds
     */
    public static void beginFrame(long frameTimeNs) {
        if (frameTimeNs != lastFrameTimeNs) {
            lastFrameTimeNs = frameTimeNs;
            CHAR_ORDINALS.clear();
        }
    }

    /**
     * Force reset a specific context's timer.
     * <p>
     * This is equivalent to calling {@link #markViewStarted(String)} but with
     * a more explicit name for API users.
     * </p>
     *
     * @param contextId The context to reset
     */
    public static void resetContext(String contextId) {
        markViewStarted(contextId);
    }

    /**
     * Get the next absolute character ordinal for the given context.
     * Ordinals are per-context and assigned based on visual position (Y, X)
     * to ensure top-to-bottom, left-to-right ordering regardless of render order.
     *
     * @param contextId  Context identifier (must not be null)
     * @param shadowPass Whether this is the shadow pass
     * @param y          Y position of the character
     * @param x          X position of the character
     * @param advance    Whether to advance the counter (usually true)
     * @return Current ordinal (0-based)
     */
    public static int nextCharOrdinal(String contextId, boolean shadowPass, float y, float x, boolean advance) {
        PositionAwareOrdinalTracker tracker = CHAR_ORDINALS.computeIfAbsent(
            ordinalKey(contextId, shadowPass),
            key -> new PositionAwareOrdinalTracker()
        );
        return tracker.getOrdinal(y, x, advance);
    }

    private static String ordinalKey(String contextId, boolean shadowPass) {
        return contextId + (shadowPass ? ":shadow" : ":main");
    }

    /**
     * Clear ordinal counters for a single context (both shadow and main streams).
     */
    public static void resetOrdinals(String contextId) {
        CHAR_ORDINALS.remove(ordinalKey(contextId, true));
        CHAR_ORDINALS.remove(ordinalKey(contextId, false));
    }
}
