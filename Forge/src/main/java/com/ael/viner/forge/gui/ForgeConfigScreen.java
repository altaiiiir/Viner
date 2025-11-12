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
            case "vineAll" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.BOOLEAN,
                    new ForgeConfigManager().getVineAll(),
                    "vineAll");
            case "shapeVine" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.BOOLEAN,
                    new ForgeConfigManager().getShapeVine(),
                    "shapeVine");
            case "vineableLimit" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.INT,
                    new ForgeConfigManager().getVineableLimit(),
                    "vineableLimit");
            case "exhaustionPerBlock" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.DOUBLE,
                    new ForgeConfigManager().getExhaustionPerBlock(),
                    "exhaustionPerBlock");
            case "heightAbove" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.INT,
                    new ForgeConfigManager().getHeightAbove(),
                    "heightAbove");
            case "heightBelow" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.INT,
                    new ForgeConfigManager().getHeightBelow(),
                    "heightBelow");
            case "widthLeft" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.INT,
                    new ForgeConfigManager().getWidthLeft(),
                    "widthLeft");
            case "widthRight" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.INT,
                    new ForgeConfigManager().getWidthRight(),
                    "widthRight");
            case "layerOffset" ->
                syncConfigWithServer(
                    ConfigSyncPacket.ConfigType.INT,
                    new ForgeConfigManager().getLayerOffset(),
                    "layerOffset");
            default -> {
              // Unknown config type - log for debugging
              System.out.println("Unknown config type: " + configType);
            }
          }
        });
  }
}
