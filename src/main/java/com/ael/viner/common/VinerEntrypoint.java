package com.ael.viner.common;

public class VinerEntrypoint {
  private static IVinerMod instance;

  public static void setInstance(IVinerMod mod) {
    instance = mod;
  }

  public static IVinerMod get() {
    return instance;
  }
}
