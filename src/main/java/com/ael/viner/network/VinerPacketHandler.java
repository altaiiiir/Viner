package com.ael.viner.network;

import com.ael.viner.forge.VinerForge;
import com.ael.viner.network.packets.ConfigSyncPacket;
import com.ael.viner.network.packets.MouseScrollPacket;
import com.ael.viner.network.packets.VeinMiningPacket;
import com.ael.viner.network.packets.VinerKeyPressedPacket;
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
            new ResourceLocation(VinerForge.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    /**
     * Registers all packet classes and their respective handlers.
     */
    public static void register() {
        int id = 0;

        INSTANCE.registerMessage(id++, VeinMiningPacket.class,
                VeinMiningPacket::encode,
                buf -> VeinMiningPacket.decode(buf, VeinMiningPacket.FACTORY),
                (packet, ctx) -> packet.handle(packet, ctx)
        );

        INSTANCE.registerMessage(id++, VinerKeyPressedPacket.class, VinerKeyPressedPacket::encode,
                buf -> VinerKeyPressedPacket.decode(buf, VinerKeyPressedPacket.FACTORY),
                (packet, ctx) -> packet.handle(packet, ctx));

        INSTANCE.registerMessage(id++, MouseScrollPacket.class, MouseScrollPacket::encode,
                buf -> MouseScrollPacket.decode(buf, MouseScrollPacket.FACTORY),
                (packet, ctx) -> packet.handle(packet, ctx));

        INSTANCE.registerMessage(id++, ConfigSyncPacket.class, ConfigSyncPacket::encode,
                buf -> ConfigSyncPacket.decode(buf, ConfigSyncPacket.FACTORY),
                (packet, ctx) -> packet.handle(packet, ctx));

    }

}

