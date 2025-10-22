package net.tysontheember.emberstextapi.net;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.UUID;
import java.util.function.Supplier;

public record S2C_UpdateMessagePacket(UUID id, CompoundTag nbt) {
    public static void encode(S2C_UpdateMessagePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
        buf.writeNbt(packet.nbt);
    }

    public static S2C_UpdateMessagePacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        CompoundTag tag = buf.readNbt();
        return new S2C_UpdateMessagePacket(id, tag == null ? new CompoundTag() : tag);
    }

    public static void handle(S2C_UpdateMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(packet.nbt);
                    ClientMessageManager.update(packet.id, message);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
