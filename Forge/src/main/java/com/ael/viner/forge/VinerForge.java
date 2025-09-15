package com.ael.viner.forge;

import java.lang.invoke.MethodHandles;

import com.ael.viner.common.IPlayerRegistry;
import com.ael.viner.common.IVinerMod;
import com.ael.viner.common.VinerCore;
import com.ael.viner.common.config.IConfigManager;
import com.ael.viner.forge.config.Config;
import com.ael.viner.forge.config.ForgeConfigManager;
import com.ael.viner.forge.network.VinerPacketHandler;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import com.ael.viner.forge.registry.VinerPlayerRegistry;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * The main mod class for Viner mod (Forge-specific entrypoint). This class is the entry point for
 * all Forge-specific functionality.
 */
@Mod(VinerCore.MOD_ID)
public class VinerForge implements IVinerMod {

  // Keep this for backwards compatibility with existing code
  public static final String MOD_ID = VinerCore.MOD_ID;

  private final VinerPlayerRegistry vinerPlayerRegistry;
  private final ForgeConfigManager configManager;

  /**
   * Constructor for the VinerForge class. This is where we register Forge-specific functionality.
   */
  public VinerForge(FMLJavaModLoadingContext context) {
    // Set this instance as the global mod instance
    VinerCore.setInstance(this);

    // Create the player registry
    vinerPlayerRegistry = VinerPlayerRegistry.create();

    // Create the config manager
    configManager = new ForgeConfigManager();

    var modBusGroup = context.getModBusGroup();

    // Register the commonSetup method for modloading
    FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::onCommonSetup);

    // Register ourselves for server and other game events we are interested in
    BusGroup.DEFAULT.register(MethodHandles.lookup(), this);

    // Register the packet handler for networking
    VinerPacketHandler.register();

    // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
    context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  /**
   * Gets the Forge implementation instance of the mod.
   *
   * @return The VinerForge instance cast from VinerCore's instance
   */
  public static VinerForge getInstance() {
    if (VinerCore.get() instanceof VinerForge forge) {
      return forge;
    }
    return null;
  }

  @Override
  public IPlayerRegistry getPlayerRegistry() {
    return vinerPlayerRegistry;
  }

  @Override
  public IConfigManager getConfigManager() {
    return configManager;
  }

  private void onCommonSetup(final FMLCommonSetupEvent event) {
    VinerBlockRegistry.setup();
  }
}
