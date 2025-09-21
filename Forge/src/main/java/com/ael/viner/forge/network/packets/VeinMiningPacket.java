package com.ael.viner.forge.network.packets;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public final class VeinMiningPacket extends AbstractPacket<List<BlockPos>> {
  public VeinMiningPacket(List<BlockPos> blocks) {
    super(blocks);
  }

  public List<BlockPos> blocks() {
    return data();
  }

  public static void encode(VeinMiningPacket pkt, FriendlyByteBuf buf) {
    buf.writeCollection(pkt.blocks(), (FriendlyByteBuf b, BlockPos p) -> b.writeBlockPos(p));
  }

  public static VeinMiningPacket decode(FriendlyByteBuf buf) {
    return new VeinMiningPacket(buf.readList((FriendlyByteBuf b) -> b.readBlockPos()));
  }

  @Override
  public void handle(CustomPayloadEvent.Context ctx) {
    var player = ctx.getSender();
    if (player != null) {
      com.ael.viner.forge.util.MiningUtils.mineBlocks(player, blocks());
    }
    ctx.setPacketHandled(true);
  }
}
