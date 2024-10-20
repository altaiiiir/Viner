package com.ael.viner.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
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
     * Uses reflection to access methods dynamically in case they change between Forge versions.
     *
     * @param msg The message to encode.
     * @param buf The byte buffer to write to.
     */
    public static <T> void encode(@NotNull AbstractPacket<T> msg, @NotNull FriendlyByteBuf buf) {
        try {
            if (msg.getData() instanceof Boolean) {
                Method writeBooleanMethod = buf.getClass().getMethod("writeBoolean", boolean.class);
                writeBooleanMethod.invoke(buf, msg.getData());

            } else if (msg.getData() instanceof Double) {
                Method writeDoubleMethod = buf.getClass().getMethod("writeDouble", double.class);
                writeDoubleMethod.invoke(buf, msg.getData());

            } else if (msg.getData() instanceof Collection) {
                Collection<BlockPos> collection = (Collection<BlockPos>) msg.getData();
                if (!collection.isEmpty() && collection.iterator().next() instanceof BlockPos) {
                    Method writeCollectionMethod = buf.getClass().getMethod("writeCollection", Collection.class, Method.class);
                    Method writeBlockPosMethod = buf.getClass().getMethod("writeBlockPos", BlockPos.class);
                    writeCollectionMethod.invoke(buf, collection, writeBlockPosMethod);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Decodes this packet from a byte buffer using a PacketFactory.
     *
     * @param buf The byte buffer to read from.
     * @param factory The factory that creates the packet.
     * @param <T> The type of the packet.
     * @return The decoded packet.
     */
    public static <T extends AbstractPacket<?>> T decode(@NotNull FriendlyByteBuf buf, PacketFactory<T> factory) {
        return factory.create(buf);
    }

    /**
     * Handles this packet upon receipt.
     * Uses reflection to handle NetworkEvent.Context dynamically.
     *
     * @param msg The packet message.
     * @param ctx The network context supplier.
     */
    public void handle(AbstractPacket<T> msg, @NotNull Supplier<Object> ctx) {
        handleContext(msg, ctx); // Call handleContext to manage NetworkEvent.Context through reflection
    }

    /**
     * Helper method to handle reflection for NetworkEvent.Context
     */
    protected void handleContext(AbstractPacket<T> msg, Supplier<Object> ctxSupplier) {
        try {
            Object context = ctxSupplier.get(); // Get the NetworkEvent.Context object dynamically

            // Dynamically determine which version of Minecraft/Forge we are running
            boolean usePayloadChannel = false;

            try {
                // Check if PayloadChannel (new networking system) exists, implying 1.21.x or later
                Class<?> payloadChannelClass = Class.forName("net.minecraftforge.network.PayloadChannel");
                usePayloadChannel = true;
            } catch (ClassNotFoundException e) {
                // PayloadChannel doesn't exist, assume we are on 1.20.x or an earlier system
                usePayloadChannel = false;
            }

            // Now dynamically handle based on the version detected
            if (usePayloadChannel) {
                // Newer version (1.21.x or future) handling using PayloadChannel
                Method enqueueWorkMethod = context.getClass().getMethod("enqueueWork", Runnable.class);
                Method setPacketHandledMethod = context.getClass().getMethod("setPacketHandled", boolean.class);

                enqueueWorkMethod.invoke(context, (Runnable) () -> {
                    // Your packet handling logic for 1.21.x or newer
                    System.out.println("Handling packet for newer version (1.21.x or later): " + msg.getData());
                });

                setPacketHandledMethod.invoke(context, true);

            } else {
                // Older version (1.20.x) handling using SimpleChannel
                Method enqueueWorkMethod = context.getClass().getMethod("enqueueWork", Runnable.class);
                Method setPacketHandledMethod = context.getClass().getMethod("setPacketHandled", boolean.class);

                enqueueWorkMethod.invoke(context, (Runnable) () -> {
                    // Your packet handling logic for 1.20.x or earlier
                    System.out.println("Handling packet for 1.20.x or earlier: " + msg.getData());
                });

                setPacketHandledMethod.invoke(context, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
