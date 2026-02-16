package net.tysontheember.emberstextapi.network.fabric.packets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.fabric.FabricNetworkHandler;

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

        // Clear queue packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.ClearQueuePayload.TYPE, (payload, context) -> {
            context.client().execute(ClientMessageManager::closeAll);
        });
    }
}
