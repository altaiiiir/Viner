package com.ael.viner.forge;

import com.ael.viner.common.IVinerMod;
import com.ael.viner.common.VinerEntrypoint;
import com.ael.viner.forge.config.Config;
import com.ael.viner.forge.network.VinerPacketHandler;
import com.ael.viner.forge.registry.VinerPlayerRegistry;
import com.ael.viner.forge.setup.CommonSetup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * The main mod class for Viner mod (Forge-specific entrypoint). This class is the entry point for
 * all Forge-specific functionality.
 */
@Mod(VinerForge.MOD_ID) // The value here should match an entry in the META-INF/mods.toml file
public class VinerForge implements IVinerMod {

  private static VinerForge instance;
  private final VinerPlayerRegistry vinerPlayerRegistry;

  public static final String MOD_ID = "viner";

  /**
   * Constructor for the VinerForge class. This is where we register Forge-specific functionality.
   */
  public VinerForge() {
    VinerEntrypoint.setInstance(this);
    instance = this;
    vinerPlayerRegistry = VinerPlayerRegistry.create();

    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    // Register the CommonSetup method for mod loading
    modEventBus.addListener(CommonSetup::setup);

    // Register ourselves for server and other game events we are interested in
    MinecraftForge.EVENT_BUS.register(this);

    // Register the packet handler for networking
    VinerPacketHandler.register();

    // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  // I'm not a fan of using a singleton here, but not sure how else to handle this
  public static VinerForge getInstance() {
    return instance;
  }

  @Override
  public VinerPlayerRegistry getPlayerRegistry() {
    return vinerPlayerRegistry;
  }
}
