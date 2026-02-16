package net.tysontheember.emberstextapi.network.fabric.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Utility class for encoding/decoding Fabric network packets.
 */
public class FabricPacketCodecs {
    public static FriendlyByteBuf encodeOpenMessage(UUID id, CompoundTag data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(id);
        buf.writeNbt(data);
        return buf;
    }

    public static FriendlyByteBuf encodeUpdateMessage(UUID id, CompoundTag data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(id);
        buf.writeNbt(data);
        return buf;
    }

    public static FriendlyByteBuf encodeCloseMessage(UUID id) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(id);
        return buf;
    }

    public static FriendlyByteBuf encodeCloseAllMessages() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    public static FriendlyByteBuf encodeClearQueue() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}
