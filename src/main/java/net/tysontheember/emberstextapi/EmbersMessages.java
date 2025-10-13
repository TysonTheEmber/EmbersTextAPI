package net.tysontheember.emberstextapi;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.net.C2S_CloseAllMessagesPacket;
import net.tysontheember.emberstextapi.net.C2S_CloseMessagePacket;
import net.tysontheember.emberstextapi.net.C2S_OpenMessagePacket;
import net.tysontheember.emberstextapi.net.C2S_UpdateMessagePacket;
import net.tysontheember.emberstextapi.network.Network;

import java.util.UUID;

public final class EmbersMessages {
    private EmbersMessages() {
    }

    public static UUID open(ServerPlayer player, ImmersiveMessage message) {
        UUID id = UUID.randomUUID();
        send(player, new C2S_OpenMessagePacket(id, message.toNbt()));
        return id;
    }

    public static void update(ServerPlayer player, UUID id, ImmersiveMessage message) {
        if (id == null) {
            return;
        }
        send(player, new C2S_UpdateMessagePacket(id, message.toNbt()));
    }

    public static void close(ServerPlayer player, UUID id) {
        if (id == null) {
            return;
        }
        send(player, new C2S_CloseMessagePacket(id));
    }

    public static void closeAll(ServerPlayer player) {
        send(player, new C2S_CloseAllMessagesPacket());
    }

    private static void send(ServerPlayer player, Object packet) {
        Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
