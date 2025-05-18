package com.ael.viner.forge.network.packets;

import com.ael.viner.common.VinerCore;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Packet sent from client to server when the vine key is pressed or released. */
public class VinerKeyPressedPacket {
  private final boolean pressed;

  public VinerKeyPressedPacket(boolean pressed) {
    this.pressed = pressed;
  }

  public boolean isPressed() {
    return pressed;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeBoolean(pressed);
  }

  public static VinerKeyPressedPacket decode(FriendlyByteBuf buf) {
    return new VinerKeyPressedPacket(buf.readBoolean());
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get()
        .enqueueWork(
            () -> {
              ServerPlayer player = ctx.get().getSender();
              if (player != null) {
                VinerCore.getPlayerRegistry().setVineKeyPressed(player, pressed);
              }
            });
    ctx.get().setPacketHandled(true);
  }
}
