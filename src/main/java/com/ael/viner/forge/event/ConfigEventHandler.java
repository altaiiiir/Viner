package com.ael.viner.forge.event;

import com.ael.viner.forge.VinerForge;
import com.ael.viner.forge.registry.VinerBlockRegistry;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Objects;

/**
 * Handles configuration events for the Viner mod, ensuring the block registry is updated
 * when the mod configuration is reloaded.
 */
@Mod.EventBusSubscriber(modid = VinerForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigEventHandler {

    /**
     * Responds to mod configuration reloading events by updating the block registry
     * if the event is related to the Viner mod.
     *
     * @param event The event triggered when the mod configuration is reloaded.
     */
    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent.Reloading event) {
        final ModConfig config = event.getConfig();
        // Check if the event is related to the Viner mod
        if (Objects.equals(config.getModId(), VinerForge.MOD_ID)){
            // Update the block registry to reflect the new configuration
            VinerBlockRegistry.setup();
        }
    }
} 