// (C) 2024 Rigger LLC
package com.uchicom.jcam;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.jcodec.codecs.h264.BufferH264ES;
import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

public class Main {

  public static void main(String[] args) {

    try {
      long startTime = System.currentTimeMillis();
      File file = new File("test.h264");

      var buf = NIOUtils.fetchFromFile(file);
      try (var es = new BufferH264ES(buf)) {
        var decoder = new H264Decoder();
        Picture pic = Picture.create(1920, 1088, ColorSpace.YUV420);

        var i = 0;
        Packet nextFrame = es.nextFrame();
        var start = nextFrame.data.position();
        var frame = decoder.decodeFrame(nextFrame.data, pic.getData()).cropped();
        var length = nextFrame.data.position() - start;
        System.out.println(length);
        while ((nextFrame = es.nextFrame()) != null) {
          start = nextFrame.data.position();
          frame = decoder.decodeFrame(nextFrame.data, pic.getData());
          length = nextFrame.data.position() - start;
          System.out.println(length);
          if (length > 10000 && !nextFrame.isKeyFrame()) {
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(frame);
            System.out.println(bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
            ImageIO.write(bufferedImage, "png", new File("frame" + i + ".png"));
          }
          i++;
        }
        System.out.println("frame count:" + i);
        System.out.println("time:" + (System.currentTimeMillis() - startTime) / 1000 + "s");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
