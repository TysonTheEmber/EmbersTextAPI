package net.tysontheember.emberstextapi.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewStateTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewStateTracker.class);

    private static final float LINE_HEIGHT_PIXELS = 9.0f;
    private static final ConcurrentHashMap<String, Long> VIEW_START_TIMES = new ConcurrentHashMap<>();
    private static volatile String currentTooltipContext = null;
    private static volatile String currentScreenContext = null;
    private static volatile String currentQuestContext = null;

    private static final ConcurrentHashMap<String, PositionAwareOrdinalTracker> CHAR_ORDINALS = new ConcurrentHashMap<>();

    private static volatile long lastFrameTimeNs = -1;
    private static volatile String lastTooltipContext = null;

    private static class PositionAwareOrdinalTracker {
        private final Map<String, Integer> positionToOrdinal = new HashMap<>();
        private float minY = Float.MAX_VALUE;
        private final Map<Integer, Float> minXPerLine = new HashMap<>();

        int getOrdinal(float y, float x, boolean advance) {
            String posKey = positionKey(y, x);

            Integer cached = positionToOrdinal.get(posKey);
            if (cached != null) {
                return cached;
            }

            if (!advance) {
                return calculateOrdinal(y, x);
            }

            if (y < minY) {
                minY = y;
            }

            int absoluteLineIndex = getAbsoluteLineIndex(y);
            Float currentMinX = minXPerLine.get(absoluteLineIndex);
            if (currentMinX == null || x < currentMinX) {
                minXPerLine.put(absoluteLineIndex, x);
            }

            int ordinal = calculateOrdinal(y, x);
            positionToOrdinal.put(posKey, ordinal);
            return ordinal;
        }

        private int calculateOrdinal(float y, float x) {
            float relativeY = (minY == Float.MAX_VALUE) ? 0 : (y - minY);
            int relativeLineIndex = getRelativeLineIndex(relativeY);

            int absoluteLineIndex = getAbsoluteLineIndex(y);
            Float lineMinX = minXPerLine.get(absoluteLineIndex);
            float relativeX = (lineMinX == null) ? x : (x - lineMinX);

            int charIndexInLine = Math.max(0, (int) Math.round(relativeX / 6.0f));

            return (relativeLineIndex * 10000) + charIndexInLine;
        }

        private int getAbsoluteLineIndex(float absoluteY) {
            return (int) Math.round(absoluteY / LINE_HEIGHT_PIXELS);
        }

        private int getRelativeLineIndex(float relativeY) {
            return Math.max(0, (int) Math.round(relativeY / LINE_HEIGHT_PIXELS));
        }

        private static String positionKey(float y, float x) {
            int yRounded = Math.round(y * 10);
            int xRounded = Math.round(x * 10);
            return yRounded + "," + xRounded;
        }
    }

    public static long getViewStartTime(String contextId) {
        return VIEW_START_TIMES.computeIfAbsent(contextId, key -> {
            long currentTime = System.currentTimeMillis();
            LOGGER.debug("TRACKER: Auto-created view context: {} at {}", key, currentTime);
            return currentTime;
        });
    }

    public static void markViewStarted(String contextId) {
        if (contextId == null || contextId.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Long previousTime = VIEW_START_TIMES.put(contextId, currentTime);

        if (previousTime == null) {
            LOGGER.debug("TRACKER: New view context started: {} at {}", contextId, currentTime);
        } else {
            LOGGER.debug("TRACKER: View context reset: {} (was {}, now {})", contextId, previousTime, currentTime);
        }

        resetOrdinals(contextId);

    }

    public static void updateTooltipContext(String tooltipContext) {
        if (!java.util.Objects.equals(tooltipContext, lastTooltipContext)) {
            LOGGER.debug("TRACKER: Tooltip context changed: {} -> {}", lastTooltipContext, tooltipContext);

            if (tooltipContext != null) {
                markViewStarted(tooltipContext);
            }

            if (tooltipContext == null && lastTooltipContext != null) {
                onTooltipDisappeared();
            }

            lastTooltipContext = tooltipContext;
        }

        currentTooltipContext = tooltipContext;
    }

    private static void onTooltipDisappeared() {
        LOGGER.debug("Tooltip disappeared - clearing empty tooltip cache");
        clearEmptyTooltipCache();
    }

    private static void clearEmptyTooltipCache() {
        try {
            Class.forName("net.tysontheember.emberstextapi.client.ViewStateEventHandler")
                .getMethod("clearEmptyTooltipCache")
                .invoke(null);
        } catch (Exception e) {
            LOGGER.warn("Could not clear empty tooltip cache", e);
        }
    }

    public static void markScreenOpened(String screenClass) {
        if (screenClass == null || screenClass.isEmpty()) {
            return;
        }

        LOGGER.debug("Screen opened: {}", screenClass);
        currentScreenContext = screenClass;
        markViewStarted("screen:" + screenClass);
    }

    public static void markScreenClosed() {
        if (currentScreenContext != null) {
            LOGGER.debug("Screen closed: {}", currentScreenContext);
            currentScreenContext = null;
        }
    }

    public static void markQuestViewed(String questId) {
        if (questId == null || questId.isEmpty()) {
            return;
        }

        LOGGER.debug("TRACKER: Quest viewed: {}", questId);
        currentQuestContext = "quest:" + questId;
        markViewStarted(currentQuestContext);
    }

    public static String getCurrentTooltipContext() {
        return currentTooltipContext;
    }

    public static String getCurrentScreenContext() {
        return currentScreenContext;
    }

    public static String getCurrentQuestContext() {
        return currentQuestContext;
    }

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

    public static int getTrackedContextCount() {
        return VIEW_START_TIMES.size();
    }

    public static void beginFrame(long frameTimeNs) {
        if (frameTimeNs != lastFrameTimeNs) {
            lastFrameTimeNs = frameTimeNs;
            CHAR_ORDINALS.clear();
        }
    }

    public static void resetContext(String contextId) {
        markViewStarted(contextId);
    }

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

    public static void resetOrdinals(String contextId) {
        CHAR_ORDINALS.remove(ordinalKey(contextId, true));
        CHAR_ORDINALS.remove(ordinalKey(contextId, false));
    }
}
