package com.ael.viner.forge.gui;

import static com.ael.viner.forge.network.packets.ConfigSyncPacket.syncConfigWithServer;

import com.ael.viner.common.gui.ConfigScreen;
import com.ael.viner.forge.config.ForgeConfigManager;
import com.ael.viner.forge.network.packets.ConfigSyncPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Forge-specific wrapper for the shared ConfigScreen. Handles Forge-specific networking and config
 * management.
 */
@OnlyIn(Dist.CLIENT)
public class ForgeConfigScreen extends ConfigScreen {

  public ForgeConfigScreen() {
    super(
        new ForgeConfigManager(),
        configType -> {
          // Handle Forge-specific network synchronization
          switch (configType) {
            case "vineableBlocks" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.BLOCK_LIST,
                    new ForgeConfigManager().getVineableBlocks(),
                    "vineableBlocks");
            case "unvineableBlocks" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.BLOCK_LIST,
                    new ForgeConfigManager().getUnvineableBlocks(),
                    "unvineableBlocks");
            default -> {
              // For other config types, just trigger a general sync
              // This could be enhanced to handle specific sync types
            }
          }
        });
  }
}
