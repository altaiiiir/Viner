package com.ael.viner;

import com.ael.viner.common.CommonSetup;
import com.ael.viner.config.Config;
import com.ael.viner.network.VinerPacketHandler;
import com.ael.viner.registry.VinerPlayerRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * The main mod class for Viner mod.
 * This class is the entry point for all mod-specific functionality.
 */
@Mod(Viner.MOD_ID)  // The value here should match an entry in the META-INF/mods.toml file
public class Viner {

    private static Viner instance;
    private final VinerPlayerRegistry vinerPlayerRegistry;

    public static final String MOD_ID = "viner";

    /**
     * Constructor for the Viner class.
     * This is where we register mod-specific functionality.
     */
    public Viner(FMLJavaModLoadingContext context) {

        instance = this;
        vinerPlayerRegistry = VinerPlayerRegistry.create();

        IEventBus modEventBus = context.getModEventBus();

        // Register the CommonSetup method for mod loading
        modEventBus.addListener(CommonSetup::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the packet handler for networking
        VinerPacketHandler.register();

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // I'm not a fan of using a singleton here, but not sure how else to handle this
    public static Viner getInstance(){
        return instance;
    }

    public VinerPlayerRegistry getPlayerRegistry(){
        return vinerPlayerRegistry;
    }
}
