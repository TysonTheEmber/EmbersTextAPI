package net.tysontheember.emberstextapi.core.render;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface EmbersBakedGlyph {
    void emberstextapi$render(GlyphRenderSettings settings, boolean italic, float xOffset, Matrix4f pose,
            VertexConsumer consumer, int packedLight);
}
