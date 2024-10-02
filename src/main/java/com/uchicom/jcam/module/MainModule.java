// (C) 2024 Rigger LLC
package com.uchicom.jcam.module;

import com.uchicom.jcam.logger.DailyRollingFileHandler;
import dagger.Provides;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

@dagger.Module
public class MainModule {

  @Provides
  static Logger logger() {
    try {
      var PROJECT_NAME = "jcam";
      var name =
          Stream.of(Thread.currentThread().getStackTrace())
              .map(StackTraceElement::getClassName)
              .filter(className -> className.endsWith("Main"))
              .findFirst()
              .orElse(PROJECT_NAME);
      Logger logger = Logger.getLogger(name);
      if (!PROJECT_NAME.equals(name)) {
        if (Arrays.stream(logger.getHandlers())
            .filter(handler -> handler instanceof DailyRollingFileHandler)
            .findFirst()
            .isEmpty()) {
          logger.addHandler(new DailyRollingFileHandler(name + "_%d.log"));
        }
      }
      return logger;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
