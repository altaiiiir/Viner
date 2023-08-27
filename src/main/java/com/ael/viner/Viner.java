package com.ael.viner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Viner.MOD_ID)
public class Viner {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "viner";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // Create a map to store blockName to Block mappings
    static Map<String, Block> blockMap = new HashMap<>();

    // create vineable blocks set from configurable json
    static Set<Block> VINEABLE_BLOCKS = new HashSet<>();

    private static final Map<String, TagKey<Block>> TAG_MAP = new HashMap<>();

    private static final KeyMapping SHIFT_KEY_BINDING = new KeyMapping("key.shift", GLFW.GLFW_KEY_LEFT_SHIFT, "key.categories.gameplay");

    static {
        try {
            // Iterate over all fields in the BlockTags class
            for (Field field : BlockTags.class.getDeclaredFields()) {
                // Check if the field is of type TagKey<Block>
                if (field.getType() == TagKey.class) {
                    TagKey<Block> tagKey = (TagKey<Block>) field.get(null); // null because it's a static field
                    TAG_MAP.put(tagKey.getClass().getName(), tagKey);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // default config for vineable blocks
    private static final String DEFAULT_CONFIG_VINEABLE = """
            {
              "vineable_limit":5,
              "vineable_blocks": [
                "minecraft:oak_log",
                "minecraft:spruce_log",
                "minecraft:birch_log",
                "minecraft:jungle_log",
                "minecraft:acacia_log",
                "minecraft:dark_oak_log",
                "minecraft:crimson_stem",
                "minecraft:warped_stem",
                "minecraft:stripped_oak_log",
                "minecraft:stripped_spruce_log",
                "minecraft:stripped_birch_log",
                "minecraft:stripped_jungle_log",
                "minecraft:stripped_acacia_log",
                "minecraft:stripped_dark_oak_log",
                "minecraft:stripped_crimson_stem",
                "minecraft:stripped_warped_stem",
                "minecraft:iron_ore",
                "minecraft:gold_ore",
                "minecraft:diamond_ore",
                "minecraft:emerald_ore",
                "minecraft:lapis_ore",
                "minecraft:redstone_ore",
                "minecraft:nether_quartz_ore",
                "minecraft:ancient_debris",
                "minecraft:coal_ore",
                "minecraft:copper_ore",
                "minecraft:tin_ore",
                "minecraft:lead_ore",
                "minecraft:aluminum_ore",
                "minecraft:amethyst_block",
                "minecraft:deepslate_iron_ore",
                "minecraft:deepslate_gold_ore",
                "minecraft:deepslate_diamond_ore",
                "minecraft:deepslate_emerald_ore",
                "minecraft:deepslate_lapis_ore",
                "minecraft:deepslate_redstone_ore",
                "minecraft:deepslate_copper_ore",
                "minecraft:deepslate_tin_ore",
                "minecraft:deepslate_lead_ore",
                "minecraft:deepslate_aluminum_ore",
                "minecraft:deepslate_coal_ore",
                "minecraft:deepslate_lapis_ore",
                "minecraft:deepslate_copper_ore",
                "minecraft:deepslate_tin_ore",
                "minecraft:deepslate_lead_ore",
                "minecraft:deepslate_aluminum_ore",
                "minecraft:deepslate_coal_ore"
              ]
            }""";

    // Default vineable blocks limit, sets how many blocks break per block break
    private static int VINEABLE_LIMIT = 10;

    public Viner() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Populate the blockMap with blockName to Block mappings
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
            if (location != null) {
                String blockName = location.toString();
                blockMap.put(blockName, block);
            }
        }

        // Add keymapping to Game
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(
                Minecraft.getInstance().options.keyMappings,
                SHIFT_KEY_BINDING
        );

        // Get the path to your config file
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("viner/vineable_blocks.json");
        String jsonConfig;

        // Check if the config file exists, if not, create it with default settings
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                Files.write(configPath, DEFAULT_CONFIG_VINEABLE.getBytes());
                LOGGER.info("Created default config file at: {}", configPath);
            } catch (IOException e) {
                LOGGER.error("Error creating default config file: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        // read the file in
        try {
            jsonConfig = new String(Files.readAllBytes(configPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Json object of config file
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonConfig, JsonObject.class);

        // Gets the vineable break limit
        VINEABLE_LIMIT = jsonObject.getAsJsonPrimitive("vineable_limit").getAsInt();

        // Gets the vineable blocks config
        JsonArray vineableBlocksConfigArray = jsonObject.getAsJsonArray("vineable_blocks");

        // Iterate over each entry in the vineableBlocksConfigArray
        for (JsonElement blockNameOrTag : vineableBlocksConfigArray.asList()) {

            // Convert the current JsonElement to a string
            String entry = blockNameOrTag.getAsString();

            // Check if the entry starts with '#', indicating it's a tag
            if (entry.startsWith("#")) {

                // Extract the tag name by removing the '#' prefix
                String tagName = entry.substring(1);

                // Fetch the tag using Forge's utility
                TagKey<Block> tagKey = TAG_MAP.get(tagName);

                //if (tagKey != null) {
                    // Assuming you have a method to get all blocks for a given TagKey
                    //Collection<Block> blocksForTag = getBlocksForTag(tagKey);
                    //VINEABLE_BLOCKS.addAll(blocksForTag);
                //}
            } else {

                // If the entry doesn't start with '#', it's a direct block name
                // Fetch the block associated with this name and add it to the VINEABLE_BLOCKS set
                VINEABLE_BLOCKS.add(blockMap.get(entry));
            }
        }
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            Minecraft.getInstance().options.keyMappings = ArrayUtils.add(
                    Minecraft.getInstance().options.keyMappings,
                    SHIFT_KEY_BINDING
            );
        }

        /**
         * Recursively collects all connected blocks of the same type starting from a given position
         *
         * @param world           The world or level in which the blocks are located
         * @param pos             The current block position being checked
         * @param targetState     The block state of the target block type we're looking for
         * @param connectedBlocks A list to store positions of blocks that match the target type and are connected
         * @param visited         A set to keep track of block positions that have already been visited
         *
         * <p>This method starts from the provided block position and checks in all directions (north, south, east, west, up, down)
         * to find blocks of the same type. If a matching block is found, its position is added to the connectedBlocks list, and
         * the method is recursively called for that position to continue the search. The visited set ensures that each block position
         * is checked only once.</p>
         */
        private static void collectConnectedBlocks(Level world, BlockPos pos, BlockState targetState,
                                                   List<BlockPos> connectedBlocks, Set<BlockPos> visited) {
            // Check if the current position has not been visited and if its block type matches the target block type
            if (!visited.contains(pos) && targetState.getBlock().equals(world.getBlockState(pos).getBlock())) {
                // Mark the current position as visited
                visited.add(pos);
                // Add the current position to the list of connected blocks
                connectedBlocks.add(pos);

                // Check all adjacent blocks in all directions
                for (Direction direction : Direction.values()) {
                    collectConnectedBlocks(world, pos.offset(direction.getNormal()),
                            targetState, connectedBlocks, visited);
                }
            }
        }

        /**
         * Event handler for when a block is broken in the game.
         *
         * @param baseEvent The base event object passed when any event is triggered.
         *
         * <p>This method is triggered whenever a block is broken in the game. It checks if the block is of a specific type
         * (vineable) and if the player is crouching while breaking the block. If both conditions are met, it will break
         * all connected blocks of the same type up to a configurable limit. The method also checks for the Silk Touch
         * enchantment on the tool being used and drops resources accordingly.</p>
         */
        @SubscribeEvent
        public static void onBlockBroken(Event baseEvent) {

            // Ensure the event is a block break event
            if (!(baseEvent instanceof BlockEvent.BreakEvent event)) return;

            // Check if the event is server-side (not client-side)
            if (!event.getLevel().isClientSide()) {

                // Retrieve the player who broke the block
                Player player = event.getPlayer();

                // Retrieve the tool in the player's main hand
                ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

                // Check if the player is crouching while breaking the block
                if (SHIFT_KEY_BINDING.isDown()) {
                    BlockPos pos = event.getPos();
                    BlockState targetBlockState = event.getLevel().getBlockState(pos);

                    // Check if the broken block is in the list of vineable blocks
                    if (VINEABLE_BLOCKS.contains(targetBlockState.getBlock())) {

                        // Ensure the player has the capability to harvest the block
                        boolean canHarvest = targetBlockState.canHarvestBlock(event.getLevel(), event.getPos(), player);
                        if (canHarvest) {

                            // Lists to keep track of connected blocks and visited positions
                            List<BlockPos> connectedBlocks = new ArrayList<>();
                            Set<BlockPos> visited = new HashSet<>();
                            collectConnectedBlocks((Level) event.getLevel(), pos, targetBlockState, connectedBlocks, visited);

                            // Counter for the number of blocks broken
                            int blockCount = 0;

                            // Check if the tool has the Silk Touch enchantment
                            boolean hasSilkTouch =
                                    EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

                            // Iterate through the connected blocks and break them
                            for (BlockPos connectedPos : connectedBlocks) {

                                // Ensure we don't break more than the configured limit of blocks
                                if (blockCount < VINEABLE_LIMIT) {
                                    BlockState connectedBlockState = event.getLevel().getBlockState(connectedPos);
                                    if (VINEABLE_BLOCKS.contains(connectedBlockState.getBlock())) {

                                        // Drop the block itself if the tool has Silk Touch
                                        if (hasSilkTouch) {
                                            Block.popResource((Level) event.getLevel(), connectedPos,
                                                    new ItemStack(connectedBlockState.getBlock()));
                                        } else {
                                            // Otherwise, drop the block's resources
                                            Block.dropResources(connectedBlockState,
                                                    (Level) event.getLevel(), event.getPos());
                                        }
                                        // Remove the block from the world
                                        event.getLevel().removeBlock(connectedPos, false);
                                        blockCount++;
                                    }
                                }
                            }

                            // Update the damage value of the tool based on the number of blocks broken
                            ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
                            item.setDamageValue(item.getDamageValue() + blockCount);
                        }
                    }
                }
            }
        }
    }
}
