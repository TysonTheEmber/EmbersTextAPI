package net.tysontheember.emberstextapi.network;

import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

/**
 * Platform-agnostic network handler interface.
 * <p>
 * Different platforms (Forge, Fabric, NeoForge) implement this interface
 * to provide platform-specific networking capabilities.
 * </p>
 */
public interface NetworkHandler {

    /**
     * Initialize the network handler and register all packets.
     * Called during mod initialization.
     */
    void register();

    /**
     * Send an immersive message to a specific player.
     *
     * @param player The player to send the message to
     * @param message The message to send
     */
    void sendOpenMessage(ServerPlayer player, ImmersiveMessage message);

    /**
     * Update an existing message for a specific player.
     *
     * @param player The player to update the message for
     * @param id The message ID to update
     * @param message The updated message data
     */
    void sendUpdateMessage(ServerPlayer player, String id, ImmersiveMessage message);

    /**
     * Close a specific message for a player.
     *
     * @param player The player to close the message for
     * @param id The message ID to close
     */
    void sendCloseMessage(ServerPlayer player, String id);

    /**
     * Close all messages for a player.
     *
     * @param player The player to close all messages for
     */
    void sendCloseAllMessages(ServerPlayer player);

    /**
     * Clear the message queue for a player.
     *
     * @param player The player to clear the queue for
     */
    void sendClearQueue(ServerPlayer player);
}
