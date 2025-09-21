package com.ael.viner.forge.network.packets;

import net.minecraftforge.event.network.CustomPayloadEvent;

/** Base class for Forge 1.21.8 packets: data holder + instance handler. */
public abstract class AbstractPacket<T> {
  private final T data;

  protected AbstractPacket(T data) {
    this.data = data;
  }

  public T data() {
    return data;
  }

  /** Handle on the main thread via SimpleChannel.consumerMainThread(PacketClass::handle). */
  public abstract void handle(CustomPayloadEvent.Context ctx);
}
