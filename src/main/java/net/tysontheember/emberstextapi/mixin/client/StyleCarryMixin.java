package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleCarryMixin {
    @Inject(method = "withColor", at = @At("RETURN"))
    private void emberstextapi$carryColor(TextColor color, CallbackInfoReturnable<Style> cir) {
        Style returned = cir.getReturnValue();
        if (returned != null) {
            ETAStyleOps.copyExtras((Style) (Object) this, returned);
        }
    }

    @Inject(method = "withBold", at = @At("RETURN"))
    private void emberstextapi$carryBold(Boolean bold, CallbackInfoReturnable<Style> cir) {
        Style returned = cir.getReturnValue();
        if (returned != null) {
            ETAStyleOps.copyExtras((Style) (Object) this, returned);
        }
    }

    @Inject(method = "withItalic", at = @At("RETURN"))
    private void emberstextapi$carryItalic(Boolean italic, CallbackInfoReturnable<Style> cir) {
        Style returned = cir.getReturnValue();
        if (returned != null) {
            ETAStyleOps.copyExtras((Style) (Object) this, returned);
        }
    }

    @Inject(method = "withUnderlined", at = @At("RETURN"))
    private void emberstextapi$carryUnderline(Boolean underline, CallbackInfoReturnable<Style> cir) {
        Style returned = cir.getReturnValue();
        if (returned != null) {
            ETAStyleOps.copyExtras((Style) (Object) this, returned);
        }
    }

    @Inject(method = "withStrikethrough", at = @At("RETURN"))
    private void emberstextapi$carryStrikethrough(Boolean strikethrough, CallbackInfoReturnable<Style> cir) {
        Style returned = cir.getReturnValue();
        if (returned != null) {
            ETAStyleOps.copyExtras((Style) (Object) this, returned);
        }
    }

    @Inject(method = "withObfuscated", at = @At("RETURN"))
    private void emberstextapi$carryObfuscated(Boolean obfuscated, CallbackInfoReturnable<Style> cir) {
        Style returned = cir.getReturnValue();
        if (returned != null) {
            ETAStyleOps.copyExtras((Style) (Object) this, returned);
        }
    }

    @Inject(method = "applyTo", at = @At("RETURN"))
    private void emberstextapi$carryApplyTo(Style other, CallbackInfoReturnable<Style> cir) {
        Style returned = cir.getReturnValue();
        if (returned != null) {
            if (other != null) {
                ETAStyleOps.copyExtras(other, returned);
            }
            ETAStyleOps.copyExtras((Style) (Object) this, returned);
        }
    }
}
