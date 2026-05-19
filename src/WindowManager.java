import java.awt.image.BufferedImage;
import javax.swing.*;

public class WindowManager {
    JFrame frame = new JFrame("");
    PixelPanel panel;
    int width;
    int length;

    //keep
    int[] windowColorBuffer;

    //remove

    boolean displayTilesOnScreen;
    boolean errorOccured;
    WindowManager(int windowWidth, int windowHeight){
        frame.setSize(windowWidth, windowHeight);
        width = windowWidth;
        length = windowHeight;
        panel = new PixelPanel(width,length);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        windowColorBuffer = new int[width * length];

        displayTilesOnScreen = false;
        errorOccured = false;
        populateBuffers();
        frame.add(panel);
    }
    void openWindow(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
    void updateScreen(TileManager tm){
      combineColorBuffers(tm);
        System.arraycopy(windowColorBuffer, 0, panel.pixels, 0, windowColorBuffer.length);
        populateBuffers();
        panel.repaint();
    }
    void addInputListener(InputManager input){
      panel.addKeyListener(input);
    }
    boolean inTileBounds(Tile t, int x, int y){
      if(x < 0 || x >= t.tileWidth)return false;
      if(y < 0 || y >= t.tileLength)return false;
      return true;
    }
    void populateBuffers(){
      java.util.Arrays.fill(windowColorBuffer, 0);
    }
    void clearScreen(){
        panel.clear(0xFF000000); // opaque black
    }
    void renderTile(Tile t, Scene s, CameraManager c){
      for(int i = 0; i < t.indicesCount; i+=4){
         int pos0 = t.visibleIndices[i];
         int pos1 = t.visibleIndices[i+1];
         int pos2 = t.visibleIndices[i+2];
         int entityID = t.visibleIndices[i+3];

         double x0 = s.globalVertices[pos0];
         double y0 = s.globalVertices[pos0+1];
         double z0 = s.globalVertices[pos0+2];
         double w0 = s.globalVertices[pos0+3];
         double u0 = s.globalVertices[pos0+4];
         double v0 = s.globalVertices[pos0+5];

         double x1 = s.globalVertices[pos1];
         double y1 = s.globalVertices[pos1+1];
         double z1 = s.globalVertices[pos1+2];
         double w1 = s.globalVertices[pos1+3];
         double u1 = s.globalVertices[pos1+4];
         double v1 = s.globalVertices[pos1+5];

         double x2 = s.globalVertices[pos2];
         double y2 = s.globalVertices[pos2+1];
         double z2 = s.globalVertices[pos2+2];
         double w2 = s.globalVertices[pos2+3];
         double u2 = s.globalVertices[pos2+4];
         double v2 = s.globalVertices[pos2+5];

         Entity e = s.entities.get(entityID);
         BufferedImage texture = e.texture;

         if (y0 > y1) { 
               double tx=x0, ty=y0, tz=z0, tw=w0, tu=u0, tv=v0; x0=x1; y0=y1; z0=z1; w0=w1; u0=u1; v0=v1; x1=tx; y1=ty; z1=tz; w1=tw; u1=tu; v1=tv;
            }
            if (y0 > y2) { 
               double tx=x0, ty=y0, tz=z0, tw=w0, tu=u0, tv=v0; x0=x2; y0=y2; z0=z2; w0=w2; u0=u2; v0=v2; x2=tx; y2=ty; z2=tz; w2=tw; u2=tu; v2=tv;
            }
            if (y1 > y2) { 
               double tx=x1, ty=y1, tz=z1, tw=w1, tu=u1, tv=v1; x1=x2; y1=y2; z1=z2; w1=w2; u1=u2; v1=v2; x2=tx; y2=ty; z2=tz; w2=tw; u2=tu; v2=tv; 
            }

         int[] xValues1 = interpolate(
            (int)x0,
            (int)y0,
            (int)x1,
            (int)y1);
         int[] xValues2 = interpolate(
            (int)x1,
            (int)y1,
            (int)x2,
            (int)y2);
         int[] xValues3 = interpolate(
            (int)x0,
            (int)y0,
            (int)x2,
            (int)y2);
            int[] combinedArray = new int[xValues1.length + xValues2.length];

         double denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0-y2);
         u0 *= (1/w0);
         u1 *= (1/w1);
         u2 *= (1/w2);

         v0 *= (1/w0);
         v1 *= (1/w1);
         v2 *= (1/w2);

         z0 *= (1/w0);
         z1 *= (1/w1);
         z2 *= (1/w2);
         try{
            System.arraycopy(xValues1, 0, combinedArray, 0, xValues1.length);
            System.arraycopy(xValues2, 0, combinedArray, xValues1.length, xValues2.length);
            int left = decideWhichIsLeft(combinedArray, xValues3);
            int[] xLeftValues;
            int[] xRightValues;
            int drawLength = Math.abs((int)y2 - (int)y0);
            if(left == 0){
               xLeftValues = combinedArray;
               xRightValues = xValues3;
            } else {
               xLeftValues = xValues3;
               xRightValues = combinedArray;
            }
            for(int k = 0; k < drawLength; k++){
               int currY = (k+(int)y0);
               if(currY > t.yOffset || currY < (t.yOffset - t.tileLength)) continue;
               int xStart = xLeftValues[k];
               int xEnd = xRightValues[k];
               for(int j = xStart; j < xEnd; j++){
                  if (j < (t.xOffset-t.tileWidth) || j > (t.xOffset)) continue;
                  int px = j;
                  int py = currY;
                  double b0 = ((y1-y2)*(px-x2)+(x2-x1)*(py-y2))/denominator;
                  double b1 = ((y2-y0)*(px-x2)+(x0-x2)*(py-y2))/denominator;
                  double b2 = 1 - b1 - b0;
                  double u = b0*u0 + b1*u1 + b2*u2;
                  double v = b0*v0 + b1*v1 + b2*v2;
                  double z = b0 *z0 + b1*z1 + b2*z2;
                  double wInterpolated = b0*(1/w0) + b1*(1/w1) + b2*(1/w2);
                  u /= wInterpolated;
                  v /= wInterpolated;
                  z /= wInterpolated;
                  try {
                     sampleTexture(s, t, px, py, u, v, z, texture);
                  } catch (Exception b) {
                     b.printStackTrace();
                  }
               }
            }
            } catch (Exception b){
               System.out.println("error: ");
               b.printStackTrace();
               errorOccured =  true; 
         }
      }
    }
   //main bottleneck
   void sampleTexture(Scene s, Tile t, int px, int py, double u, double v, double z, BufferedImage texture){
      int tileW = (int) t.tileWidth;
      int xLocal = px;
      int yLocal = py + (int)(t.tileLength - t.yOffset);
      int pixel = (yLocal * tileW) + xLocal;
      if(localDepthTest(t,px, py, z)){
         if(texture != null){
            int textureWidth = texture.getWidth();
            int textureHeight = texture.getHeight();
            u *= (int)textureWidth;
            v *= (int)textureHeight;
            int texU = Math.max(0, Math.min((int)u, textureWidth - 1));
            int texV = Math.max(0, Math.min((int)v, textureHeight - 1));
            
            if(u < 0 || v < 0) {
               return;
            }
            if (xLocal < 0 || xLocal >= (int)t.tileWidth || yLocal < 0 || yLocal >= (int)t.tileLength) {
               return; 
            }
            t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] = texture.getRGB((int)texU, (int)texV);

         } else {
            t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] =  0xFF00FF;
         }
      }
   }
   void combineColorBuffers(TileManager tm){
      for(Tile t : tm.tiles){
         int srcPos = 0;
         int length = t.tileColorBuffer.length;
         int destPos = (int)(t.tileWidth * (t.yOffset - t.tileLength));
         System.arraycopy(t.tileColorBuffer,srcPos, windowColorBuffer, destPos, length);
      }
   }
   boolean localDepthTest(Tile t, int x, int y, double z){
      int xLocal = x;
      int yLocal = y + (int)(t.tileLength - t.yOffset);
      if(!inTileBounds(t, xLocal, yLocal))return false;
      return(z >= t.tileDepthBuffer[yLocal * (int)t.tileWidth + xLocal]);
   }
    int[] interpolate(int x1, int y1, int x2, int y2) {
        if (y1 == y2) return new int[0];

        if (y1 > y2) {
            int tx = x1, ty = y1;
            x1 = x2; y1 = y2;
            x2 = tx; y2 = ty;
        }

        int height = y2 - y1;
        int[] xValues = new int[height];
        double slope = (double)(x2 - x1) / (y2 - y1);

        for (int i = 0; i < height; i++) {
            xValues[i] = (int)(x1 + slope * i);
        }

        return xValues;
    }

    int decideWhichIsLeft(int[] array1, int[] array2){
      int mid = array1.length/2;
      try{
         if(array1[mid] <= array2[mid]){
            return 0;
         } else {
            return 1;
         }
      } catch(Exception e){
         // e.printStackTrace();
         return 0;
      }
   }
}