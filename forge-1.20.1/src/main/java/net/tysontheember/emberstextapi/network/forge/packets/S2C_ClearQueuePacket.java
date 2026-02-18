package net.tysontheember.emberstextapi.network.forge.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;

import java.util.function.Supplier;

/**
 * Packet to clear a named channel queue on the client.
 * An empty channel string clears all queues immediately.
 */
public record S2C_ClearQueuePacket(String channel) {

    public static void encode(S2C_ClearQueuePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.channel);
    }

    public static S2C_ClearQueuePacket decode(FriendlyByteBuf buf) {
        return new S2C_ClearQueuePacket(buf.readUtf());
    }

    public static void handle(S2C_ClearQueuePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    if (packet.channel.isEmpty()) {
                        ClientMessageManager.clearAllQueues();
                    } else {
                        ClientMessageManager.clearQueue(packet.channel);
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}
