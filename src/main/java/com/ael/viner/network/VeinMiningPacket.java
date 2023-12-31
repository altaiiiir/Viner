package com.ael.viner.network;

import com.ael.viner.util.MiningUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class VeinMiningPacket extends AbstractPacket<List<BlockPos>>{

    public VeinMiningPacket(List<BlockPos> blockPosList) {
        super(blockPosList);
    }

    public static final PacketFactory<VeinMiningPacket> FACTORY = buf ->
            new VeinMiningPacket(buf.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readBlockPos));

    @Override
    public void handle(AbstractPacket<List<BlockPos>> msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> processMiningPacket(msg, ctx.get()));
        ctx.get().setPacketHandled(true);
    }

    public static void processMiningPacket(@NotNull AbstractPacket<List<BlockPos>> msg, NetworkEvent.@NotNull Context context) {
        ServerPlayer player = context.getSender();
        List<BlockPos> blocksToMine = msg.getData();
        MiningUtils.mineBlocks(player, blocksToMine);
    }
}

