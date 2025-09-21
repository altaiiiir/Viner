package com.ael.viner.forge.network.packets;

import com.ael.viner.common.VinerCore;
import com.ael.viner.forge.network.VinerPacketHandler;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConfigSyncPacket extends AbstractPacket<ConfigSyncPacket.ConfigData> {
  private static final Logger LOGGER = LogManager.getLogger();

  // ----- data model -----
  public enum ConfigType {
    BOOLEAN,
    DOUBLE,
    INT,
    BLOCK_LIST
  }

  public record ConfigData(ConfigType type, Object value, String configName) {}

  public ConfigSyncPacket(ConfigData data) {
    super(data);
  }

  // ----- codec -----
  public static void encode(ConfigSyncPacket msg, FriendlyByteBuf buf) {
    var d = msg.data();
    buf.writeInt(d.type().ordinal());
    switch (d.type()) {
      case BOOLEAN -> buf.writeBoolean((Boolean) d.value());
      case DOUBLE -> buf.writeDouble((Double) d.value());
      case INT -> buf.writeInt((Integer) d.value());
      case BLOCK_LIST -> {
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) d.value();
        buf.writeCollection(list, (FriendlyByteBuf b, String s) -> b.writeUtf(s));
      }
    }
    buf.writeUtf(d.configName() != null ? d.configName() : "");
  }

  public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
    var type = ConfigType.values()[buf.readInt()];
    Object value =
        switch (type) {
          case BOOLEAN -> buf.readBoolean();
          case DOUBLE -> buf.readDouble();
          case INT -> buf.readInt();
          case BLOCK_LIST -> buf.readList((FriendlyByteBuf b) -> b.readUtf());
        };
    String name = buf.readUtf();
    return new ConfigSyncPacket(new ConfigData(type, value, name));
  }

  // ----- client -> server send helper -----
  public static void syncConfigWithServer(ConfigType type, Object value, String configName) {
    VinerPacketHandler.sendToServer(new ConfigSyncPacket(new ConfigData(type, value, configName)));
  }

  // ----- server-side handler -----
  @Override
  public void handle(CustomPayloadEvent.Context ctx) {
    var player = ctx.getSender();
    if (player == null) {
      ctx.setPacketHandled(true);
      return;
    }

    var d = data();
    var name = d.configName();
    LOGGER.info("Received config packet: {} = {} ({})", name, d.value(), d.type());

    switch (d.type()) {
      case BOOLEAN -> {
        boolean v = (Boolean) d.value();
        if ("vineAll".equals(name)) {
          VinerCore.getPlayerRegistry().setVineAllEnabled(player, v);
        } else if ("shapeVine".equals(name)) {
          VinerCore.getPlayerRegistry().setShapeVine(player, v);
        }
      }
      case DOUBLE -> {
        double v = (Double) d.value();
        if ("exhaustionPerBlock".equals(name)) {
          VinerCore.getPlayerRegistry().setExhaustionPerBlock(player, v);
        }
      }
      case INT -> {
        int v = (Integer) d.value();
        var reg = VinerCore.getPlayerRegistry();
        switch (name) {
          case "vineableLimit" -> reg.setVineableLimit(player, v);
          case "heightAbove" -> reg.setHeightAbove(player, v);
          case "heightBelow" -> reg.setHeightBelow(player, v);
          case "widthLeft" -> reg.setWidthLeft(player, v);
          case "widthRight" -> reg.setWidthRight(player, v);
          case "layerOffset" -> reg.setLayerOffset(player, v);
        }
      }
      case BLOCK_LIST -> {
        // @SuppressWarnings("unchecked")
        List<String> entriesRaw =
            (d.value() instanceof List<?> l)
                ? (List<String>)
                    l.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .collect(Collectors.toList())
                : List.of();

        var reg = VinerCore.getPlayerRegistry();
        if ("vineableBlocks".equals(name)) {
          reg.setVineableBlocks(player, entriesRaw);
          reg.setVineableTags(player, entriesRaw);
          VinerBlockRegistry.setup();
        } else if ("unvineableBlocks".equals(name)) {
          reg.setUnvineableBlocks(player, entriesRaw);
          reg.setUnvineableTags(player, entriesRaw);
          VinerBlockRegistry.setup();
        }
      }
    }

    ctx.setPacketHandled(true);
  }
}
