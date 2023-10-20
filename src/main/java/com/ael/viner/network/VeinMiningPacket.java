package com.ael.viner.network;

import com.ael.viner.util.MiningUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
        List<BlockPos> blocksToMine = msg.blockPosList;
        MiningUtils.mineBlocks(player, blocksToMine);
    }


}
