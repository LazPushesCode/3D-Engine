
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;



public class Texture {
    int height;
    int width;
    int[] buffer;

    Texture(String source){
        try {
            BufferedImage image = ImageIO.read(new  File(source));
            height = image.getHeight();
            width = image.getWidth();
            buffer = new int[height * width];
            image.getRGB(0, 0, width, height, buffer, 0, width);
        } catch (Exception e) {
        }
    }
    
}
