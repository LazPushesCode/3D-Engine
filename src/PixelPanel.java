import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import javax.swing.*;

public class PixelPanel extends JPanel {
    private final BufferedImage image;
    private final int[] pixels;
    private final int width;

    public PixelPanel(int width, int length){
        image = new BufferedImage(width, length, BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt)
            image.getRaster().getDataBuffer()).getData();
            this.width = width;
        setPreferredSize(new Dimension(width, length));
    }
    public void setPixel(int x, int y, int argb){
        if(x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) return;
        pixels[y * width + x] = argb;
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
    public void clear(int argb) {
        Arrays.fill(pixels, argb);
    }    
}