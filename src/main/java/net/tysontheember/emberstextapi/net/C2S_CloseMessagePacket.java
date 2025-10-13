package net.tysontheember.emberstextapi.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;

import java.util.UUID;
import java.util.function.Supplier;

public record C2S_CloseMessagePacket(UUID id) {
    public static void encode(C2S_CloseMessagePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
    }

    public static C2S_CloseMessagePacket decode(FriendlyByteBuf buf) {
        return new C2S_CloseMessagePacket(buf.readUUID());
    }

    public static void handle(C2S_CloseMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {
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
