package net.tysontheember.emberstextapi.mixin;

import java.util.Objects;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.core.style.EmbersStyle;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Injects additional metadata storage into vanilla {@link Style} instances.
 */
@Mixin(Style.class)
public abstract class StyleMixin implements EmbersStyle {
    @Unique
    private SpanEffectState emberstextapi$spanEffectState;

    @Override
    public SpanEffectState emberstextapi$getSpanEffectState() {
        return emberstextapi$spanEffectState;
    }

    @Override
    public void emberstextapi$setSpanEffectState(SpanEffectState state) {
        this.emberstextapi$spanEffectState = state == null || state.isEmpty() ? null : state.copy();
    }

    @ModifyReturnValue(method = "withColor(Lnet/minecraft/network/chat/TextColor;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithColor(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withBold(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithBold(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withItalic(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithItalic(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withUnderlined(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithUnderlined(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withStrikethrough(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithStrikethrough(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withObfuscated(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithObfuscated(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withClickEvent(Lnet/minecraft/network/chat/ClickEvent;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithClickEvent(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withHoverEvent(Lnet/minecraft/network/chat/HoverEvent;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithHoverEvent(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withInsertion(Ljava/lang/String;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithInsertion(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "withFont(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateWithFont(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "applyFormat(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateApplyFormat(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "applyFormats([Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private Style emberstextapi$propagateApplyFormats(Style returned) {
        return emberstextapi$copyMetadataTo(returned);
    }

    @ModifyReturnValue(method = "applyTo", at = @At("RETURN"))
    private Style emberstextapi$mergeMetadata(Style returned, Style other) {
        if (returned == null) {
            return null;
        }
        SpanEffectState base = other instanceof EmbersStyle embers ? embers.emberstextapi$getSpanEffectState() : null;
        SpanEffectState overlay = emberstextapi$getSpanEffectState();
        SpanEffectState merged = SpanEffectState.merge(base, overlay);
        ((EmbersStyle) (Object) returned).emberstextapi$setSpanEffectState(merged);
        return returned;
    }

    @ModifyReturnValue(method = "equals", at = @At("RETURN"))
    private boolean emberstextapi$compareMetadata(boolean original, Object other) {
        if (!original) {
            return false;
        }
        if (!(other instanceof EmbersStyle embers)) {
            return false;
        }
        return Objects.equals(emberstextapi$getSpanEffectState(), embers.emberstextapi$getSpanEffectState());
    }

    @ModifyReturnValue(method = "hashCode", at = @At("RETURN"))
    private int emberstextapi$hashMetadata(int original) {
        SpanEffectState state = emberstextapi$getSpanEffectState();
        return state == null ? original : 31 * original + state.hashCode();
    }

    @Unique
    private Style emberstextapi$copyMetadataTo(Style target) {
        if (target == null || target == (Object) this) {
            return target;
        }
        SpanEffectState state = emberstextapi$getSpanEffectState();
        if (state != null) {
            ((EmbersStyle) (Object) target).emberstextapi$setSpanEffectState(state.copy());
        }
        return target;
    }
}
