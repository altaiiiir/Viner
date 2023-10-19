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


@Mod.EventBusSubscriber(modid = Viner.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final Logger LOGGER = LogUtils.getLogger();


    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue VINEABLE_LIMIT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VINEABLE_BLOCKS;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNVINEABLE_BLOCKS;

    static {
        BUILDER.push("viner");

        VINEABLE_LIMIT = BUILDER
                .comment("Maximum number of blocks to vein mine")
                .defineInRange("vineableLimit", 5, 1, Integer.MAX_VALUE);

        VINEABLE_BLOCKS = BUILDER
                .comment("List of blocks/tags that can be vein mined. Tags must start with '#'")
                .defineList("vineableBlocks", Arrays.asList("#minecraft:ores", "#minecraft:logs", "#minecraft:leaves", "#forge:ores"),
                        obj -> obj instanceof String && ((String) obj).matches("^#?[a-z_]+:[a-z_]+$"));

        UNVINEABLE_BLOCKS = BUILDER
                .comment("List of blocks that will not vein mined. This will override blocks from tags in VINEABLE_BLOCKS")
                .defineList("unvineableBlocks", ArrayList::new,
                        obj -> obj instanceof String && ((String) obj).matches("^[a-z_]+:[a-z_]+$"));

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        // FIXME: It would be nice to have the old config be migrated to the new one.
        //        this implementation "mostly" works, but causes a lot of duplication (and is messy code)
        //        so leaving it out for now.

        // upgradeConfig();
    }

    public static void upgradeConfig() {
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


                    if (!isVeinmineable){
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
