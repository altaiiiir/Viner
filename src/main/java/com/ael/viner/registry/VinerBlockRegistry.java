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
    private static Boolean vineAll;

    // Logger instance for logging
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Setup method to initialize Vineable blocks and tags.
     */
    public static void setup() {
        vineableBlocks = initializeVineableBlocks();
        unvineableBlocks = initializeUnvineableBlocks();
        vineableTags = initializeVineableTags();
        vineAll = initializeVineAll();
    }

    private static boolean initializeVineAll(){
        return Config.VINE_ALL.get();
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

    public static Boolean getVineAll() {
        if (vineAll == null) {
            setup();
        }
        return vineAll;
    }

    private static List<Block> getBlocksFromConfigEntries(List<String> entries) {
        List<Block> blocks = new ArrayList<>();
        for (String entry : entries) {
            if (!entry.startsWith("#")) {
                blocks.add(ForgeRegistries.BLOCKS.getValue(getResourceLocationFromEntry(entry)));
            }
        }
        return blocks;
    }

    private static List<TagKey<Block>> getTagsFromConfigEntries(List<String> entries) {
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

    public static int getVeinableLimit() {
        return Config.VINEABLE_LIMIT.get();
    }
}
