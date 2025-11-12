package com.ael.viner.forge.config;

import com.ael.viner.forge.VinerForge;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

/** Configuration class for the Viner mod, defining various settings and their default values. */
@Mod.EventBusSubscriber(modid = VinerForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

  /** Builder for creating the configuration specification. */
  public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

  /** Configuration specification containing all defined settings. */
  public static final ForgeConfigSpec SPEC;

  /** Config setting for the maximum number of blocks to vein mine. */
  public static final ForgeConfigSpec.BooleanValue SHAPE_VINE;

  /** Config setting for the maximum number of blocks to vein mine. */
  public static final ForgeConfigSpec.IntValue VINEABLE_LIMIT;

  // Config setting for exhaustion per block
  public static final ForgeConfigSpec.DoubleValue EXHAUSTION_PER_BLOCK;

  /** Config setting for the list of blocks/tags that can be vein mined. */
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VINEABLE_BLOCKS;

  /**
   * Config setting for the list of blocks that will not be vein mined, overriding blocks from tags
   * in VINEABLE_BLOCKS.
   */
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNVINEABLE_BLOCKS;

  /** Config setting for whether all blocks should be vineable */
  public static final ForgeConfigSpec.ConfigValue<Boolean> VINE_ALL;

  /** Config setting for height above mined block for mining zone */
  public static final ForgeConfigSpec.IntValue HEIGHT_ABOVE;

  /** Config setting for height below mined block for mining zone */
  public static final ForgeConfigSpec.IntValue HEIGHT_BELOW;

  /** Config setting for width to the left of mined block for mining zone */
  public static final ForgeConfigSpec.IntValue WIDTH_LEFT;

  /** Config setting for width to the right of mined block for mining zone */
  public static final ForgeConfigSpec.IntValue WIDTH_RIGHT;

  /** Config setting for layer offset of each layer, used for staircase mining */
  public static final ForgeConfigSpec.IntValue LAYER_OFFSET;

  // Validation predicates for config lists
  private static final java.util.function.Predicate<Object> BLOCK_TAG_VALID =
      obj -> obj instanceof String && ((String) obj).matches("^#?[a-z_]+:[a-z_]+$");
  private static final java.util.function.Predicate<Object> BLOCK_VALID =
      obj -> obj instanceof String && ((String) obj).matches("^[a-z_]+:[a-z_]+$");

  static {
    // Start a configuration category for viner settings
    BUILDER.push("viner");

    // Define shapeVine setting
    SHAPE_VINE =
        BUILDER
            .comment("A 'true' or 'false' field to enable Shape Vine Mode.")
            .define("shapeVine", false);

    // Define exhaustionPerBlock setting
    EXHAUSTION_PER_BLOCK =
        BUILDER
            .comment("Amount of hunger added per block mined with vein mining.")
            .defineInRange("exhaustionPerBlock", 0.25, 0.0, 20.0);

    // Define veinable limit setting
    VINEABLE_LIMIT =
        BUILDER
            .comment("Maximum number of blocks to vein mine")
            .defineInRange("vineableLimit", 5, 1, Integer.MAX_VALUE);

    // Define vineable blocks setting with a validation pattern
    VINEABLE_BLOCKS =
        BUILDER
            .comment("List of blocks/tags that can be vein mined. Tags must start with '#'")
            .defineList(
                "vineableBlocks",
                java.util.List.of(
                    "#minecraft:ores",
                    "#minecraft:logs",
                    "#minecraft:leaves",
                    "#forge:ores",
                    "minecraft:skulk"),
                BLOCK_TAG_VALID);

    // Define unvineable blocks setting with a validation pattern
    UNVINEABLE_BLOCKS =
        BUILDER
            .comment(
                "List of blocks that will not vein mined. This will override blocks from tags in VINEABLE_BLOCKS")
            .defineList("unvineableBlocks", java.util.List::of, BLOCK_VALID);

    VINE_ALL =
        BUILDER
            .comment("A 'true' or 'false' field to allow Viner to mine any block.")
            .define("vineAll", false);

    // Group shape mining related settings under a 'shape' subcategory
    BUILDER.push("shape");

    // Define heightAbove setting
    HEIGHT_ABOVE =
        BUILDER
            .comment(
                "(Must have SHAPE_VINE enabled) The number of blocks to mine above the starting block. "
                    + "This value sets how far upwards the tool mines from the starting position. "
                    + "Minimum is 0 (no mining above), and there is no upper limit.")
            .defineInRange("heightAbove", 1, 0, Integer.MAX_VALUE);

    // Define heightBelow setting
    HEIGHT_BELOW =
        BUILDER
            .comment(
                "(Must have SHAPE_VINE enabled) The number of blocks to mine below the starting block. "
                    + "This value sets how far downwards the tool mines from the starting position. "
                    + "Minimum is 0 (no mining below), and there is no upper limit.")
            .defineInRange("heightBelow", 1, 0, Integer.MAX_VALUE);

    // Define widthLeft setting
    WIDTH_LEFT =
        BUILDER
            .comment(
                "(Must have SHAPE_VINE enabled) The number of blocks to mine to the left of the starting block. "
                    + "This value sets how far to the left (from the player's perspective) the tool mines. "
                    + "Minimum is 0 (no mining to the left), and there is no upper limit.")
            .defineInRange("widthLeft", 0, 0, Integer.MAX_VALUE);

    // Define widthRight setting
    WIDTH_RIGHT =
        BUILDER
            .comment(
                "(Must have SHAPE_VINE enabled) The number of blocks to mine to the right of the starting block. "
                    + "This value sets how far to the right (from the player's perspective) the tool mines. "
                    + "Minimum is 0 (no mining to the right), and there is no upper limit.")
            .defineInRange("widthRight", 0, 0, Integer.MAX_VALUE);

    // Define layerOffset setting
    LAYER_OFFSET =
        BUILDER
            .comment(
                "(Must have SHAPE_VINE enabled) The number of blocks defining the vertical distance between "
                    + "consecutive layers mined. Allows for staircase mining.")
            .defineInRange("layerOffset", 0, -64, 256);

    // End the shape subcategory
    BUILDER.pop();

    // End the configuration category for viner settings
    BUILDER.pop();

    // Build the configuration specification
    SPEC = BUILDER.build();
  }
}
