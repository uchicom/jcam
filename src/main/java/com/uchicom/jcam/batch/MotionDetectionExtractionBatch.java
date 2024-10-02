// (C) 2024 Rigger LLC
package com.uchicom.jcam.batch;

import com.uchicom.jcam.module.MainModule;
import com.uchicom.jcam.service.DateTimeService;
import com.uchicom.util.Parameter;
import dagger.Component;
import jakarta.inject.Inject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.codecs.h264.BufferH264ES;
import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.jcodec.scale.ColorUtil;

public class MotionDetectionExtractionBatch {

  @Component(modules = MainModule.class)
  interface MainComponent {
    MotionDetectionExtractionBatch main();
  }

  public static void main(String[] args) {
    DaggerMotionDetectionExtractionBatch_MainComponent.builder()
        .build()
        .main()
        .execute(new Parameter(args));
  }

  private final Logger logger;
  private final DateTimeService dateTimeService;

  @Inject
  public MotionDetectionExtractionBatch(Logger logger, DateTimeService dateTimeService) {
    this.logger = logger;
    this.dateTimeService = dateTimeService;
  }

  public void execute(Parameter parameter) {

    try {
      var dir = parameter.getFile("dir", new File("."));
      long startTime = System.currentTimeMillis();
      var files =
          dir.listFiles(
              new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return name.endsWith(".h264");
                }
              });
      for (var file : files) {
        logger.info("MotionDetectionExtraction file:" + file.getName());
        var frameCount = processFile(file, parameter);
        logger.info("MotionDetectionExtraction Total:" + frameCount);
      }

      logger.info("time:" + (System.currentTimeMillis() - startTime) / 1000 + "s");
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
  }

  int processFile(File file, Parameter parameter) throws Exception {

    var threshold = parameter.getInt("threshold", 10000);
    var width = parameter.getInt("width", 640);
    var height = parameter.getInt("height", 480);
    var display = parameter.is("display");
    // h264ファイルが格納されているディレクトリに、出力先のディレクトリを設定
    var dir = new File(file.getParentFile(), file.getName() + "_mde");
    dir.mkdirs();
    // ファイルに関するバイトバッファを取得
    var buf = NIOUtils.fetchFromFile(file);
    try (var es = new BufferH264ES(buf)) {
      var decoder = new H264Decoder();
      Picture pic = Picture.create(width, height, ColorSpace.YUV420);

      var i = 0;
      Packet nextFrame = null;
      Picture rgbPic = Picture.create(width, height, ColorSpace.RGB);
      var before = false;
      var transform = ColorUtil.getTransform(ColorSpace.YUV420, ColorSpace.RGB);
      SequenceEncoder encoder = null;
      int last = 0;
      while ((nextFrame = es.nextFrame()) != null) {
        if (i % 1000 == 0) {
          logger.info("MotionDetectionExtraction Count:" + i);
        }
        var length = nextFrame.getData().limit() - nextFrame.data.position();
        // 現在のフレームをデコードして、ピクチャに反映、一連のデータなので、常に反映する必要がある
        // キーフレームまでさかのぼって、エンコード開始すれば、スピードアップしそう。
        // ファイルを処理するバッチなら有効だけど、ストリームだと無理か。
        var frame = decoder.decodeFrame(nextFrame.data, pic.getData());
        if (length > threshold && (!nextFrame.isKeyFrame() || before)) {
          if (encoder == null) {
            encoder =
                SequenceEncoder.createSequenceEncoder(new File(dir, "output" + i + ".h264"), 5);
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(frame);
            if (display) {
              display(bufferedImage, i, length);
            }
            ImageIO.write(bufferedImage, "png", new File(dir, "frame" + i + ".png"));
          }
          transform.transform(frame, rgbPic);
          encoder.encodeNativeFrame(rgbPic);
          before = true;
          last = i;
        } else if (encoder != null) {
          if (i - last < 10) {
            transform.transform(frame, rgbPic);
            encoder.encodeNativeFrame(rgbPic);
          } else {
            encoder.finish();
            encoder = null;
          }

        } else {
          before = false;
        }
        i++;
      }
      if (encoder != null) {
        encoder.finish();
      }
      return i;
    }
  }

  void display(BufferedImage bufferedImage, int i, int length) {
    var g = bufferedImage.getGraphics();
    g.setColor(Color.GRAY);
    g.drawString("No: " + String.valueOf(i), 10, 440);
    g.drawString("Length: " + String.valueOf(length), 10, 450);
    g.drawString(
        "DateTime: "
            + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTimeService.getLocalDateTime()),
        10,
        460);
    g.dispose();
  }
}
