package net.tysontheember.emberstextapi.network.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.platform.NetworkHelper;

import java.util.List;

public class NeoForgeNetworkHelper implements NetworkHelper {

    @Override
    public void register() {
        NeoForgeNetworkHandler.register();
    }

    @Override
    public void sendMessage(ServerPlayer player, ImmersiveMessage message) {
        NeoForgeNetworkHandler.sendMessage(player, message);
    }

    @Override
    public void sendOpenMessage(ServerPlayer player, ImmersiveMessage message) {
        NeoForgeNetworkHandler.sendOpenMessage(player, message);
    }

    @Override
    public void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message) {
        NeoForgeNetworkHandler.sendUpdateMessage(player, messageId, message);
    }

    @Override
    public void sendCloseMessage(ServerPlayer player, String messageId) {
        NeoForgeNetworkHandler.sendCloseMessage(player, messageId);
    }

    @Override
    public void sendCloseAllMessages(ServerPlayer player) {
        NeoForgeNetworkHandler.sendCloseAllMessages(player);
    }

    @Override
    public void sendQueue(ServerPlayer player, String channel, List<List<ImmersiveMessage>> steps) {
        NeoForgeNetworkHandler.sendQueue(player, channel, steps);
    }

    @Override
    public void sendClearQueue(ServerPlayer player, String channel) {
        NeoForgeNetworkHandler.sendClearQueue(player, channel);
    }

    @Override
    public void sendClearAllQueues(ServerPlayer player) {
        NeoForgeNetworkHandler.sendClearAllQueues(player);
    }
}
