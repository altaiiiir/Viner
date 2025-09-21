package com.ael.viner.forge.network;

import com.ael.viner.forge.VinerForge;
import com.ael.viner.forge.network.packets.ConfigSyncPacket;
import com.ael.viner.forge.network.packets.VeinMiningPacket;
import com.ael.viner.forge.network.packets.VinerKeyPressedPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

/** Handles packet registration and networking channel setup for the Viner mod. */
public class VinerPacketHandler {
  /** Protocol version for networking. Change this string for each significant protocol change. */
  private static final String PROTOCOL_VERSION = "1";

  /** Networking channel instance for sending and receiving packets. */
  public static final SimpleChannel INSTANCE =
      ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(VinerForge.MOD_ID, "main"))
          .networkProtocolVersion(Integer.parseInt(PROTOCOL_VERSION))
          .simpleChannel();

  /** Registers all packet classes and their respective handlers. */
  @SuppressWarnings("deprecation")
  public static void register() {
    int id = 0;
    // Register VeinMiningPacket
    // client -> server
    INSTANCE
        .messageBuilder(VeinMiningPacket.class, id++)
        .encoder(VeinMiningPacket::encode)
        .decoder(VeinMiningPacket::decode)
        .consumerMainThread(VeinMiningPacket::handle)
        .add();

    INSTANCE
        .messageBuilder(VinerKeyPressedPacket.class, id++)
        .encoder(VinerKeyPressedPacket::encode)
        .decoder(VinerKeyPressedPacket::decode)
        .consumerMainThread(VinerKeyPressedPacket::handle)
        .add();

    // server -> client
    INSTANCE
        .messageBuilder(ConfigSyncPacket.class, id++)
        .encoder(ConfigSyncPacket::encode)
        .decoder(ConfigSyncPacket::decode)
        .consumerMainThread(ConfigSyncPacket::handle)
        .add();
  }

  // (client → server)
  public static void sendToServer(Object msg) {
    var mc = net.minecraft.client.Minecraft.getInstance();
    var net = mc != null ? mc.getConnection() : null;
    if (net != null) {
      INSTANCE.send(msg, net.getConnection());
    }
  }

  // (server → one client)
  public static void sendTo(net.minecraft.server.level.ServerPlayer player, Object msg) {
    var conn = player.connection;
    if (conn != null) {
      INSTANCE.send(msg, conn.getConnection());
    }
  }
}
