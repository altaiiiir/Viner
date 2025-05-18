package com.ael.viner.forge.registry;

import com.ael.viner.forge.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Registry class for managing Vineable blocks and tags for the Viner mod. */
public class VinerBlockRegistry {
  private static final Logger LOGGER = LogManager.getLogger();

  public record VinerBlockConfig(
      List<Block> vineableBlocks,
      List<Block> unvineableBlocks,
      List<TagKey<Block>> vineableTags,
      List<TagKey<Block>> unvineableTags,
      double exhaustionPerBlock,
      boolean vineAll,
      int vineableLimit,
      int heightAbove,
      int heightBelow,
      int widthLeft,
      int widthRight,
      int layerOffset,
      boolean shapeVine) {}

  private static VinerBlockConfig config;

  public static void setup() {
    // Read values from config
    double exhaustion = Config.EXHAUSTION_PER_BLOCK.get();
    boolean vineAll = Config.VINE_ALL.get();
    int vineableLimit = Config.VINEABLE_LIMIT.get();
    int heightAbove = Config.HEIGHT_ABOVE.get();
    int heightBelow = Config.HEIGHT_BELOW.get();
    int widthLeft = Config.WIDTH_LEFT.get();
    int widthRight = Config.WIDTH_RIGHT.get();
    int layerOffset = Config.LAYER_OFFSET.get();
    boolean shapeVine = Config.SHAPE_VINE.get();

    LOGGER.info("VinerBlockRegistry setup - current config values:");
    LOGGER.info("  vineableLimit: " + vineableLimit);
    LOGGER.info("  exhaustionPerBlock: " + exhaustion);
    LOGGER.info("  vineAll: " + vineAll);
    LOGGER.info("  shapeVine: " + shapeVine);

    List<String> vineableBlockEntries =
        Config.VINEABLE_BLOCKS.get().stream().map(String::valueOf).toList();
    List<String> unvineableBlockEntries =
        Config.UNVINEABLE_BLOCKS.get().stream().map(String::valueOf).toList();
    config =
        new VinerBlockConfig(
            getBlocksFromConfigEntries(vineableBlockEntries),
            getBlocksFromConfigEntries(unvineableBlockEntries),
            getTagsFromConfigEntries(vineableBlockEntries),
            getTagsFromConfigEntries(unvineableBlockEntries),
            exhaustion,
            vineAll,
            vineableLimit,
            heightAbove,
            heightBelow,
            widthLeft,
            widthRight,
            layerOffset,
            shapeVine);

    LOGGER.info("VinerBlockRegistry setup complete");
  }

  private static VinerBlockConfig getConfig() {
    if (config == null) setup();
    return config;
  }

  public static List<Block> getVineableBlocks() {
    return getConfig().vineableBlocks();
  }

  public static List<Block> getUnvineableBlocks() {
    return getConfig().unvineableBlocks();
  }

  public static List<TagKey<Block>> getVineableTags() {
    return getConfig().vineableTags();
  }

  public static List<TagKey<Block>> getUnvineableTags() {
    return getConfig().unvineableTags();
  }

  public static double getExhaustionPerBlock() {
    return getConfig().exhaustionPerBlock();
  }

  public static boolean isVineAll() {
    return getConfig().vineAll();
  }

  public static int getVineableLimit() {
    return getConfig().vineableLimit();
  }

  public static int getHeightAbove() {
    return getConfig().heightAbove();
  }

  public static int getHeightBelow() {
    return getConfig().heightBelow();
  }

  public static int getWidthLeft() {
    return getConfig().widthLeft();
  }

  public static int getWidthRight() {
    return getConfig().widthRight();
  }

  public static int getLayerOffset() {
    return getConfig().layerOffset();
  }

  public static boolean isShapeVine() {
    return getConfig().shapeVine();
  }

  public static List<Block> getBlocksFromConfigEntries(List<String> entries) {
    List<Block> blocks = new ArrayList<>();
    for (String entry : entries) {
      if (!entry.startsWith("#")) {
        blocks.add(ForgeRegistries.BLOCKS.getValue(getResourceLocationFromEntry(entry)));
      }
    }
    return blocks;
  }

  public static List<TagKey<Block>> getTagsFromConfigEntries(List<String> entries) {
    List<TagKey<Block>> tags = new ArrayList<>();
    for (String entry : entries) {
      if (entry.startsWith("#")) {
        TagKey<Block> tagKey = getTagKeyEntry(entry);
        tags.add(tagKey);
      }
    }
    return tags;
  }

  public static ResourceLocation getResourceLocationFromEntry(String entry) {
    String[] splitName = entry.startsWith("#") ? entry.substring(1).split(":") : entry.split(":");
    return new ResourceLocation(splitName[0], splitName[1]);
  }

  public static TagKey<Block> getTagKeyEntry(String entry) {
    return Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
        .createTagKey(getResourceLocationFromEntry(entry));
  }
}
