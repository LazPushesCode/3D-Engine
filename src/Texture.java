
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Texture {
    int height;
    int width;
    int[] buffer; // Contains pixel color (WindowManager extracts rgb values from this color)

    // Initializes Texture and its values
    // Takes String as parameter, which is path to the texture in project files
    Texture(String source){
        try {
            BufferedImage image = ImageIO.read(new  File(source)); // Texture itself
            height = image.getHeight();
            width = image.getWidth();
            buffer = new int[height * width]; // Image size is height * width, so make buffer same size
            image.getRGB(0, 0, width, height, buffer, 0, width);
        } catch (Exception e) {
        }
    }
    
}
