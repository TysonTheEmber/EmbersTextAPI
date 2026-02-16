package net.tysontheember.emberstextapi.fabric;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.fabric.FabricNetworkHandler;
import net.tysontheember.emberstextapi.platform.NetworkHelper;

/**
 * Fabric implementation of NetworkHelper.
 */
public class FabricNetworkHelper implements NetworkHelper {
    private final FabricNetworkHandler handler = FabricNetworkHandler.getInstance();

    @Override
    public void register() {
        handler.register();
    }

    @Override
    public void sendMessage(ServerPlayer player, ImmersiveMessage message) {
        handler.sendMessage(player, message);
    }

    @Override
    public void sendOpenMessage(ServerPlayer player, ImmersiveMessage message) {
        handler.sendOpenMessage(player, message);
    }

    @Override
    public void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message) {
        handler.sendUpdateMessage(player, messageId, message);
    }

    @Override
    public void sendCloseMessage(ServerPlayer player, String messageId) {
        handler.sendCloseMessage(player, messageId);
    }

    @Override
    public void sendCloseAllMessages(ServerPlayer player) {
        handler.sendCloseAllMessages(player);
    }

    @Override
    public void sendClearQueue(ServerPlayer player) {
        handler.sendClearQueue(player);
    }
}
