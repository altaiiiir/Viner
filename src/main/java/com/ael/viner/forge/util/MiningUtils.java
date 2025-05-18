package com.ael.viner.forge.util;

import com.ael.viner.common.IPlayerData;
import com.ael.viner.common.IPlayerRegistry;
import com.ael.viner.common.VinerEntrypoint;
import com.ael.viner.common.util.BlockTagUtils;
import com.ael.viner.forge.player.VinerPlayerData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class MiningUtils {

  /**
   * Mines a list of blocks on behalf of a player, applying the appropriate tool enchantments,
   * updating tool damage, and spawning drops at the position of the first block in the list.
   *
   * @param player The player who is mining the blocks.
   * @param blocksToMine A list of BlockPos representing the blocks to be mined.
   */
  public static void mineBlocks(ServerPlayer player, List<BlockPos> blocksToMine) {
    if (player == null) return;

    Level level = player.level();
    ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

    IPlayerRegistry registry = VinerEntrypoint.get().getPlayerRegistry();
    IPlayerData playerData = registry.getPlayerData(player);
    var vineableLimit = playerData.getVineableLimit();

    // Check for client side, return early if true
    if (level.isClientSide() || vineableLimit <= 0) return;

    // Initial block position for spawning all drops
    BlockPos firstBlockPos = blocksToMine.get(0);
    int currentBlock = 1;

    for (BlockPos blockPos : blocksToMine) {

      // Stop breaking blocks if the tool is about to break or if it's the first block
      if (currentBlock++ != 1 && applyDamage(tool, 1)) break;

      // Protect storage by handling inventory before removing the block
      protectStorage(level, blockPos);

      spawnBlockDrops(player, (ServerLevel) level, blockPos, tool, firstBlockPos);
      spawnExp(level.getBlockState(blockPos), (ServerLevel) level, firstBlockPos, tool);

      boolean isIceWithoutSilkTouch =
          level.getBlockState(blockPos).getBlock() == Blocks.ICE
              && tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) == 0;
      level.setBlockAndUpdate(
          blockPos,
          isIceWithoutSilkTouch
              ? Blocks.WATER.defaultBlockState()
              : Blocks.AIR.defaultBlockState());
    }
  }

  private static void protectStorage(Level level, BlockPos blockPos) {
    BlockEntity blockEntity = level.getBlockEntity(blockPos);
    if (blockEntity != null) {
      blockEntity
          .getCapability(ForgeCapabilities.ITEM_HANDLER)
          .ifPresent(
              itemHandler -> {
                NonNullList<ItemStack> inventoryContents =
                    NonNullList.withSize(itemHandler.getSlots(), ItemStack.EMPTY);
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                  inventoryContents.set(
                      i, itemHandler.getStackInSlot(i).copy()); // Make a copy of the stack to avoid
                  // modifying the original
                }

                // Serialize the inventory into the BlockEntity's custom persistent data
                CompoundTag inventoryTag = new CompoundTag();
                ContainerHelper.saveAllItems(inventoryTag, inventoryContents, true);
                CompoundTag persistentData =
                    blockEntity.getPersistentData(); // Retrieve custom persistent data
                persistentData.put(
                    "Items", inventoryTag.getList("Items", 10)); // Store inventory data

                // Ensure the BlockEntity knows it has changed
                blockEntity.setChanged();
              });
    }
  }

  private static void spawnExp(
      BlockState blockState, ServerLevel level, BlockPos blockPos, ItemStack tool) {

    // Gets current tools enchantments
    int fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
    int silkTouchLevel = tool.getEnchantmentLevel(Enchantments.SILK_TOUCH);

    // Gets the XP expected to drop from a block
    int exp = blockState.getExpDrop(level, level.random, blockPos, fortuneLevel, silkTouchLevel);

    // If there's any XP to drop, it drops it
    if (exp > 0) {
      blockState.getBlock().popExperience(level, blockPos, exp);
    }
  }

  /**
   * Spawns block drops for the block at the specified position, applying special handling for
   * Skulker Boxes to retain their contents in the dropped item.
   *
   * @param player The player breaking the block, used for loot context.
   * @param level The server level where the block is located.
   * @param blockPos The position of the block being broken.
   * @param tool The tool used to break the block, used for loot context.
   * @param firstBlockPos The position where the dropped items should spawn.
   */
  private static void spawnBlockDrops(
      ServerPlayer player,
      ServerLevel level,
      BlockPos blockPos,
      ItemStack tool,
      BlockPos firstBlockPos) {
    // Attempt to retrieve the BlockEntity at the given position
    BlockEntity blockEntity = level.getBlockEntity(blockPos);
    CompoundTag blockEntityTag = null;

    // Check if the BlockEntity is an instance of SkulkerBoxBlockEntity
    if (blockEntity instanceof ShulkerBoxBlockEntity) {
      // Save the block entity data, excluding metadata, to a new CompoundTag
      blockEntityTag = blockEntity.saveWithoutMetadata();
    }

    // Use the block's loot table to determine the items to drop, considering the
    // tool used and the player
    List<ItemStack> itemsToDrop =
        Block.getDrops(level.getBlockState(blockPos), level, blockPos, null, player, tool);

    // Iterate through the determined items to drop
    for (ItemStack item : itemsToDrop) {
      // If we have saved BlockEntity data (from a Skulker Box) and the item is a
      // Skulker Box, attach the data
      if (blockEntityTag != null
          && item.getItem() instanceof BlockItem
          && ((BlockItem) item.getItem()).getBlock() instanceof ShulkerBoxBlock) {
        CompoundTag itemTag = item.getOrCreateTag();
        itemTag.put("BlockEntityTag", blockEntityTag);
        item.setTag(itemTag);
      }

      // Calculate random offset for drop position
      double d0 = (double) (level.random.nextFloat() * 0.5F) + 0.25D;
      double d1 = (double) (level.random.nextFloat() * 0.5F) + 0.25D;
      double d2 = (double) (level.random.nextFloat() * 0.5F) + 0.25D;

      // Create and spawn the item entity at the calculated position
      ItemEntity itemEntity =
          new ItemEntity(
              level,
              firstBlockPos.getX() + d0,
              firstBlockPos.getY() + d1,
              firstBlockPos.getZ() + d2,
              item);
      level.addFreshEntity(itemEntity);
    }
  }

  /**
   * Initiates the collection of connected blocks of the same type as the specified block. It
   * creates fresh lists for connected blocks and visited positions, then calls the recursive
   * collect method to find all connected blocks.
   *
   * @param level The level where the block exists.
   * @param pos The position of the block being vein mined.
   * @param targetState The BlockState of the block being vein mined.
   * @return A list of BlockPos representing all connected blocks of the same type.
   */
  public static List<BlockPos> collectConnectedBlocks(
      Level level,
      BlockPos pos,
      BlockState targetState,
      Vec3i lookPos,
      int vineableLimit,
      boolean isShapeVine,
      int heightAbove,
      int heightBelow,
      int widthLeft,
      int widthRight,
      int layerOffset) {
    List<BlockPos> connectedBlocks = new ArrayList<>();
    Set<BlockPos> visited = new HashSet<>();

    if (isShapeVine) {
      collectConfigurablePattern(
          level,
          lookPos,
          pos,
          targetState,
          connectedBlocks,
          visited,
          vineableLimit,
          heightAbove,
          heightBelow,
          widthLeft,
          widthRight,
          layerOffset);
    } else {
      collect(level, pos, targetState, connectedBlocks, visited, vineableLimit);
    }

    return connectedBlocks;
  }

  /**
   * Recursively collects connected blocks of the same type to the specified block, up to a maximum
   * limit defined in VinerBlockRegistry. This method checks adjacent and diagonal blocks for the
   * same block type, adding them to a list if they match.
   *
   * @param level The level where the block exists.
   * @param pos The position of the current block being checked.
   * @param targetState The BlockState of the block being vein mined.
   * @param connectedBlocks A list to store positions of connected blocks of the same type.
   * @param visited A set to keep track of already visited positions, to avoid infinite recursion.
   */
  private static void collect(
      Level level,
      BlockPos pos,
      BlockState targetState,
      List<BlockPos> connectedBlocks,
      Set<BlockPos> visited,
      int vineableLimit) {
    Queue<BlockPos> queue = new LinkedList<>();
    queue.add(pos);

    while (!queue.isEmpty() && connectedBlocks.size() < vineableLimit) {
      BlockPos currentPos = queue.poll();

      if (visited.contains(currentPos)
          || !targetState.getBlock().equals(level.getBlockState(currentPos).getBlock())) {
        continue;
      }

      visited.add(currentPos);
      connectedBlocks.add(currentPos);

      // Add all adjacent blocks to the queue
      for (Direction direction : Direction.values()) {
        queue.add(currentPos.relative(direction));
      }

      // Add all diagonal blocks to the queue
      for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
          for (int dz = -1; dz <= 1; dz++) {
            if (dx != 0 || dy != 0 || dz != 0) {
              queue.add(currentPos.offset(dx, dy, dz));
            }
          }
        }
      }
    }
  }

  /**
   * Efficiently mines blocks in a pattern based on player's look direction and given dimensions
   * This method layers the mining process, using rectangles defined by height and width parameters
   * Mining progresses depth-wise in the direction the player is looking, respecting the limit from
   * VinerBlockRegistry
   *
   * @param level The level where the block exists
   * @param lookPos The current look position of the player, determining the depth direction for
   *     mining
   * @param pos Starting position for mining
   * @param targetState The BlockState of the block to be mined
   * @param connectedBlocks List to store positions of mined blocks
   * @param visited Set to track visited positions, preventing recursion loops
   * @param heightAbove Number of blocks to mine above the starting block
   * @param heightBelow Number of blocks to mine below the starting block
   * @param widthLeft Number of blocks to mine left of the starting block
   * @param widthRight Number of blocks to mine right of the starting block
   */
  private static void collectConfigurablePattern(
      Level level,
      Vec3i lookPos,
      BlockPos pos,
      BlockState targetState,
      List<BlockPos> connectedBlocks,
      Set<BlockPos> visited,
      int vineableLimit,
      int heightAbove,
      int heightBelow,
      int widthLeft,
      int widthRight,
      int layerOffset) {
    Queue<BlockPos> queue = new LinkedList<>();
    queue.add(pos);

    Vec3i horizontalDirection = new Vec3i(lookPos.getZ(), 0, -lookPos.getX());
    int blockVolumeToMine = (heightAbove + heightBelow + 1) * (widthLeft + widthRight + 1);

    while (!queue.isEmpty() && connectedBlocks.size() + blockVolumeToMine <= vineableLimit) {
      BlockPos currentPos = queue.poll();
      Set<BlockPos> potentialBlocks = new HashSet<>();

      /// Collect all potential blocks in the current layer
      for (int h = -heightBelow; h <= heightAbove; h++) {
        for (int w = -widthLeft; w <= widthRight; w++) {
          BlockPos blockToMine =
              currentPos.offset(horizontalDirection.getX() * w, h, horizontalDirection.getZ() * w);
          potentialBlocks.add(blockToMine);
        }
      }

      // Remove already visited blocks
      potentialBlocks.removeAll(visited);
      for (BlockPos blockToMine : potentialBlocks) {
        if (targetState.getBlock().equals(level.getBlockState(blockToMine).getBlock())) {
          visited.add(blockToMine);
          connectedBlocks.add(blockToMine);
        }
      }

      // Queue next block in mining direction
      BlockPos nextDepthBlock =
          currentPos.offset(lookPos.getX(), lookPos.getY() - layerOffset, lookPos.getZ());
      if (!visited.contains(nextDepthBlock) && !level.getBlockState(nextDepthBlock).isAir()) {
        queue.add(nextDepthBlock);
      }
    }
  }

  /** Helper to get the block ID as a String (e.g., "minecraft:stone"). */
  private static String getBlockId(Block block) {
    ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
    return id != null ? id.toString() : "";
  }

  /**
   * Checks if a block is vineable based on predefined criteria. A block is considered vineable if:
   * 1. It does not exist in the list of unvineable blocks. 2. It either exists within specified
   * tags or in the list of vineable blocks.
   *
   * @param block The block to be checked.
   * @return true if the block is vineable, false otherwise.
   */
  public static boolean isVineable(Block block, Player player) {
    IPlayerRegistry registry = VinerEntrypoint.get().getPlayerRegistry();
    IPlayerData playerData = registry.getPlayerData(player);

    return playerData.isVineAllEnabled()
        || (!blockExistsInUnvineableBlocks(block, player)
            && blockExistsInVineableBlocks(block, player));
  }

  /**
   * Checks if a specified block is listed as unvineable in the registry.
   *
   * @param block The block to check.
   * @return true if the block is unvineable, false otherwise.
   */
  private static boolean blockExistsInUnvineableBlocks(Block block, Player player) {
    IPlayerRegistry registry = VinerEntrypoint.get().getPlayerRegistry();
    IPlayerData playerData = registry.getPlayerData(player);
    String blockId = getBlockId(block);
    if (playerData instanceof VinerPlayerData vinerData) {
      return BlockTagUtils.isBlockIdInList(blockId, vinerData.getUnvineableBlockIds())
          || blockExistsInUnvineableTags(block, player);
    }
    return false;
  }

  /**
   * Checks if a specified block is listed as vineable in the registry.
   *
   * @param block The block to check.
   * @return true if the block is vineable, false otherwise.
   */
  private static boolean blockExistsInVineableBlocks(Block block, Player player) {
    IPlayerRegistry registry = VinerEntrypoint.get().getPlayerRegistry();
    IPlayerData playerData = registry.getPlayerData(player);
    String blockId = getBlockId(block);
    if (playerData instanceof VinerPlayerData vinerData) {
      return BlockTagUtils.isBlockIdInList(blockId, vinerData.getVineableBlockIds())
          || blockExistsInVineableTags(block, player);
    }
    return false;
  }

  /**
   * Checks if a specified block is listed as vineable under any tag in the registry.
   *
   * @param block The block to check.
   * @return true if the block is vineable under any tag, false otherwise.
   */
  private static boolean blockExistsInVineableTags(Block block, Player player) {
    IPlayerRegistry registry = VinerEntrypoint.get().getPlayerRegistry();
    IPlayerData playerData = registry.getPlayerData(player);
    String blockId = getBlockId(block);
    if (playerData instanceof VinerPlayerData vinerData) {
      return BlockTagUtils.isBlockIdInTags(
          blockId,
          vinerData.getVineableTagIds(),
          tagId -> {
            TagKey<Block> tagKey =
                TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(tagId));
            var tagBlocks = ForgeRegistries.BLOCKS.tags().getTag(tagKey);

            return tagBlocks.stream()
                .map(MiningUtils::getBlockId)
                .collect(java.util.stream.Collectors.toSet());
          });
    }
    return false;
  }

  /**
   * Checks if a specified block is listed as unvineable under any tag in the registry.
   *
   * @param block The block to check.
   * @return true if the block is vineable under any tag, false otherwise.
   */
  private static boolean blockExistsInUnvineableTags(Block block, Player player) {
    IPlayerRegistry registry = VinerEntrypoint.get().getPlayerRegistry();
    IPlayerData playerData = registry.getPlayerData(player);
    String blockId = getBlockId(block);
    if (playerData instanceof VinerPlayerData vinerData) {
      return BlockTagUtils.isBlockIdInTags(
          blockId,
          vinerData.getUnvineableTagIds(),
          tagId -> {
            TagKey<Block> tagKey =
                TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(tagId));
            var tagBlocks = ForgeRegistries.BLOCKS.tags().getTag(tagKey);

            return tagBlocks.stream()
                .map(MiningUtils::getBlockId)
                .collect(java.util.stream.Collectors.toSet());
          });
    }
    return false;
  }

  /**
   * Retrieves the level of the Unbreaking enchantment on a specified tool.
   *
   * @param tool The tool whose Unbreaking enchantment level is to be checked.
   * @return The level of the Unbreaking enchantment, or 0 if not present.
   */
  public static int getUnbreakingLevel(ItemStack tool) {
    return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, tool);
  }

  /**
   * Calculates the chance of a tool taking damage based on its Unbreaking enchantment level.
   *
   * @param unbreakingLevel The level of the Unbreaking enchantment on the tool.
   * @return The chance of the tool taking damage.
   */
  public static double getDamageChance(int unbreakingLevel) {
    return 1.0 / (unbreakingLevel + 1);
  }

  /**
   * Applies damage to a specified tool. Taking Unbreaking into consideration
   *
   * @param tool The tool to be damaged.
   * @param damage The amount of damage to apply.
   * @return A boolean containing whether the applyDamage function broke the weapon
   */
  public static boolean applyDamage(@NotNull ItemStack tool, int damage) {

    if (!tool.isDamageableItem()) return false;

    int unbreakingLevel = getUnbreakingLevel(tool);
    double chance = getDamageChance(unbreakingLevel);
    double random = Math.random();

    // Only apply damage if the random value is less than the damage chance.
    if (random < chance) {
      int newDamage = tool.getDamageValue() + damage;
      int maxDamage = tool.getMaxDamage();

      // Prevent over-damaging the tool
      if (newDamage >= maxDamage) {
        tool.setDamageValue(maxDamage);
        return true;
      } else {
        tool.setDamageValue(newDamage);
      }
    }

    return false;
  }
}
