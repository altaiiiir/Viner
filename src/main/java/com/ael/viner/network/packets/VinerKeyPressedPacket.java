package com.ael.viner.network.packets;

import com.ael.viner.Viner;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class VinerKeyPressedPacket extends AbstractPacket<Boolean> {

    public VinerKeyPressedPacket(boolean keyPressed) {
        super(keyPressed);
    }

    public static final PacketFactory<VinerKeyPressedPacket> FACTORY = buf ->
            new VinerKeyPressedPacket(buf.readBoolean());

    @Override
    public void handle(AbstractPacket<Boolean> msg, Supplier<Object> ctxSupplier) {
        try {
            // Get the context object dynamically
            Object context = ctxSupplier.get();

            // Use reflection to call the appropriate methods on the context object
            Method enqueueWorkMethod = context.getClass().getMethod("enqueueWork", Runnable.class);
            Method setPacketHandledMethod = context.getClass().getMethod("setPacketHandled", boolean.class);

            enqueueWorkMethod.invoke(context, (Runnable) () -> {
                // Use Viner's player registry to set the key press status
                Viner.getInstance().getPlayerRegistry().setVineKeyPressed(null, msg.getData()); // no need for a player reference
            });

            // Mark the packet as handled
            setPacketHandledMethod.invoke(context, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
