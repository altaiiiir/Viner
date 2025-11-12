package com.ael.viner.forge.player;

import com.ael.viner.common.IPlayerData;
import com.ael.viner.forge.registry.VinerBlockRegistry;
import java.util.List;
import java.util.UUID;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public record VinerPlayerData(
    List<Block> vineableBlocks,
    List<Block> unvineableBlocks,
    List<TagKey<Block>> vineableTags,
    List<TagKey<Block>> unvineableTags,
    boolean vineKeyPressed,
    boolean vineAllEnabled,
    double exhaustionPerBlock,
    int vineableLimit,
    int heightAbove,
    int heightBelow,
    int widthLeft,
    int widthRight,
    int layerOffset,
    boolean isShapeVine)
    implements IPlayerData {

  public VinerPlayerData(UUID playerId) {
    this(
        VinerBlockRegistry.getVineableBlocks(),
        VinerBlockRegistry.getUnvineableBlocks(),
        VinerBlockRegistry.getVineableTags(),
        VinerBlockRegistry.getUnvineableTags(),
        false,
        VinerBlockRegistry.isVineAll(),
        VinerBlockRegistry.getExhaustionPerBlock(),
        VinerBlockRegistry.getVineableLimit(),
        VinerBlockRegistry.getHeightAbove(),
        VinerBlockRegistry.getHeightBelow(),
        VinerBlockRegistry.getWidthLeft(),
        VinerBlockRegistry.getWidthRight(),
        VinerBlockRegistry.getLayerOffset(),
        VinerBlockRegistry.isShapeVine());
  }

  @Override
  public boolean isVineKeyPressed() {
    return vineKeyPressed;
  }

  @Override
  public boolean isVineAllEnabled() {
    return vineAllEnabled;
  }

  @Override
  public double getExhaustionPerBlock() {
    return exhaustionPerBlock;
  }

  @Override
  public int getVineableLimit() {
    return vineableLimit;
  }

  @Override
  public int getHeightAbove() {
    return heightAbove;
  }

  @Override
  public int getHeightBelow() {
    return heightBelow;
  }

  @Override
  public int getWidthLeft() {
    return widthLeft;
  }

  @Override
  public int getWidthRight() {
    return widthRight;
  }

  @Override
  public int getLayerOffset() {
    return layerOffset;
  }

  @Override
  public boolean isShapeVine() {
    return isShapeVine;
  }

  public List<String> getVineableBlockIds() {
    return vineableBlocks.stream()
        .map(
            block ->
                block != null
                        && net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block)
                            != null
                    ? net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block).toString()
                    : "")
        .toList();
  }

  public List<String> getUnvineableBlockIds() {
    return unvineableBlocks.stream()
        .map(
            block ->
                block != null
                        && net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block)
                            != null
                    ? net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block).toString()
                    : "")
        .toList();
  }

  public List<String> getVineableTagIds() {
    return vineableTags.stream().map(tag -> tag != null ? tag.location().toString() : "").toList();
  }

  public List<String> getUnvineableTagIds() {
    return unvineableTags.stream()
        .map(tag -> tag != null ? tag.location().toString() : "")
        .toList();
  }

  public static VinerPlayerData fromConfig() {
    return new VinerPlayerData(
        com.ael.viner.forge.registry.VinerBlockRegistry.getVineableBlocks(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getUnvineableBlocks(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getVineableTags(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getUnvineableTags(),
        false, // vineKeyPressed default
        com.ael.viner.forge.registry.VinerBlockRegistry.isVineAll(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getExhaustionPerBlock(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getVineableLimit(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getHeightAbove(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getHeightBelow(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getWidthLeft(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getWidthRight(),
        com.ael.viner.forge.registry.VinerBlockRegistry.getLayerOffset(),
        com.ael.viner.forge.registry.VinerBlockRegistry.isShapeVine());
  }

  public static class VinerPlayerDataBuilder {
    private List<Block> vineableBlocks;
    private List<Block> unvineableBlocks;
    private List<TagKey<Block>> vineableTags;
    private List<TagKey<Block>> unvineableTags;
    private boolean vineKeyPressed;
    private boolean vineAllEnabled;
    private double exhaustionPerBlock;
    private int vineableLimit;
    private int heightAbove;
    private int heightBelow;
    private int widthLeft;
    private int widthRight;
    private int layerOffset;
    private boolean isShapeVine;

    public VinerPlayerDataBuilder(VinerPlayerData data) {
      this.vineableBlocks = data.vineableBlocks();
      this.unvineableBlocks = data.unvineableBlocks();
      this.vineableTags = data.vineableTags();
      this.unvineableTags = data.unvineableTags();
      this.vineKeyPressed = data.vineKeyPressed();
      this.vineAllEnabled = data.vineAllEnabled();
      this.exhaustionPerBlock = data.exhaustionPerBlock();
      this.vineableLimit = data.vineableLimit();
      this.heightAbove = data.heightAbove();
      this.heightBelow = data.heightBelow();
      this.widthLeft = data.widthLeft();
      this.widthRight = data.widthRight();
      this.layerOffset = data.layerOffset();
      this.isShapeVine = data.isShapeVine();
    }

    public VinerPlayerDataBuilder vineableBlocks(List<Block> value) {
      this.vineableBlocks = value;
      return this;
    }

    public VinerPlayerDataBuilder unvineableBlocks(List<Block> value) {
      this.unvineableBlocks = value;
      return this;
    }

    public VinerPlayerDataBuilder vineableTags(List<TagKey<Block>> value) {
      this.vineableTags = value;
      return this;
    }

    public VinerPlayerDataBuilder unvineableTags(List<TagKey<Block>> value) {
      this.unvineableTags = value;
      return this;
    }

    public VinerPlayerDataBuilder vineKeyPressed(boolean value) {
      this.vineKeyPressed = value;
      return this;
    }

    public VinerPlayerDataBuilder vineAllEnabled(boolean value) {
      this.vineAllEnabled = value;
      return this;
    }

    public VinerPlayerDataBuilder exhaustionPerBlock(double value) {
      this.exhaustionPerBlock = value;
      return this;
    }

    public VinerPlayerDataBuilder vineableLimit(int value) {
      this.vineableLimit = value;
      return this;
    }

    public VinerPlayerDataBuilder heightAbove(int value) {
      this.heightAbove = value;
      return this;
    }

    public VinerPlayerDataBuilder heightBelow(int value) {
      this.heightBelow = value;
      return this;
    }

    public VinerPlayerDataBuilder widthLeft(int value) {
      this.widthLeft = value;
      return this;
    }

    public VinerPlayerDataBuilder widthRight(int value) {
      this.widthRight = value;
      return this;
    }

    public VinerPlayerDataBuilder layerOffset(int value) {
      this.layerOffset = value;
      return this;
    }

    public VinerPlayerDataBuilder isShapeVine(boolean value) {
      this.isShapeVine = value;
      return this;
    }

    public VinerPlayerData build() {
      return new VinerPlayerData(
          vineableBlocks,
          unvineableBlocks,
          vineableTags,
          unvineableTags,
          vineKeyPressed,
          vineAllEnabled,
          exhaustionPerBlock,
          vineableLimit,
          heightAbove,
          heightBelow,
          widthLeft,
          widthRight,
          layerOffset,
          isShapeVine);
    }
  }
}
