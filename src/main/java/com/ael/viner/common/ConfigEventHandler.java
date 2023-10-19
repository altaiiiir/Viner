package com.ael.viner.common;

import com.ael.viner.Viner;
import com.ael.viner.registry.VinerBlockRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Viner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigEventHandler {
    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent.Reloading event) {
        final ModConfig config = event.getConfig();
        if (Objects.equals(config.getModId(), Viner.MOD_ID)){
            // The config has been updated, so we need to update the registry
            VinerBlockRegistry.setup();
        }
    }
}
