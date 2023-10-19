package com.ael.viner.network;

import com.ael.viner.util.MiningUtils;
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
        ctx.get().enqueueWork(() -> processMiningPacket(msg, ctx.get()));
        ctx.get().setPacketHandled(true);
    }

    public static void processMiningPacket(VeinMiningPacket msg, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();

        if (player == null)
            return;

        Level level = player.level();
        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);

        // I don't think this is possible, but can't be sure.
        if (level.isClientSide())
            return;

        // We ideally want all the drops to spawn at the first blockPos
        BlockPos firstBlockPos = msg.blockPosList.get(0);

        for (BlockPos blockPos: msg.blockPosList) {

            // Getting block state of connected block
            BlockState blockState = level.getBlockState(blockPos);

            // Checking for Silk Touch enchantment
            boolean hasSilkTouch = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

            if (hasSilkTouch) {
                Block.popResource(level, firstBlockPos, new ItemStack(blockState.getBlock()));
            } else {
                // FIXME: We need to implement logic for Fortune
                Block.dropResources(blockState, level, firstBlockPos);
            }

            // Removing block from world
            level.removeBlock(blockPos, false);

            // Updating tool damage
            ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
            int unbreakingLevel = MiningUtils.getUnbreakingLevel(tool);
            double chance = MiningUtils.getDamageChance(unbreakingLevel);

            if (Math.random() < chance) {
                MiningUtils.applyDamage(tool, msg.blockPosList.size());  // assuming 1 damage per block
            }
        }
    }
}
