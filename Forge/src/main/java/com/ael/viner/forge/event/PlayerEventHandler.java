package com.ael.viner.forge.event;

import static com.ael.viner.forge.VinerForge.MOD_ID;

import com.ael.viner.forge.VinerForge;
import com.ael.viner.forge.network.VinerPacketHandler;
import com.ael.viner.forge.network.packets.ConfigSyncPacket;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import com.ael.viner.forge.registry.VinerPlayerRegistry;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Handles player-related events for the Viner mod. */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * When a player logs in, ensure their data is properly initialized with the latest configuration.
   *
   * @param event The player login event
   */
  @SubscribeEvent
  public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      LOGGER.info("Player logged in: {}", player.getName().getString());

      // Ensure the block registry is up to date
      VinerBlockRegistry.setup();

      // Initialize player data
      VinerForge instance = VinerForge.getInstance();
      if (instance != null
          && instance.getPlayerRegistry() instanceof VinerPlayerRegistry registry) {
        // Force refresh this player's data
        registry.getPlayerData(player); // This creates the player data if it doesn't exist
        LOGGER.info("Initialized Viner data for player: {}", player.getName().getString());
      }
    }
  }

  @SubscribeEvent
  public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
      return;
    }

    List<String> vineableBlocks =
        VinerBlockRegistry.getVineableBlocks().stream()
            .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
            .collect(Collectors.toList());

    ConfigSyncPacket vineableBlocksPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BLOCK_LIST, vineableBlocks, "vineableBlocks"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), vineableBlocksPacket);

    List<String> unvineableBlocks =
        VinerBlockRegistry.getUnvineableBlocks().stream()
            .map(block -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())
            .collect(Collectors.toList());
    ConfigSyncPacket unvineableBlocksPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BLOCK_LIST, unvineableBlocks, "unvineableBlocks"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), unvineableBlocksPacket);

    boolean vineAllEnabled = VinerBlockRegistry.isVineAll();
    ConfigSyncPacket vineAllPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BOOLEAN, vineAllEnabled, "vineAll"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), vineAllPacket);

    double exhaustionPerBlock = VinerBlockRegistry.getExhaustionPerBlock();
    ConfigSyncPacket exhaustionPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.DOUBLE, exhaustionPerBlock, "exhaustionPerBlock"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), exhaustionPacket);

    int vineableLimit = VinerBlockRegistry.getVineableLimit();
    ConfigSyncPacket vineableLimitPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, vineableLimit, "vineableLimit"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), vineableLimitPacket);

    int heightAbove = VinerBlockRegistry.getHeightAbove();
    ConfigSyncPacket heightAbovePacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, heightAbove, "heightAbove"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), heightAbovePacket);

    int heightBelow = VinerBlockRegistry.getHeightBelow();
    ConfigSyncPacket heightBelowPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, heightBelow, "heightBelow"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), heightBelowPacket);

    int widthLeft = VinerBlockRegistry.getWidthLeft();
    ConfigSyncPacket widthLeftPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, widthLeft, "widthLeft"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), widthLeftPacket);

    int widthRight = VinerBlockRegistry.getWidthRight();
    ConfigSyncPacket widthRightPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, widthRight, "widthRight"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), widthRightPacket);

    int layerOffset = VinerBlockRegistry.getLayerOffset();
    ConfigSyncPacket layerOffsetPacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.INT, layerOffset, "layerOffset"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), layerOffsetPacket);

    Boolean shapeVine = VinerBlockRegistry.isShapeVine();
    ConfigSyncPacket shapeVinePacket =
        new ConfigSyncPacket(
            new ConfigSyncPacket.ConfigData(
                ConfigSyncPacket.ConfigType.BOOLEAN, shapeVine, "shapeVine"));
    VinerPacketHandler.INSTANCE.send(
        PacketDistributor.PLAYER.with(() -> serverPlayer), shapeVinePacket);
  }
}
