package com.ael.viner.util;

import com.ael.viner.Viner;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MiningUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mines a list of blocks on behalf of a player, applying the appropriate tool enchantments,
     * updating tool damage, and spawning drops at the position of the first block in the list.
     *
     * @param player       The player who is mining the blocks.
     * @param blocksToMine A list of BlockPos representing the blocks to be mined.
     */
    public static void mineBlocks(ServerPlayer player, List<BlockPos> blocksToMine) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (player == null)
            return;

        Level level = player.level();
        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

        var vineableLimit = Viner.getInstance().getPlayerRegistry().getPlayerData(player).getVineableLimit();

        // Check for client side, return early if true
        if (level.isClientSide() || vineableLimit <= 0)
            return;

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

            int silkTouchLevel = 0;

            try {
                // For Minecraft 1.21.x or newer (using BuiltInRegistries)
                Class<?> builtInRegistriesClass = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
                Object enchantmentRegistry = builtInRegistriesClass.getField("ENCHANTMENT").get(null);  // Fetch the ENCHANTMENT registry

                // Create ResourceLocation using reflection for 1.21.x
                Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
                Constructor<?> resourceLocationConstructor = resourceLocationClass.getDeclaredConstructor(String.class, String.class);
                resourceLocationConstructor.setAccessible(true);
                Object silkTouchResourceLocation = resourceLocationConstructor.newInstance("minecraft", "silk_touch");

                // Create the ResourceKey for the Enchantment registry
                Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");
                Method createMethod = resourceKeyClass.getMethod("create", Class.forName("net.minecraft.resources.ResourceKey"), resourceLocationClass);

                // Get the ResourceKey for Registry.ENCHANTMENT
                Object enchantmentRegistryKey = builtInRegistriesClass.getField("ENCHANTMENT").get(null);

                // Create the ResourceKey for silk_touch enchantment
                Object silkTouchKey = createMethod.invoke(null, enchantmentRegistryKey, silkTouchResourceLocation);

                // Use the new getHolderOrThrow method to retrieve the Holder<Enchantment>
                Method getHolderOrThrowMethod = enchantmentRegistry.getClass().getMethod("getHolderOrThrow", resourceKeyClass);
                Holder<Enchantment> silkTouchHolder = (Holder<Enchantment>) getHolderOrThrowMethod.invoke(enchantmentRegistry, silkTouchKey);

                // Use the new getItemEnchantmentLevel method for Holder<Enchantment>
                silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder, tool);

            } catch (Exception e) {
                // Fallback for 1.20.x
                Method getEnchantmentLevelMethod = tool.getClass().getMethod("getEnchantmentLevel", Enchantment.class);
                silkTouchLevel = (int) getEnchantmentLevelMethod.invoke(tool, Enchantments.SILK_TOUCH);
            }

            boolean isIceWithoutSilkTouch = level.getBlockState(blockPos).getBlock() == Blocks.ICE && silkTouchLevel == 0;
            level.setBlockAndUpdate(blockPos, isIceWithoutSilkTouch ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
    }

    private static void protectStorage(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null) {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                NonNullList<ItemStack> inventoryContents = NonNullList.withSize(itemHandler.getSlots(), ItemStack.EMPTY);
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    inventoryContents.set(i, itemHandler.getStackInSlot(i).copy()); // Make a copy of the stack to avoid modifying the original
                }

                // Serialize the inventory into a new CompoundTag
                CompoundTag inventoryTag = new CompoundTag();

                try {
                    // Try handling 1.21.x
                    Method saveAllItemsMethod = ContainerHelper.class.getMethod("saveAllItems", CompoundTag.class, NonNullList.class, boolean.class, Class.forName("net.minecraft.core.HolderLookup$Provider"));
                    Object holderProvider = getHolderLookupProvider(level);  // Fetch the HolderLookup.Provider dynamically
                    saveAllItemsMethod.invoke(null, inventoryTag, inventoryContents, true, holderProvider);

                } catch (NoSuchMethodException e) {
                    // Fallback to 1.20.x version
                    try {
                        Method saveAllItemsMethod = ContainerHelper.class.getMethod("saveAllItems", CompoundTag.class, NonNullList.class, boolean.class);
                        saveAllItemsMethod.invoke(null, inventoryTag, inventoryContents, true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    // Handle 1.21.x for BlockEntity
                    Method saveWithFullMetadataMethod = blockEntity.getClass().getMethod("saveWithFullMetadata", Class.forName("net.minecraft.core.HolderLookup$Provider"));
                    Object holderProvider = getHolderLookupProvider(level);  // Fetch the HolderLookup.Provider dynamically
                    CompoundTag blockEntityTag = (CompoundTag) saveWithFullMetadataMethod.invoke(blockEntity, holderProvider);

                    // Store the inventory data in custom persistent data
                    CompoundTag persistentData = blockEntityTag.getCompound("ForgeData");
                    persistentData.put("Items", inventoryTag.getList("Items", 10));

                } catch (NoSuchMethodException e) {
                    // Handle 1.20.x for BlockEntity
                    try {
                        Method saveMethod = blockEntity.getClass().getMethod("saveWithFullMetadata");
                        CompoundTag blockEntityTag = (CompoundTag) saveMethod.invoke(blockEntity);
                        CompoundTag persistentData = blockEntityTag.getCompound("ForgeData");
                        persistentData.put("Items", inventoryTag.getList("Items", 10));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // Ensure the BlockEntity knows it has changed
                blockEntity.setChanged();
            });
        }
    }

    private static Object getHolderLookupProvider(Level level) {
        try {
            // Handle for server-side: ServerLevel to get RegistryAccess
            if (level instanceof ServerLevel serverLevel) {
                // Try accessing 'asLookup' dynamically for 1.21.x
                Method asLookupMethod = serverLevel.registryAccess().getClass().getMethod("asLookup");
                return asLookupMethod.invoke(serverLevel.registryAccess());
            }

            // Handle for client-side: Use Minecraft to get RegistryAccess
            if (Minecraft.getInstance().level != null) {
                Method asLookupMethod = Minecraft.getInstance().level.registryAccess().getClass().getMethod("asLookup");
                return asLookupMethod.invoke(Minecraft.getInstance().level.registryAccess());
            }

        } catch (NoSuchMethodException e) {
            // If 'asLookup' does not exist (e.g., in 1.20.x), we fallback
            try {
                if (level instanceof ServerLevel serverLevel) {
                    return serverLevel.registryAccess();  // Return RegistryAccess directly for 1.20.x
                }
                if (Minecraft.getInstance().level != null) {
                    return Minecraft.getInstance().level.registryAccess();  // Return RegistryAccess directly for 1.20.x
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return null if HolderLookup.Provider cannot be obtained
        return null;
    }

    private static void spawnExp(BlockState blockState, ServerLevel level, BlockPos blockPos, ItemStack tool) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        int fortuneLevel = 0;
        int silkTouchLevel = 0;

        try {
            // For Minecraft 1.21.x or newer (using BuiltInRegistries)
            Class<?> builtInRegistriesClass = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            Object enchantmentRegistry = builtInRegistriesClass.getField("ENCHANTMENT").get(null);  // Fetch the ENCHANTMENT registry

            // Create ResourceLocation using reflection for 1.21.x
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Constructor<?> resourceLocationConstructor = resourceLocationClass.getDeclaredConstructor(String.class, String.class);
            resourceLocationConstructor.setAccessible(true);

            // Get ResourceKey for "block_fortune" and "silk_touch"
            Object fortuneResourceLocation = resourceLocationConstructor.newInstance("minecraft", "fortune");
            Object silkTouchResourceLocation = resourceLocationConstructor.newInstance("minecraft", "silk_touch");

            // Create the ResourceKey for the Enchantment registry
            Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");
            Method createMethod = resourceKeyClass.getMethod("create", Class.forName("net.minecraft.resources.ResourceKey"), resourceLocationClass);

            // Get the ResourceKey for Registry.ENCHANTMENT
            Object enchantmentRegistryKey = builtInRegistriesClass.getField("ENCHANTMENT").get(null);

            // Create the ResourceKey for fortune and silk_touch enchantments
            Object fortuneKey = createMethod.invoke(null, enchantmentRegistryKey, fortuneResourceLocation);
            Object silkTouchKey = createMethod.invoke(null, enchantmentRegistryKey, silkTouchResourceLocation);

            // Use the new getHolderOrThrow method to retrieve the Holder<Enchantment> for both enchantments
            Method getHolderOrThrowMethod = enchantmentRegistry.getClass().getMethod("getHolderOrThrow", resourceKeyClass);
            Holder<Enchantment> fortuneHolder = (Holder<Enchantment>) getHolderOrThrowMethod.invoke(enchantmentRegistry, fortuneKey);
            Holder<Enchantment> silkTouchHolder = (Holder<Enchantment>) getHolderOrThrowMethod.invoke(enchantmentRegistry, silkTouchKey);

            // Use the new getItemEnchantmentLevel method for both enchantments
            fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(fortuneHolder, tool);
            silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder, tool);

        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            // Fallback for Minecraft 1.20.x
            Method getFortuneLevelMethod = tool.getClass().getMethod("getFortuneLevel", Enchantment.class);
            fortuneLevel = (int) getFortuneLevelMethod.invoke(tool, Enchantments.FORTUNE);
            Method getEnchantmentLevelMethod = tool.getClass().getMethod("getEnchantmentLevel", Enchantment.class);
            silkTouchLevel = (int) getEnchantmentLevelMethod.invoke(tool, Enchantments.SILK_TOUCH);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Gets the XP expected to drop from a block
        int exp = blockState.getExpDrop(level, level.random, blockPos, fortuneLevel, silkTouchLevel);

        // If there's any XP to drop, it drops it
        if (exp > 0) {
            blockState.getBlock().popExperience(level, blockPos, exp);
        }
    }


    /**
     * Spawns block drops for the block at the specified position, applying special handling for Skulker Boxes
     * to retain their contents in the dropped item.
     *
     * @param player        The player breaking the block, used for loot context.
     * @param level         The server level where the block is located.
     * @param blockPos      The position of the block being broken.
     * @param tool          The tool used to break the block, used for loot context.
     * @param firstBlockPos The position where the dropped items should spawn.
     */
    private static void spawnBlockDrops(ServerPlayer player, ServerLevel level, BlockPos blockPos, ItemStack tool, BlockPos firstBlockPos) {
        // Attempt to retrieve the BlockEntity at the given position
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        CompoundTag blockEntityTag = null;

        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            try {
                // Check if we're in 1.21.x (using reflection to see if saveWithoutMetadata requires HolderLookup.Provider)
                Method saveWithoutMetadataMethod = blockEntity.getClass().getMethod("saveWithoutMetadata", HolderLookup.Provider.class);

                // For 1.21.x, we need to get the HolderLookup.Provider
                Method asLookupMethod = Objects.requireNonNull(blockEntity.getLevel()).registryAccess().getClass().getMethod("asLookup");
                Object lookup = asLookupMethod.invoke(blockEntity.getLevel().registryAccess());

                // Invoke the asProvider method on the lookup object to get HolderLookup.Provider
                Method asProviderMethod = lookup.getClass().getMethod("asProvider");
                Object holderProvider = asProviderMethod.invoke(lookup);

                // Call saveWithoutMetadata with the HolderLookup.Provider
                blockEntityTag = (CompoundTag) saveWithoutMetadataMethod.invoke(blockEntity, holderProvider);
            } catch (NoSuchMethodException e) {
                try {
                    // Fall back to 1.20.x saveWithoutMetadata method without HolderLookup.Provider
                    Method saveWithoutMetadataMethod = blockEntity.getClass().getMethod("saveWithoutMetadata");

                    // Call the saveWithoutMetadata method for 1.20.x
                    blockEntityTag = (CompoundTag) saveWithoutMetadataMethod.invoke(blockEntity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // Use the block's loot table to determine the items to drop, considering the tool used and the player
        List<ItemStack> itemsToDrop = Block.getDrops(level.getBlockState(blockPos), level, blockPos, null, player, tool);

        // Iterate through the determined items to drop
        for (ItemStack item : itemsToDrop) {
            // If we have saved BlockEntity data (from a Shulker Box) and the item is a Shulker Box, attach the data
            if (blockEntityTag != null && item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof ShulkerBoxBlock) {
                try {
                    // Use reflection to access getOrCreateTag and setTag in 1.21.x and 1.20.x
                    Method getOrCreateTagMethod = item.getClass().getMethod("getOrCreateTag");
                    CompoundTag itemTag = (CompoundTag) getOrCreateTagMethod.invoke(item);

                    itemTag.put("BlockEntityTag", blockEntityTag);

                    Method setTagMethod = item.getClass().getMethod("setTag", CompoundTag.class);
                    setTagMethod.invoke(item, itemTag);
                } catch (NoSuchMethodException e) {
                    // Handle potential cases where the methods might be renamed or unavailable
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Calculate random offset for drop position
            double d0 = (double) (level.random.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double) (level.random.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double) (level.random.nextFloat() * 0.5F) + 0.25D;

            // Create and spawn the item entity at the calculated position
            ItemEntity itemEntity = new ItemEntity(level, firstBlockPos.getX() + d0, firstBlockPos.getY() + d1, firstBlockPos.getZ() + d2, item);
            level.addFreshEntity(itemEntity);
        }
    }


    /**
     * Initiates the collection of connected blocks of the same type as the specified block.
     * It creates fresh lists for connected blocks and visited positions, then calls the
     * recursive collect method to find all connected blocks.
     *
     * @param level       The level where the block exists.
     * @param pos         The position of the block being vein mined.
     * @param targetState The BlockState of the block being vein mined.
     * @return A list of BlockPos representing all connected blocks of the same type.
     */
    public static List<BlockPos> collectConnectedBlocks(Level level, BlockPos pos, BlockState targetState,
                                                        Vec3i lookPos, int vineableLimit, boolean isShapeVine, int heightAbove,
                                                        int heightBelow, int widthLeft, int widthRight, int layerOffset) {
        List<BlockPos> connectedBlocks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();

        if (isShapeVine) {
            collectConfigurablePattern(level, lookPos, pos, targetState, connectedBlocks, visited, vineableLimit,
                    heightAbove, heightBelow, widthLeft, widthRight, layerOffset);
        } else {
            collect(level, pos, targetState, connectedBlocks, visited, vineableLimit);
        }

        return connectedBlocks;
    }


    /**
     * Recursively collects connected blocks of the same type to the specified block,
     * up to a maximum limit defined in VinerBlockRegistry. This method checks adjacent
     * and diagonal blocks for the same block type, adding them to a list if they match.
     *
     * @param level           The level where the block exists.
     * @param pos             The position of the current block being checked.
     * @param targetState     The BlockState of the block being vein mined.
     * @param connectedBlocks A list to store positions of connected blocks of the same type.
     * @param visited         A set to keep track of already visited positions, to avoid infinite recursion.
     */

    private static void collect(Level level, BlockPos pos, BlockState targetState, List<BlockPos> connectedBlocks, Set<BlockPos> visited, int vineableLimit) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);

        while (!queue.isEmpty() && connectedBlocks.size() < vineableLimit) {
            BlockPos currentPos = queue.poll();

            if (visited.contains(currentPos) || !targetState.getBlock().equals(level.getBlockState(currentPos).getBlock())) {
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
     * Mining progresses depth-wise in the direction the player is looking, respecting the limit from VinerBlockRegistry
     *
     * @param level           The level where the block exists
     * @param lookPos         The current look position of the player, determining the depth direction for mining
     * @param pos             Starting position for mining
     * @param targetState     The BlockState of the block to be mined
     * @param connectedBlocks List to store positions of mined blocks
     * @param visited         Set to track visited positions, preventing recursion loops
     * @param heightAbove     Number of blocks to mine above the starting block
     * @param heightBelow     Number of blocks to mine below the starting block
     * @param widthLeft       Number of blocks to mine left of the starting block
     * @param widthRight      Number of blocks to mine right of the starting block
     */
    private static void collectConfigurablePattern(Level level, Vec3i lookPos, BlockPos pos, BlockState targetState, List<BlockPos> connectedBlocks, Set<BlockPos> visited, int vineableLimit, int heightAbove, int heightBelow, int widthLeft, int widthRight, int layerOffset) {
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
                    BlockPos blockToMine = currentPos.offset(
                            horizontalDirection.getX() * w, h,
                            horizontalDirection.getZ() * w);
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
            BlockPos nextDepthBlock = currentPos.offset(lookPos.getX(), lookPos.getY() - layerOffset, lookPos.getZ());
            if (!visited.contains(nextDepthBlock) && !level.getBlockState(nextDepthBlock).isAir()) {
                queue.add(nextDepthBlock);
            }
        }
    }

    /**
     * Checks if a block is vineable based on predefined criteria.
     * A block is considered vineable if:
     * 1. It does not exist in the list of unvineable blocks.
     * 2. It either exists within specified tags or in the list of vineable blocks.
     *
     * @param block The block to be checked.
     * @return true if the block is vineable, false otherwise.
     */
    public static boolean isVineable(Block block, Player player) {
        return Viner.getInstance().getPlayerRegistry().getPlayerData(player).isVineAllEnabled() || (!blockExistsInUnvineableBlocks(block, player) && blockExistsInVineableBlocks(block, player));
    }


    /**
     * Checks if a specified block is listed as unvineable in the registry.
     *
     * @param block The block to check.
     * @return true if the block is unvineable, false otherwise.
     */
    private static boolean blockExistsInUnvineableBlocks(Block block, Player player) {
        return Viner.getInstance().getPlayerRegistry().getPlayerData(player).getUnvineableBlocks().contains(block) || blockExistsInUnvineableTags(block, player);
    }

    /**
     * Checks if a specified block is listed as vineable in the registry.
     *
     * @param block The block to check.
     * @return true if the block is vineable, false otherwise.
     */
    private static boolean blockExistsInVineableBlocks(Block block, Player player) {
        return Viner.getInstance().getPlayerRegistry().getPlayerData(player).getVineableBlocks().contains(block) || blockExistsInVineableTags(block, player);
    }

    /**
     * Checks if a specified block is listed as vineable under any tag in the registry.
     *
     * @param block The block to check.
     * @return true if the block is vineable under any tag, false otherwise.
     */
    private static boolean blockExistsInVineableTags(Block block, Player player) {
        // Iterating through each tag to check if the block is vineable under any tag
        List<TagKey<Block>> tags = Viner.getInstance().getPlayerRegistry().getPlayerData(player).getVineableTags();
        for (var tagKey : tags) {
            if (tagContainsBlock(tagKey, block)) {
                return true;
            }
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
        // Iterating through each tag to check if the block is vineable under any tag
        List<TagKey<Block>> tags = Viner.getInstance().getPlayerRegistry().getPlayerData(player).getUnvineableTags();
        for (var tagKey : tags) {
            if (tagContainsBlock(tagKey, block)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if a specified block is contained within a given tag.
     *
     * @param tagKey The key of the tag to check.
     * @param block  The block to check for within the tag.
     * @return true if the tag contains the block, false otherwise.
     */
    private static boolean tagContainsBlock(TagKey<Block> tagKey, Block block) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).getTag(tagKey).contains(block);
    }


    /**
     * Retrieves the level of the Unbreaking enchantment on a specified tool.
     *
     * @param tool The tool whose Unbreaking enchantment level is to be checked.
     * @return The level of the Unbreaking enchantment, or 0 if not present.
     */
    public static int getUnbreakingLevel(ItemStack tool) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int unbreakingLevel = 0;

        try {
            // For Minecraft 1.21.x or newer (using BuiltInRegistries)
            Class<?> builtInRegistriesClass = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            Object enchantmentRegistry = builtInRegistriesClass.getField("ENCHANTMENT").get(null);  // Fetch the ENCHANTMENT registry

            // Create ResourceLocation using reflection for 1.21.x
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Constructor<?> resourceLocationConstructor = resourceLocationClass.getDeclaredConstructor(String.class, String.class);
            resourceLocationConstructor.setAccessible(true);

            // Get ResourceLocation for "unbreaking"
            Object unbreakingResourceLocation = resourceLocationConstructor.newInstance("minecraft", "unbreaking");

            // Create the ResourceKey for the Enchantment registry
            Class<?> resourceKeyClass = Class.forName("net.minecraft.resources.ResourceKey");
            Method createMethod = resourceKeyClass.getMethod("create", Class.forName("net.minecraft.resources.ResourceKey"), resourceLocationClass);

            // Get the ResourceKey for Registry.ENCHANTMENT
            Object enchantmentRegistryKey = builtInRegistriesClass.getField("ENCHANTMENT").get(null);

            // Create the ResourceKey for unbreaking enchantment
            Object unbreakingKey = createMethod.invoke(null, enchantmentRegistryKey, unbreakingResourceLocation);

            // Use the new getHolderOrThrow method to retrieve the Holder<Enchantment> for unbreaking
            Method getHolderOrThrowMethod = enchantmentRegistry.getClass().getMethod("getHolderOrThrow", resourceKeyClass);
            Holder<Enchantment> unbreakingHolder = (Holder<Enchantment>) getHolderOrThrowMethod.invoke(enchantmentRegistry, unbreakingKey);

            // Use the new getItemEnchantmentLevel method for unbreaking
            unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(unbreakingHolder, tool);

        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            // Fallback for Minecraft 1.20.x
            Method getUnbreakingLevelMethod = tool.getClass().getMethod("getUnbreakingLevel", Enchantment.class);
            unbreakingLevel = (int) getUnbreakingLevelMethod.invoke(tool, Enchantments.SILK_TOUCH);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return unbreakingLevel;
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
     * @param tool   The tool to be damaged.
     * @param damage The amount of damage to apply.
     * @return A boolean containing whether the applyDamage function broke the weapon
     */
    public static boolean applyDamage(@NotNull ItemStack tool, int damage) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        if (!tool.isDamageableItem())
            return false;

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
