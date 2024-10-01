// (C) 2024 Rigger LLC
package com.uchicom.jcam.batch;

import com.uchicom.jcam.module.MainModule;
import com.uchicom.util.Parameter;
import dagger.Component;
import jakarta.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import javax.imageio.ImageIO;
import org.jcodec.codecs.h264.BufferH264ES;
import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

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

  @Inject
  public MotionDetectionExtractionBatch() {}

  public void execute(Parameter parameter) {

    try {
      var threshold = parameter.getInt("threshold", 10000);
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
        System.out.println("MotionDetectionExtraction file:" + file.getName());
        var frameCount = processFile(file, threshold);
        System.out.println("MotionDetectionExtraction Count:" + frameCount);
      }

      System.out.println("time:" + (System.currentTimeMillis() - startTime) / 1000 + "s");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  int processFile(File file, int threshold) throws Exception {

    var dir = new File(file.getParentFile(), file.getName() + "_mde");
    dir.mkdirs();
    var buf = NIOUtils.fetchFromFile(file);
    try (var es = new BufferH264ES(buf)) {
      var decoder = new H264Decoder();
      Picture pic = Picture.create(1920, 1088, ColorSpace.YUV420);

      var i = 0;
      Packet nextFrame = es.nextFrame();
      var start = nextFrame.data.position();
      var frame = decoder.decodeFrame(nextFrame.data, pic.getData()).cropped();
      var length = nextFrame.data.position() - start;
      var before = false;
      while ((nextFrame = es.nextFrame()) != null) {
        start = nextFrame.data.position();
        frame = decoder.decodeFrame(nextFrame.data, pic.getData());
        length = nextFrame.data.position() - start;
        if (length > threshold && (!nextFrame.isKeyFrame() || before)) {
          BufferedImage bufferedImage = AWTUtil.toBufferedImage(frame);
          System.out.println(bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
          ImageIO.write(bufferedImage, "png", new File(dir, "frame" + i + ".png"));
          before = true;
        } else {
          before = false;
        }
        i++;
      }
      return i;
    }
  }
}
