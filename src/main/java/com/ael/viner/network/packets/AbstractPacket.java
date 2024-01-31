package com.ael.viner.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

public abstract class AbstractPacket<T> {
    private final T data;

    public interface PacketFactory<T extends AbstractPacket<?>> {
        T create(FriendlyByteBuf buf);
    }

    public AbstractPacket(T data) {
        this.data = data;
    }

    protected T getData() {
        return data;
    }


    /**
     * Encodes this packet into a byte buffer for transmission.
     *
     * @param msg The message to encode.
     * @param buf The byte buffer to write to.
     */
    public static <T> void encode(@NotNull AbstractPacket<T> msg, @NotNull FriendlyByteBuf buf){
        if (msg.getData() instanceof Boolean) {
            buf.writeBoolean((Boolean) msg.getData());
        } else if (msg.getData() instanceof Double) {
            buf.writeDouble((Double) msg.getData());
        } else if (msg.getData() instanceof Collection) {
            Collection<BlockPos> collection = (Collection<BlockPos>) msg.getData();
            if (!collection.isEmpty() && collection.iterator().next() instanceof BlockPos) {
                buf.writeCollection(collection, FriendlyByteBuf::writeBlockPos);
            }
        }
    }

    public static <T extends AbstractPacket<?>> T decode(@NotNull FriendlyByteBuf buf, PacketFactory<T> factory) {
        return factory.create(buf);
    }

    public abstract void handle(AbstractPacket<T> msg, @NotNull Supplier<NetworkEvent.Context> ctx);
}