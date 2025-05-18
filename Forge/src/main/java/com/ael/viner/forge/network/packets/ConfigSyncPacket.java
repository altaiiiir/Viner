package com.ael.viner.forge.network.packets;

import com.ael.viner.common.VinerCore;
import com.ael.viner.forge.network.VinerPacketHandler;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ConfigSyncPacket extends AbstractPacket<ConfigSyncPacket.ConfigData> {
  private static final Logger LOGGER = LogManager.getLogger();

  public static final PacketFactory<ConfigSyncPacket> FACTORY =
      buf -> {
        ConfigType type = ConfigType.values()[buf.readInt()];
        Object value =
            switch (type) {
              case BOOLEAN -> buf.readBoolean();
              case DOUBLE -> buf.readDouble();
              case INT -> buf.readInt();
              case BLOCK_LIST -> buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
            };
        return new ConfigSyncPacket(new ConfigData(type, value, null));
      };

  public ConfigSyncPacket(ConfigData data) {
    super(data);
  }

  public static void encode(ConfigSyncPacket msg, FriendlyByteBuf buf) {
    ConfigData data = msg.getData();
    buf.writeInt(data.type().ordinal());
    switch (data.type()) {
      case BOOLEAN -> buf.writeBoolean((Boolean) data.value());
      case DOUBLE -> buf.writeDouble((Double) data.value());
      case INT -> buf.writeInt((Integer) data.value());
      case BLOCK_LIST -> {
        Object value = data.value();
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) value;
        buf.writeCollection(list, FriendlyByteBuf::writeUtf);
      }
    }
  }

  public static void syncConfigWithServer(ConfigType type, Object value, String configName) {
    ConfigSyncPacket packet =
        new ConfigSyncPacket(new ConfigSyncPacket.ConfigData(type, value, configName));
    VinerPacketHandler.INSTANCE.sendToServer(packet);
  }

  @Override
  public void handle(AbstractPacket<ConfigData> msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
    ServerPlayer player = ctx.get().getSender();
    if (player == null) return; // for single player

    LOGGER.info(
        "Received config packet for {} with type {} and value {}",
        msg.getData().configName(),
        msg.getData().type(),
        msg.getData().value());

    if ("vineAll".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.BOOLEAN) {
      VinerCore.getPlayerRegistry().setVineAllEnabled(player, (Boolean) msg.getData().value());
      LOGGER.info(
          "Updated vineAll to {} for player {}",
          msg.getData().value(),
          player.getName().getString());
    } else if ("vineableLimit".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.INT) {
      VinerCore.getPlayerRegistry().setVineableLimit(player, (Integer) msg.getData().value());
      LOGGER.info(
          "Updated vineableLimit to {} for player {}",
          msg.getData().value(),
          player.getName().getString());
    } else if ("exhaustionPerBlock".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.DOUBLE) {
      VinerCore.getPlayerRegistry().setExhaustionPerBlock(player, (Double) msg.getData().value());
    } else if ("heightAbove".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.INT) {
      VinerCore.getPlayerRegistry().setHeightAbove(player, (Integer) msg.getData().value());
    } else if ("heightBelow".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.INT) {
      VinerCore.getPlayerRegistry().setHeightBelow(player, (Integer) msg.getData().value());
    } else if ("widthLeft".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.INT) {
      VinerCore.getPlayerRegistry().setWidthLeft(player, (Integer) msg.getData().value());
    } else if ("widthRight".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.INT) {
      VinerCore.getPlayerRegistry().setWidthRight(player, (Integer) msg.getData().value());
    } else if ("layerOffset".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.INT) {
      VinerCore.getPlayerRegistry().setLayerOffset(player, (Integer) msg.getData().value());
    } else if ("shapeVine".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.BOOLEAN) {
      VinerCore.getPlayerRegistry().setShapeVine(player, (Boolean) msg.getData().value());
    } else if ("vineableBlocks".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.BLOCK_LIST) {
      Object value = msg.getData().value();
      List<String> entries;
      if (value instanceof List<?> list) {
        entries =
            list.stream().allMatch(e -> e instanceof String)
                ? list.stream().map(e -> (String) e).collect(Collectors.toList())
                : List.of();
      } else {
        entries = List.of();
      }
      LOGGER.info(
          "Setting vineableBlocks for player {} to {} entries",
          player.getName().getString(),
          entries.size());
      VinerCore.getPlayerRegistry().setVineableBlocks(player, entries);
      VinerCore.getPlayerRegistry().setVineableTags(player, entries);
      VinerBlockRegistry.setup();
    } else if ("unvineableBlocks".equals(msg.getData().configName())
        && msg.getData().type() == ConfigType.BLOCK_LIST) {
      Object value = msg.getData().value();
      List<String> entries;
      if (value instanceof List<?> list) {
        entries =
            list.stream().allMatch(e -> e instanceof String)
                ? list.stream().map(e -> (String) e).collect(Collectors.toList())
                : List.of();
      } else {
        entries = List.of();
      }
      LOGGER.info(
          "Setting unvineableBlocks for player {} to {} entries",
          player.getName().getString(),
          entries.size());
      VinerCore.getPlayerRegistry().setUnvineableBlocks(player, entries);
      VinerCore.getPlayerRegistry().setUnvineableTags(player, entries);
      VinerBlockRegistry.setup();
    }

    ctx.get().setPacketHandled(true);
  }

  public enum ConfigType {
    BOOLEAN,
    DOUBLE,
    INT,
    BLOCK_LIST
  }

  public record ConfigData(ConfigType type, Object value, String configName) {}
}
