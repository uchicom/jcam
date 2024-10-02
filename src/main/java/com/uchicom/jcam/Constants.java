// (C) 2024 Rigger LLC
package com.uchicom.jcam;

import com.uchicom.jcam.enumeration.Config;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class Constants {
  public static final ZoneId ZONE_ID = ZoneId.of(Context.get(Config.ZONE_ID));
  public static final ZoneOffset ZONE_OFFSET =
      ZoneOffset.ofHours(Context.getInteger(Config.ZONE_OFFSET));

  public static final String LOG_DIR = Context.get(Config.LOG_DIR);
  public static final String LOG_FORMAT = Context.get(Config.LOG_FORMAT);
}
