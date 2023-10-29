package com.ael.viner.common;

import com.ael.viner.registry.VinerBlockRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonSetup {


    public static void setup(final FMLCommonSetupEvent event) {

        // Set up the block registry
        VinerBlockRegistry.setup();
    }

}
