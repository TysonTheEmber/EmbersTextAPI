package net.tysontheember.emberstextapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

@Mixin(StringDecomposer.class)
public interface StringDecomposerAccess {
    @Invoker
    static boolean callFeedChar(Style style, FormattedCharSink sink, int index, char character) {
        throw new IllegalStateException("Mixin bridge not applied");
    }
}
