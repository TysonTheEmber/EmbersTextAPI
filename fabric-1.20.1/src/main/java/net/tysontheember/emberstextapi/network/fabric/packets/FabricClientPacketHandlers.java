package net.tysontheember.emberstextapi.network.fabric.packets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.client.QueuedMessage;
import net.tysontheember.emberstextapi.client.QueueStep;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.fabric.FabricNetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FabricClientPacketHandlers {

    private static final int MAX_QUEUE_STEPS = 1024;
    private static final int MAX_MESSAGES_PER_STEP = 256;

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.OPEN_MESSAGE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUUID();
            CompoundTag data = buf.readNbt();

            client.execute(() -> {
                if (data != null) {
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(data);
                    ClientMessageManager.open(id, message);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.UPDATE_MESSAGE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUUID();
            CompoundTag data = buf.readNbt();

            client.execute(() -> {
                if (data != null) {
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(data);
                    ClientMessageManager.update(id, message);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CLOSE_MESSAGE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUUID();
            client.execute(() -> ClientMessageManager.close(id));
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CLOSE_ALL_MESSAGES_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(ClientMessageManager::closeAll);
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CLEAR_QUEUE_PACKET, (client, handler, buf, responseSender) -> {
            String channel = buf.readUtf();
            client.execute(() -> {
                if (channel.isEmpty()) {
                    ClientMessageManager.clearAllQueuesPending();
                } else {
                    ClientMessageManager.clearQueue(channel);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.STOP_QUEUE_PACKET, (client, handler, buf, responseSender) -> {
            String channel = buf.readUtf();
            client.execute(() -> {
                if (channel.isEmpty()) {
                    ClientMessageManager.clearAllQueues();
                } else {
                    ClientMessageManager.stopQueue(channel);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.OPEN_QUEUE_PACKET, (client, handler, buf, responseSender) -> {
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
                List<CompoundTag> msgs = new ArrayList<>(msgCount);
                for (int m = 0; m < msgCount; m++) {
                    stepIds.add(buf.readUUID());
                    CompoundTag tag = buf.readNbt();
                    msgs.add(tag == null ? new CompoundTag() : tag);
                }
                ids.add(stepIds);
                stepData.add(msgs);
            }

            client.execute(() -> {
                List<QueueStep> steps = new ArrayList<>();
                for (int s = 0; s < stepData.size(); s++) {
                    List<QueuedMessage> qmsgs = new ArrayList<>();
                    for (int m = 0; m < stepData.get(s).size(); m++) {
                        ImmersiveMessage message = ImmersiveMessage.fromNbt(stepData.get(s).get(m));
                        qmsgs.add(new QueuedMessage(ids.get(s).get(m), message));
                    }
                    steps.add(new QueueStep(qmsgs));
                }
                ClientMessageManager.enqueueSteps(channel, steps);
            });
        });
    }
}
