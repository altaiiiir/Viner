package com.ael.viner.forge.network.packets;

import com.ael.viner.common.VinerCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public final class VinerKeyPressedPacket extends AbstractPacket<Boolean> {
  public VinerKeyPressedPacket(boolean pressed) {
    super(pressed);
  }

  public boolean pressed() {
    return data();
  }

  public static void encode(VinerKeyPressedPacket pkt, FriendlyByteBuf buf) {
    buf.writeBoolean(pkt.pressed());
  }

  public static VinerKeyPressedPacket decode(FriendlyByteBuf buf) {
    return new VinerKeyPressedPacket(buf.readBoolean());
  }

  @Override
  public void handle(CustomPayloadEvent.Context ctx) {
    var player = ctx.getSender();
    if (player != null) {
      VinerCore.getPlayerRegistry().setVineKeyPressed(player, pressed());
    }
    ctx.setPacketHandled(true);
  }
}
