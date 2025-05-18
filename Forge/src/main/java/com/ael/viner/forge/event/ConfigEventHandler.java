package com.ael.viner.forge.event;

import com.ael.viner.forge.VinerForge;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import com.ael.viner.forge.registry.VinerPlayerRegistry;
import java.util.Objects;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles configuration events for the Viner mod, ensuring the block registry and players are
 * updated when the mod configuration is reloaded.
 */
@Mod.EventBusSubscriber(modid = VinerForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigEventHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Responds to mod configuration reloading events by updating the block registry if the event is
   * related to the Viner mod.
   *
   * @param event The event triggered when the mod configuration is reloaded.
   */
  @SubscribeEvent
  public static void onModConfigEvent(final ModConfigEvent.Reloading event) {
    final ModConfig config = event.getConfig();
    // Check if the event is related to the Viner mod
    if (Objects.equals(config.getModId(), VinerForge.MOD_ID)) {
      LOGGER.info("Viner config reloaded: " + config.getFileName());

      // Update the block registry to reflect the new configuration
      VinerBlockRegistry.setup();
      LOGGER.debug(
          "VinerBlockRegistry updated - vineableLimit: " + VinerBlockRegistry.getVineableLimit());

      // Force refresh player data based on the updated registry
      VinerForge instance = VinerForge.getInstance();
      if (instance != null
          && instance.getPlayerRegistry() instanceof VinerPlayerRegistry registry) {
        LOGGER.info("Refreshing player data");
        registry.refreshAllPlayers();
      } else {
        LOGGER.warn("Failed to refresh player data - instance null? " + (instance == null));
      }
    }
  }

  /**
   * Responds to mod configuration loading events to ensure everything is set up initially.
   *
   * @param event The event triggered when the mod configuration is loaded.
   */
  @SubscribeEvent
  public static void onModConfigLoad(final ModConfigEvent.Loading event) {
    final ModConfig config = event.getConfig();
    if (Objects.equals(config.getModId(), VinerForge.MOD_ID)) {
      LOGGER.info("Viner config loaded: " + config.getFileName());
      VinerBlockRegistry.setup();
    }
  }
}
