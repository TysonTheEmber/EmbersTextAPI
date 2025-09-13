package net.tysontheember.emberstextapi.immersivemessages.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.ImmersiveMessagesManager;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.function.Supplier;

/**
 * Networking packet that transfers an {@link ImmersiveMessage} from the server
 * to the client.  The original project uses a more feature rich networking
 * library; this version sticks with Forge's {@link SimpleChannel} for
 * simplicity.
 */
public class TooltipPacket {
    private static final String PROTOCOL = "2";
    private static final ResourceLocation ID = new ResourceLocation(EmbersTextAPI.MODID, "tooltip");
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ID, () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private final ImmersiveMessage message;

    public TooltipPacket(ImmersiveMessage message) {
        this.message = message;
    }

    public static void register() {
        CHANNEL.registerMessage(0, TooltipPacket.class, TooltipPacket::encode, TooltipPacket::decode, TooltipPacket::handle);
    }

    private static void encode(TooltipPacket packet, FriendlyByteBuf buf) {
        packet.message.encode(buf);
    }

    private static TooltipPacket decode(FriendlyByteBuf buf) {
        return new TooltipPacket(ImmersiveMessage.decode(buf));
    }

    private static void handle(TooltipPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ImmersiveMessagesManager.showToPlayer(mc.player, packet.message);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
