package com.ael.viner;

import com.ael.viner.client.ClientSetup;
import com.ael.viner.common.CommonSetup;
import com.ael.viner.config.Config;
import com.ael.viner.network.VinerPacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * The main mod class for Viner mod.
 * This class is the entry point for all mod-specific functionality.
 */
@Mod(Viner.MOD_ID)  // The value here should match an entry in the META-INF/mods.toml file
public class Viner {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "viner";

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Constructor for the Viner class.
     * This is where we register mod-specific functionality.
     */
    public Viner() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the ClientSetup method for mod loading
        modEventBus.addListener(ClientSetup::setup);

        // Register the CommonSetup method for mod loading
        modEventBus.addListener(CommonSetup::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the packet handler for networking
        VinerPacketHandler.register();

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
