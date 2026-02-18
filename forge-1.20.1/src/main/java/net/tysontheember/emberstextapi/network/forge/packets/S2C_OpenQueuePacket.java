package net.tysontheember.emberstextapi.network.forge.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.client.QueuedMessage;
import net.tysontheember.emberstextapi.client.QueueStep;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server-to-client packet that sends an ordered queue of steps on a named channel.
 * <p>
 * Each step contains one or more simultaneously-displayed messages. The next step
 * starts only after every message in the current step has expired.
 * </p>
 */
public record S2C_OpenQueuePacket(String channel, List<List<UUID>> ids, List<List<CompoundTag>> stepData) {

    public static void encode(S2C_OpenQueuePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.channel);
        buf.writeVarInt(packet.stepData.size());
        for (int s = 0; s < packet.stepData.size(); s++) {
            List<UUID> stepIds = packet.ids.get(s);
            List<CompoundTag> stepMsgs = packet.stepData.get(s);
            buf.writeVarInt(stepMsgs.size());
            for (int m = 0; m < stepMsgs.size(); m++) {
                buf.writeUUID(stepIds.get(m));
                buf.writeNbt(stepMsgs.get(m));
            }
        }
    }

    public static S2C_OpenQueuePacket decode(FriendlyByteBuf buf) {
        String channel = buf.readUtf();
        int stepCount = buf.readVarInt();
        List<List<UUID>> ids = new ArrayList<>(stepCount);
        List<List<CompoundTag>> stepData = new ArrayList<>(stepCount);
        for (int s = 0; s < stepCount; s++) {
            int msgCount = buf.readVarInt();
            List<UUID> stepIds = new ArrayList<>(msgCount);
            List<CompoundTag> msgs = new ArrayList<>(msgCount);
            for (int m = 0; m < msgCount; m++) {
                stepIds.add(buf.readUUID());
                CompoundTag tag = buf.readNbt();
                msgs.add(tag == null ? new CompoundTag() : tag);
            }
            ids.add(stepIds);
            stepData.add(msgs);
        }
        return new S2C_OpenQueuePacket(channel, ids, stepData);
    }

    public static void handle(S2C_OpenQueuePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    List<QueueStep> steps = new ArrayList<>();
                    for (int s = 0; s < packet.stepData.size(); s++) {
                        List<QueuedMessage> msgs = new ArrayList<>();
                        List<UUID> stepIds = packet.ids.get(s);
                        List<CompoundTag> stepMsgs = packet.stepData.get(s);
                        for (int m = 0; m < stepMsgs.size(); m++) {
                            ImmersiveMessage message = ImmersiveMessage.fromNbt(stepMsgs.get(m));
                            msgs.add(new QueuedMessage(stepIds.get(m), message));
                        }
                        steps.add(new QueueStep(msgs));
                    }
                    ClientMessageManager.enqueueSteps(packet.channel, steps);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
