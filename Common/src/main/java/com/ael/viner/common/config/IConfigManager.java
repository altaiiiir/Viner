package com.ael.viner.common.config;

import java.util.List;

/**
 * Platform-agnostic interface for managing mod configuration. Different mod loaders (Forge, Fabric,
 * NeoForge) can provide their own implementations.
 */
public interface IConfigManager {

  // Boolean configuration getters
  boolean getVineAll();

  boolean getShapeVine();

  // Numeric configuration getters
  int getVineableLimit();

  double getExhaustionPerBlock();

  int getHeightAbove();

  int getHeightBelow();

  int getWidthLeft();

  int getWidthRight();

  int getLayerOffset();

  // List configuration getters
  List<String> getVineableBlocks();

  List<String> getUnvineableBlocks();

  // Boolean configuration setters
  void setVineAll(boolean value);

  void setShapeVine(boolean value);

  // Numeric configuration setters
  void setVineableLimit(int value);

  void setExhaustionPerBlock(double value);

  void setHeightAbove(int value);

  void setHeightBelow(int value);

  void setWidthLeft(int value);

  void setWidthRight(int value);

  void setLayerOffset(int value);

  // List configuration setters
  void setVineableBlocks(List<String> blocks);

  void setUnvineableBlocks(List<String> blocks);

  // Save configuration changes
  void save();
}
