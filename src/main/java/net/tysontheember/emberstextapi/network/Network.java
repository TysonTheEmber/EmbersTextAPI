package net.tysontheember.emberstextapi.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.network.TooltipPacket;
import net.tysontheember.emberstextapi.net.S2C_ClearQueuePacket;
import net.tysontheember.emberstextapi.net.S2C_CloseAllMessagesPacket;
import net.tysontheember.emberstextapi.net.S2C_CloseMessagePacket;
import net.tysontheember.emberstextapi.net.S2C_OpenMessagePacket;
import net.tysontheember.emberstextapi.net.S2C_UpdateMessagePacket;

/**
 * Central registration point for mod networking.
 */
public final class Network {
    private static final String PROTOCOL = "3";
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(EmbersTextAPI.MODID, "tooltip");
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ID, () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private Network() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, TooltipPacket.class, TooltipPacket::encode, TooltipPacket::decode, TooltipPacket::handle);
        CHANNEL.registerMessage(id++, S2C_OpenMessagePacket.class, S2C_OpenMessagePacket::encode, S2C_OpenMessagePacket::decode, S2C_OpenMessagePacket::handle);
        CHANNEL.registerMessage(id++, S2C_UpdateMessagePacket.class, S2C_UpdateMessagePacket::encode, S2C_UpdateMessagePacket::decode, S2C_UpdateMessagePacket::handle);
        CHANNEL.registerMessage(id++, S2C_CloseMessagePacket.class, S2C_CloseMessagePacket::encode, S2C_CloseMessagePacket::decode, S2C_CloseMessagePacket::handle);
        CHANNEL.registerMessage(id++, S2C_CloseAllMessagesPacket.class, S2C_CloseAllMessagesPacket::encode, S2C_CloseAllMessagesPacket::decode, S2C_CloseAllMessagesPacket::handle);
        CHANNEL.registerMessage(id, S2C_ClearQueuePacket.class, S2C_ClearQueuePacket::encode, S2C_ClearQueuePacket::decode, S2C_ClearQueuePacket::handle);
    }
}
