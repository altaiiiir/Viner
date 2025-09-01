package com.ael.viner.common;

import com.ael.viner.common.config.IConfigManager;

public interface IVinerMod {
  IPlayerRegistry getPlayerRegistry();

  IConfigManager getConfigManager();
}
