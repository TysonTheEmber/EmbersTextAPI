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

public class FabricClientPacketHandlers {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.OpenMessagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.data() != null) {
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(payload.data(), context.player().registryAccess());
                    ClientMessageManager.open(payload.id(), message);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.UpdateMessagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.data() != null) {
                    UUID id = UUID.fromString(payload.messageId());
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(payload.data(), context.player().registryAccess());
                    ClientMessageManager.update(id, message);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CloseMessagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                UUID id = UUID.fromString(payload.messageId());
                ClientMessageManager.close(id);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CloseAllMessagesPayload.TYPE, (payload, context) -> {
            context.client().execute(ClientMessageManager::closeAll);
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.OpenQueuePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                List<QueueStep> steps = new ArrayList<>();
                for (int s = 0; s < payload.ids().size(); s++) {
                    List<UUID> stepIds = payload.ids().get(s);
                    List<net.minecraft.nbt.CompoundTag> stepNbts = payload.stepData().get(s);
                    List<QueuedMessage> messages = new ArrayList<>();
                    for (int m = 0; m < stepIds.size(); m++) {
                        ImmersiveMessage msg = ImmersiveMessage.fromNbt(stepNbts.get(m), context.player().registryAccess());
                        messages.add(new QueuedMessage(stepIds.get(m), msg));
                    }
                    steps.add(new QueueStep(messages));
                }
                ClientMessageManager.enqueueSteps(payload.channel(), steps);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.ClearQueuePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.channel().isEmpty()) {
                    ClientMessageManager.clearAllQueuesPending();
                } else {
                    ClientMessageManager.clearQueue(payload.channel());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.StopQueuePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.channel().isEmpty()) {
                    ClientMessageManager.clearAllQueues();
                } else {
                    ClientMessageManager.stopQueue(payload.channel());
                }
            });
        });
    }
}
