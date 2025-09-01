package com.ael.viner.forge.config;

import com.ael.viner.common.config.IConfigManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Forge-specific implementation of the config manager interface. Bridges the platform-agnostic
 * interface with Forge's ForgeConfigSpec system.
 */
public class ForgeConfigManager implements IConfigManager {

  @Override
  public boolean getVineAll() {
    return Config.VINE_ALL.get();
  }

  @Override
  public boolean getShapeVine() {
    return Config.SHAPE_VINE.get();
  }

  @Override
  public int getVineableLimit() {
    return Config.VINEABLE_LIMIT.get();
  }

  @Override
  public double getExhaustionPerBlock() {
    return Config.EXHAUSTION_PER_BLOCK.get();
  }

  @Override
  public int getHeightAbove() {
    return Config.HEIGHT_ABOVE.get();
  }

  @Override
  public int getHeightBelow() {
    return Config.HEIGHT_BELOW.get();
  }

  @Override
  public int getWidthLeft() {
    return Config.WIDTH_LEFT.get();
  }

  @Override
  public int getWidthRight() {
    return Config.WIDTH_RIGHT.get();
  }

  @Override
  public int getLayerOffset() {
    return Config.LAYER_OFFSET.get();
  }

  @Override
  public List<String> getVineableBlocks() {
    return new ArrayList<>(Config.VINEABLE_BLOCKS.get());
  }

  @Override
  public List<String> getUnvineableBlocks() {
    return new ArrayList<>(Config.UNVINEABLE_BLOCKS.get());
  }

  @Override
  public void setVineAll(boolean value) {
    Config.VINE_ALL.set(value);
  }

  @Override
  public void setShapeVine(boolean value) {
    Config.SHAPE_VINE.set(value);
  }

  @Override
  public void setVineableLimit(int value) {
    Config.VINEABLE_LIMIT.set(value);
  }

  @Override
  public void setExhaustionPerBlock(double value) {
    Config.EXHAUSTION_PER_BLOCK.set(value);
  }

  @Override
  public void setHeightAbove(int value) {
    Config.HEIGHT_ABOVE.set(value);
  }

  @Override
  public void setHeightBelow(int value) {
    Config.HEIGHT_BELOW.set(value);
  }

  @Override
  public void setWidthLeft(int value) {
    Config.WIDTH_LEFT.set(value);
  }

  @Override
  public void setWidthRight(int value) {
    Config.WIDTH_RIGHT.set(value);
  }

  @Override
  public void setLayerOffset(int value) {
    Config.LAYER_OFFSET.set(value);
  }

  @Override
  public void setVineableBlocks(List<String> blocks) {
    Config.VINEABLE_BLOCKS.set(blocks);
  }

  @Override
  public void setUnvineableBlocks(List<String> blocks) {
    Config.UNVINEABLE_BLOCKS.set(blocks);
  }

  @Override
  public void save() {
    // ForgeConfigSpec automatically saves when values are set
    // No explicit save needed for Forge
  }
}
