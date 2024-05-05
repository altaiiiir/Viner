package com.ael.viner.network.packets;

import com.ael.viner.Viner;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

public class MouseScrollPacket extends AbstractPacket<Double> {
    public static final PacketFactory<MouseScrollPacket> FACTORY = buf -> {
        double scrollDelta = buf.readDouble();
        return new MouseScrollPacket(scrollDelta);
    };
    private static final Logger LOGGER = LogUtils.getLogger();

    public MouseScrollPacket(Double data) {
        super(data);
    }

    public static void processScrollEvent(Double data, NetworkEvent.@NotNull Context context) {
        ServerPlayer player = context.getSender();
        double scrollDelta = data;

        if (Viner.getInstance().getPlayerRegistry()
                .getPlayerData(Objects.requireNonNull(context.getSender()))
                .isVineKeyPressed() && scrollDelta != 0) {

            // Old Scroll Data Handling, might be useful in the future

            Component message = Component.literal("Shape vine enabled");
            assert player != null;
            player.sendSystemMessage(message);
        }
    }

    @Override
    public void handle(AbstractPacket<Double> msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> processScrollEvent(msg.getData(), ctx.get()));
        ctx.get().setPacketHandled(true);
    }

}