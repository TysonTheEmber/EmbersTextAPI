package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public class GlitchEffect extends BaseEffect {

    private final float frequency;
    private final float shiftChance;
    private final float jitterChance;
    private final float blinkChance;
    private final float offsetMultiplier;
    private final float chromatic;
    private final int numSlices;

    public GlitchEffect(@NotNull Params params) {
        super(params);
        this.frequency = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
        this.shiftChance = params.getDouble("s").map(Number::floatValue).orElse(0.08f);
        this.jitterChance = params.getDouble("j").map(Number::floatValue).orElse(0.015f);
        this.blinkChance = params.getDouble("b").map(Number::floatValue).orElse(0.003f);
        this.offsetMultiplier = params.getDouble("o").map(Number::floatValue).orElse(1.0f);
        this.chromatic = params.getDouble("c").map(Number::floatValue).orElse(0.0f);
        this.numSlices = (int) Math.max(2, Math.min(5, params.getDouble("slices").orElse(2.0)));
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        double time = Util.getMillis() * 0.025 * frequency;
        int pulse = (int) time % 3;

        long seed = settings.index + settings.codepoint + (long) (time * 1000);

        if (pulse == 1 && seedToFloat(seed) < jitterChance) {
            float jitterX = (seedToFloat(seed * 31 + 1) - 0.5f) * 8f * offsetMultiplier;
            float jitterY = (seedToFloat(seed * 31 + 2) - 0.5f) * 4f * offsetMultiplier;
            settings.x += jitterX;
            settings.y += jitterY;
        }

        if (seedToFloat(seed * 37 + 3) < blinkChance) {
            settings.a *= seedToFloat(seed * 37 + 4) < 0.3f ? 0.0f : 0.3f;
        }

        double sliceTime = time * 2;
        long sliceSeed = (long) sliceTime * 1000L * hashCode();

        if (seedToFloat(sliceSeed) < shiftChance) {
            if (numSlices == 2) {
                applyTwoSliceGlitch(settings, sliceSeed);
            } else {
                applyMultiSliceGlitch(settings, sliceSeed);
            }
        }
    }

    private void applyTwoSliceGlitch(EffectSettings settings, long seed) {

        if (settings.isShadow) {
            settings.addSibling(settings.copy());
            settings.x -= settings.shadowOffset;
            settings.y -= settings.shadowOffset;
        }

        float mask = 0.5f + (seedToFloat(seed * 41 + 1) - 0.5f) * 0.5f;

        float offset = (0.75f + seedToFloat(seed * 41 + 2) * 0.75f) * offsetMultiplier;
        if (seedToFloat(seed * 41 + 3) < 0.5f) {
            offset = -offset;
        }

        EffectSettings topSlice = settings.copy();
        topSlice.x += offset;
        topSlice.a *= Math.min(1f, 0.5f + seedToFloat(seed * 41 + 4));
        topSlice.maskBottom = mask;

        if (topSlice.isShadow) {
            topSlice.y -= 1;
            topSlice.r = topSlice.r > 0.5f ? topSlice.r - 0.5f : topSlice.r + 0.5f;
            topSlice.g *= 0.2f;
            topSlice.b *= 0.2f;
        }

        if (chromatic > 0 && Math.abs(offset) > 0.3f) {
            addSliceChromatic(settings, topSlice, offset);
        }

        settings.addSibling(topSlice);

        settings.maskTop = 1 - mask;
        settings.x -= offset;
        settings.a *= Math.min(1f, 0.5f + seedToFloat(seed * 41 + 5));

        if (settings.isShadow) {
            settings.y += 1;
            settings.r *= 0.2f;
            settings.g = settings.g > 0.5f ? settings.g - 0.5f : settings.g + 0.5f;
            settings.b = settings.b > 0.5f ? settings.b - 0.5f : settings.b + 0.5f;
        }

        if (chromatic > 0 && Math.abs(offset) > 0.3f) {
            addSliceChromatic(settings, settings, -offset);
        }
    }

    private void applyMultiSliceGlitch(EffectSettings settings, long seed) {

        if (settings.isShadow) {
            settings.addSibling(settings.copy());
            settings.x -= settings.shadowOffset;
            settings.y -= settings.shadowOffset;
        }

        float[] boundaries = new float[numSlices + 1];
        boundaries[0] = 0f;
        boundaries[numSlices] = 1f;

        for (int i = 1; i < numSlices; i++) {
            float minBound = boundaries[i - 1] + (0.8f / numSlices);
            float maxBound = 1f - (numSlices - i) * (0.8f / numSlices);
            boundaries[i] = minBound + seedToFloat(seed * 47 + i * 7) * (maxBound - minBound);
        }

        float[] offsets = new float[numSlices];
        for (int i = 0; i < numSlices; i++) {
            float baseOffset = (0.75f + seedToFloat(seed * 53 + i * 11) * 0.75f) * offsetMultiplier;
            offsets[i] = (i % 2 == 0) ? baseOffset : -baseOffset;

            if (seedToFloat(seed * 59 + i * 13) < 0.3f) {
                offsets[i] = -offsets[i];
            }
        }

        settings.maskTop = boundaries[0];
        settings.maskBottom = 1f - boundaries[1];
        settings.x += offsets[0];
        applySliceShadowColor(settings, 0);

        if (chromatic > 0 && Math.abs(offsets[0]) > 0.3f) {
            addSliceChromatic(settings, settings, offsets[0]);
        }

        for (int i = 1; i < numSlices; i++) {
            EffectSettings slice = settings.copy();
            slice.maskTop = boundaries[i];
            slice.maskBottom = 1f - boundaries[i + 1];
            slice.x += offsets[i] - offsets[0];
            slice.a *= Math.min(1f, 0.5f + seedToFloat(seed * 61 + i * 17));

            applySliceShadowColor(slice, i);

            if (chromatic > 0 && Math.abs(offsets[i]) > 0.3f) {
                addSliceChromatic(settings, slice, offsets[i]);
            }

            settings.addSibling(slice);
        }
    }

    private void applySliceShadowColor(EffectSettings slice, int sliceIndex) {
        if (!slice.isShadow) return;

        switch (sliceIndex % 3) {
            case 0:
                slice.r = slice.r > 0.5f ? slice.r - 0.5f : slice.r + 0.5f;
                slice.g *= 0.2f;
                slice.b *= 0.2f;
                break;
            case 1:
                slice.r *= 0.2f;
                slice.g = slice.g > 0.5f ? slice.g - 0.5f : slice.g + 0.5f;
                slice.b *= 0.2f;
                break;
            case 2:
                slice.r *= 0.2f;
                slice.g *= 0.2f;
                slice.b = slice.b > 0.5f ? slice.b - 0.5f : slice.b + 0.5f;
                break;
        }
    }

    private void addSliceChromatic(EffectSettings parent, EffectSettings slice, float offset) {
        float chromaticOffset = Math.signum(offset) * chromatic;

        EffectSettings redFringe = slice.copy();
        redFringe.x += chromaticOffset * 0.6f;
        redFringe.g = 0f;
        redFringe.b = 0f;
        redFringe.a *= 0.4f;
        parent.addSibling(redFringe);

        EffectSettings cyanFringe = slice.copy();
        cyanFringe.x -= chromaticOffset * 0.6f;
        cyanFringe.r = 0f;
        cyanFringe.a *= 0.4f;
        parent.addSibling(cyanFringe);
    }

    private static float seedToFloat(long seed) {
        seed = (seed ^ (seed >>> 33)) * 0xff51afd7ed558ccdL;
        seed = (seed ^ (seed >>> 33)) * 0xc4ceb9fe1a85ec53L;
        seed = seed ^ (seed >>> 33);
        return (seed & 0x7FFFFFFFL) / (float) 0x80000000L;
    }

    @NotNull
    @Override
    public String getName() {
        return "glitch";
    }
}
