package com.ael.viner.network;

import com.ael.viner.Viner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class VinerKeyPressedPacket {

    private final boolean shiftPressed;

    public VinerKeyPressedPacket(boolean shiftPressed){
        this.shiftPressed = shiftPressed;
    }

    public static void encode(@NotNull VinerKeyPressedPacket msg, @NotNull FriendlyByteBuf buf){
        buf.writeBoolean(msg.shiftPressed);
    }

    public static @NotNull VinerKeyPressedPacket decode(@NotNull FriendlyByteBuf buf) {
        return new VinerKeyPressedPacket(buf.readBoolean());
    }

    public static void handle(VinerKeyPressedPacket msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> Viner.getInstance().getPlayerRegistry().setVineKeyPressed(context.getSender(), msg.shiftPressed));
    }
}
