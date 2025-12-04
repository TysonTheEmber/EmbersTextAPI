package net.tysontheember.emberstextapi.duck;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import org.joml.Matrix4f;

/**
 * Duck interface for augmenting Minecraft's BakedGlyph class with custom rendering.
 * <p>
 * Implemented via Mixin on {@link net.minecraft.client.gui.font.glyphs.BakedGlyph}
 * to accept {@link EffectSettings} instead of raw render parameters.
 * </p>
 */
public interface ETABakedGlyph {

    void emberstextapi$render(
        EffectSettings settings,
        boolean italic,
        float boldOffset,
        Matrix4f pose,
        VertexConsumer buffer,
        int packedLight
    );
}
