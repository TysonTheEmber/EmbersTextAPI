package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.typewriter.TypewriterConfig;
import net.tysontheember.emberstextapi.typewriter.TypewriterTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Typewriter effect that reveals text character by character.
 * <p>
 * Each {@code <typewriter>} tag instance gets its own animation track, allowing
 * multiple independent typewriter animations in the same text or UI.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code speed=N} - Milliseconds per character (default: 20)</li>
 *   <li>{@code s=N} - Characters per second (legacy, converted to ms)</li>
 *   <li>{@code sound=ID} - Sound resource to play per character (e.g., "minecraft:block.note_block.hat")</li>
 *   <li>{@code sound=off} - Explicitly disable sound</li>
 *   <li>{@code resetDelay=N} - Seconds before animation resets when UI hidden (default: 1.0)</li>
 *   <li>{@code repeat=yes|no|N} - How many times to play: "yes" (infinite), "no" (once), or a number</li>
 * </ul>
 *
 * <h3>Examples:</h3>
 * <pre>
 * &lt;typewriter&gt;Default speed text&lt;/typewriter&gt;
 * &lt;typewriter speed=50&gt;Slower reveal&lt;/typewriter&gt;
 * &lt;typewriter speed=80 sound="minecraft:block.note_block.hat"&gt;With click sound&lt;/typewriter&gt;
 * &lt;typewriter resetDelay=0.5&gt;Quick reset (500ms)&lt;/typewriter&gt;
 * &lt;typewriter resetDelay=2&gt;Slow reset (2 seconds)&lt;/typewriter&gt;
 * &lt;typewriter repeat=no&gt;Plays once, stays revealed&lt;/typewriter&gt;
 * &lt;typewriter repeat=3&gt;Plays 3 times then stays revealed&lt;/typewriter&gt;
 * </pre>
 *
 * <h3>Track Management:</h3>
 * <p>
 * The typewriter track is set on the Style by LiteralContentsMixin using the text
 * content (via {@code text.intern()}) as the cache key. This ensures:
 * <ul>
 *   <li>Same text always uses same track (consistent animation state)</li>
 *   <li>Different UI contexts (chat, tooltip) don't interfere with each other's tracks</li>
 *   <li>Multi-line text animates correctly using absolute character indices</li>
 * </ul>
 * </p>
 *
 * @see TypewriterConfig
 * @see TypewriterTrack
 */
public class TypewriterEffect extends BaseEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterEffect.class);

    /** Milliseconds per character for this effect instance. */
    private final int speedMs;

    /** Sound resource ID to play per character, or null for silent. */
    @Nullable
    private final String sound;

    /** Reset delay in milliseconds. When UI is hidden longer than this, animation restarts. */
    private final long resetDelayMs;

    /**
     * Maximum number of times to play the animation.
     * -1 = infinite, 1 = play once, N = play N times.
     */
    private final int maxPlays;

    /**
     * Create a new typewriter effect with the given parameters.
     *
     * @param params effect parameters from markup parsing
     */
    public TypewriterEffect(@NotNull Params params) {
        super(params);

        // Parse speed: prefer "speed" in ms, fallback to legacy "s" in chars/sec
        this.speedMs = params.getDouble("speed")
                .or(() -> params.getDouble("s").map(s -> s > 0 ? 1000.0 / s : 1000.0))
                .map(Number::intValue)
                .filter(ms -> ms > 0)
                .orElse(TypewriterConfig.getDefaultSpeedMs());

        // Parse sound parameter
        this.sound = params.getString("sound").orElse(null);

        // Parse reset delay: in seconds (e.g., resetDelay=0.5 for 500ms)
        // Default is 1 second (1000ms)
        this.resetDelayMs = params.getDouble("resetDelay")
                .map(seconds -> (long) (seconds * 1000))
                .filter(ms -> ms >= 0)
                .orElse(1000L);

        // Parse repeat parameter: "yes" (infinite), "no" (once), or a number
        this.maxPlays = params.getString("repeat")
                .map(TypewriterEffect::parseRepeat)
                .orElse(TypewriterConfig.getDefaultMaxPlays());

        LOGGER.debug("TypewriterEffect created: speedMs={}, maxPlays={}, repeat param={}",
                speedMs, this.maxPlays, params.getString("repeat").orElse("(not set)"));
    }

    /**
     * Parse the repeat parameter value.
     *
     * @param value "yes", "no", or a number
     * @return -1 for infinite, or a positive number
     */
    private static int parseRepeat(String value) {
        if (value == null || value.isEmpty()) {
            return TypewriterConfig.getDefaultMaxPlays();
        }

        String lower = value.toLowerCase().trim();

        // "yes", "true", "infinite" = infinite repeats
        if ("yes".equals(lower) || "true".equals(lower) || "infinite".equals(lower)) {
            return -1;
        }

        // "no", "false", "once" = play once
        if ("no".equals(lower) || "false".equals(lower) || "once".equals(lower)) {
            return 1;
        }

        // Try to parse as a number
        try {
            int n = Integer.parseInt(lower);
            return n <= 0 ? -1 : n; // Treat 0 or negative as infinite
        } catch (NumberFormatException e) {
            return TypewriterConfig.getDefaultMaxPlays();
        }
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Short-circuit if typewriter is globally disabled
        if (!TypewriterConfig.isEnabled()) {
            return; // All text visible immediately
        }

        // Get track from EffectSettings (set by StringRenderOutputMixin from Style)
        TypewriterTrack track = settings.typewriterTrack;

        // If no track was set on Style, typewriter won't work
        // This can happen for non-LiteralContents text or edge cases
        if (track == null) {
            return;
        }

        // Configure track with this effect's parameters
        track.setInterval(speedMs);
        track.setResetDelayMs(resetDelayMs);
        track.setMaxPlays(maxPlays);
        if (sound != null && track.getSound() == null) {
            track.setSound(sound);
        }

        // Debug log at first character only
        if (settings.absoluteIndex == 0) {
            LOGGER.debug("TypewriterEffect.apply: effect.maxPlays={}, track.maxPlays={}, track.playCount={}, track.index={}, track.totalChars={}",
                    this.maxPlays, track.getMaxPlays(), track.getPlayCount(), track.index, track.getTotalChars());
        }

        // If animation has completed all plays, show all text
        if (track.isCompleted()) {
            // Only log once per track at index 0
            if (settings.absoluteIndex == 0) {
                LOGGER.debug("Track already completed: maxPlays={}, playCount={}, totalChars={}",
                        track.getMaxPlays(), track.getPlayCount(), track.getTotalChars());
            }
            return; // All text visible
        }

        // Check if track should reset based on time since last access
        // This handles the case where tooltip/UI was hidden and reappears
        track.checkAndResetIfNeeded();

        // Update track timing (advances revealed count based on elapsed time)
        track.update();

        // Hide character if not yet revealed
        // settings.absoluteIndex is the GLOBAL character position (spanOffset + localIndex)
        // track.index is the number of characters revealed so far
        if (settings.absoluteIndex >= track.index) {
            settings.a = 0.0f;
        }
    }

    /**
     * Get the configured speed in milliseconds per character.
     *
     * @return milliseconds between character reveals
     */
    public int getSpeedMs() {
        return speedMs;
    }

    /**
     * Get the configured sound resource ID.
     *
     * @return sound ID or null if silent
     */
    @Nullable
    public String getSound() {
        return sound;
    }

    /**
     * Get the configured reset delay in milliseconds.
     *
     * @return reset delay in milliseconds
     */
    public long getResetDelayMs() {
        return resetDelayMs;
    }

    /**
     * Get the configured max plays.
     *
     * @return -1 for infinite, or a positive number
     */
    public int getMaxPlays() {
        return maxPlays;
    }

    @NotNull
    @Override
    public String getName() {
        return "typewriter";
    }
}
