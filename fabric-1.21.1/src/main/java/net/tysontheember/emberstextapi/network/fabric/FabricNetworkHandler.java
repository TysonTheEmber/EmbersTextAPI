package net.tysontheember.emberstextapi.network.fabric;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.fabric.EmbersTextAPIFabric;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fabric-specific network handler implementation.
 * Uses Fabric Networking API v1 payload-based system for MC 1.21.1.
 */
public final class FabricNetworkHandler implements NetworkHandler {

    private static final FabricNetworkHandler INSTANCE = new FabricNetworkHandler();

    private FabricNetworkHandler() {
    }

    public static FabricNetworkHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void register() {
        // Register payload types for S2C packets
        PayloadTypeRegistry.playS2C().register(OpenMessagePayload.TYPE, OpenMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateMessagePayload.TYPE, UpdateMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(CloseMessagePayload.TYPE, CloseMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(CloseAllMessagesPayload.TYPE, CloseAllMessagesPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClearQueuePayload.TYPE, ClearQueuePayload.STREAM_CODEC);

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
        ServerPlayNetworking.send(player, new OpenMessagePayload(id, data));
    }

    @Override
    public void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message) {
        CompoundTag data = message.toNbt();
        ServerPlayNetworking.send(player, new UpdateMessagePayload(messageId, data));
    }

    @Override
    public void sendCloseMessage(ServerPlayer player, String messageId) {
        ServerPlayNetworking.send(player, new CloseMessagePayload(messageId));
    }

    @Override
    public void sendCloseAllMessages(ServerPlayer player) {
        ServerPlayNetworking.send(player, new CloseAllMessagesPayload());
    }

    @Override
    public void sendClearQueue(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ClearQueuePayload());
    }

    // ===== Payload Records =====

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
            (buf, payload) -> {},
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
            (buf, payload) -> {},
            buf -> new ClearQueuePayload()
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
