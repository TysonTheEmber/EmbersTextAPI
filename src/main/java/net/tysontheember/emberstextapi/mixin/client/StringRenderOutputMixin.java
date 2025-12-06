package net.tysontheember.emberstextapi.mixin.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.duck.ETABakedGlyph;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.util.EffectUtil;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Mixin for Font.StringRenderOutput to intercept character rendering.
 * <p>
 * This mixin is the core of the global text styling system. It intercepts
 * the {@code accept()} method which is called for each character during
 * text rendering, applies effects from the Style's effect list, and renders
 * the character with the modified parameters.
 * </p>
 *
 * <h3>Priority 1200:</h3>
 * <p>
 * Set to run after most mods to ensure we capture the final style state.
 * </p>
 *
 * @see ETAStyle
 * @see EffectSettings
 * @see Effect
 */
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput", priority = 1200)
public abstract class StringRenderOutputMixin {

    // ===== Shadow Fields from StringRenderOutput =====

    @Shadow
    @Final
    private Matrix4f pose;

    @Shadow
    @Final
    private MultiBufferSource bufferSource;

    @Shadow
    private float x;

    @Shadow
    private float y;

    @Shadow(remap = false, aliases = {"f_92938_", "field_24240", "b"})
    @Final
    private Font this$0;

    @Shadow
    @Final
    private boolean dropShadow;

    @Shadow
    @Final
    private float dimFactor;

    @Shadow
    @Final
    private float a;

    @Shadow
    @Final
    private float b;

    @Shadow
    @Final
    private float r;

    @Shadow
    @Final
    private float g;

    @Shadow
    @Final
    private Font.DisplayMode mode;

    @Shadow
    @Final
    private int packedLightCoords;

    @Shadow
    protected abstract void addEffect(BakedGlyph.Effect effect);

    // ===== Mixin Injection =====

    /**
     * Intercept character rendering to apply effects.
     * <p>
     * This method is called for each character during text rendering.
     * If the style has effects attached, we take over the rendering
     * process completely to apply those effects.
     * </p>
     *
     * @param index Character index in the string
     * @param style Style for this character
     * @param codepoint Unicode codepoint of the character
     * @param cir Callback info returnable
     */
    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$accept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        // Cast to our duck interface to access effects and item data
        ETAStyle etaStyle = (ETAStyle) style;

        // Check if this style has an item to render
        String itemId = etaStyle.emberstextapi$getItemId();
        if (itemId != null) {
            emberstextapi$renderItem(etaStyle, itemId);
            // Cancel vanilla rendering - we rendered an item instead
            cir.setReturnValue(true);
            return;
        }

        List<Effect> effects = etaStyle.emberstextapi$getEffects().asList();

        // If no effects, let vanilla handle it
        if (effects.isEmpty()) {
            return;
        }

        // Get font rendering components
        FontAccess fontAccess = (FontAccess) this$0;
        FontSet fontSet = fontAccess.callGetFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(codepoint, fontAccess.getFilterFishyGlyphs());
        BakedGlyph bakedGlyph = style.isObfuscated() && codepoint != 32
                ? fontSet.getRandomGlyph(glyphInfo)
                : fontSet.getGlyph(codepoint);

        // Extract color from style
        float red, green, blue;
        float alpha = this.a;

        TextColor textColor = style.getColor();
        if (textColor != null) {
            int colorValue = textColor.getValue();
            red = ((colorValue >> 16) & 0xFF) / 255.0f * this.dimFactor;
            green = ((colorValue >> 8) & 0xFF) / 255.0f * this.dimFactor;
            blue = (colorValue & 0xFF) / 255.0f * this.dimFactor;
        } else {
            red = this.r;
            green = this.g;
            blue = this.b;
        }

        // Calculate shadow offset
        float shadowOffset = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0f;

        // Only render if glyph is not empty
        if (!(bakedGlyph instanceof EmptyGlyph)) {
            // Create effect settings with initial state
            EffectSettings settings = new EffectSettings(
                    this.x + shadowOffset,  // x with shadow offset
                    this.y + shadowOffset,  // y with shadow offset
                    red,                    // r
                    green,                  // g
                    blue,                   // b
                    alpha,                  // a
                    index,                  // index
                    codepoint,              // codepoint
                    this.dropShadow         // isShadow
            );
            settings.shadowOffset = shadowOffset;
            settings.absoluteIndex = index; // For global effects, set absolute index

            // Initialize siblings list with the main settings
            // This allows effects to operate on the base layer
            settings.siblings = Lists.newArrayList(settings);

            // Apply all effects in order
            // Each effect can modify the main settings and/or its siblings
            for (Effect effect : effects) {
                try {
                    // Apply effect to all current siblings
                    int siblingCount = settings.siblings.size();
                    for (int i = 0; i < siblingCount; i++) {
                        effect.apply(settings.siblings.get(i));
                    }
                } catch (Exception e) {
                    // Log but don't crash - one broken effect shouldn't break all rendering
                    // LOGGER would be better but keeping it simple for now
                    e.printStackTrace();
                }
            }

            // Render all sibling layers
            for (EffectSettings sibling : settings.siblings) {
                emberstextapi$renderChar(sibling, codepoint, style, fontSet, glyphInfo, bakedGlyph);
            }

            // Update color values for decorations (strikethrough/underline)
            // Use the final color from the main settings
            red = settings.r;
            green = settings.g;
            blue = settings.b;
            alpha = settings.a;
        }

        // Get glyph width for positioning and decorations
        float glyphWidth = glyphInfo.getAdvance(style.isBold());

        // Render strikethrough decoration
        if (alpha != 0 && style.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(
                    this.x + shadowOffset - 1.0f,
                    this.y + shadowOffset + 4.5f,
                    this.x + shadowOffset + glyphWidth,
                    this.y + shadowOffset + 4.5f - 1.0f,
                    0.01f,
                    red,
                    green,
                    blue,
                    alpha
            ));
        }

        // Render underline decoration
        if (alpha != 0 && style.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(
                    this.x + shadowOffset - 1.0f,
                    this.y + shadowOffset + 9.0f,
                    this.x + shadowOffset + glyphWidth,
                    this.y + shadowOffset + 9.0f - 1.0f,
                    0.01f,
                    red,
                    green,
                    blue,
                    alpha
            ));
        }

        // Advance x position for next character
        this.x += glyphWidth;

        // Cancel vanilla rendering - we handled it
        cir.setReturnValue(true);
    }

    /**
     * Render a single character with the given effect settings.
     * <p>
     * This method handles:
     * <ul>
     *   <li>Codepoint changes (some effects change the character)</li>
     *   <li>Alpha culling (skip if fully transparent)</li>
     *   <li>Rotation matrix transformation</li>
     *   <li>Italic and bold rendering</li>
     * </ul>
     * </p>
     *
     * @param settings Effect settings for this character
     * @param originalCodepoint Original codepoint (before effects)
     * @param style Text style
     * @param fontSet Font set for rendering
     * @param glyphInfo Glyph metrics
     * @param bakedGlyph Original baked glyph
     */
    @Unique
    private void emberstextapi$renderChar(
            EffectSettings settings,
            int originalCodepoint,
            Style style,
            FontSet fontSet,
            GlyphInfo glyphInfo,
            BakedGlyph bakedGlyph) {

        // Skip if fully transparent
        if (settings.a == 0) {
            return;
        }

        // Handle codepoint changes (e.g., obfuscate effect)
        if (settings.codepoint != originalCodepoint) {
            bakedGlyph = fontSet.getGlyph(settings.codepoint);
            if (bakedGlyph instanceof EmptyGlyph) {
                return;
            }
        }

        // Get vertex consumer for this glyph's render type
        VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));

        // Handle rotation if present
        Matrix4f renderPose = this.pose;
        if (settings.rot != 0) {
            // Calculate rotation origin (center of glyph)
            float glyphWidth = glyphInfo.getAdvance(style.isBold());
            float originX = glyphWidth / 2f;
            float originY = this$0.lineHeight / 2f;

            // Apply rotation around center point
            renderPose = EffectUtil.rotate(this.pose, settings, settings.rot, originX, originY);
        }

        // Render the glyph using our custom render method
        ETABakedGlyph etaGlyph = (ETABakedGlyph) bakedGlyph;
        etaGlyph.emberstextapi$render(
                settings,
                style.isItalic(),
                0f, // boldOffset = 0 for first pass
                renderPose,
                vertexConsumer,
                packedLightCoords
        );

        // Render bold pass with offset
        if (style.isBold()) {
            etaGlyph.emberstextapi$render(
                    settings,
                    style.isItalic(),
                    glyphInfo.getBoldOffset(),
                    renderPose,
                    vertexConsumer,
                    packedLightCoords
            );
        }
    }

    /**
     * Render an item icon inline with text.
     * <p>
     * This method handles rendering Minecraft items as inline icons
     * when a Style has item data attached via ETAStyle.
     * </p>
     *
     * @param etaStyle The style containing item data
     * @param itemId Item resource location (e.g., "minecraft:diamond")
     */
    @Unique
    private void emberstextapi$renderItem(ETAStyle etaStyle, String itemId) {
        try {
            // Parse item ID
            net.minecraft.resources.ResourceLocation itemLocation = net.minecraft.resources.ResourceLocation.tryParse(itemId);
            if (itemLocation == null) {
                return;
            }

            // Get the item from registry
            net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemLocation);
            if (item == null) {
                return;
            }

            // Create item stack
            int count = etaStyle.emberstextapi$getItemCount() != null ? etaStyle.emberstextapi$getItemCount() : 1;
            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, count);

            // Get offsets - default -4, -4 for best visual alignment
            float offsetX = etaStyle.emberstextapi$getItemOffsetX() != null ? etaStyle.emberstextapi$getItemOffsetX() : -4.0f;
            float offsetY = etaStyle.emberstextapi$getItemOffsetY() != null ? etaStyle.emberstextapi$getItemOffsetY() : -4.0f;

            // Item size (standard Minecraft item icon is 16x16)
            int itemSize = 16;

            // Translate to item position
            Matrix4f itemPose = new Matrix4f(this.pose);
            itemPose.translate(this.x + offsetX, this.y + offsetY, 0);

            // Render the item using GuiGraphics-like rendering
            // Since we're in the middle of text rendering, we need to be careful
            // We'll use the MultiBufferSource to ensure proper rendering order
            var mc = net.minecraft.client.Minecraft.getInstance();

            // End current batch to ensure items render on top of text (if possible)
            if (this.bufferSource instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }

            // Create a temporary GuiGraphics for item rendering
            // This is needed because ItemRenderer expects to work with GuiGraphics
            // We need to get or create a BufferSource
            net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource;
            if (this.bufferSource instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource bs) {
                bufferSource = bs;
            } else {
                // Fallback: use render buffers from Minecraft instance
                bufferSource = mc.renderBuffers().bufferSource();
            }

            var guiGraphics = new net.minecraft.client.gui.GuiGraphics(mc, bufferSource);
            guiGraphics.pose().mulPoseMatrix(itemPose);

            // Render the item
            guiGraphics.renderItem(stack, 0, 0);

            // End batch to flush the item rendering
            bufferSource.endBatch();

            // Advance x position for next character
            // Item is offset by offsetX and has width itemSize
            // Advance past the right edge (offsetX + itemSize) plus minimal padding
            this.x += offsetX + itemSize; // Accounts for offset + width + 0px padding

        } catch (Exception e) {
            // If item rendering fails, just skip it and continue
            // This prevents crashes from invalid item IDs
        }
    }
}
