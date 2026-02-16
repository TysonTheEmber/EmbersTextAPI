package net.tysontheember.emberstextapi.network.neoforge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class NeoForgeNetworkHandler {

    private static final String PROTOCOL_VERSION = "3";
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath("emberstextapi", "tooltip");

    public static void register() {
        // Network registration is handled via @PayloadRegistrar in main mod class
    }

    public static void sendMessage(ServerPlayer player, ImmersiveMessage message) {
        PacketDistributor.sendToPlayer(player, new TooltipPayload(message.toNbt(player.registryAccess())));
    }

    public static void sendOpenMessage(ServerPlayer player, ImmersiveMessage message) {
        UUID id = UUID.randomUUID();
        PacketDistributor.sendToPlayer(player, new OpenMessagePayload(id, message.toNbt(player.registryAccess())));
    }

    public static void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message) {
        PacketDistributor.sendToPlayer(player, new UpdateMessagePayload(messageId, message.toNbt(player.registryAccess())));
    }

    public static void sendCloseMessage(ServerPlayer player, String messageId) {
        PacketDistributor.sendToPlayer(player, new CloseMessagePayload(messageId));
    }

    public static void sendCloseAllMessages(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new CloseAllMessagesPayload());
    }

    public static void sendClearQueue(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClearQueuePayload());
    }

    // Payload records
    public record TooltipPayload(CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TooltipPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "tooltip"));

        public static final StreamCodec<FriendlyByteBuf, TooltipPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeNbt(payload.data),
            buf -> new TooltipPayload(buf.readNbt())
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenMessagePayload(UUID id, CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenMessagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "open_message"));

        public static final StreamCodec<FriendlyByteBuf, OpenMessagePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.id);
                buf.writeNbt(payload.data);
            },
            buf -> new OpenMessagePayload(buf.readUUID(), buf.readNbt())
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record UpdateMessagePayload(String messageId, CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<UpdateMessagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "update_message"));

        public static final StreamCodec<FriendlyByteBuf, UpdateMessagePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.messageId);
                buf.writeNbt(payload.data);
            },
            buf -> new UpdateMessagePayload(buf.readUtf(), buf.readNbt())
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CloseMessagePayload(String messageId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<CloseMessagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "close_message"));

        public static final StreamCodec<FriendlyByteBuf, CloseMessagePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.messageId),
            buf -> new CloseMessagePayload(buf.readUtf())
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record CloseAllMessagesPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<CloseAllMessagesPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "close_all_messages"));

        public static final StreamCodec<FriendlyByteBuf, CloseAllMessagesPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {}, // No data to write
            buf -> new CloseAllMessagesPayload()
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClearQueuePayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClearQueuePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "clear_queue"));

        public static final StreamCodec<FriendlyByteBuf, ClearQueuePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {}, // No data to write
            buf -> new ClearQueuePayload()
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Client handlers
    public static void handleTooltip(TooltipPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.core.HolderLookup.Provider provider = context.player().registryAccess();
            ImmersiveMessage message = ImmersiveMessage.fromNbt(payload.data, provider);
            // Generate random UUID for legacy display message
            net.tysontheember.emberstextapi.client.ClientMessageManager.open(UUID.randomUUID(), message);
        });
    }

    public static void handleOpenMessage(OpenMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.core.HolderLookup.Provider provider = context.player().registryAccess();
            ImmersiveMessage message = ImmersiveMessage.fromNbt(payload.data, provider);
            net.tysontheember.emberstextapi.client.ClientMessageManager.open(payload.id, message);
        });
    }

    public static void handleUpdateMessage(UpdateMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.core.HolderLookup.Provider provider = context.player().registryAccess();
            UUID id = UUID.fromString(payload.messageId);
            ImmersiveMessage message = ImmersiveMessage.fromNbt(payload.data, provider);
            net.tysontheember.emberstextapi.client.ClientMessageManager.update(id, message);
        });
    }

    public static void handleCloseMessage(CloseMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            UUID id = UUID.fromString(payload.messageId);
            net.tysontheember.emberstextapi.client.ClientMessageManager.close(id);
        });
    }

    public static void handleCloseAllMessages(CloseAllMessagesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.tysontheember.emberstextapi.client.ClientMessageManager.closeAll();
        });
    }

    public static void handleClearQueue(ClearQueuePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // clearQueue() method not yet implemented in ClientMessageManager
            // TODO: Add clearQueue() method or use closeAll()
            net.tysontheember.emberstextapi.client.ClientMessageManager.closeAll();
        });
    }
}
