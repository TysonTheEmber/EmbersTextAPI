package net.tysontheember.emberstextapi.util;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class EffectApplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/EffectApplicator");

    private EffectApplicator() {}

    public static EffectSettings buildSettings(
            ETAStyle etaStyle, Style style, int index, int codepoint,
            float x, float y, float shadowOffset,
            float r, float g, float b, float a,
            boolean dropShadow) {

        EffectSettings settings = new EffectSettings(
                x + shadowOffset, y + shadowOffset,
                r, g, b, a,
                index, codepoint, dropShadow
        );
        settings.shadowOffset = shadowOffset;

        settings.obfuscateKey = etaStyle.emberstextapi$getObfuscateKey();
        settings.obfuscateStableKey = etaStyle.emberstextapi$getObfuscateStableKey();
        if (settings.obfuscateKey == null) {
            settings.obfuscateKey = style;
        }
        if (settings.obfuscateStableKey == null) {
            settings.obfuscateStableKey = style;
        }
        settings.obfuscateSpanStart = etaStyle.emberstextapi$getObfuscateSpanStart();
        settings.obfuscateSpanLength = etaStyle.emberstextapi$getObfuscateSpanLength();

        int globalIndex = etaStyle.emberstextapi$getTypewriterIndex();
        if (globalIndex >= 0) {
            settings.absoluteIndex = globalIndex;

            int spanStart = settings.obfuscateSpanStart;
            if (spanStart >= 0) {
                settings.index = globalIndex - spanStart;
            } else {
                settings.index = globalIndex;
            }
        } else {
            settings.absoluteIndex = index;
        }

        TypewriterTrack track = etaStyle.emberstextapi$getTypewriterTrack();
        if (track != null) {
            settings.typewriterTrack = track;
            settings.typewriterIndex = globalIndex >= 0 ? globalIndex : index;
        }

        return settings;
    }

    public static void applyEffects(List<Effect> effects, EffectSettings settings) {
        for (Effect effect : effects) {
            try {
                effect.apply(settings);
                List<EffectSettings> currentSiblings = settings.getSiblingsOrEmpty();
                for (int i = 0; i < currentSiblings.size(); i++) {
                    effect.apply(currentSiblings.get(i));
                }
            } catch (Exception e) {
                LOGGER.warn("Effect {} failed to apply: {}", effect.getName(), e.getMessage());
            }
        }
    }

    public static void renderChar(
            EffectSettings settings,
            int originalCodepoint,
            Style style,
            GlyphSource glyphSource,
            GlyphInfo glyphInfo,
            BakedGlyph bakedGlyph,
            float lineHeight) {

        if (settings.a == 0) {
            return;
        }

        if (settings.useRandomGlyph) {
            bakedGlyph = glyphSource.getRandomGlyph(net.minecraft.util.RandomSource.create(), originalCodepoint);
        } else if (settings.codepoint != originalCodepoint) {
            bakedGlyph = glyphSource.getGlyph(settings.codepoint);
        }

    }
}
