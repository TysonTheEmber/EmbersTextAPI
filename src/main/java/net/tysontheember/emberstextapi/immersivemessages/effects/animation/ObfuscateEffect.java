package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfKey;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateAnimator;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateTrack;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateTracks;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Obfuscate effect that provides 4 different obfuscation behaviors.
 * <p>
 * This effect can reveal text progressively (fade in), hide text progressively (fade out),
 * keep text constantly obfuscated, or create a random flickering mask.
 * </p>
 *
 * <h3>Modes:</h3>
 * <ul>
 *   <li><b>reveal</b> - Text starts obfuscated and progressively becomes readable</li>
 *   <li><b>hide</b> - Text starts readable and progressively becomes obfuscated</li>
 *   <li><b>constant</b> - Text stays completely obfuscated</li>
 *   <li><b>random</b> - Random characters flicker between obfuscated and readable</li>
 * </ul>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code mode} - reveal | hide | constant | random (default: constant)</li>
 *   <li>{@code speed} - Milliseconds per character for reveal/hide (default: 20)</li>
 *   <li>{@code direction} - left | right | center | edges | random (default: left)</li>
 *   <li>{@code alphabet} - minecraft | readable (default: minecraft)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <obfuscate>Constant obfuscation</obfuscate>
 * <obfuscate mode=reveal speed=50>Slowly revealing...</obfuscate>
 * <obfuscate mode=hide direction=right>Hiding from right</obfuscate>
 * <obfuscate mode=random>Flickering text</obfuscate>
 * <obfuscate mode=reveal direction=center>Reveal from center</obfuscate>
 * <obfuscate mode=reveal direction=edges>Reveal from edges</obfuscate>
 * }</pre>
 */
public class ObfuscateEffect extends BaseEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObfuscateEffect.class);

    /** Effect mode: reveal, hide, constant, random */
    private final Mode mode;

    /** Speed in milliseconds per character (reveal/hide) */
    private final int speedMs;

    /** Direction for reveal/hide animations */
    private final ObfuscateMode direction;

    /** Whether to use readable alphabet (vs minecraft random glyphs) */
    private final boolean useReadableAlphabet;

    /** Random instance for random mode */
    private static final Random RANDOM = new Random();

    /** Track length detection - stores max index seen per track */
    private final Map<Object, Integer> lengthDetection = new HashMap<>();

    /**
     * Internal mode enum for the four obfuscate behaviors
     */
    private enum Mode {
        REVEAL,
        HIDE,
        CONSTANT,
        RANDOM
    }

    /**
     * Creates a new obfuscate effect with the given parameters.
     *
     * @param params Effect parameters from markup parsing
     */
    public ObfuscateEffect(@NotNull Params params) {
        super(params);

        // Parse mode parameter
        this.mode = params.getString("mode")
                .map(String::toLowerCase)
                .map(m -> switch (m) {
                    case "reveal" -> Mode.REVEAL;
                    case "hide" -> Mode.HIDE;
                    case "random" -> Mode.RANDOM;
                    default -> Mode.CONSTANT;
                })
                .orElse(Mode.CONSTANT);

        // Parse speed parameter (default 20ms like TypewriterEffect)
        int rawSpeed = params.getDouble("speed")
                .map(Number::intValue)
                .filter(ms -> ms > 0)
                .orElse(20);
        this.speedMs = ValidationHelper.clamp("obfuscate", "speed", rawSpeed, 1, 10000);

        // Parse direction parameter
        this.direction = params.getString("direction")
                .or(() -> params.getString("dir"))
                .map(String::toUpperCase)
                .map(d -> switch (d) {
                    case "LEFT" -> ObfuscateMode.LEFT;
                    case "RIGHT" -> ObfuscateMode.RIGHT;
                    case "CENTER" -> ObfuscateMode.CENTER;
                    case "EDGES" -> ObfuscateMode.EDGES;
                    case "RANDOM" -> ObfuscateMode.RANDOM;
                    default -> ObfuscateMode.LEFT;
                })
                .orElse(ObfuscateMode.LEFT);

        // Parse alphabet parameter
        this.useReadableAlphabet = params.getString("alphabet")
                .or(() -> params.getString("alph"))
                .map(String::toLowerCase)
                .map(a -> a.equals("readable"))
                .orElse(false);

        LOGGER.debug("ObfuscateEffect created: mode={}, speedMs={}, direction={}, readable={}",
                mode, speedMs, direction, useReadableAlphabet);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        switch (mode) {
            case CONSTANT -> applyConstant(settings);
            case REVEAL -> applyReveal(settings);
            case HIDE -> applyHide(settings);
            case RANDOM -> applyRandom(settings);
        }
    }

    /**
     * Apply constant obfuscation to all characters.
     */
    private void applyConstant(@NotNull EffectSettings settings) {
        if (useReadableAlphabet) {
            settings.codepoint = getReadableRandomChar(settings.index);
        } else {
            settings.useRandomGlyph = true;
        }
    }

    /**
     * Apply reveal animation - text starts obfuscated and becomes readable.
     */
    private void applyReveal(@NotNull EffectSettings settings) {
        ObfuscateTrack track = getOrCreateTrack(settings);

        // Update max length seen (for dynamic length detection)
        updateLengthDetection(track, settings.index);

        // Initialize track if needed
        if (track.order == null || needsReinitialize(track, settings)) {
            int detectedLength = getDetectedLength(track);
            if (detectedLength > 0) {
                initializeTrack(track, detectedLength);
            } else {
                // Not enough info yet, default to obfuscated
                applyObfuscation(settings);
                return;
            }
        }

        // Check if track should reset (tooltip closed and reopened)
        // Set reset delay if not configured
        if (track.resetDelayMs == 0) {
            track.resetDelayMs = 1000; // 1 second default like TypewriterTrack
        }
        boolean wasReset = track.checkAndResetIfNeeded();

        // Ensure timing is started
        long now = System.currentTimeMillis();
        if (track.startTimeMs == 0) {
            track.startTimeMs = now;
        }

        // Update last access time for reset detection
        track.lastAccessMs = now;

        // Calculate how many characters should be revealed
        long elapsed = now - track.startTimeMs;
        int revealedCount = (int) (elapsed / speedMs);

        // Debug logging (only for first character to avoid spam)
        if (settings.index == 0) {
            LOGGER.info("REVEAL DEBUG: idx=0 elapsed={}ms revealedCount={} speedMs={} startTime={} now={} wasReset={} trackLength={}",
                    elapsed, revealedCount, speedMs, track.startTimeMs, now, wasReset, track.length);
        }

        // Check if this character should be obfuscated based on its rank
        if (settings.index >= 0 && settings.index < track.ranks.length) {
            int rank = track.ranks[settings.index];
            if (rank >= revealedCount) {
                // This character hasn't been revealed yet
                applyObfuscation(settings);
                if (settings.index == 0) {
                    LOGGER.info("REVEAL DEBUG: idx=0 rank={} >= revealedCount={} → OBFUSCATED", rank, revealedCount);
                }
            } else if (settings.index == 0) {
                LOGGER.info("REVEAL DEBUG: idx=0 rank={} < revealedCount={} → REVEALED", rank, revealedCount);
            }
        }
    }

    /**
     * Apply hide animation - text starts readable and becomes obfuscated.
     */
    private void applyHide(@NotNull EffectSettings settings) {
        ObfuscateTrack track = getOrCreateTrack(settings);

        // Update max length seen
        updateLengthDetection(track, settings.index);

        // Initialize track if needed
        if (track.order == null || needsReinitialize(track, settings)) {
            int detectedLength = getDetectedLength(track);
            if (detectedLength > 0) {
                initializeTrack(track, detectedLength);
            } else {
                // Not enough info yet, default to readable
                return;
            }
        }

        // Check if track should reset (tooltip closed and reopened)
        // Set reset delay if not configured
        if (track.resetDelayMs == 0) {
            track.resetDelayMs = 1000; // 1 second default like TypewriterTrack
        }
        boolean wasReset = track.checkAndResetIfNeeded();

        // Ensure timing is started
        long now = System.currentTimeMillis();
        if (track.startTimeMs == 0) {
            track.startTimeMs = now;
        }

        // Update last access time for reset detection
        track.lastAccessMs = now;

        // Calculate how many characters should be hidden
        long elapsed = now - track.startTimeMs;
        int hiddenCount = (int) (elapsed / speedMs);

        // Debug logging (only for first character to avoid spam)
        if (settings.index == 0) {
            LOGGER.info("HIDE DEBUG: idx=0 elapsed={}ms hiddenCount={} speedMs={} startTime={} now={} wasReset={} trackLength={}",
                    elapsed, hiddenCount, speedMs, track.startTimeMs, now, wasReset, track.length);
        }

        // Check if this character should be obfuscated based on its rank
        if (settings.index >= 0 && settings.index < track.ranks.length) {
            int rank = track.ranks[settings.index];
            if (rank < hiddenCount) {
                // This character has been hidden
                applyObfuscation(settings);
                if (settings.index == 0) {
                    LOGGER.info("HIDE DEBUG: idx=0 rank={} < hiddenCount={} → OBFUSCATED", rank, hiddenCount);
                }
            } else if (settings.index == 0) {
                LOGGER.info("HIDE DEBUG: idx=0 rank={} >= hiddenCount={} → VISIBLE", rank, hiddenCount);
            }
        }
    }

    /**
     * Apply random flickering - random characters obfuscate for 0.5-2 seconds then reveal.
     */
    private void applyRandom(@NotNull EffectSettings settings) {
        ObfuscateTrack track = getOrCreateTrack(settings);
        long now = System.currentTimeMillis();

        // Update max length seen
        updateLengthDetection(track, settings.index);
        int detectedLength = getDetectedLength(track);

        if (detectedLength <= 0) {
            // Not enough info yet
            return;
        }

        // Initialize random state if needed
        if (track.lastRandomUpdateMs == 0) {
            track.lastRandomUpdateMs = now;
            track.length = detectedLength;
        }

        // Update which characters are obfuscated
        updateRandomObfuscation(track, now, detectedLength);

        // Check if this character is currently obfuscated
        if (track.currentlyObfuscatedIndices.contains(settings.index)) {
            applyObfuscation(settings);
        }
    }

    /**
     * Update random obfuscation state - pick new characters to obfuscate.
     */
    private void updateRandomObfuscation(ObfuscateTrack track, long now, int textLength) {
        // Remove characters whose time is up
        Iterator<Integer> iterator = track.currentlyObfuscatedIndices.iterator();
        while (iterator.hasNext()) {
            int index = iterator.next();
            Long revealTime = track.obfuscateUntilMs.get(index);
            if (revealTime != null && now >= revealTime) {
                iterator.remove();
                track.obfuscateUntilMs.remove(index);
            }
        }

        // Pick new random characters to obfuscate (20% chance per frame, but only if we have fewer than 30% obfuscated)
        if (track.currentlyObfuscatedIndices.size() < textLength * 0.3) {
            // Every 100ms, consider adding new obfuscated characters
            if (now - track.lastRandomUpdateMs > 100) {
                track.lastRandomUpdateMs = now;

                // Pick 1-3 new characters
                int numToAdd = RANDOM.nextInt(3) + 1;
                for (int i = 0; i < numToAdd; i++) {
                    int randomIndex = RANDOM.nextInt(textLength);
                    if (!track.currentlyObfuscatedIndices.contains(randomIndex)) {
                        // Obfuscate for 500ms to 2000ms
                        long duration = 500 + RANDOM.nextInt(1500);
                        track.currentlyObfuscatedIndices.add(randomIndex);
                        track.obfuscateUntilMs.put(randomIndex, now + duration);
                    }
                }
            }
        }
    }

    /**
     * Apply obfuscation to a character (either minecraft or readable alphabet).
     */
    private void applyObfuscation(@NotNull EffectSettings settings) {
        if (useReadableAlphabet) {
            settings.codepoint = getReadableRandomChar(settings.index);
        } else {
            settings.useRandomGlyph = true;
        }
    }

    /**
     * Update length detection for dynamic span length tracking.
     */
    private void updateLengthDetection(ObfuscateTrack track, int index) {
        Object key = track.cacheKey;
        if (key != null) {
            Integer currentMax = lengthDetection.get(key);
            if (currentMax == null || index > currentMax) {
                lengthDetection.put(key, index);
            }
        }
    }

    /**
     * Get detected length from tracking.
     */
    private int getDetectedLength(ObfuscateTrack track) {
        Object key = track.cacheKey;
        if (key != null) {
            Integer max = lengthDetection.get(key);
            if (max != null) {
                return max + 1; // Convert from 0-based index to length
            }
        }
        // Fallback to track.length if set
        return track.length > 0 ? track.length : 0;
    }

    /**
     * Get or create an obfuscate track for the current context.
     * Uses stable key for tooltips (text.intern() based) for persistent animation.
     */
    private ObfuscateTrack getOrCreateTrack(@NotNull EffectSettings settings) {
        // Prefer stable key for tooltips (uses text.intern() for persistence)
        // Fall back to regular obfuscateKey, then effect instance
        Object key = settings.obfuscateStableKey;
        if (key == null) {
            key = settings.obfuscateKey;
        }
        if (key == null) {
            key = this;
        }

        // For obfuscate effect, we want span-level independence
        // Use the already-composite key if it's an ObfKey, otherwise wrap it
        ObfKey compositeKey;
        if (key instanceof ObfKey) {
            compositeKey = (ObfKey) key;
        } else {
            int spanId = settings.obfuscateSpanStart >= 0 ? settings.obfuscateSpanStart : 0;
            compositeKey = new ObfKey(key, spanId);
        }

        // Check if we already have this track cached in settings
        if (settings.obfuscateTrack != null && compositeKey.equals(settings.obfuscateTrack.cacheKey)) {
            return settings.obfuscateTrack;
        }

        // Get or create from global cache
        ObfuscateTrack track = ObfuscateTracks.getInstance().get(compositeKey);
        track.cacheKey = compositeKey;
        settings.obfuscateTrack = track;

        return track;
    }

    /**
     * Check if track needs reinitialization.
     */
    private boolean needsReinitialize(ObfuscateTrack track, EffectSettings settings) {
        String dirKey = direction.name();
        int detectedLength = getDetectedLength(track);

        return track.length != detectedLength || !dirKey.equals(track.directionKey);
    }

    /**
     * Initialize a track with reveal order based on direction.
     */
    private void initializeTrack(ObfuscateTrack track, int length) {
        // Create reveal order
        List<Integer> revealOrder = ObfuscateAnimator.createRevealOrder(direction, length, RANDOM);

        // Convert list to arrays for efficient lookup
        track.order = new int[revealOrder.size()];
        track.ranks = new int[revealOrder.size()];

        for (int i = 0; i < revealOrder.size(); i++) {
            int charIndex = revealOrder.get(i);
            track.order[i] = charIndex;
            track.ranks[charIndex] = i;
        }

        track.length = length;
        track.directionKey = direction.name();
        track.intervalMs = speedMs;
        // DON'T reset timers here - we only want to reset on tooltip reopen (checkAndResetIfNeeded)
        // Resetting here causes the timer to reset every time length changes during first render
        // track.resetTimers(); // REMOVED - this was the bug!

        LOGGER.debug("Initialized obfuscate track: length={}, direction={}", length, direction);
    }

    /**
     * Get a readable random character (alphanumeric).
     */
    private int getReadableRandomChar(int seed) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Use time to make it change frequently
        long timeSeed = Util.getMillis() / 50; // Change every 50ms
        long combinedSeed = timeSeed + seed * 31L;

        // Simple hash
        combinedSeed = combinedSeed ^ (combinedSeed >>> 33);
        combinedSeed *= 0xff51afd7ed558ccdL;
        combinedSeed = combinedSeed ^ (combinedSeed >>> 33);

        int index = (int) Math.abs(combinedSeed % chars.length());
        return chars.charAt(index);
    }

    @NotNull
    @Override
    public String getName() {
        return "obfuscate";
    }
}
