package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(FontSet.class)
public abstract class FontSetMixin {

    @ModifyVariable(method = "reload(Ljava/util/List;Ljava/util/Set;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private List<GlyphProvider.Conditional> emberstextapi$addSdfProviders(List<GlyphProvider.Conditional> providers) {

        return providers;
    }
}
