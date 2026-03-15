package net.tysontheember.emberstextapi.mixin.client.emojiful;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.mixin.client.FontAccess;
import net.tysontheember.emberstextapi.util.EffectApplicator;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

/**
 * Compatibility mixin for Emojiful's EmojiCharacterRenderer.
 * <p>
 * Targets both class names used across Emojiful versions:
 * <ul>
 *   <li>Forge 1.20.1 (v4.x): {@code EmojiFontRenderer$EmojiCharacterRenderer}</li>
 *   <li>NeoForge 1.21.1 (v5.x): {@code EmojiFontHelper$EmojiCharacterRenderer}</li>
 * </ul>
 * <p>
 * Uses {@code @Pseudo} + {@code require = 0} so the mixin is optional and gracefully
 * absent when Emojiful is not installed.
 */
@Pseudo
@Mixin(targets = {
    "com.hrznstudio.emojiful.render.EmojiFontHelper$EmojiCharacterRenderer",
    "com.hrznstudio.emojiful.render.EmojiFontRenderer$EmojiCharacterRenderer"
}, priority = 1200)
public abstract class EmojiCharacterRendererMixin {

    @Unique
    private static final Logger emberstextapi$LOGGER = LoggerFactory.getLogger("EmbersTextAPI/EmojifulCompat");

    // Cached reflection fields — initialized once, null if reflection fails
    @Unique private static boolean emberstextapi$reflectionInitialized = false;
    @Unique private static Field emberstextapi$fieldX;
    @Unique private static Field emberstextapi$fieldY;
    @Unique private static Field emberstextapi$fieldPose;
    @Unique private static Field emberstextapi$fieldBuffer;
    @Unique private static Field emberstextapi$fieldDropShadow;
    @Unique private static Field emberstextapi$fieldDimFactor;
    @Unique private static Field emberstextapi$fieldR;
    @Unique private static Field emberstextapi$fieldG;
    @Unique private static Field emberstextapi$fieldB;
    @Unique private static Field emberstextapi$fieldA;
    @Unique private static Field emberstextapi$fieldPackedLight;
    @Unique private static Field emberstextapi$fieldEmojis;
    @Unique private static Field emberstextapi$fieldSeeThrough;
    @Unique private static Field emberstextapi$fieldFont;
    // true if this$0 IS a Font (Forge: EmojiFontRenderer extends Font)
    @Unique private static boolean emberstextapi$this0IsFont = false;

    @Unique
    private static void emberstextapi$initReflection(Object target) {
        if (emberstextapi$reflectionInitialized) return;
        emberstextapi$reflectionInitialized = true;

        try {
            Class<?> clazz = target.getClass();

            emberstextapi$fieldX = emberstextapi$findField(clazz, "x");
            emberstextapi$fieldY = emberstextapi$findField(clazz, "y");
            emberstextapi$fieldPose = emberstextapi$findField(clazz, "matrix", "pose");
            emberstextapi$fieldBuffer = emberstextapi$findField(clazz, "buffer", "bufferSource");
            emberstextapi$fieldDropShadow = emberstextapi$findField(clazz, "dropShadow");
            emberstextapi$fieldDimFactor = emberstextapi$findField(clazz, "dimFactor");
            emberstextapi$fieldR = emberstextapi$findField(clazz, "r");
            emberstextapi$fieldG = emberstextapi$findField(clazz, "g");
            emberstextapi$fieldB = emberstextapi$findField(clazz, "b");
            emberstextapi$fieldA = emberstextapi$findField(clazz, "a");
            emberstextapi$fieldPackedLight = emberstextapi$findField(clazz, "packedLight", "packedLightCoords");
            emberstextapi$fieldEmojis = emberstextapi$findField(clazz, "emojis");
            emberstextapi$fieldSeeThrough = emberstextapi$findField(clazz, "seeThrough");

            // Try this$0 first — in Forge, EmojiFontRenderer extends Font so this$0 IS a Font
            emberstextapi$fieldFont = emberstextapi$findField(clazz, "this$0");
            if (emberstextapi$fieldFont != null && Font.class.isAssignableFrom(emberstextapi$fieldFont.getType())) {
                emberstextapi$this0IsFont = true;
            } else {
                // NeoForge: EmojiFontHelper doesn't extend Font, no this$0 or it's not a Font
                // We'll use Minecraft.getInstance().font as fallback
                emberstextapi$fieldFont = null;
                emberstextapi$this0IsFont = false;
            }

            emberstextapi$LOGGER.info("Emojiful compat: reflection initialized (fontFromThis0={})", emberstextapi$this0IsFont);
        } catch (Exception e) {
            emberstextapi$LOGGER.warn("Emojiful compat: failed to initialize reflection: {}", e.getMessage());
        }
    }

    @Unique
    private static Field emberstextapi$findField(Class<?> clazz, String... names) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (String name : names) {
                try {
                    Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {}
            }
        }
        return null;
    }

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void emberstextapi$onAccept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        ETAStyle etaStyle = (ETAStyle) style;
        List<Effect> effects = etaStyle.emberstextapi$getEffects().asList();

        if (effects.isEmpty()) {
            return;
        }

        emberstextapi$initReflection(this);

        // Skip emoji placeholder chars — let Emojiful render the sprite
        if (codepoint == 0x2603) {
            return;
        }

        try {
            // Skip emoji positions tracked in the emojis map
            if (emberstextapi$fieldEmojis != null) {
                @SuppressWarnings("unchecked")
                HashMap<Integer, ?> emojis = (HashMap<Integer, ?>) emberstextapi$fieldEmojis.get(this);
                if (emojis != null && emojis.containsKey(index)) {
                    return;
                }
            }

            if (emberstextapi$fieldX == null || emberstextapi$fieldY == null ||
                emberstextapi$fieldPose == null || emberstextapi$fieldBuffer == null) {
                return;
            }

            float x = emberstextapi$fieldX.getFloat(this);
            float y = emberstextapi$fieldY.getFloat(this);
            Matrix4f pose = (Matrix4f) emberstextapi$fieldPose.get(this);
            MultiBufferSource bufferSource = (MultiBufferSource) emberstextapi$fieldBuffer.get(this);
            boolean dropShadow = emberstextapi$fieldDropShadow != null && emberstextapi$fieldDropShadow.getBoolean(this);
            float dimFactor = emberstextapi$fieldDimFactor != null ? emberstextapi$fieldDimFactor.getFloat(this) : 1.0f;
            float baseR = emberstextapi$fieldR != null ? emberstextapi$fieldR.getFloat(this) : 1.0f;
            float baseG = emberstextapi$fieldG != null ? emberstextapi$fieldG.getFloat(this) : 1.0f;
            float baseB = emberstextapi$fieldB != null ? emberstextapi$fieldB.getFloat(this) : 1.0f;
            float baseA = emberstextapi$fieldA != null ? emberstextapi$fieldA.getFloat(this) : 1.0f;
            int packedLight = emberstextapi$fieldPackedLight != null ? emberstextapi$fieldPackedLight.getInt(this) : 15728880;

            // Get Font — from this$0 (Forge: EmojiFontRenderer IS a Font) or Minecraft.getInstance().font
            Font font;
            if (emberstextapi$this0IsFont && emberstextapi$fieldFont != null) {
                font = (Font) emberstextapi$fieldFont.get(this);
            } else {
                font = Minecraft.getInstance().font;
            }

            // Derive DisplayMode from seeThrough boolean
            boolean seeThrough = emberstextapi$fieldSeeThrough != null && emberstextapi$fieldSeeThrough.getBoolean(this);
            Font.DisplayMode displayMode = seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;

            if (font == null || pose == null || bufferSource == null) {
                return;
            }

            FontAccess fontAccess = (FontAccess) font;
            FontSet fontSet = fontAccess.callGetFontSet(style.getFont());
            GlyphInfo glyphInfo = fontSet.getGlyphInfo(codepoint, fontAccess.getFilterFishyGlyphs());
            BakedGlyph bakedGlyph = style.isObfuscated() && codepoint != 32
                    ? fontSet.getRandomGlyph(glyphInfo)
                    : fontSet.getGlyph(codepoint);

            float red, green, blue;
            float alpha = baseA;

            TextColor textColor = style.getColor();
            if (textColor != null) {
                int colorValue = textColor.getValue();
                red = ((colorValue >> 16) & 0xFF) / 255.0f * dimFactor;
                green = ((colorValue >> 8) & 0xFF) / 255.0f * dimFactor;
                blue = (colorValue & 0xFF) / 255.0f * dimFactor;
            } else {
                red = baseR;
                green = baseG;
                blue = baseB;
            }

            float shadowOffset = dropShadow ? glyphInfo.getShadowOffset() : 0.0f;

            if (!(bakedGlyph instanceof EmptyGlyph)) {
                EffectSettings settings = EffectApplicator.buildSettings(
                        etaStyle, style, index, codepoint,
                        x, y, shadowOffset,
                        red, green, blue, alpha, dropShadow
                );

                EffectApplicator.applyEffects(effects, settings);

                EffectApplicator.renderChar(settings, codepoint, style, fontSet, glyphInfo, bakedGlyph,
                        pose, bufferSource, displayMode, packedLight, font.lineHeight);

                if (settings.hasSiblings()) {
                    for (EffectSettings sibling : settings.getSiblingsOrEmpty()) {
                        EffectApplicator.renderChar(sibling, codepoint, style, fontSet, glyphInfo, bakedGlyph,
                                pose, bufferSource, displayMode, packedLight, font.lineHeight);
                    }
                }
            }

            float glyphWidth = glyphInfo.getAdvance(style.isBold());
            emberstextapi$fieldX.setFloat(this, x + glyphWidth);

            cir.setReturnValue(true);

        } catch (Exception e) {
            emberstextapi$LOGGER.debug("Emojiful compat: failed to apply effects at index {}: {}", index, e.getMessage());
        }
    }
}
