package com.ael.viner.forge.event;

import static com.ael.viner.forge.VinerForge.MOD_ID;

import com.ael.viner.forge.gui.ConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {
  public static KeyMapping VINE_KEY_BINDING;
  public static KeyMapping VINER_CONFIG_KEY_BINDING;
  private static boolean vineKeyPressed = false;

  @SubscribeEvent
  public static void onClientTick(TickEvent.ClientTickEvent event) {
    if (Minecraft.getInstance().player == null) {
      return;
    }
    if (VINE_KEY_BINDING.isDown() != vineKeyPressed) {
      vineKeyPressed = VINE_KEY_BINDING.isDown();
      com.ael.viner.forge.network.VinerPacketHandler.INSTANCE.sendToServer(
          new com.ael.viner.forge.network.packets.VinerKeyPressedPacket(vineKeyPressed));
    }
  }

  @SubscribeEvent
  public static void onClientTickConfig(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.screen == null && VINER_CONFIG_KEY_BINDING.isDown()) {
        mc.setScreen(new ConfigScreen());
      }
    }
  }

  @SubscribeEvent
  public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) return;
    double scrollDelta = event.getScrollDelta();
    if (scrollDelta != 0 && VINE_KEY_BINDING.isDown()) {
      // Placeholder for future scroll packet
    }
  }

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
