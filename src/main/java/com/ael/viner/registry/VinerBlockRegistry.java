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

public class VinerBlockRegistry {

    // Lists to store veinmineable blocks and tags
    public static List<Block> VINEABLE_BLOCKS;
    public static List<Block> UNVINEABLE_BLOCKS;
    public static List<TagKey<Block>> VINEABLE_TAGS = new ArrayList<>();

    // Logger instance for logging
    private static final Logger LOGGER = LogUtils.getLogger();

    // Setup method to initialize veinmineable blocks and tags
    public static void setup(){
        VINEABLE_BLOCKS = new ArrayList<>(getVineableBlocks());
        UNVINEABLE_BLOCKS = new ArrayList<>(getUnvineableBlocks());
        VINEABLE_TAGS = new ArrayList<>(getVineableTags());
    }

    // Method to get veinmineable blocks from the config
    public static List<Block> getVineableBlocks() {
        List<Block> blocks = new ArrayList<>();
        for(String entry : Config.VINEABLE_BLOCKS.get()) {  // Iterating through each entry in the config
            if (!entry.startsWith("#")) {  // Check if the entry is not a tag
                blocks.add(ForgeRegistries.BLOCKS.getValue(getResourceLocationFromEntry(entry)));  // Add the block to the list
            }
        }
        return blocks;  // Return the list of veinmineable blocks
    }

    public static List<Block> getUnvineableBlocks() {
        List<Block> blocks = new ArrayList<>();
        for(String entry : Config.UNVINEABLE_BLOCKS.get()) {  // Iterating through each entry in the config
            if (!entry.startsWith("#")) {  // Check if the entry is not a tag
                blocks.add(ForgeRegistries.BLOCKS.getValue(getResourceLocationFromEntry(entry)));  // Add the block to the list
            }
        }
        return blocks;  // Return the list of veinmineable blocks
    }

    // Helper method to get ResourceLocation from an entry string
    public static ResourceLocation getResourceLocationFromEntry(String entry){
        String[] splitName = entry.startsWith("#") ? entry.substring(1).split(":") : entry.split(":") ;  // Splitting the entry string
        return new ResourceLocation(splitName[0], splitName[1]);  // Returning the ResourceLocation object
    }

    // Helper method to get TagKey from an entry string
    public static TagKey<Block> getTagKeyEntry(String entry){
        return ForgeRegistries.BLOCKS.tags().createTagKey(getResourceLocationFromEntry(entry));  // Creating and returning the TagKey object
    }

    // Method to get veinmineable tags from the config
    public static List<TagKey<Block>> getVineableTags() {
        List<TagKey<Block>> blockTagKeys = new ArrayList<>();
        for(String entry : Config.VINEABLE_BLOCKS.get()) {  // Iterating through each entry in the config
            if (entry.startsWith("#")) {  // Check if the entry is a tag
                blockTagKeys.add(getTagKeyEntry(entry));  // Add the tag to the list
            }
        }
        return blockTagKeys;  // Return the list of veinmineable tags
    }

    // Method to get the veinmineable limit from the config
    public static int getVeinableLimit(){
        return Config.VINEABLE_LIMIT.get();
    }
}
