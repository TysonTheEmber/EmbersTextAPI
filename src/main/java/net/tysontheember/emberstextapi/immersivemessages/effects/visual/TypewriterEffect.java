package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.typewriter.TypewriterConfig;
import net.tysontheember.emberstextapi.typewriter.TypewriterTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 * </ul>
 *
 * <h3>Examples:</h3>
 * <pre>
 * &lt;typewriter&gt;Default speed text&lt;/typewriter&gt;
 * &lt;typewriter speed=50&gt;Slower reveal&lt;/typewriter&gt;
 * &lt;typewriter speed=80 sound="minecraft:block.note_block.hat"&gt;With click sound&lt;/typewriter&gt;
 * &lt;typewriter resetDelay=0.5&gt;Quick reset (500ms)&lt;/typewriter&gt;
 * &lt;typewriter resetDelay=2&gt;Slow reset (2 seconds)&lt;/typewriter&gt;
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

    /** Milliseconds per character for this effect instance. */
    private final int speedMs;

    /** Sound resource ID to play per character, or null for silent. */
    @Nullable
    private final String sound;

    /** Reset delay in milliseconds. When UI is hidden longer than this, animation restarts. */
    private final long resetDelayMs;

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
        if (sound != null && track.getSound() == null) {
            track.setSound(sound);
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

    @NotNull
    @Override
    public String getName() {
        return "typewriter";
    }
}
