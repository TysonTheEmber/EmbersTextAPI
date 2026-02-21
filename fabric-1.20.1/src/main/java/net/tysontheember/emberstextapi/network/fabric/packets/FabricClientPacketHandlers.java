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

/**
 * Registers client-side packet handlers for Fabric.
 */
public class FabricClientPacketHandlers {
    public static void register() {
        // Open message packet
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

        // Update message packet
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

        // Close message packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CLOSE_MESSAGE_PACKET, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUUID();
            client.execute(() -> ClientMessageManager.close(id));
        });

        // Close all messages packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CLOSE_ALL_MESSAGES_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(ClientMessageManager::closeAll);
        });

        // Clear queue packet — empty channel means clear all pending
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

        // Stop queue packet — closes current message + clears pending steps; empty channel means stop all
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

        // Open queue packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.OPEN_QUEUE_PACKET, (client, handler, buf, responseSender) -> {
            String channel = buf.readUtf();
            int stepCount = buf.readVarInt();
            List<List<UUID>> ids = new ArrayList<>(stepCount);
            List<List<CompoundTag>> stepData = new ArrayList<>(stepCount);
            for (int s = 0; s < stepCount; s++) {
                int msgCount = buf.readVarInt();
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
