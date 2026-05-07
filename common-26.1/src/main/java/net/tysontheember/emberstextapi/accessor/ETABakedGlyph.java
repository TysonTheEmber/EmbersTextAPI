package net.tysontheember.emberstextapi.accessor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import org.joml.Matrix4f;

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
