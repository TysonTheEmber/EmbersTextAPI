package net.tysontheember.emberstextapi.platform;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.List;

public interface NetworkHelper {

    static NetworkHelper getInstance() {
        return NetworkHelperImpl.INSTANCE;
    }

    void register();

    void sendMessage(ServerPlayer player, ImmersiveMessage message);

    void sendOpenMessage(ServerPlayer player, ImmersiveMessage message);

    void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message);

    void sendCloseMessage(ServerPlayer player, String messageId);

    void sendCloseAllMessages(ServerPlayer player);

    void sendQueue(ServerPlayer player, String channel, List<List<ImmersiveMessage>> steps);

    void sendClearQueue(ServerPlayer player, String channel);

    void sendClearAllQueues(ServerPlayer player);

    void sendStopQueue(ServerPlayer player, String channel);

    void sendStopAllQueues(ServerPlayer player);
}
