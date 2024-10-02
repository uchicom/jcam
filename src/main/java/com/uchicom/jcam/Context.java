// (C) 2024 Rigger LLC
package com.uchicom.jcam;

import com.uchicom.jcam.enumeration.Config;
import com.uchicom.jcam.module.MainModule;
import com.uchicom.util.ResourceUtil;
import dagger.Component;
import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

public class Context {

  private static final Logger logger = DaggerContext_MainComponent.create().logger();

  @Component(modules = MainModule.class)
  interface MainComponent {
    Logger logger();
  }

  private static final Context context = new Context();
  private final Properties configProperties;

  private Context() {
    // 設定
    var propertyFilePath =
        System.getProperty("com.uchicom.jcam.config", "src/main/resources/config.properties");
    logger.info(propertyFilePath);

    configProperties = ResourceUtil.createProperties(new File(propertyFilePath), "UTF-8");
  }

  /** 設定 */
  public static String get(Config config) {
    return context.configProperties.getProperty(config.name());
  }

  /** 設定 */
  public static Integer getInteger(Config config) {
    return Integer.parseInt(context.configProperties.getProperty(config.name()));
  }

  /** 設定 */
  public static Boolean getBoolean(Config config) {
    return Boolean.parseBoolean(context.configProperties.getProperty(config.name()));
  }
}
