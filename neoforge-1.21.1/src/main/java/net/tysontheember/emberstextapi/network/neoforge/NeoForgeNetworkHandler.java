package net.tysontheember.emberstextapi.network.neoforge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tysontheember.emberstextapi.client.QueueStep;
import net.tysontheember.emberstextapi.client.QueuedMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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

    public static void sendQueue(ServerPlayer player, String channel, List<List<ImmersiveMessage>> steps) {
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
        PacketDistributor.sendToPlayer(player, new OpenQueuePayload(channel, ids, stepData));
    }

    public static void sendClearQueue(ServerPlayer player, String channel) {
        PacketDistributor.sendToPlayer(player, new ClearQueuePayload(channel));
    }

    public static void sendClearAllQueues(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClearQueuePayload(""));
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
            (buf, payload) -> {},
            buf -> new CloseAllMessagesPayload()
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Payload for opening a named-channel queue of steps.
     * Wire format: channel, stepCount, then for each step: msgCount, then for each message: UUID + NBT.
     */
    public record OpenQueuePayload(String channel, List<List<UUID>> ids, List<List<CompoundTag>> stepData)
            implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<OpenQueuePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "open_queue"));

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
                List<List<UUID>> ids = new ArrayList<>(stepCount);
                List<List<CompoundTag>> stepData = new ArrayList<>(stepCount);
                for (int s = 0; s < stepCount; s++) {
                    int msgCount = buf.readVarInt();
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

    /**
     * Payload for clearing a channel queue. Empty channel string means clear all.
     */
    public record ClearQueuePayload(String channel) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClearQueuePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emberstextapi", "clear_queue"));

        public static final StreamCodec<FriendlyByteBuf, ClearQueuePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.channel),
            buf -> new ClearQueuePayload(buf.readUtf())
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

    public static void handleOpenQueue(OpenQueuePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.core.HolderLookup.Provider provider = context.player().registryAccess();
            List<QueueStep> steps = new ArrayList<>();
            for (int s = 0; s < payload.ids().size(); s++) {
                List<UUID> stepIds = payload.ids().get(s);
                List<CompoundTag> stepNbts = payload.stepData().get(s);
                List<QueuedMessage> messages = new ArrayList<>();
                for (int m = 0; m < stepIds.size(); m++) {
                    ImmersiveMessage msg = ImmersiveMessage.fromNbt(stepNbts.get(m), provider);
                    messages.add(new QueuedMessage(stepIds.get(m), msg));
                }
                steps.add(new QueueStep(messages));
            }
            net.tysontheember.emberstextapi.client.ClientMessageManager.enqueueSteps(payload.channel(), steps);
        });
    }

    public static void handleClearQueue(ClearQueuePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.channel().isEmpty()) {
                net.tysontheember.emberstextapi.client.ClientMessageManager.clearAllQueues();
            } else {
                net.tysontheember.emberstextapi.client.ClientMessageManager.clearQueue(payload.channel());
            }
        });
    }
}
