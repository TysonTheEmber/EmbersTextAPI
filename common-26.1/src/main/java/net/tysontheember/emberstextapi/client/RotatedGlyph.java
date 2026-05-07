package net.tysontheember.emberstextapi.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public record RotatedGlyph(TextRenderable.Styled delegate, float rotation, float centerX, float centerY) implements TextRenderable.Styled {

    @Override
    public void render(Matrix4fc pose, VertexConsumer buffer, int packedLight, boolean seeThrough) {

        Matrix4f rotated = new Matrix4f(pose);
        rotated.translate(centerX, centerY, 0f);
        rotated.rotateZ(rotation);
        rotated.translate(-centerX, -centerY, 0f);
        delegate.render(rotated, buffer, packedLight, seeThrough);
    }

    @Override
    public RenderType renderType(Font.DisplayMode displayMode) {
        return delegate.renderType(displayMode);
    }

    @Override
    public GpuTextureView textureView() {
        return delegate.textureView();
    }

    @Override
    public RenderPipeline guiPipeline() {
        return delegate.guiPipeline();
    }

    @Override
    public float left() {
        return delegate.left();
    }

    @Override
    public float top() {
        return delegate.top();
    }

    @Override
    public float right() {
        return delegate.right();
    }

    @Override
    public float bottom() {
        return delegate.bottom();
    }

    @Override
    public Style style() {
        return delegate.style();
    }

    @Override
    public float activeLeft() {
        return delegate.activeLeft();
    }

    @Override
    public float activeTop() {
        return delegate.activeTop();
    }

    @Override
    public float activeRight() {
        return delegate.activeRight();
    }

    @Override
    public float activeBottom() {
        return delegate.activeBottom();
    }
}
