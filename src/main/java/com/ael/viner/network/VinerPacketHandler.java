package com.ael.viner.network;

import com.ael.viner.Viner;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Handles packet registration and networking channel setup for the Viner mod.
 */
public class VinerPacketHandler {
    /**
     * Protocol version for networking. Change this string for each significant protocol change.
     */
    private static final String PROTOCOL_VERSION = "1";

    /**
     * Networking channel instance for sending and receiving packets.
     */
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Viner.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    /**
     * Registers all packet classes and their respective handlers.
     */
    public static void register() {
        int id = 0;
        // Register the VeinMiningPacket class and its encoder, decoder, and handler methods
        INSTANCE.registerMessage(id++, VeinMiningPacket.class, VeinMiningPacket::encode,
                VeinMiningPacket::decode, VeinMiningPacket::handle);
    }
}

