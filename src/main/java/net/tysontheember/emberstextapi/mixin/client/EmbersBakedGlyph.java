package net.tysontheember.emberstextapi.mixin.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.tysontheember.emberstextapi.core.render.GlyphRenderSettings;

public interface EmbersBakedGlyph {
    void emberstextapi$render(GlyphRenderSettings settings, boolean italic, float xOffset, Matrix4f pose,
            VertexConsumer consumer, int packedLight);
}
