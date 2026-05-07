package net.tysontheember.emberstextapi.network.fabric.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.UUID;

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

    public static FriendlyByteBuf encodeClearQueue(String channel) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(channel);
        return buf;
    }

    public static FriendlyByteBuf encodeStopQueue(String channel) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(channel);
        return buf;
    }

    public static FriendlyByteBuf encodeOpenQueue(String channel, List<List<UUID>> ids, List<List<CompoundTag>> stepData) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(channel);
        buf.writeVarInt(stepData.size());
        for (int s = 0; s < stepData.size(); s++) {
            List<UUID> stepIds = ids.get(s);
            List<CompoundTag> msgs = stepData.get(s);
            buf.writeVarInt(msgs.size());
            for (int m = 0; m < msgs.size(); m++) {
                buf.writeUUID(stepIds.get(m));
                buf.writeNbt(msgs.get(m));
            }
        }
        return buf;
    }
}
