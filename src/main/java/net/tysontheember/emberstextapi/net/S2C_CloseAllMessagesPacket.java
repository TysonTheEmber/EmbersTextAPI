package net.tysontheember.emberstextapi.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;

import java.util.function.Supplier;

public record S2C_CloseAllMessagesPacket() {
    public static void encode(S2C_CloseAllMessagesPacket packet, FriendlyByteBuf buf) {
    }

    public static S2C_CloseAllMessagesPacket decode(FriendlyByteBuf buf) {
        return new S2C_CloseAllMessagesPacket();
    }

    public static void handle(S2C_CloseAllMessagesPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ClientMessageManager.closeAll();
                }
            });
        }
        context.setPacketHandled(true);
    }
}
