package net.tysontheember.emberstextapi.network;

import net.minecraftforge.network.simple.SimpleChannel;
import net.tysontheember.emberstextapi.network.forge.ForgeNetworkHandler;

/**
 * Central registration point for mod networking.
 * <p>
 * This class is maintained for backward compatibility.
 * New code should use {@link ForgeNetworkHandler} directly.
 * </p>
 *
 * @deprecated Use {@link ForgeNetworkHandler} instead
 */
@Deprecated
public final class Network {
    /**
     * The Forge SimpleChannel.
     * Delegates to ForgeNetworkHandler for backward compatibility.
     */
    public static final SimpleChannel CHANNEL = ForgeNetworkHandler.CHANNEL;

    private Network() {
    }

    /**
     * Register all network packets.
     * Delegates to ForgeNetworkHandler.
     */
    public static void register() {
        ForgeNetworkHandler.getInstance().register();
    }
}
