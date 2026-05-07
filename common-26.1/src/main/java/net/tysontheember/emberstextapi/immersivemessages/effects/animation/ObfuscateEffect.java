package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.minecraft.util.Util;
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
import java.util.concurrent.ThreadLocalRandom;

public class ObfuscateEffect extends BaseEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObfuscateEffect.class);

    private final Mode mode;

    private final int speedMs;

    private final ObfuscateMode direction;

    private final boolean useReadableAlphabet;

    private final Map<Object, Integer> lengthDetection = new HashMap<>();

    private enum Mode {
        REVEAL,
        HIDE,
        CONSTANT,
        RANDOM
    }

    public ObfuscateEffect(@NotNull Params params) {
        super(params);

        this.mode = params.getString("mode")
                .map(String::toLowerCase)
                .map(m -> switch (m) {
                    case "reveal" -> Mode.REVEAL;
                    case "hide" -> Mode.HIDE;
                    case "random" -> Mode.RANDOM;
                    default -> Mode.CONSTANT;
                })
                .orElse(Mode.CONSTANT);

        int rawSpeed = params.getDouble("speed")
                .map(Number::intValue)
                .filter(ms -> ms > 0)
                .orElse(20);
        this.speedMs = ValidationHelper.clamp("obfuscate", "speed", rawSpeed, 1, 10000);

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

    private void applyConstant(@NotNull EffectSettings settings) {
        if (useReadableAlphabet) {
            settings.codepoint = getReadableRandomChar(settings.index);
        } else {
            settings.useRandomGlyph = true;
        }
    }

    private void applyReveal(@NotNull EffectSettings settings) {
        ObfuscateTrack track = getOrCreateTrack(settings);

        int spanLength = resolveSpanLength(track, settings);

        if (track.order == null || needsReinitialize(track, spanLength)) {
            if (spanLength > 0) {
                initializeTrack(track, spanLength);
            } else {
                applyObfuscation(settings);
                return;
            }
        }

        if (track.resetDelayMs == 0) {
            track.resetDelayMs = 1000;
        }
        track.checkAndResetIfNeeded();

        long now = System.currentTimeMillis();
        if (track.startTimeMs == 0) {
            track.startTimeMs = now;
        }
        track.lastAccessMs = now;

        long elapsed = now - track.startTimeMs;
        int revealedCount = (int) (elapsed / speedMs);

        if (settings.index >= 0 && settings.index < track.ranks.length) {
            int rank = track.ranks[settings.index];
            if (rank >= revealedCount) {
                applyObfuscation(settings);
            }
        }
    }

    private void applyHide(@NotNull EffectSettings settings) {
        ObfuscateTrack track = getOrCreateTrack(settings);

        int spanLength = resolveSpanLength(track, settings);

        if (track.order == null || needsReinitialize(track, spanLength)) {
            if (spanLength > 0) {
                initializeTrack(track, spanLength);
            } else {
                return;
            }
        }

        if (track.resetDelayMs == 0) {
            track.resetDelayMs = 1000;
        }
        track.checkAndResetIfNeeded();

        long now = System.currentTimeMillis();
        if (track.startTimeMs == 0) {
            track.startTimeMs = now;
        }
        track.lastAccessMs = now;

        long elapsed = now - track.startTimeMs;
        int hiddenCount = (int) (elapsed / speedMs);

        if (settings.index >= 0 && settings.index < track.ranks.length) {
            int rank = track.ranks[settings.index];
            if (rank < hiddenCount) {
                applyObfuscation(settings);
            }
        }
    }

    private void applyRandom(@NotNull EffectSettings settings) {
        ObfuscateTrack track = getOrCreateTrack(settings);
        long now = System.currentTimeMillis();

        int detectedLength = resolveSpanLength(track, settings);

        if (detectedLength <= 0) {
            return;
        }

        if (track.lastRandomUpdateMs == 0) {
            track.lastRandomUpdateMs = now;
            track.length = detectedLength;
        }

        updateRandomObfuscation(track, now, detectedLength);

        if (track.currentlyObfuscatedIndices.contains(settings.index)) {
            applyObfuscation(settings);
        }
    }

    private void updateRandomObfuscation(ObfuscateTrack track, long now, int textLength) {

        Iterator<Integer> iterator = track.currentlyObfuscatedIndices.iterator();
        while (iterator.hasNext()) {
            int index = iterator.next();
            Long revealTime = track.obfuscateUntilMs.get(index);
            if (revealTime != null && now >= revealTime) {
                iterator.remove();
                track.obfuscateUntilMs.remove(index);
            }
        }

        if (track.currentlyObfuscatedIndices.size() < textLength * 0.3) {

            if (now - track.lastRandomUpdateMs > 100) {
                track.lastRandomUpdateMs = now;

                int numToAdd = ThreadLocalRandom.current().nextInt(3) + 1;
                for (int i = 0; i < numToAdd; i++) {
                    int randomIndex = ThreadLocalRandom.current().nextInt(textLength);
                    if (!track.currentlyObfuscatedIndices.contains(randomIndex)) {

                        long duration = 500 + ThreadLocalRandom.current().nextInt(1500);
                        track.currentlyObfuscatedIndices.add(randomIndex);
                        track.obfuscateUntilMs.put(randomIndex, now + duration);
                    }
                }
            }
        }
    }

    private void applyObfuscation(@NotNull EffectSettings settings) {
        if (useReadableAlphabet) {
            settings.codepoint = getReadableRandomChar(settings.index);
        } else {
            settings.useRandomGlyph = true;
        }
    }

    private void updateLengthDetection(ObfuscateTrack track, int index) {
        Object key = track.cacheKey;
        if (key != null) {
            Integer currentMax = lengthDetection.get(key);
            if (currentMax == null || index > currentMax) {
                lengthDetection.put(key, index);
            }
        }
    }

    private int getDetectedLength(ObfuscateTrack track) {
        Object key = track.cacheKey;
        if (key != null) {
            Integer max = lengthDetection.get(key);
            if (max != null) {
                return max + 1;
            }
        }

        return track.length > 0 ? track.length : 0;
    }

    private ObfuscateTrack getOrCreateTrack(@NotNull EffectSettings settings) {

        Object key = settings.obfuscateStableKey;
        if (key == null) {
            key = settings.obfuscateKey;
        }
        if (key == null) {
            key = this;
        }

        ObfKey compositeKey;
        if (key instanceof ObfKey) {
            compositeKey = (ObfKey) key;
        } else {
            int spanId = settings.obfuscateSpanStart >= 0 ? settings.obfuscateSpanStart : 0;
            compositeKey = new ObfKey(key, spanId);
        }

        if (settings.obfuscateTrack != null && compositeKey.equals(settings.obfuscateTrack.cacheKey)) {
            return settings.obfuscateTrack;
        }

        ObfuscateTrack track = ObfuscateTracks.getInstance().get(compositeKey);
        track.cacheKey = compositeKey;
        settings.obfuscateTrack = track;

        return track;
    }

    private int resolveSpanLength(ObfuscateTrack track, EffectSettings settings) {

        if (settings.obfuscateSpanLength > 0) {
            return settings.obfuscateSpanLength;
        }

        updateLengthDetection(track, settings.index);
        return getDetectedLength(track);
    }

    private boolean needsReinitialize(ObfuscateTrack track, int targetLength) {
        return track.length != targetLength || !direction.name().equals(track.directionKey);
    }

    private void initializeTrack(ObfuscateTrack track, int length) {
        List<Integer> revealOrder = ObfuscateAnimator.createRevealOrder(direction, length, ThreadLocalRandom.current());

        track.order = new int[revealOrder.size()];
        track.ranks = new int[length];

        for (int i = 0; i < revealOrder.size(); i++) {
            int charIndex = revealOrder.get(i);
            track.order[i] = charIndex;
            track.ranks[charIndex] = i;
        }

        track.length = length;
        track.directionKey = direction.name();
        track.intervalMs = speedMs;

        LOGGER.debug("Initialized obfuscate track: dir={} len={}", direction, length);
    }

    private int getReadableRandomChar(int seed) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        long timeSeed = Util.getMillis() / 50;
        long combinedSeed = timeSeed + seed * 31L;

        combinedSeed = combinedSeed ^ (combinedSeed >>> 33);
        combinedSeed *= 0xff51afd7ed558ccdL;
        combinedSeed = combinedSeed ^ (combinedSeed >>> 33);

        int index = Math.floorMod(combinedSeed, chars.length());
        return chars.charAt(index);
    }

    @NotNull
    @Override
    public String getName() {
        return "obfuscate";
    }
}
