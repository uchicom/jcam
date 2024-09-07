package com.uchicom.jcam;

import java.io.File;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class Main {

    public static void main(String[] args) {
      
        try (Java2DFrameConverter converter = new Java2DFrameConverter();
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);){
        // 開始
        grabber.start();
        for (int i = 0; i < 10; i++) {
          // 画像取得
          var image = grabber.grab();

          // 画像表示
          if (image != null) {
            var bufferedImage = converter.getBufferedImage(image, 1.0D, false, null);
            ImageIO.write(bufferedImage, "jpg", new File(System.currentTimeMillis() + ".jpg"));
          }
        }
        
        try {
          // 終了
          grabber.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
}