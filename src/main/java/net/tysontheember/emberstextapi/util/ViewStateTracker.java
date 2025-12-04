package net.tysontheember.emberstextapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks when text becomes visible to enable view-based animation resets.
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
public class ViewStateTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewStateTracker.class);

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
     * Last frame's tooltip context for change detection.
     */
    private static volatile String lastTooltipContext = null;

    /**
     * Get the timestamp when a specific view context became visible.
     * <p>
     * If the context is not tracked, returns the current time (making it immediately visible).
     * </p>
     *
     * @param contextId The context identifier
     * @return Timestamp in milliseconds when the context became visible
     */
    public static long getViewStartTime(String contextId) {
        return VIEW_START_TIMES.getOrDefault(contextId, System.currentTimeMillis());
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
            LOGGER.debug("New view context started: {} at {}", contextId, currentTime);
        } else {
            LOGGER.debug("View context reset: {} (was {}, now {})", contextId, previousTime, currentTime);
        }
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
            LOGGER.trace("Tooltip context changed: {} -> {}", lastTooltipContext, tooltipContext);

            // If we have a new tooltip context, mark it as started
            if (tooltipContext != null) {
                markViewStarted(tooltipContext);
            }

            lastTooltipContext = tooltipContext;
        }

        currentTooltipContext = tooltipContext;
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

        LOGGER.debug("Quest viewed: {}", questId);
        markViewStarted("quest:" + questId);
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
     * Clear all tracked view states.
     * <p>
     * Useful for cleanup or testing. Not typically needed during normal operation.
     * </p>
     */
    public static void clear() {
        VIEW_START_TIMES.clear();
        currentTooltipContext = null;
        currentScreenContext = null;
        lastTooltipContext = null;
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
}
