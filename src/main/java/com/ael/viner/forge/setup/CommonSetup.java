package com.ael.viner.forge.setup;

import com.ael.viner.forge.registry.VinerBlockRegistry;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonSetup {

    public static void setup(final FMLCommonSetupEvent event) {
        // Set up the block registry
        VinerBlockRegistry.setup();
    }

} 