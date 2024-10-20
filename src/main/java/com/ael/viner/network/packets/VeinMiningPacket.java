package com.ael.viner.network.packets;

import com.ael.viner.util.MiningUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

public class VeinMiningPacket extends AbstractPacket<List<BlockPos>> {

    public VeinMiningPacket(List<BlockPos> blockPosList) {
        super(blockPosList);
    }

    public static final PacketFactory<VeinMiningPacket> FACTORY = buf ->
            new VeinMiningPacket(buf.readCollection(NonNullList::createWithCapacity, VeinMiningPacket::readBlockPos));

    /**
     * Resolves the ambiguity by explicitly calling the appropriate method.
     */
    private static BlockPos readBlockPos(FriendlyByteBuf buf) {
        return buf.readBlockPos(); // Ensure we're calling the correct method signature
    }

    @Override
    public void handle(AbstractPacket<List<BlockPos>> msg, Supplier<Object> ctxSupplier) {
        try {
            Object context = ctxSupplier.get();

            Method enqueueWorkMethod = context.getClass().getMethod("enqueueWork", Runnable.class);
            Method setPacketHandledMethod = context.getClass().getMethod("setPacketHandled", boolean.class);

            enqueueWorkMethod.invoke(context, (Runnable) () -> {
                try {
                    ServerPlayer player = (ServerPlayer) context.getClass().getMethod("getSender").invoke(context);
                    processMiningPacket(msg, player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            setPacketHandledMethod.invoke(context, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processMiningPacket(AbstractPacket<List<BlockPos>> msg, ServerPlayer player) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<BlockPos> blocksToMine = msg.getData();
        MiningUtils.mineBlocks(player, blocksToMine);
    }
}
