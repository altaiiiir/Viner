package com.ael.viner.network.packets;

import com.ael.viner.Viner;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

public class MouseScrollPacket extends AbstractPacket<Double> {
    public static final PacketFactory<MouseScrollPacket> FACTORY = buf -> {
        double scrollDelta = buf.readDouble();
        return new MouseScrollPacket(scrollDelta);
    };

    public MouseScrollPacket(Double data) {
        super(data);
    }

    public static void processScrollEvent(Double data, Object context) {
        try {
            // Use reflection to get the player (ServerPlayer) from the context
            Method getSenderMethod = context.getClass().getMethod("getSender");
            ServerPlayer player = (ServerPlayer) getSenderMethod.invoke(context);
            double scrollDelta = data;

            if (Viner.getInstance().getPlayerRegistry()
                    .getPlayerData(Objects.requireNonNull(player))
                    .isVineKeyPressed() && scrollDelta != 0) {

                // Old Scroll Data Handling, might be useful in the future
                Component message = Component.literal("Shape vine enabled");
                player.sendSystemMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(AbstractPacket<Double> msg, @NotNull Supplier<Object> ctxSupplier) {
        try {
            Object context = ctxSupplier.get();

            // Use reflection to access enqueueWork and setPacketHandled
            Method enqueueWorkMethod = context.getClass().getMethod("enqueueWork", Runnable.class);
            Method setPacketHandledMethod = context.getClass().getMethod("setPacketHandled", boolean.class);

            // Schedule the work to be done on the network thread
            enqueueWorkMethod.invoke(context, (Runnable) () -> processScrollEvent(msg.getData(), context));

            // Mark the packet as handled
            setPacketHandledMethod.invoke(context, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
