package net.tysontheember.emberstextapi.network.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.NetworkHandler;
import net.tysontheember.emberstextapi.network.forge.packets.*;

/**
 * Forge-specific network handler implementation.
 * <p>
 * Uses Forge's SimpleChannel for packet registration and transmission.
 * </p>
 */
public final class ForgeNetworkHandler implements NetworkHandler {
    private static final String PROTOCOL = "3";
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(EmbersTextAPI.MODID, "tooltip");

    /**
     * The Forge SimpleChannel used for network communication.
     * Made public for backward compatibility with existing code.
     */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ID,
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private static final ForgeNetworkHandler INSTANCE = new ForgeNetworkHandler();

    private ForgeNetworkHandler() {
    }

    /**
     * Get the singleton instance.
     */
    public static ForgeNetworkHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, TooltipPacket.class, TooltipPacket::encode, TooltipPacket::decode, TooltipPacket::handle);
        CHANNEL.registerMessage(id++, S2C_OpenMessagePacket.class, S2C_OpenMessagePacket::encode, S2C_OpenMessagePacket::decode, S2C_OpenMessagePacket::handle);
        CHANNEL.registerMessage(id++, S2C_UpdateMessagePacket.class, S2C_UpdateMessagePacket::encode, S2C_UpdateMessagePacket::decode, S2C_UpdateMessagePacket::handle);
        CHANNEL.registerMessage(id++, S2C_CloseMessagePacket.class, S2C_CloseMessagePacket::encode, S2C_CloseMessagePacket::decode, S2C_CloseMessagePacket::handle);
        CHANNEL.registerMessage(id++, S2C_CloseAllMessagesPacket.class, S2C_CloseAllMessagesPacket::encode, S2C_CloseAllMessagesPacket::decode, S2C_CloseAllMessagesPacket::handle);
        CHANNEL.registerMessage(id, S2C_ClearQueuePacket.class, S2C_ClearQueuePacket::encode, S2C_ClearQueuePacket::decode, S2C_ClearQueuePacket::handle);
    }

    @Override
    public void sendOpenMessage(ServerPlayer player, ImmersiveMessage message) {
        java.util.UUID id = java.util.UUID.randomUUID();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2C_OpenMessagePacket(id, message.toNbt()));
    }

    @Override
    public void sendUpdateMessage(ServerPlayer player, String id, ImmersiveMessage message) {
        java.util.UUID uuid = java.util.UUID.fromString(id);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2C_UpdateMessagePacket(uuid, message.toNbt()));
    }

    @Override
    public void sendCloseMessage(ServerPlayer player, String id) {
        java.util.UUID uuid = java.util.UUID.fromString(id);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2C_CloseMessagePacket(uuid));
    }

    @Override
    public void sendCloseAllMessages(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2C_CloseAllMessagesPacket());
    }

    @Override
    public void sendClearQueue(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2C_ClearQueuePacket());
    }
}
