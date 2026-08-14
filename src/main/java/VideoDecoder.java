import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;


public class VideoDecoder {
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter;
    private int[] currentFrameBuffer;
    private int width;
    private int height;
    private double targetFrameTimeMs;
    private long lastFrameTime;

    public VideoDecoder(String path){
        try {
            grabber = new FFmpegFrameGrabber(path);
            grabber.start();

            width = grabber.getImageWidth();
            height = grabber.getImageHeight();
            double fps = (grabber.getFrameRate() > 0) ? grabber.getFrameRate() : 30;
            targetFrameTimeMs = 1000.0 / fps;
            currentFrameBuffer = new int[width * height];
            converter = new Java2DFrameConverter();
            lastFrameTime = System.currentTimeMillis();

            nextFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void update(){
        long now = System.currentTimeMillis();
        if (now - lastFrameTime >= targetFrameTimeMs) {
            nextFrame();
            lastFrameTime = now;
        }
    }
    private void nextFrame() {
        try {
            Frame frame = grabber.grabImage();
            if (frame == null) {
                grabber.restart();
                frame = grabber.grabImage();
            }

            if (frame != null && frame.image != null) {
                BufferedImage bi = converter.convert(frame);
                if (bi != null) {
                    bi.getRGB(0, 0, width, height, currentFrameBuffer, 0, width);
                    for (int i = 0; i < currentFrameBuffer.length; i++) {
                        currentFrameBuffer[i] |= 0xFF000000;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int[] getFrameBuffer() { return currentFrameBuffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
