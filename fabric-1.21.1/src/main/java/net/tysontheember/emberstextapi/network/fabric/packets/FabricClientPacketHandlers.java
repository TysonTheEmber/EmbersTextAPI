package net.tysontheember.emberstextapi.network.fabric.packets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.client.QueueStep;
import net.tysontheember.emberstextapi.client.QueuedMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.fabric.FabricNetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registers client-side packet handlers for Fabric 1.21.1.
 * Uses the payload-based networking API.
 */
public class FabricClientPacketHandlers {
    public static void register() {
        // Open message packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.OpenMessagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.data() != null) {
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(payload.data());
                    ClientMessageManager.open(payload.id(), message);
                }
            });
        });

        // Update message packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.UpdateMessagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.data() != null) {
                    UUID id = UUID.fromString(payload.messageId());
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(payload.data());
                    ClientMessageManager.update(id, message);
                }
            });
        });

        // Close message packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CloseMessagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                UUID id = UUID.fromString(payload.messageId());
                ClientMessageManager.close(id);
            });
        });

        // Close all messages packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CloseAllMessagesPayload.TYPE, (payload, context) -> {
            context.client().execute(ClientMessageManager::closeAll);
        });

        // Open queue packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.OpenQueuePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                List<QueueStep> steps = new ArrayList<>();
                for (int s = 0; s < payload.ids().size(); s++) {
                    List<UUID> stepIds = payload.ids().get(s);
                    List<net.minecraft.nbt.CompoundTag> stepNbts = payload.stepData().get(s);
                    List<QueuedMessage> messages = new ArrayList<>();
                    for (int m = 0; m < stepIds.size(); m++) {
                        ImmersiveMessage msg = ImmersiveMessage.fromNbt(stepNbts.get(m));
                        messages.add(new QueuedMessage(stepIds.get(m), msg));
                    }
                    steps.add(new QueueStep(messages));
                }
                ClientMessageManager.enqueueSteps(payload.channel(), steps);
            });
        });

        // Clear queue packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.ClearQueuePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.channel().isEmpty()) {
                    ClientMessageManager.clearAllQueues();
                } else {
                    ClientMessageManager.clearQueue(payload.channel());
                }
            });
        });
    }
}
