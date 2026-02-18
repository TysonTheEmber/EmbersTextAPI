package net.tysontheember.emberstextapi.network.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.fabric.EmbersTextAPIFabric;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.NetworkHandler;
import net.tysontheember.emberstextapi.network.fabric.packets.FabricPacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fabric-specific network handler implementation.
 * Uses Fabric Networking API for packet transmission.
 */
public final class FabricNetworkHandler implements NetworkHandler {
    public static final ResourceLocation TOOLTIP_PACKET = new ResourceLocation(EmbersTextAPIFabric.MODID, "tooltip");
    public static final ResourceLocation OPEN_MESSAGE_PACKET = new ResourceLocation(EmbersTextAPIFabric.MODID, "open_message");
    public static final ResourceLocation UPDATE_MESSAGE_PACKET = new ResourceLocation(EmbersTextAPIFabric.MODID, "update_message");
    public static final ResourceLocation CLOSE_MESSAGE_PACKET = new ResourceLocation(EmbersTextAPIFabric.MODID, "close_message");
    public static final ResourceLocation CLOSE_ALL_MESSAGES_PACKET = new ResourceLocation(EmbersTextAPIFabric.MODID, "close_all_messages");
    public static final ResourceLocation CLEAR_QUEUE_PACKET = new ResourceLocation(EmbersTextAPIFabric.MODID, "clear_queue");
    public static final ResourceLocation OPEN_QUEUE_PACKET = new ResourceLocation(EmbersTextAPIFabric.MODID, "open_queue");

    private static final FabricNetworkHandler INSTANCE = new FabricNetworkHandler();

    private FabricNetworkHandler() {
    }

    public static FabricNetworkHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void register() {
        // Fabric client-side packet handlers are registered in the client initializer
        EmbersTextAPIFabric.LOGGER.info("Fabric network handler registered");
    }

    @Override
    public void sendMessage(ServerPlayer player, ImmersiveMessage message) {
        sendOpenMessage(player, message);
    }

    @Override
    public void sendOpenMessage(ServerPlayer player, ImmersiveMessage message) {
        UUID id = UUID.randomUUID();
        CompoundTag data = message.toNbt();
        ServerPlayNetworking.send(player, OPEN_MESSAGE_PACKET,
            FabricPacketCodecs.encodeOpenMessage(id, data));
    }

    @Override
    public void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message) {
        UUID uuid = UUID.fromString(messageId);
        CompoundTag data = message.toNbt();
        ServerPlayNetworking.send(player, UPDATE_MESSAGE_PACKET,
            FabricPacketCodecs.encodeUpdateMessage(uuid, data));
    }

    @Override
    public void sendCloseMessage(ServerPlayer player, String messageId) {
        UUID uuid = UUID.fromString(messageId);
        ServerPlayNetworking.send(player, CLOSE_MESSAGE_PACKET,
            FabricPacketCodecs.encodeCloseMessage(uuid));
    }

    @Override
    public void sendCloseAllMessages(ServerPlayer player) {
        ServerPlayNetworking.send(player, CLOSE_ALL_MESSAGES_PACKET,
            FabricPacketCodecs.encodeCloseAllMessages());
    }

    @Override
    public void sendQueue(ServerPlayer player, String channel, List<List<ImmersiveMessage>> steps) {
        List<List<UUID>> ids = new ArrayList<>();
        List<List<CompoundTag>> stepData = new ArrayList<>();
        for (List<ImmersiveMessage> step : steps) {
            List<UUID> stepIds = new ArrayList<>();
            List<CompoundTag> msgs = new ArrayList<>();
            for (ImmersiveMessage msg : step) {
                stepIds.add(UUID.randomUUID());
                msgs.add(msg.toNbt());
            }
            ids.add(stepIds);
            stepData.add(msgs);
        }
        ServerPlayNetworking.send(player, OPEN_QUEUE_PACKET,
            FabricPacketCodecs.encodeOpenQueue(channel, ids, stepData));
    }

    @Override
    public void sendClearQueue(ServerPlayer player, String channel) {
        ServerPlayNetworking.send(player, CLEAR_QUEUE_PACKET,
            FabricPacketCodecs.encodeClearQueue(channel));
    }

    @Override
    public void sendClearAllQueues(ServerPlayer player) {
        ServerPlayNetworking.send(player, CLEAR_QUEUE_PACKET,
            FabricPacketCodecs.encodeClearQueue(""));
    }
}
