package net.tysontheember.emberstextapi.network.fabric;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.fabric.EmbersTextAPIFabric;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FabricNetworkHandler implements NetworkHandler {

    private static final int MAX_QUEUE_STEPS = 1024;
    private static final int MAX_MESSAGES_PER_STEP = 256;

    private static final FabricNetworkHandler INSTANCE = new FabricNetworkHandler();

    private FabricNetworkHandler() {
    }

    public static FabricNetworkHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void register() {

        PayloadTypeRegistry.clientboundPlay().register(OpenMessagePayload.TYPE, OpenMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UpdateMessagePayload.TYPE, UpdateMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CloseMessagePayload.TYPE, CloseMessagePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CloseAllMessagesPayload.TYPE, CloseAllMessagesPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenQueuePayload.TYPE, OpenQueuePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClearQueuePayload.TYPE, ClearQueuePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StopQueuePayload.TYPE, StopQueuePayload.STREAM_CODEC);

        EmbersTextAPIFabric.LOGGER.info("Fabric network handler registered");
    }

    @Override
    public void sendMessage(ServerPlayer player, ImmersiveMessage message) {
        sendOpenMessage(player, message);
    }

    @Override
    public void sendOpenMessage(ServerPlayer player, ImmersiveMessage message) {
        UUID id = UUID.randomUUID();
        CompoundTag data = message.toNbt(player.registryAccess());
        ServerPlayNetworking.send(player, new OpenMessagePayload(id, data));
    }

    @Override
    public void sendUpdateMessage(ServerPlayer player, String messageId, ImmersiveMessage message) {
        CompoundTag data = message.toNbt(player.registryAccess());
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
    public void sendQueue(ServerPlayer player, String channel, List<List<ImmersiveMessage>> steps) {
        List<List<UUID>> ids = new ArrayList<>();
        List<List<CompoundTag>> stepData = new ArrayList<>();
        for (List<ImmersiveMessage> stepMsgs : steps) {
            List<UUID> stepIds = new ArrayList<>();
            List<CompoundTag> stepNbts = new ArrayList<>();
            for (ImmersiveMessage msg : stepMsgs) {
                stepIds.add(UUID.randomUUID());
                stepNbts.add(msg.toNbt(player.registryAccess()));
            }
            ids.add(stepIds);
            stepData.add(stepNbts);
        }
        ServerPlayNetworking.send(player, new OpenQueuePayload(channel, ids, stepData));
    }

    @Override
    public void sendClearQueue(ServerPlayer player, String channel) {
        ServerPlayNetworking.send(player, new ClearQueuePayload(channel));
    }

    @Override
    public void sendClearAllQueues(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ClearQueuePayload(""));
    }

    @Override
    public void sendStopQueue(ServerPlayer player, String channel) {
        ServerPlayNetworking.send(player, new StopQueuePayload(channel));
    }

    @Override
    public void sendStopAllQueues(ServerPlayer player) {
        ServerPlayNetworking.send(player, new StopQueuePayload(""));
    }

    public record OpenMessagePayload(UUID id, CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenMessagePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("emberstextapi", "open_message"));

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
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("emberstextapi", "update_message"));

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
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("emberstextapi", "close_message"));

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
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("emberstextapi", "close_all_messages"));

        public static final StreamCodec<FriendlyByteBuf, CloseAllMessagesPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {},
            buf -> new CloseAllMessagesPayload()
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenQueuePayload(String channel, List<List<UUID>> ids, List<List<CompoundTag>> stepData)
            implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<OpenQueuePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("emberstextapi", "open_queue"));

        public static final StreamCodec<FriendlyByteBuf, OpenQueuePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.channel);
                buf.writeVarInt(payload.ids.size());
                for (int s = 0; s < payload.ids.size(); s++) {
                    List<UUID> stepIds = payload.ids.get(s);
                    List<CompoundTag> stepNbts = payload.stepData.get(s);
                    buf.writeVarInt(stepIds.size());
                    for (int m = 0; m < stepIds.size(); m++) {
                        buf.writeUUID(stepIds.get(m));
                        buf.writeNbt(stepNbts.get(m));
                    }
                }
            },
            buf -> {
                String channel = buf.readUtf();
                int stepCount = buf.readVarInt();
                if (stepCount < 0 || stepCount > MAX_QUEUE_STEPS) {
                    throw new io.netty.handler.codec.DecoderException("Invalid queue step count: " + stepCount);
                }
                List<List<UUID>> ids = new ArrayList<>(stepCount);
                List<List<CompoundTag>> stepData = new ArrayList<>(stepCount);
                for (int s = 0; s < stepCount; s++) {
                    int msgCount = buf.readVarInt();
                    if (msgCount < 0 || msgCount > MAX_MESSAGES_PER_STEP) {
                        throw new io.netty.handler.codec.DecoderException("Invalid queue message count: " + msgCount);
                    }
                    List<UUID> stepIds = new ArrayList<>(msgCount);
                    List<CompoundTag> stepNbts = new ArrayList<>(msgCount);
                    for (int m = 0; m < msgCount; m++) {
                        stepIds.add(buf.readUUID());
                        stepNbts.add(buf.readNbt());
                    }
                    ids.add(stepIds);
                    stepData.add(stepNbts);
                }
                return new OpenQueuePayload(channel, ids, stepData);
            }
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClearQueuePayload(String channel) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClearQueuePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("emberstextapi", "clear_queue"));

        public static final StreamCodec<FriendlyByteBuf, ClearQueuePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.channel),
            buf -> new ClearQueuePayload(buf.readUtf())
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StopQueuePayload(String channel) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<StopQueuePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("emberstextapi", "stop_queue"));

        public static final StreamCodec<FriendlyByteBuf, StopQueuePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.channel),
            buf -> new StopQueuePayload(buf.readUtf())
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
