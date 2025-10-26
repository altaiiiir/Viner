package com.ael.viner.forge.event;

import static com.ael.viner.forge.VinerForge.MOD_ID;

import com.ael.viner.forge.VinerForge;
import com.ael.viner.forge.gui.ForgeConfigScreen;
import com.ael.viner.forge.network.VinerPacketHandler;
import com.ael.viner.forge.network.packets.VinerKeyPressedPacket;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Handles client-side events for the Viner mod, specifically MOD bus events that occur during mod
 * loading and initialization.
 */
@Mod.EventBusSubscriber(
    modid = VinerForge.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public class ClientEventHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  public static KeyMapping VINE_KEY_BINDING;
  public static KeyMapping VINER_CONFIG_KEY_BINDING;

  /**
   * Responds to client config changes
   *
   * @param event The config event
   */
  @SubscribeEvent
  public static void onModConfigEvent(final ModConfigEvent event) {
    LOGGER.info("[CLIENT] Viner config event: " + event.getClass().getSimpleName());

    if (event.getConfig().getModId().equals(VinerForge.MOD_ID)) {
      LOGGER.info("[CLIENT] Viner config changed: " + event.getConfig().getFileName());

      // For client-side, we need to update the block registry as well
      if (event instanceof ModConfigEvent.Reloading) {
        LOGGER.info("[CLIENT] Reloading viner block registry");
        VinerBlockRegistry.setup();
      }
    }
  }

  /** Handles registration of key bindings */
  @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class ModBusEvents {
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
      event.register(
          VINE_KEY_BINDING =
              new KeyMapping("key.vine", GLFW.GLFW_KEY_LEFT_SHIFT, "key.categories.ael.viner"));
      event.register(
          VINER_CONFIG_KEY_BINDING =
              new KeyMapping(
                  "key.vine.config", GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.ael.viner"));
    }
  }
}

/**
 * Handles gameplay events that happen during normal play. These must be registered on the FORGE
 * event bus, not the MOD bus.
 */
@Mod.EventBusSubscriber(modid = VinerForge.MOD_ID, value = Dist.CLIENT)
class ClientGameplayEventHandler {
  private static final Logger LOGGER = LogManager.getLogger();
  private static boolean vineKeyPressed = false;

  @SubscribeEvent
  public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
    if (Minecraft.getInstance().player == null) {
      return;
    }
    if (ClientEventHandler.VINE_KEY_BINDING.isDown() != vineKeyPressed) {
      vineKeyPressed = ClientEventHandler.VINE_KEY_BINDING.isDown();
      VinerPacketHandler.sendToServer(new VinerKeyPressedPacket(vineKeyPressed));
    }
  }

  @SubscribeEvent
  public static void onClientTickConfig(TickEvent.ClientTickEvent.Post event) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.screen == null && ClientEventHandler.VINER_CONFIG_KEY_BINDING.isDown()) {
      mc.setScreen(new ForgeConfigScreen());
    }
  }

  @SubscribeEvent
  public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    double scrollDelta = event.getDeltaY();
    if (scrollDelta != 0 && ClientEventHandler.VINE_KEY_BINDING.isDown()) {
      // Placeholder for future scroll packet
    }
  }

  /** When a player joins, log that client config is active */
  @SubscribeEvent
  public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
    LOGGER.info(
        "[CLIENT] Player logged in, checking config: vineableLimit="
            + VinerBlockRegistry.getVineableLimit());
  }
}
