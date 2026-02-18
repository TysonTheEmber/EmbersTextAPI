package net.tysontheember.emberstextapi.platform;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.List;

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
     * Send a full queue to a player. Appends if channel already active on client.
     * Each element of {@code steps} is a list of messages shown simultaneously in that step.
     */
    void sendQueue(ServerPlayer player, String channel, List<List<ImmersiveMessage>> steps);

    /**
     * Clear a specific channel queue on the client. Pass empty string to clear all.
     */
    void sendClearQueue(ServerPlayer player, String channel);

    /**
     * Clear all channel queues on the client immediately.
     */
    void sendClearAllQueues(ServerPlayer player);
}
