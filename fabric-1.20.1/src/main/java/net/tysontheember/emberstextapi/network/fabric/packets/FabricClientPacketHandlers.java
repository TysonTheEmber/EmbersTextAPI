package net.tysontheember.emberstextapi.network.fabric.packets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.ImmersiveMessagesManager;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.network.fabric.FabricNetworkHandler;

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

            client.execute(() -> {
                ClientMessageManager.close(id);
            });
        });

        // Close all messages packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CLOSE_ALL_MESSAGES_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(ClientMessageManager::closeAll);
        });

        // Clear queue packet
        ClientPlayNetworking.registerGlobalReceiver(FabricNetworkHandler.CLEAR_QUEUE_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(ImmersiveMessagesManager::clear);
        });
    }
}
