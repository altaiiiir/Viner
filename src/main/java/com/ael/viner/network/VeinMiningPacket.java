package com.ael.viner.network;

import com.ael.viner.registry.VinerBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class VeinMiningPacket {
    private final List<BlockPos> blockPosList;

    public VeinMiningPacket(List<BlockPos> blockPosList) {
        this.blockPosList = blockPosList;
    }

    public static void encode(VeinMiningPacket msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.blockPosList, FriendlyByteBuf::writeBlockPos);
    }

    public static VeinMiningPacket decode(FriendlyByteBuf buf) {
        return new VeinMiningPacket(buf.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readBlockPos));
    }

    public static void handle(VeinMiningPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();

            if (player == null)
                return;

            Level level = player.level();
            ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (level.isClientSide())
                return;

            // We ideally want all the drops to spawn at the first blockPos
            BlockPos firstBlockPos = msg.blockPosList.get(0);

            for (BlockPos blockPos: msg.blockPosList) {

                // Checking for Silk Touch enchantment
                boolean hasSilkTouch = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

                // Initializing block count
                int blockCount = 0;

                // Exit condition if block count reaches veinmineable limit
                if (blockCount >= VinerBlockRegistry.getVeinableLimit())
                    break;

                // Getting block state of connected block
                BlockState blockState = level.getBlockState(blockPos);

                if (hasSilkTouch) {
                    Block.popResource(level, firstBlockPos, new ItemStack(blockState.getBlock()));
                } else {
                    Block.dropResources(blockState, level, firstBlockPos);
                }

                // Removing block from world
                level.removeBlock(blockPos, false);
                blockCount++;

                // Updating tool damage
                // TODO: Should we take efficacy into account?
                ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
                item.setDamageValue(item.getDamageValue() + blockCount);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
