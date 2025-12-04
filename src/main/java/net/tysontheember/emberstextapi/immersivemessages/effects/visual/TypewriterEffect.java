package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.util.ViewStateTracker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Typewriter effect that reveals characters progressively from left to right.
 * <p>
 * Creates a classic typewriter animation where text appears one character at a time.
 * The effect automatically resets every time the text becomes visible (tooltips appear,
 * screens open, etc.), ensuring the animation always starts from character 0.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code s} (speed, default: 20.0) - Characters per second reveal rate</li>
 *   <li>{@code d} (delay, default: 0.0) - Initial delay in seconds before animation starts</li>
 *   <li>{@code c} (cycle, default: false) - Whether to cycle the animation (restart when complete)</li>
 *   <li>{@code id} (context id, optional) - Custom context identifier for independent reset tracking</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <typewriter>Text appears character by character</typewriter>
 * <typewriter s=30>Fast typing (30 chars/sec)</typewriter>
 * <typewriter s=10 d=1.0>Slow typing with 1 second delay</typewriter>
 * <typewriter s=15 c=true>Repeating typewriter effect</typewriter>
 * <typewriter s=20 id=quest_123>Quest-specific typewriter</typewriter>
 * }</pre>
 *
 * <h3>View Reset Behavior:</h3>
 * <p>
 * The typewriter effect automatically resets and starts from character 0 when:
 * <ul>
 *   <li>Hovering over an item (tooltip appears)</li>
 *   <li>Opening a GUI screen (quest descriptions, etc.)</li>
 *   <li>Changing between different tooltips/screens</li>
 *   <li>Reopening the same tooltip/screen after closing it</li>
 * </ul>
 * </p>
 * <p>
 * Each unique text context (based on content hash or custom ID) maintains its own
 * animation state, so different tooltips can have independent typewriter animations.
 * </p>
 *
 * <h3>Technical Details:</h3>
 * <ul>
 *   <li>Characters beyond the reveal index are made fully transparent (alpha = 0)</li>
 *   <li>Uses high-precision timing based on system time for smooth animation</li>
 *   <li>Integrates with {@link ViewStateTracker} for automatic reset detection</li>
 *   <li>Thread-safe for concurrent rendering of multiple text elements</li>
 * </ul>
 */
public class TypewriterEffect extends BaseEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterEffect.class);
    private static String lastLoggedContext = null;

    /** Track text content hash per effect instance to create unique quest contexts */
    private Integer textContentHash = null;

    /** Characters per second reveal rate */
    private final float speed;

    /** Initial delay in seconds before animation starts */
    private final float delay;

    /** Whether to cycle the animation when complete */
    private final boolean cycle;

    /** Custom context identifier for independent tracking */
    private final String contextId;

    /**
     * Creates a new typewriter effect with the given parameters.
     *
     * @param params Effect parameters from markup parsing
     */
    public TypewriterEffect(@NotNull Params params) {
        super(params);
        this.speed = params.getDouble("s").map(Number::floatValue).orElse(20.0f);
        this.delay = params.getDouble("d").map(Number::floatValue).orElse(0.0f);
        this.cycle = params.getBoolean("c").orElse(false);
        this.contextId = params.getString("id").orElse(null);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Reset per-frame ordinal counters when a new frame begins.
        long frameTimeNs = net.minecraft.client.Minecraft.getInstance().getFrameTimeNs();
        ViewStateTracker.beginFrame(frameTimeNs);

        // Get the current view context to determine when text became visible
        String contextKey = contextId;

        // If no custom context ID was provided, automatically detect the current context
        if (contextKey == null) {
            // Priority 1: Try to use current quest context (FTB Quests)
            contextKey = ViewStateTracker.getCurrentQuestContext();

            // Priority 2: Check if we're in a screen context
            String screenContext = ViewStateTracker.getCurrentScreenContext();

            // Priority 3: Try to use current tooltip context
            if (contextKey == null) {
                String tooltipContext = ViewStateTracker.getCurrentTooltipContext();

                // If we have a screen context (quest menu open), check if this is description or tooltip
                if (screenContext != null && tooltipContext != null && tooltipContext.startsWith("tooltip:empty:")) {
                    // In quest screen with empty tooltip - need to distinguish tooltip vs description
                    // Quest descriptions render with more complex geometry, tooltips are simpler
                    // Use text hash to create unique context for description (separate from tooltip)
                    if (settings.index == 0 && textContentHash == null) {
                        textContentHash = System.identityHashCode(this);
                    }
                    // Quest descriptions use screen+text hash (separate from tooltip context)
                    contextKey = screenContext + ":text" + textContentHash;
                } else if (tooltipContext != null) {
                    // Not in quest screen, or regular tooltip - use tooltip context directly
                    if (tooltipContext.startsWith("tooltip:empty:") && tooltipContext.length() > 14) {
                        // Quest tooltip (not in screen) - use the unique timestamped context
                        contextKey = tooltipContext;
                    } else {
                        // Regular tooltip (items with content)
                        contextKey = tooltipContext;
                    }
                }
            }

            // Priority 4: Use screen context if no other context found
            if (contextKey == null && screenContext != null) {
                contextKey = screenContext;
            }

        // Priority 5: If still no context, use "default"
        if (contextKey == null) {
            contextKey = "default";
            // Don't mark as started here - let getViewStartTime handle it
        }
        }

        // Get the time when this view context became visible
        // If context doesn't exist yet, this will return current time (making it visible immediately)
        long viewStartTime = ViewStateTracker.getViewStartTime(contextKey);

        // DEBUG: Log context info once per context change (only on first character)
        if (settings.index == 0 && !contextKey.equals(lastLoggedContext)) {
            long currentTime = System.currentTimeMillis();
            float elapsed = (currentTime - viewStartTime) / 1000.0f;
            LOGGER.info("TYPEWRITER DEBUG: Using context '{}', start time: {}, current time: {}, elapsed: {}s",
                contextKey, viewStartTime, currentTime, elapsed);
            lastLoggedContext = contextKey;
        }

        // Calculate elapsed time since view became visible (in seconds)
        float elapsedSeconds = (System.currentTimeMillis() - viewStartTime) / 1000.0f;

        // Use a per-context absolute ordinal that spans wrapped lines. We advance
        // only on the main pass so shadow/bold reuse the same ordinal.
        // Advance separate ordinal streams for shadow and main so each pass
        // has correct per-glyph order without double-counting.
        int absoluteOrdinal = ViewStateTracker.nextCharOrdinal(contextKey, settings.isShadow, true);

        // Apply initial delay
        float animationTime = Math.max(0, elapsedSeconds - delay);

        // Calculate how many characters should be visible
        float revealProgress = animationTime * speed;

        // Handle cycling if enabled
        if (cycle && revealProgress > 0) {
            // Get total text length from settings context (we'll use a reasonable max)
            // Since we don't know total length here, we'll cycle every 100 characters
            int maxChars = 100;
            revealProgress = revealProgress % maxChars;
        }

        int visibleChars = (int) revealProgress;

        // DEBUG: Log visibility calculation for first few characters
        if (absoluteOrdinal < 3 && elapsedSeconds < 1.0f) {
            LOGGER.info("TYPEWRITER CHAR DEBUG: ord={}, visibleChars={}, elapsed={}, alpha will be={}",
                absoluteOrdinal, visibleChars, elapsedSeconds,
                (absoluteOrdinal >= visibleChars ? 0.0f : settings.a));
        }

        // Hide this character if it's beyond the reveal point
        if (absoluteOrdinal >= visibleChars) {
            settings.a = 0.0f; // Make character invisible (kills shadow too)
        }
        // Characters before the reveal point stay visible (no modification needed)
    }

    @NotNull
    @Override
    public String getName() {
        return "typewriter";
    }
}
