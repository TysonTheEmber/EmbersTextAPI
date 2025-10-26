package net.tysontheember.emberstextapi.mixin.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.tysontheember.emberstextapi.core.render.GlyphRenderSettings;

@Mixin(BakedGlyph.class)
public abstract class BakedGlyphMixin implements EmbersBakedGlyph {
    @Shadow
    @Final
    private float left;
    @Shadow
    @Final
    private float right;
    @Shadow
    @Final
    private float up;
    @Shadow
    @Final
    private float down;
    @Shadow
    @Final
    private float u0;
    @Shadow
    @Final
    private float v0;
    @Shadow
    @Final
    private float u1;
    @Shadow
    @Final
    private float v1;

    @Override
    public void emberstextapi$render(GlyphRenderSettings settings, boolean italic, float xOffset, Matrix4f pose,
            VertexConsumer consumer, int packedLight) {
        float x = settings.renderX() + xOffset;
        float y = settings.renderY();
        float left = x + this.left;
        float right = x + this.right;
        float top = this.up - 3.0f;
        float bottom = this.down - 3.0f;
        float renderTop = y + top;
        float renderBottom = y + bottom;
        float italicTop = italic ? 1.0f - 0.25f * top : 0.0f;
        float italicBottom = italic ? 1.0f - 0.25f * bottom : 0.0f;
        float u0 = this.u0;
        float u1 = this.u1;
        float v0 = this.v0;
        float v1 = this.v1;

        consumer.vertex(pose, left + italicTop, renderTop, 0.0f)
                .color(settings.red(), settings.green(), settings.blue(), settings.alpha())
                .uv(u0, v0)
                .uv2(packedLight)
                .endVertex();
        consumer.vertex(pose, left + italicBottom, renderBottom, 0.0f)
                .color(settings.red(), settings.green(), settings.blue(), settings.alpha())
                .uv(u0, v1)
                .uv2(packedLight)
                .endVertex();
        consumer.vertex(pose, right + italicBottom, renderBottom, 0.0f)
                .color(settings.red(), settings.green(), settings.blue(), settings.alpha())
                .uv(u1, v1)
                .uv2(packedLight)
                .endVertex();
        consumer.vertex(pose, right + italicTop, renderTop, 0.0f)
                .color(settings.red(), settings.green(), settings.blue(), settings.alpha())
                .uv(u1, v0)
                .uv2(packedLight)
                .endVertex();
    }
}
