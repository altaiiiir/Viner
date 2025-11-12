package com.ael.viner.forge.registry;

import static com.ael.viner.forge.registry.VinerBlockRegistry.getBlocksFromConfigEntries;
import static com.ael.viner.forge.registry.VinerBlockRegistry.getTagsFromConfigEntries;

import com.ael.viner.common.IPlayerData;
import com.ael.viner.common.IPlayerRegistry;
import com.ael.viner.forge.player.VinerPlayerData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VinerPlayerRegistry implements IPlayerRegistry {
  private static final Logger LOGGER = LogManager.getLogger();
  private final Map<UUID, VinerPlayerData> players;

  public VinerPlayerRegistry() {
    players = new HashMap<>();
  }

  public static VinerPlayerRegistry create() {
    return new VinerPlayerRegistry();
  }

  @Override
  public IPlayerData getPlayerData(Object player) {
    if (player instanceof Player) {
      return players.computeIfAbsent(((Player) player).getUUID(), VinerPlayerData::new);
    }
    throw new IllegalArgumentException("Player must be a net.minecraft.world.entity.player.Player");
  }

  public VinerPlayerData getPlayerData(Player player) {
    return players.computeIfAbsent(player.getUUID(), VinerPlayerData::new);
  }

  /**
   * Refreshes all player data instances with the latest configuration values. Should be called when
   * global configuration changes are detected.
   */
  public void refreshAllPlayers() {
    if (players.isEmpty()) {
      LOGGER.info("No players to refresh");
      return;
    }

    LOGGER.info("Refreshing data for " + players.size() + " player(s)");

    // Create a new map to avoid concurrent modification
    Map<UUID, VinerPlayerData> updatedPlayers = new HashMap<>();

    // Update each player's data while preserving player-specific state
    for (Map.Entry<UUID, VinerPlayerData> entry : players.entrySet()) {
      VinerPlayerData oldData = entry.getValue();

      // Log the current block lists before refresh
      LOGGER.info("Player " + entry.getKey() + " before refresh:");
      LOGGER.info("  vineableBlocks: " + oldData.getVineableBlockIds().size() + " entries");
      LOGGER.info("  unvineableBlocks: " + oldData.getUnvineableBlockIds().size() + " entries");

      // Create new data with updated config values but preserve player-specific state and block
      // lists
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(VinerPlayerData.fromConfig())
              .vineKeyPressed(oldData.isVineKeyPressed())
              // Keep the player's custom block lists if they have them
              .vineableBlocks(
                  oldData.vineableBlocks().isEmpty()
                      ? VinerBlockRegistry.getVineableBlocks()
                      : oldData.vineableBlocks())
              .unvineableBlocks(
                  oldData.unvineableBlocks().isEmpty()
                      ? VinerBlockRegistry.getUnvineableBlocks()
                      : oldData.unvineableBlocks())
              .vineableTags(
                  oldData.vineableTags().isEmpty()
                      ? VinerBlockRegistry.getVineableTags()
                      : oldData.vineableTags())
              .unvineableTags(
                  oldData.unvineableTags().isEmpty()
                      ? VinerBlockRegistry.getUnvineableTags()
                      : oldData.unvineableTags())
              .build();

      // Log the updated block lists
      LOGGER.info("Player " + entry.getKey() + " after refresh:");
      LOGGER.info(
          "  old vineableLimit="
              + oldData.getVineableLimit()
              + ", new vineableLimit="
              + newData.getVineableLimit());
      LOGGER.info("  vineableBlocks: " + newData.getVineableBlockIds().size() + " entries");
      LOGGER.info("  unvineableBlocks: " + newData.getUnvineableBlockIds().size() + " entries");

      updatedPlayers.put(entry.getKey(), newData);
    }

    // Replace the entire players map
    players.clear();
    players.putAll(updatedPlayers);
    LOGGER.info("Player data refresh complete");
  }

  @Override
  public void setVineAllEnabled(Object player, boolean enabled) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).vineAllEnabled(enabled).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setExhaustionPerBlock(Object player, double exhaustionPerBlock) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData)
              .exhaustionPerBlock(exhaustionPerBlock)
              .build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setVineableLimit(Object player, int vineableLimit) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).vineableLimit(vineableLimit).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setHeightAbove(Object player, int heightAbove) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).heightAbove(heightAbove).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setHeightBelow(Object player, int heightBelow) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).heightBelow(heightBelow).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setWidthLeft(Object player, int widthLeft) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).widthLeft(widthLeft).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setWidthRight(Object player, int widthRight) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).widthRight(widthRight).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setLayerOffset(Object player, int layerOffset) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).layerOffset(layerOffset).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setShapeVine(Object player, boolean shapeVine) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).isShapeVine(shapeVine).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setVineableBlocks(Object player, List<String> vineableBlockIds) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      List<Block> blocks = getBlocksFromConfigEntries(vineableBlockIds);
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).vineableBlocks(blocks).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setUnvineableBlocks(Object player, List<String> unvineableBlockIds) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      List<Block> blocks = getBlocksFromConfigEntries(unvineableBlockIds);
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).unvineableBlocks(blocks).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setVineableTags(Object player, List<String> vineableTagNames) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      List<TagKey<Block>> tags = getTagsFromConfigEntries(vineableTagNames);
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).vineableTags(tags).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setUnvineableTags(Object player, List<String> unvineableTagNames) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      List<TagKey<Block>> tags = getTagsFromConfigEntries(unvineableTagNames);
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).unvineableTags(tags).build();
      players.put(uuid, newData);
    }
  }

  @Override
  public void setVineKeyPressed(Object player, boolean pressed) {
    if (player instanceof ServerPlayer serverPlayer) {
      UUID uuid = serverPlayer.getUUID();
      VinerPlayerData oldData = getPlayerData(serverPlayer);
      VinerPlayerData newData =
          new VinerPlayerData.VinerPlayerDataBuilder(oldData).vineKeyPressed(pressed).build();
      players.put(uuid, newData);
    }
  }
}
