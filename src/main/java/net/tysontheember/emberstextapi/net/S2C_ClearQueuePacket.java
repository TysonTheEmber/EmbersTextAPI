package net.tysontheember.emberstextapi.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.immersivemessages.ImmersiveMessagesManager;

import java.util.function.Supplier;

/**
 * Packet to clear the ImmersiveMessagesManager queue on the client.
 */
public record S2C_ClearQueuePacket() {
    public static void encode(S2C_ClearQueuePacket packet, FriendlyByteBuf buf) {
    }

    public static S2C_ClearQueuePacket decode(FriendlyByteBuf buf) {
        return new S2C_ClearQueuePacket();
    }

    public static void handle(S2C_ClearQueuePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ImmersiveMessagesManager.clear();
                }
            });
        }
        context.setPacketHandled(true);
    }
}
