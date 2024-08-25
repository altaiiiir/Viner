package com.ael.viner.config;

import com.ael.viner.Viner;
import com.ael.viner.registry.VinerBlockRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Configuration class for the Viner mod, defining various settings and their default values.
 */
@Mod.EventBusSubscriber(modid = Viner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    /**
     * Logger instance for logging configuration-related messages.
     */
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Builder for creating the configuration specification.
     */
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /**
     * Configuration specification containing all defined settings.
     */
    public static final ForgeConfigSpec SPEC;

    /**
     * Config setting for the maximum number of blocks to vein mine.
     */
    public static final ForgeConfigSpec.BooleanValue SHAPE_VINE;

    /**
     * Config setting for the maximum number of blocks to vein mine.
     */
    public static final ForgeConfigSpec.IntValue VINEABLE_LIMIT;

    // Config setting for exhaustion per block
    public static final ForgeConfigSpec.DoubleValue EXHAUSTION_PER_BLOCK;

    /**
     * Config setting for the list of blocks/tags that can be vein mined.
     */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VINEABLE_BLOCKS;

    /**
     * Config setting for the list of blocks that will not be vein mined,
     * overriding blocks from tags in VINEABLE_BLOCKS.
     */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNVINEABLE_BLOCKS;

    /**
     * Config setting for whether all blocks should be vineable
     */
    public static final ForgeConfigSpec.ConfigValue<Boolean> VINE_ALL;

    /**
     * Config setting for height above mined block for mining zone
     */
    public static final ForgeConfigSpec.IntValue HEIGHT_ABOVE;

    /**
     * Config setting for height below mined block for mining zone
     */
    public static final ForgeConfigSpec.IntValue HEIGHT_BELOW;

    /**
     * Config setting for width to the left of mined block for mining zone
     */
    public static final ForgeConfigSpec.IntValue WIDTH_LEFT;

    /**
     * Config setting for width to the right of mined block for mining zone
     */
    public static final ForgeConfigSpec.IntValue WIDTH_RIGHT;

    /**
     * Config setting for layer offset of each layer, used for staircase mining
     */
    public static final ForgeConfigSpec.IntValue LAYER_OFFSET;


    static {
        // Start a configuration category for viner settings
        BUILDER.push("viner");

        // Define shapeVine setting
        SHAPE_VINE = BUILDER
                .comment("A 'true' or 'false' field to enable Shape Vine Mode.")
                .define("shapeVine", false);

        // Define exhaustionPerBlock setting
        EXHAUSTION_PER_BLOCK = BUILDER
                .comment("Amount of hunger added per block mined with vein mining.")
                .defineInRange("exhaustionPerBlock", 0.25, 0.0, 20.0);

        // Define veinable limit setting
        VINEABLE_LIMIT = BUILDER
                .comment("Maximum number of blocks to vein mine")
                .defineInRange("vineableLimit", 5, 1, Integer.MAX_VALUE);

        // Define vineable blocks setting with a validation pattern
        VINEABLE_BLOCKS = BUILDER
                .comment("List of blocks/tags that can be vein mined. Tags must start with '#'")
                .defineList("vineableBlocks", Arrays.asList("#minecraft:ores", "#minecraft:logs", "#minecraft:leaves", "#forge:ores", "minecraft:skulk"),
                        obj -> obj instanceof String && ((String) obj).matches("^#?[a-z_]+:[a-z_]+$"));

        // Define unvineable blocks setting with a validation pattern
        UNVINEABLE_BLOCKS = BUILDER
                .comment("List of blocks that will not vein mined. This will override blocks from tags in VINEABLE_BLOCKS")
                .defineList("unvineableBlocks", ArrayList::new,
                        obj -> obj instanceof String && ((String) obj).matches("^[a-z_]+:[a-z_]+$"));

        VINE_ALL = BUILDER
                .comment("A 'true' or 'false' field to allow Viner to mine any block.")
                .define("vineAll", false);

        // Define heightAbove setting
        HEIGHT_ABOVE = BUILDER
                .comment("(Must have SHAPE_VINE enabled) The number of blocks to mine above the starting block. " +
                        "This value sets how far upwards the tool mines from the starting position. " +
                        "Minimum is 0 (no mining above), and there is no upper limit.")
                .defineInRange("heightAbove", 1, 0, Integer.MAX_VALUE);

        // Define heightBelow setting
        HEIGHT_BELOW = BUILDER
                .comment("(Must have SHAPE_VINE enabled) The number of blocks to mine below the starting block. " +
                        "This value sets how far downwards the tool mines from the starting position. " +
                        "Minimum is 0 (no mining below), and there is no upper limit.")
                .defineInRange("heightBelow", 1, 0, Integer.MAX_VALUE);

        // Define widthLeft setting
        WIDTH_LEFT = BUILDER
                .comment("(Must have SHAPE_VINE enabled) The number of blocks to mine to the left of the starting block. " +
                        "This value sets how far to the left (from the player's perspective) the tool mines. " +
                        "Minimum is 0 (no mining to the left), and there is no upper limit.")
                .defineInRange("widthLeft", 0, 0, Integer.MAX_VALUE);

        // Define widthRight setting
        WIDTH_RIGHT = BUILDER
                .comment("(Must have SHAPE_VINE enabled) The number of blocks to mine to the right of the starting block. " +
                        "This value sets how far to the right (from the player's perspective) the tool mines. " +
                        "Minimum is 0 (no mining to the right), and there is no upper limit.")
                .defineInRange("widthRight", 0, 0, Integer.MAX_VALUE);

        // Define layerOffset setting
        LAYER_OFFSET = BUILDER
                .comment("(Must have SHAPE_VINE enabled) The number of blocks defining the vertical distance between " +
                        "consecutive layers mined. Allows for staircase mining.")
                .defineInRange("layerOffset", 0, -64, 256);

        // End the configuration category for viner settings
        BUILDER.pop();

        // Build the configuration specification
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        // FIXME: It would be nice to have the old config be migrated to the new one.
        //        this implementation "mostly" works, but causes a lot of duplication (and is messy code)
        //        so leaving it out for now.

        // upgradeConfig();
    }

    private static void upgradeConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("viner/vineable_blocks.json");

        LOGGER.info("Checking for existing config at: {}", configPath);

        if (Files.exists(configPath)) {
            try {
                LOGGER.info("Existing config found, starting upgrade...");

                String jsonConfig = new String(Files.readAllBytes(configPath));
                Gson gson = new Gson();
                JsonObject oldConfig = gson.fromJson(jsonConfig, JsonObject.class);

                List<String> mergedBlocks = new ArrayList<>(Config.VINEABLE_BLOCKS.get());

                JsonArray oldBlocks = oldConfig.getAsJsonArray("vineable_blocks");
                for (JsonElement element : oldBlocks) {

                    String blockString = element.getAsString();
                    LOGGER.debug("Processing block: {}", blockString);

//                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockString));

                    boolean isVeinmineable = false;
                    for (var tagString : Config.VINEABLE_BLOCKS.get()) {
                        LOGGER.debug("Processing tagString: {}", tagString);

                        TagKey<Block> tagKey = VinerBlockRegistry.getTagKeyEntry(tagString);
                        LOGGER.debug("Generated tagKey: {}", tagKey);

                        ITag<Block> tag = ForgeRegistries.BLOCKS.tags().getTag(tagKey);
                        LOGGER.debug("Retrieved tag: {}", tag);

                        Block block = ForgeRegistries.BLOCKS.getValue(VinerBlockRegistry.getResourceLocationFromEntry(blockString));
                        LOGGER.debug("Retrieved block: {}", block);

                        if (tag.contains(block)) {
                            LOGGER.debug("Tag {} contains block {}", tag, block);
                            isVeinmineable = true;
                            break;
                        } else {
                            LOGGER.debug("Tag {} does not contain block {}", tag, block);
                        }
                    }


                    if (!isVeinmineable) {
                        LOGGER.debug("Block {} is not currently veinmineable, adding to config", blockString);
//                        mergedBlocks.add(blockString);
                    }
                }

                Config.VINEABLE_BLOCKS.set(mergedBlocks);
                Config.VINEABLE_LIMIT.set(Math.max(Config.VINEABLE_LIMIT.get(), oldConfig.get("vineable_limit").getAsInt()));
                LOGGER.info("Config upgrade complete");

            } catch (IOException e) {
                LOGGER.error("Error upgrading config: {}", e.getMessage());
            }
        } else {
            LOGGER.info("No existing config found, skipping upgrade");
        }
    }

}
