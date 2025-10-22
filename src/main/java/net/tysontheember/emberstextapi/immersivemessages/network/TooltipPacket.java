package net.tysontheember.emberstextapi.immersivemessages.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.network.Network;
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
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(EmbersTextAPI.MODID, "tooltip");
    public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL = Network.CHANNEL;

    private final ImmersiveMessage message;

    public TooltipPacket(ImmersiveMessage message) {
        this.message = message;
    }

    @Deprecated(forRemoval = true)
    public static void register() {
        Network.register();
    }

    public static void encode(TooltipPacket packet, FriendlyByteBuf buf) {
        packet.message.encode(buf);
    }

    public static TooltipPacket decode(FriendlyByteBuf buf) {
        return new TooltipPacket(ImmersiveMessage.decode(buf));
    }

    public static void handle(TooltipPacket packet, Supplier<NetworkEvent.Context> ctx) {
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
