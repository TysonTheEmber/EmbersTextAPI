package net.tysontheember.emberstextapi.platform;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

/**
 * Platform abstraction for networking.
 * Handles sending packets to clients in a loader-agnostic way.
 */
public interface NetworkHelper {
    /**
     * Get the singleton instance.
     */
    static NetworkHelper getInstance() {
        return NetworkHelperImpl.INSTANCE;
    }

    /**
     * Register all network packets.
     * Called during mod initialization.
     */
    void register();

    /**
     * Send an immersive message to a player.
     */
    void sendMessage(ServerPlayer player, ImmersiveMessage message);

    /**
     * Send a message open packet to a player.
     */
    void sendOpenMessage(ServerPlayer player, ImmersiveMessage message);

    /**
     * Send a message update packet to a player.
     */
    void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message);

    /**
     * Send a message close packet to a player.
     */
    void sendCloseMessage(ServerPlayer player, String messageId);

    /**
     * Send a close all messages packet to a player.
     */
    void sendCloseAllMessages(ServerPlayer player);

    /**
     * Send a clear queue packet to a player.
     */
    void sendClearQueue(ServerPlayer player);
}
