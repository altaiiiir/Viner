package com.ael.viner.registry;

import com.ael.viner.config.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registry class for managing Vineable blocks and tags for the Viner mod.
 */
public class VinerBlockRegistry {
    private static List<Block> vineableBlocks;
    private static List<Block> unvineableBlocks;
    private static List<TagKey<Block>> vineableTags;
    private static List<TagKey<Block>> unvineableTags;
    private static Double exhaustionPerBlock;
    private static Boolean vineAll;
    private static int vineableLimit;
    private static int heightAbove;
    private static int heightBelow;
    private static int widthLeft;
    private static int widthRight;
    private static int layerOffset;
    private static Boolean shapeVine;
    private static final Logger LOGGER = LogUtils.getLogger();

    // Setup method to initialize Vineable blocks and tags
    public static void setup() {
        vineableBlocks = initializeVineableBlocks();
        unvineableBlocks = initializeUnvineableBlocks();
        vineableTags = initializeVineableTags();
        unvineableTags = initializeUnvineableTags();
        exhaustionPerBlock = initializeExhaustionPerBlock();
        vineAll = initializeVineAll();
        vineableLimit = initializeVineableLimit();
        heightAbove = initializeHeightAbove();
        heightBelow = initializeHeightBelow();
        widthLeft = initializeWidthLeft();
        widthRight = initializeWidthRight();
        layerOffset = initializeLayerOffset();
        shapeVine = initializeShapeVine();
    }

    private static List<Block> initializeVineableBlocks() {
        return getBlocksFromConfigEntries((List<String>) Config.VINEABLE_BLOCKS.get());
    }
    private static List<Block> initializeUnvineableBlocks() {
        return getBlocksFromConfigEntries((List<String>) Config.UNVINEABLE_BLOCKS.get());
    }
    private static List<TagKey<Block>> initializeVineableTags() {
        return getTagsFromConfigEntries((List<String>) Config.VINEABLE_BLOCKS.get());
    }

    private static List<TagKey<Block>> initializeUnvineableTags() {
        return getTagsFromConfigEntries((List<String>) Config.UNVINEABLE_BLOCKS.get());
    }
    private static Double initializeExhaustionPerBlock() {
        return Config.EXHAUSTION_PER_BLOCK.get();
    }
    private static boolean initializeVineAll() { return Config.VINE_ALL.get(); }
    private static int initializeVineableLimit() { return Config.VINEABLE_LIMIT.get(); }
    private static int initializeHeightAbove() {
        return Config.HEIGHT_ABOVE.get();
    }
    private static int initializeHeightBelow() { return Config.HEIGHT_BELOW.get(); }
    private static int initializeWidthLeft() {
        return Config.WIDTH_LEFT.get();
    }
    private static int initializeWidthRight() {
        return Config.WIDTH_RIGHT.get();
    }

    private static int initializeLayerOffset() {
        return Config.LAYER_OFFSET.get();
    }
    private static boolean initializeShapeVine() { return Config.SHAPE_VINE.get(); }

    public static List<Block> getVineableBlocks() {
        if (vineableBlocks == null) {
            setup();
        }
        return vineableBlocks;
    }

    public static List<Block> getUnvineableBlocks() {
        if (unvineableBlocks == null) {
            setup();
        }
        return unvineableBlocks;
    }

    public static List<TagKey<Block>> getVineableTags() {
        if (vineableTags == null) {
            setup();
        }
        return vineableTags;
    }

    public static List<TagKey<Block>> getUnvineableTags() {
        if (unvineableTags == null) {
            setup();
        }
        return unvineableTags;
    }

    public static Double getExhaustionPerBlock() {
        if (exhaustionPerBlock < 0) {
            setup();
        }
        return exhaustionPerBlock;
    }

    public static Boolean isVineAll() {
        if (vineAll == null) {
            setup();
        }
        return vineAll;
    }

    public static int getVineableLimit() {
        if (vineableLimit < 0) {
            setup();
        }
        return vineableLimit;
    }

    public static int getHeightAbove() {
        if (heightAbove < 0) {
            setup();
        }
        return heightAbove;
    }

    public static int getHeightBelow() {
        if (heightBelow < 0) {
            setup();
        }
        return heightBelow;
    }

    public static int getWidthLeft() {
        if (widthLeft < 0) {
            setup();
        }
        return widthLeft;
    }

    public static int getWidthRight() {
        if (widthRight < 0) {
            setup();
        }
        return widthRight;
    }

    public static int getLayerOffset() {
        if (layerOffset < -5) {
            setup();
        }
        return layerOffset;
    }

    public static boolean isShapeVine() {
        if (shapeVine == null) {
            setup();
        }
        return shapeVine;
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
                tags.add(getTagKeyEntry(entry));
            }
        }
        return tags;
    }

    public static ResourceLocation getResourceLocationFromEntry(String entry) {
        String[] splitName = entry.startsWith("#") ? entry.substring(1).split(":") : entry.split(":");
        return new ResourceLocation(splitName[0], splitName[1]);
    }

    public static TagKey<Block> getTagKeyEntry(String entry) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).createTagKey(getResourceLocationFromEntry(entry));
    }
}
