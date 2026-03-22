package net.tysontheember.emberstextapi.mixin;

import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AnvilMenu;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the server-side anvil rename character limit to match the configured value.
 * Vanilla's handleRenameItem rejects names longer than 50 chars before passing them
 * to AnvilMenu. We intercept names in the range (50, configMax] and apply them directly.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class AnvilRenameMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleRenameItem", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$handleRenameItem(ServerboundRenameItemPacket packet, CallbackInfo ci) {
        String name = packet.getName();
        int maxLength = ConfigHelper.getInstance().getAnvilNameMaxLength();
        if (name.length() > 50 && name.length() <= maxLength) {
            if (this.player.containerMenu instanceof AnvilMenu anvilMenu) {
                anvilMenu.setItemName(name);
            }
            ci.cancel();
        }
    }
}
