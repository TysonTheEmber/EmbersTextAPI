package net.tysontheember.emberstextapi.network.forge.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;

import java.util.function.Supplier;

/**
 * Packet to force-stop a named channel queue on the client.
 * Closes the currently-active messages for that channel and clears pending steps.
 * An empty channel string stops all queues (equivalent to clearAllQueues).
 */
public record S2C_StopQueuePacket(String channel) {

    public static void encode(S2C_StopQueuePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.channel);
    }

    public static S2C_StopQueuePacket decode(FriendlyByteBuf buf) {
        return new S2C_StopQueuePacket(buf.readUtf());
    }

    public static void handle(S2C_StopQueuePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    if (packet.channel.isEmpty()) {
                        ClientMessageManager.clearAllQueues();
                    } else {
                        ClientMessageManager.stopQueue(packet.channel);
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}
