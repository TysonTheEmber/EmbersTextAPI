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
 * Server-to-client packet that opens (creates) a new immersive message.
 * <p>
 * Carries a unique message ID and the full NBT-serialized {@link ImmersiveMessage}.
 * The client deserializes and registers the message with {@link ClientMessageManager}.
 * </p>
 *
 * @param id  Unique identifier for this message instance
 * @param nbt NBT representation of the {@link ImmersiveMessage}
 */
public record S2C_OpenMessagePacket(UUID id, CompoundTag nbt) {
    public static void encode(S2C_OpenMessagePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
        buf.writeNbt(packet.nbt);
    }

    public static S2C_OpenMessagePacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        CompoundTag tag = buf.readNbt();
        return new S2C_OpenMessagePacket(id, tag == null ? new CompoundTag() : tag);
    }

    public static void handle(S2C_OpenMessagePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ImmersiveMessage message = ImmersiveMessage.fromNbt(packet.nbt);
                    ClientMessageManager.open(packet.id, message);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
