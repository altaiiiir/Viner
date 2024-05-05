package com.ael.viner.client;

import com.ael.viner.network.VinerPacketHandler;
import com.ael.viner.network.packets.VinerKeyPressedPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static com.ael.viner.Viner.MOD_ID;


/**
 * This class handles the vein mining feature when a block is broken.
 */
//@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ToolBelt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
//
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientModEvents {
    public static KeyMapping VINE_KEY_BINDING;
    public static KeyMapping VINER_CONFIG_KEY_BINDING;
    private static boolean vineKeyPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){

        if (Minecraft.getInstance().player == null) {
            return;
        }

        if (VINE_KEY_BINDING.isDown() != vineKeyPressed){
            vineKeyPressed = VINE_KEY_BINDING.isDown();
            VinerKeyPressedPacket packet = new VinerKeyPressedPacket(vineKeyPressed);
            VinerPacketHandler.INSTANCE.sendToServer(packet);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents{
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event){
            event.register(VINE_KEY_BINDING = new KeyMapping(
                    "key.vine",
                    GLFW.GLFW_KEY_LEFT_SHIFT,
                    "key.categories.ael.viner"
            ));

            event.register(VINER_CONFIG_KEY_BINDING = new KeyMapping(
                    "key.vine.config",
                    GLFW.GLFW_KEY_RIGHT_SHIFT,
                    "key.categories.ael.viner"
            ));
        }
    }



}
