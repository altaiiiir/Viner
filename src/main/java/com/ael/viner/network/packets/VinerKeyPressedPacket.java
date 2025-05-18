package com.ael.viner.network.packets;

import com.ael.viner.common.VinerEntrypoint;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class VinerKeyPressedPacket extends AbstractPacket<Boolean> {

    public VinerKeyPressedPacket(boolean keyPressed) {
        super(keyPressed);
    }

    public static final PacketFactory<VinerKeyPressedPacket> FACTORY = buf ->
            new VinerKeyPressedPacket(buf.readBoolean());


    @Override
    public void handle(AbstractPacket<Boolean> msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> VinerEntrypoint.get().getPlayerRegistry().setVineKeyPressed(context.getSender(), msg.getData()));
    }

}
