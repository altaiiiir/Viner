package com.ael.viner;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
import org.slf4j.Logger;

import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Viner.MOD_ID)
public class Viner
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "viner";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // Define the list of blocks that can be vein-mined
    private static final Set<Block> VINEABLE_BLOCKS = new HashSet<>(Arrays.asList(
            Blocks.STONE,
            Blocks.IRON_ORE,
            Blocks.COAL_ORE,
            Blocks.ACACIA_WOOD,
            Blocks.OAK_WOOD
            // Add more blocks here as needed
    ));


    public Viner()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);



    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientModEvents
    {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }

        // Utility method to collect connected blocks of the same type
        private static void collectConnectedBlocks(Level world, BlockPos pos, BlockState targetState, List<BlockPos> connectedBlocks, Set<BlockPos> visited) {
            if (!visited.contains(pos) && targetState.getBlock().equals(world.getBlockState(pos).getBlock())) {
                visited.add(pos);
                connectedBlocks.add(pos);

                for (Direction direction : Direction.values()) {
                    collectConnectedBlocks(world, pos.offset(direction.getNormal()), targetState, connectedBlocks, visited);
                }
            }
        }

        @SubscribeEvent
        public static void onBlockBroken(Event baseEvent) {

            // ensure event is a blockBreak event
            if(!(baseEvent instanceof BlockEvent.BreakEvent event)) return;

            if (!event.getLevel().isClientSide()) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && player.isCrouching()) {
                    //player.sendSystemMessage(Component.literal("block broken"));
                    BlockPos pos = event.getPos();
                    BlockState targetBlockState = event.getLevel().getBlockState(pos);

                    if (VINEABLE_BLOCKS.contains(targetBlockState.getBlock())) {
                        List<BlockPos> connectedBlocks = new ArrayList<>();
                        Set<BlockPos> visited = new HashSet<>();
                        collectConnectedBlocks((Level) event.getLevel(), pos, targetBlockState, connectedBlocks, visited);

                        int blockCount = 0;

                        // Loop through the connected blocks and break them
                        for (BlockPos connectedPos : connectedBlocks) {
                            BlockState connectedBlockState = event.getLevel().getBlockState(connectedPos);
                            if (VINEABLE_BLOCKS.contains(connectedBlockState.getBlock())) {
                                Block.dropResources(connectedBlockState, (Level) event.getLevel(), event.getPos());
                                event.getLevel().removeBlock(connectedPos, false);
                                blockCount++;
                            }
                        }
                        ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
                        int currentItemDurability = item.getMaxDamage() - item.getDamageValue();
                        item.setDamageValue(currentItemDurability-blockCount);
                        LOGGER.info("Item Damage: " + item.getDamageValue());
                    }
                }
            }
        }

    }
}
