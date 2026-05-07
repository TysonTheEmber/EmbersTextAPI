package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.tysontheember.emberstextapi.accessor.ETABakedGlyph;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedGlyph.class)
public class BakedGlyphMixin implements ETABakedGlyph {

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
    public void emberstextapi$render(
            EffectSettings settings,
            boolean italic,
            float boldOffset,
            Matrix4f pose,
            VertexConsumer buffer,
            int packedLight) {

        float x = settings.x + boldOffset;
        float leftX = x + this.left;
        float rightX = x + this.right;

        float upOffset = this.up - 3.0f;
        float downOffset = this.down - 3.0f;
        float upY = settings.y + upOffset;
        float downY = settings.y + downOffset;

        float italicOffsetUp = italic ? 1.0f - 0.25f * upOffset : 0.0f;
        float italicOffsetDown = italic ? 1.0f - 0.25f * downOffset : 0.0f;

        float u0 = this.u0;
        float u1 = this.u1;
        float v0 = this.v0;
        float v1 = this.v1;

        if (settings.maskTop != 0) {
            v0 += (this.v1 - this.v0) * settings.maskTop;
            upY += (this.down - this.up) * settings.maskTop;
        }
        if (settings.maskBottom != 0) {
            v1 -= (this.v1 - this.v0) * settings.maskBottom;
            downY -= (this.down - this.up) * settings.maskBottom;
        }

        buffer.vertex(pose, leftX + italicOffsetUp, upY, 0.0f)
                .color(settings.r, settings.g, settings.b, settings.a)
                .uv(u0, v0)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(pose, leftX + italicOffsetDown, downY, 0.0f)
                .color(settings.r, settings.g, settings.b, settings.a)
                .uv(u0, v1)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(pose, rightX + italicOffsetDown, downY, 0.0f)
                .color(settings.r, settings.g, settings.b, settings.a)
                .uv(u1, v1)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(pose, rightX + italicOffsetUp, upY, 0.0f)
                .color(settings.r, settings.g, settings.b, settings.a)
                .uv(u1, v0)
                .uv2(packedLight)
                .endVertex();
    }
}
