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

/**
 * Server-to-client packet that updates an existing active message.
 * <p>
 * If the message ID is not already active on the client, it will be created.
 * Otherwise, the existing message is replaced with the new data.
 * </p>
 *
 * @param id  Unique identifier of the message to update
 * @param nbt NBT representation of the updated {@link ImmersiveMessage}
 */
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
