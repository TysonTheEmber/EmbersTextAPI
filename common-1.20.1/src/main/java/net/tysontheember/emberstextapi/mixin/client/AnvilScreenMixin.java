package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Increases the anvil rename character limit to the value configured in the mod config.
 */
@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

    @Redirect(
            method = "subInit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setMaxLength(I)V")
    )
    private void emberstextapi$setAnvilNameMaxLength(EditBox instance, int maxLength) {
        instance.setMaxLength(ConfigHelper.getInstance().getAnvilNameMaxLength());
    }
}
