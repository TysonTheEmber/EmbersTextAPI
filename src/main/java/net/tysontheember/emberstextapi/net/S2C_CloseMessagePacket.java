package net.tysontheember.emberstextapi.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server-to-client packet that closes (removes) a single active message by ID.
 *
 * @param id Unique identifier of the message to close
 */
public record S2C_CloseMessagePacket(UUID id) {
    public static void encode(S2C_CloseMessagePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
    }

    public static S2C_CloseMessagePacket decode(FriendlyByteBuf buf) {
        return new S2C_CloseMessagePacket(buf.readUUID());
    }

    public static void handle(S2C_CloseMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ClientMessageManager.close(packet.id);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
