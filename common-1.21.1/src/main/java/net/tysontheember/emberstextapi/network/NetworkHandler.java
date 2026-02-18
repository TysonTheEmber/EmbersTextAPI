package net.tysontheember.emberstextapi.network;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.List;

/**
 * Base interface for network handlers.
 * Implemented by loader-specific network implementations.
 */
public interface NetworkHandler {
    /**
     * Register all network packets.
     */
    void register();

    /**
     * Send a legacy tooltip packet (backward compatibility).
     */
    void sendMessage(ServerPlayer player, ImmersiveMessage message);

    /**
     * Send a message open packet.
     * Generates a random UUID internally.
     */
    void sendOpenMessage(ServerPlayer player, ImmersiveMessage message);

    /**
     * Send a message update packet.
     */
    void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message);

    /**
     * Send a message close packet.
     */
    void sendCloseMessage(ServerPlayer player, String messageId);

    /**
     * Send a close all messages packet.
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
