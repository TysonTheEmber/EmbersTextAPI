package net.tysontheember.emberstextapi.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.network.TooltipPacket;
import net.tysontheember.emberstextapi.net.C2S_CloseAllMessagesPacket;
import net.tysontheember.emberstextapi.net.C2S_CloseMessagePacket;
import net.tysontheember.emberstextapi.net.C2S_OpenMessagePacket;
import net.tysontheember.emberstextapi.net.C2S_UpdateMessagePacket;

/**
 * Central registration point for mod networking.
 */
public final class Network {
    private static final String PROTOCOL = "4";
    private static final ResourceLocation ID = new ResourceLocation(EmbersTextAPI.MODID, "tooltip");
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ID, () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private Network() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, TooltipPacket.class, TooltipPacket::encode, TooltipPacket::decode, TooltipPacket::handle);
        CHANNEL.registerMessage(id++, C2S_OpenMessagePacket.class, C2S_OpenMessagePacket::encode, C2S_OpenMessagePacket::decode, C2S_OpenMessagePacket::handle);
        CHANNEL.registerMessage(id++, C2S_UpdateMessagePacket.class, C2S_UpdateMessagePacket::encode, C2S_UpdateMessagePacket::decode, C2S_UpdateMessagePacket::handle);
        CHANNEL.registerMessage(id++, C2S_CloseMessagePacket.class, C2S_CloseMessagePacket::encode, C2S_CloseMessagePacket::decode, C2S_CloseMessagePacket::handle);
        CHANNEL.registerMessage(id, C2S_CloseAllMessagesPacket.class, C2S_CloseAllMessagesPacket::encode, C2S_CloseAllMessagesPacket::decode, C2S_CloseAllMessagesPacket::handle);
    }
}
