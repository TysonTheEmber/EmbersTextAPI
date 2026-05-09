package net.tysontheember.emberstextapi.mixin.client.patchouli;

import net.tysontheember.emberstextapi.compat.patchouli.PatchouliBypass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
        "vazkii.patchouli.client.book.text.BookTextParser",
        "vazkii.patchouli.client.book.BookTextParser"
}, remap = false)
public abstract class BookTextParserMixin {

    @Inject(method = "parse", at = @At("HEAD"), require = 0, remap = false)
    private void emberstextapi$enter(CallbackInfoReturnable<?> cir) {
        PatchouliBypass.enter();
    }

    @Inject(method = "parse", at = @At("RETURN"), require = 0, remap = false)
    private void emberstextapi$exit(CallbackInfoReturnable<?> cir) {
        PatchouliBypass.exit();
    }
}
