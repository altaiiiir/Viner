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

/**
 * Packet class for handling vein mining operations, encapsulating a list of block positions to be mined.
 */
public class VeinMiningPacket {

    /**
     * List of block positions to be vein mined.
     */
    private final List<BlockPos> blockPosList;

    /**
     * Constructor initializing the block position list.
     *
     * @param blockPosList List of block positions to be vein mined.
     */
    public VeinMiningPacket(List<BlockPos> blockPosList) {
        this.blockPosList = blockPosList;
    }

    /**
     * Encodes this packet into a byte buffer for transmission.
     *
     * @param msg The message to encode.
     * @param buf The byte buffer to write to.
     */
    public static void encode(@NotNull VeinMiningPacket msg, @NotNull FriendlyByteBuf buf) {
        buf.writeCollection(msg.blockPosList, FriendlyByteBuf::writeBlockPos);
    }

    /**
     * Decodes a byte buffer into a VeinMiningPacket.
     *
     * @param buf The byte buffer to read from.
     * @return The decoded VeinMiningPacket.
     */

    public static @NotNull VeinMiningPacket decode(@NotNull FriendlyByteBuf buf) {
        return new VeinMiningPacket(buf.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readBlockPos));
    }

    /**
     * Handles this packet on the network thread.
     *
     * @param msg The message to handle.
     * @param ctx The network context.
     */
    public static void handle(VeinMiningPacket msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> processMiningPacket(msg, ctx.get()));
        ctx.get().setPacketHandled(true);
    }

    /**
     * Processes the vein mining packet, initiating the vein mining operation.
     *
     * @param msg The vein mining packet.
     * @param context The network context.
     */
    public static void processMiningPacket(@NotNull VeinMiningPacket msg, NetworkEvent.@NotNull Context context) {
        ServerPlayer player = context.getSender();
        List<BlockPos> blocksToMine = msg.blockPosList;
        MiningUtils.mineBlocks(player, blocksToMine);
    }
}

