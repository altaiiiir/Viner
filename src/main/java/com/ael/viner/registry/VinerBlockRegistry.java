package com.ael.viner.registry;

import com.ael.viner.config.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registry class for managing Vineable blocks and tags for the Viner mod.
 */
public class VinerBlockRegistry {

    // Lists to store Vineable blocks and tags
    public static List<Block> VINEABLE_BLOCKS;
    public static List<Block> UNVINEABLE_BLOCKS;
    public static List<TagKey<Block>> VINEABLE_TAGS = new ArrayList<>();

    // Logger instance for logging
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Setup method to initialize Vineable blocks and tags.
     */
    public static void setup(){
        VINEABLE_BLOCKS = new ArrayList<>(getVineableBlocks());
        UNVINEABLE_BLOCKS = new ArrayList<>(getUnvineableBlocks());
        VINEABLE_TAGS = new ArrayList<>(getVineableTags());
    }

    /**
     * Method to get Vineable blocks from the config.
     *
     * @return List of Vineable blocks.
     */
    public static @NotNull List<Block> getVineableBlocks() {
        List<Block> blocks = new ArrayList<>();
        for(String entry : Config.VINEABLE_BLOCKS.get()) {  // Iterating through each entry in the config
            if (!entry.startsWith("#")) {  // Check if the entry is not a tag
                // Add the block to the list
                blocks.add(ForgeRegistries.BLOCKS.getValue(getResourceLocationFromEntry(entry)));
            }
        }
        return blocks;  // Return the list of Vineable blocks
    }

    /**
     * Method to get Unvineable blocks from the config.
     *
     * @return List of Unvineable blocks.
     */
    public static @NotNull List<Block> getUnvineableBlocks() {
        List<Block> blocks = new ArrayList<>();
        for(String entry : Config.UNVINEABLE_BLOCKS.get()) {  // Iterating through each entry in the config
            if (!entry.startsWith("#")) {  // Check if the entry is not a tag
                // Add the block to the list
                blocks.add(ForgeRegistries.BLOCKS.getValue(getResourceLocationFromEntry(entry)));
            }
        }
        return blocks;  // Return the list of Unvineable blocks
    }

    /**
     * Helper method to get ResourceLocation from an entry string.
     *
     * @param entry Entry string.
     * @return ResourceLocation object.
     */
    public static @NotNull ResourceLocation getResourceLocationFromEntry(String entry){
        String[] splitName = entry.startsWith("#")
                ? entry.substring(1).split(":")
                : entry.split(":") ;  // Splitting the entry string
        return new ResourceLocation(splitName[0], splitName[1]);  // Returning the ResourceLocation object
    }

    /**
     * Helper method to get TagKey from an entry string.
     *
     * @param entry Entry string.
     * @return TagKey object.
     */
    public static @NotNull TagKey<Block> getTagKeyEntry(String entry){
        // Creating and returning the TagKey object
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).createTagKey(getResourceLocationFromEntry(entry));
    }

    /**
     * Method to get Vineable tags from the config.
     *
     * @return List of Vineable tags.
     */
    public static @NotNull List<TagKey<Block>> getVineableTags() {
        List<TagKey<Block>> blockTagKeys = new ArrayList<>();
        for(String entry : Config.VINEABLE_BLOCKS.get()) {  // Iterating through each entry in the config
            if (entry.startsWith("#")) {  // Check if the entry is a tag
                blockTagKeys.add(getTagKeyEntry(entry));  // Add the tag to the list
            }
        }
        return blockTagKeys;  // Return the list of Vineable tags
    }

    /**
     * Method to get the Vineable limit from the config.
     *
     * @return Vineable limit value.
     */
    public static int getVeinableLimit(){
        return Config.VINEABLE_LIMIT.get();
    }
}

