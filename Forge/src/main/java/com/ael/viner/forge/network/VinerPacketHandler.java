package com.ael.viner.forge.network;

import com.ael.viner.common.VinerCore;
import com.ael.viner.forge.VinerForge;
import com.ael.viner.forge.network.packets.ConfigSyncPacket;
import com.ael.viner.forge.network.packets.VeinMiningPacket;
import com.ael.viner.forge.network.packets.VinerKeyPressedPacket;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;

/** Handles packet registration and networking channel setup for the Viner mod. */
public class VinerPacketHandler {
  /** Protocol version for networking. Change this string for each significant protocol change. */
  private static final String PROTOCOL_VERSION = "1";

  /** Networking channel instance for sending and receiving packets. */
  public static final SimpleChannel INSTANCE = ChannelBuilder
    .named(ResourceLocation.fromNamespaceAndPath(VinerForge.MOD_ID, "main"))
    .networkProtocolVersion(Integer.parseInt(PROTOCOL_VERSION))
    .simpleChannel();
  // public static final SimpleChannel INSTANCE =
  //     NetworkRegistry.newSimpleChannel(
  //         new ResourceLocation(VinerForge.MOD_ID, "main"),
  //         () -> PROTOCOL_VERSION,
  //         PROTOCOL_VERSION::equals,
  //         PROTOCOL_VERSION::equals);

  /** Registers all packet classes and their respective handlers. */
  public static void register() {
    int id = 0;

    // Register VeinMiningPacket
    INSTANCE.registerMessage(
        id++,
        VeinMiningPacket.class,
        VeinMiningPacket::encode,
        buf -> VeinMiningPacket.decode(buf, VeinMiningPacket.FACTORY),
        (packet, ctx) -> packet.handle(packet, ctx));

    // Register VinerKeyPressedPacket with a simplified approach
    INSTANCE.registerMessage(
        id++,
        VinerKeyPressedPacket.class,
        (msg, buf) -> msg.encode(buf),
        VinerKeyPressedPacket::decode,
        (msg, ctx) -> {
          ctx.get()
              .enqueueWork(
                  () -> {
                    if (ctx.get().getSender() != null) {
                      VinerCore.getPlayerRegistry()
                          .setVineKeyPressed(ctx.get().getSender(), msg.isPressed());
                    }
                  });
          ctx.get().setPacketHandled(true);
        },
        Optional.of(NetworkDirection.PLAY_TO_SERVER));

    // Register ConfigSyncPacket
    INSTANCE.registerMessage(
        id++,
        ConfigSyncPacket.class,
        ConfigSyncPacket::encode,
        buf -> ConfigSyncPacket.decode(buf, ConfigSyncPacket.FACTORY),
        (packet, ctx) -> packet.handle(packet, ctx));
  }
}
