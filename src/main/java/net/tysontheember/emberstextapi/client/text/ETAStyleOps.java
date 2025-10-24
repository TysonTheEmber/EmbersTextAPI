package net.tysontheember.emberstextapi.client.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.duck.ETAStyle;

/**
 * Utilities for working with ETA-specific payload attached to vanilla {@link Style} instances.
 */
public final class ETAStyleOps {
    private ETAStyleOps() {
    }

    public static SpanEffect spanEffect(String id) {
        return new SpanEffect(id, Map.of());
    }

    public static SpanEffect spanEffect(String id, Map<String, String> parameters) {
        return new SpanEffect(id, parameters);
    }

    public static TypewriterTrack typewriterTrack(TypewriterTrack.Mode mode, float speedMultiplier, @Nullable String trackId) {
        return new TypewriterTrack(mode, speedMultiplier, trackId);
    }

    public static Style copyEtaPayload(Style source, Style target) {
        if (source == target) {
            return target;
        }
        ETAStyle src = (ETAStyle) source;
        ETAStyle dst = (ETAStyle) target;
        dst.eta$setEffects(src.eta$getEffects());
        dst.eta$setTrack(src.eta$getTrack());
        dst.eta$setTypewriterIndex(src.eta$getTypewriterIndex());
        dst.eta$setNeonIntensity(src.eta$getNeonIntensity());
        dst.eta$setWobbleAmplitude(src.eta$getWobbleAmplitude());
        dst.eta$setWobbleSpeed(src.eta$getWobbleSpeed());
        dst.eta$setGradientFlow(src.eta$getGradientFlow());
        return target;
    }

    public static Style merge(Style child, Style parent, Style result) {
        ETAStyle childDuck = (ETAStyle) child;
        ETAStyle parentDuck = (ETAStyle) parent;
        ETAStyle resultDuck = (ETAStyle) result;

        List<SpanEffect> effects = childDuck.eta$getEffects();
        if (effects.isEmpty()) {
            effects = parentDuck.eta$getEffects();
        }
        resultDuck.eta$setEffects(effects);

        TypewriterTrack track = childDuck.eta$getTrack();
        if (track == null || !track.isActive()) {
            track = parentDuck.eta$getTrack();
        }
        resultDuck.eta$setTrack(track);

        int index = childDuck.eta$getTypewriterIndex();
        if (index == 0) {
            index = parentDuck.eta$getTypewriterIndex();
        }
        resultDuck.eta$setTypewriterIndex(index);

        resultDuck.eta$setNeonIntensity(selectScalar(childDuck.eta$getNeonIntensity(), parentDuck.eta$getNeonIntensity()));
        resultDuck.eta$setWobbleAmplitude(selectScalar(childDuck.eta$getWobbleAmplitude(), parentDuck.eta$getWobbleAmplitude()));
        resultDuck.eta$setWobbleSpeed(selectScalar(childDuck.eta$getWobbleSpeed(), parentDuck.eta$getWobbleSpeed()));
        resultDuck.eta$setGradientFlow(selectScalar(childDuck.eta$getGradientFlow(), parentDuck.eta$getGradientFlow()));

        return result;
    }

    public static Style copyOf(Style style) {
        Style copy = style.applyTo(Style.EMPTY);
        if (copy == style) {
            copy = ensureStandalone(style);
        }
        return copyEtaPayload(style, copy);
    }

    public static Style ensureStandalone(Style style) {
        if (style != Style.EMPTY) {
            return style;
        }
        Style temp = Style.EMPTY.withBold(Boolean.FALSE);
        return temp.withBold(null);
    }

    public static boolean hasEtaPayload(Style style) {
        ETAStyle duck = (ETAStyle) style;
        return !duck.eta$getEffects().isEmpty() || (duck.eta$getTrack() != null && duck.eta$getTrack().isActive())
                || duck.eta$getTypewriterIndex() != 0 || duck.eta$getNeonIntensity() != 0.0f
                || duck.eta$getWobbleAmplitude() != 0.0f || duck.eta$getWobbleSpeed() != 0.0f || duck.eta$getGradientFlow() != 0.0f;
    }

    private static float selectScalar(float child, float parent) {
        return child != 0.0f ? child : parent;
    }

    public static List<SpanEffect> readEffects(Map<String, Map<String, String>> entries) {
        List<SpanEffect> effects = new ArrayList<>(entries.size());
        for (Map.Entry<String, Map<String, String>> entry : entries.entrySet()) {
            effects.add(new SpanEffect(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(effects);
    }
}
