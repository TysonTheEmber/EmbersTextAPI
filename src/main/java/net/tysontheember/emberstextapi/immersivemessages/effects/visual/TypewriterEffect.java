package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.util.ViewStateTracker;
import org.jetbrains.annotations.NotNull;

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
        // Get the current view context to determine when text became visible
        String contextKey = contextId;

        // If no custom context ID was provided, automatically detect the current context
        if (contextKey == null) {
            // Try to use current tooltip context
            contextKey = ViewStateTracker.getCurrentTooltipContext();

            // If no tooltip, try current screen context
            if (contextKey == null) {
                contextKey = ViewStateTracker.getCurrentScreenContext();
            }

            // If still no context, use "default" and mark it as started now
            if (contextKey == null) {
                contextKey = "default";
                ViewStateTracker.markViewStarted(contextKey);
            }
        }

        // Get the time when this view context became visible
        long viewStartTime = ViewStateTracker.getViewStartTime(contextKey);

        // Calculate elapsed time since view became visible (in seconds)
        float elapsedSeconds = (System.currentTimeMillis() - viewStartTime) / 1000.0f;

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

        // Hide this character if it's beyond the reveal point
        if (settings.index >= visibleChars) {
            settings.a = 0.0f; // Make character invisible
        }
        // Characters before the reveal point stay visible (no modification needed)
    }

    @NotNull
    @Override
    public String getName() {
        return "typewriter";
    }
}
