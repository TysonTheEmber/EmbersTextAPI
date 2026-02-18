package net.tysontheember.emberstextapi.forge;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.forge.ForgeNetworkHandler;
import net.tysontheember.emberstextapi.platform.NetworkHelper;

import java.util.List;

/**
 * Forge implementation of NetworkHelper.
 */
public class ForgeNetworkHelper implements NetworkHelper {
    private final ForgeNetworkHandler handler = ForgeNetworkHandler.getInstance();

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
    public void sendQueue(ServerPlayer player, String channel, List<List<ImmersiveMessage>> steps) {
        handler.sendQueue(player, channel, steps);
    }

    @Override
    public void sendClearQueue(ServerPlayer player, String channel) {
        handler.sendClearQueue(player, channel);
    }

    @Override
    public void sendClearAllQueues(ServerPlayer player) {
        handler.sendClearAllQueues(player);
    }
}
