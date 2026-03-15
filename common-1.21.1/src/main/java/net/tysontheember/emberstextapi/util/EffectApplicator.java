package net.tysontheember.emberstextapi.util;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.accessor.ETABakedGlyph;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTrack;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Shared utility for applying ETA effects and rendering characters with those effects.
 * Used by both StringRenderOutputMixin (vanilla path) and EmojiCharacterRendererMixin (Emojiful compat).
 */
public final class EffectApplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/EffectApplicator");

    private EffectApplicator() {}

    /**
     * Build an EffectSettings from the current rendering context and ETAStyle.
     */
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

        TypewriterTrack track = etaStyle.emberstextapi$getTypewriterTrack();
        if (track != null) {
            int typingIndex = etaStyle.emberstextapi$getTypewriterIndex();
            settings.absoluteIndex = typingIndex >= 0 ? typingIndex : index;
            settings.typewriterTrack = track;
            settings.typewriterIndex = typingIndex;
        } else {
            settings.absoluteIndex = index;
        }

        return settings;
    }

    /**
     * Apply all effects from the style to the settings, including siblings.
     */
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

    /**
     * Render a single character with the given effect settings.
     */
    public static void renderChar(
            EffectSettings settings,
            int originalCodepoint,
            Style style,
            FontSet fontSet,
            GlyphInfo glyphInfo,
            BakedGlyph bakedGlyph,
            Matrix4f pose,
            MultiBufferSource bufferSource,
            Font.DisplayMode mode,
            int packedLightCoords,
            float lineHeight) {

        if (settings.a == 0) {
            return;
        }

        if (settings.useRandomGlyph) {
            bakedGlyph = fontSet.getRandomGlyph(glyphInfo);
            if (bakedGlyph instanceof EmptyGlyph) {
                return;
            }
        } else if (settings.codepoint != originalCodepoint) {
            bakedGlyph = fontSet.getGlyph(settings.codepoint);
            if (bakedGlyph instanceof EmptyGlyph) {
                return;
            }
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(bakedGlyph.renderType(mode));

        Matrix4f renderPose = pose;
        if (settings.rot != 0) {
            float glyphWidth = glyphInfo.getAdvance(style.isBold());
            float originX = glyphWidth / 2f;
            float originY = lineHeight / 2f;
            renderPose = EffectUtil.rotate(pose, settings, settings.rot, originX, originY);
        }

        ETABakedGlyph etaGlyph = (ETABakedGlyph) bakedGlyph;
        etaGlyph.emberstextapi$render(settings, style.isItalic(), 0f, renderPose, vertexConsumer, packedLightCoords);

        if (style.isBold()) {
            etaGlyph.emberstextapi$render(settings, style.isItalic(), glyphInfo.getBoldOffset(), renderPose, vertexConsumer, packedLightCoords);
        }
    }
}
