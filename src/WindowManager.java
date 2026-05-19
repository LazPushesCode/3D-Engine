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
      // System.out.println("tile visible triangles: " + Arrays.toString(t.visibleIndices));
      for (int i = 0; i < t.indicesCount; i++) {
         int v = t.visibleIndices[i];
         int x = (int) s.globalVertices[v];
         int y = (int) s.globalVertices[v+1];
         double z = s.globalVertices[v+2];
         if(localDepthTest(t, x, y, z)){
            int xLocal = x;
            int yLocal = y + (int)(t.tileLength - t.yOffset);
            t.tileDepthBuffer[yLocal * (int)t.tileWidth + xLocal] = (float)z;
         }
      }
      for(int i = 0; i < t.indicesCount; i+=4){
         int pos1 = t.visibleIndices[i];
         int pos2 = t.visibleIndices[i+1];
         int pos3 = t.visibleIndices[i+2];
         int entityID = t.visibleIndices[i+3];

         double x1 = s.globalVertices[pos1];
         double y1 = s.globalVertices[pos1+1];
         double z1 = s.globalVertices[pos1+2];

         double x2 = s.globalVertices[pos2];
         double y2 = s.globalVertices[pos2+1];
         double z2 = s.globalVertices[pos2+2];

         double x3 = s.globalVertices[pos3];
         double y3 = s.globalVertices[pos3+1];
         double z3 = s.globalVertices[pos3+2];
         if (y1 > y2) { 
               double tx=x1, ty=y1, tz=z1; x1=x2; y1=y2; z1=z2; x2=tx; y2=ty; z2=tz; 
            }
            if (y1 > y3) { 
               double tx=x1, ty=y1, tz=z1; x1=x3; y1=y3; z1=z3; x3=tx; y3=ty; z3=tz; 
            }
            if (y2 > y3) { 
               double tx=x2, ty=y2, tz=z2; x2=x3; y2=y3; z2=z3; x3=tx; y3=ty; z3=tz; 
            }

         int[] xValues1 = interpolate(
            (int)x1,
            (int)y1,
            (int)x2,
            (int)y2);
         int[] xValues2 = interpolate(
            (int)x2,
            (int)y2,
            (int)x3,
            (int)y3);
         int[] xValues3 = interpolate(
            (int)x1,
            (int)y1,
            (int)x3,
            (int)y3);
            int[] combinedArray = new int[xValues1.length + xValues2.length];
         try{
            System.arraycopy(xValues1, 0, combinedArray, 0, xValues1.length);
            System.arraycopy(xValues2, 0, combinedArray, xValues1.length, xValues2.length);
         int left = decideWhichIsLeft(combinedArray, xValues3);
            if(left == 0){
               drawTileLines((int)y1, (int)y3, combinedArray, xValues3, z1, t, i, s, entityID);
            } else {
               drawTileLines((int)y1, (int)y3, xValues3, combinedArray, z1, t, i, s, entityID);
            }
            } catch (Exception e){
               System.out.println("error: ");
               e.printStackTrace();
               errorOccured =  true; 
         }
      }
    }
   void drawTileLines(int yStart, int yEnd, int[] xLeftValues, int[] xRightValues, double z, Tile t, int currentIndice, Scene s, int entityID){
      int drawLength = Math.abs(yEnd - yStart);
      // if(xLeftValues.length != xRightValues.length) return;
      for(int i = 0; i < drawLength; i++){
         if((i+yStart) > t.yOffset || (i+yStart) < (t.yOffset - t.tileLength)) continue;
            for(int j = xLeftValues[i]; j < xRightValues[i]; j++){
               if (j < (t.xOffset-t.tileWidth) || j > (t.xOffset)) continue;
                  try {
                        sampleTextureOnTile(s, t, 
                           t.visibleIndices[currentIndice], 
                           t.visibleIndices[currentIndice+1], 
                           t.visibleIndices[currentIndice+2],
                           j, i+yStart, currentIndice, ((j==xLeftValues[i] || j == xRightValues[i]-1)), t.visibleIndices[currentIndice+3]);
                  } catch (Exception e) {
                     //  System.out.println("j: " + j + " and after calc: " + (j - (int)(t.xOffset-t.tileWidth)) + " i: " + i + " with xOffset: " + t.xOffset);
                     e.printStackTrace();
                  }
         } 
      }
      // shows tile border
      if(!displayTilesOnScreen) return;
         // for(int i = 0; i < t.tileLength; i++){
         //    if(i == 0 || i == (t.tileLength-1)){
         //       for(int j = 0; j < t.tileWidth; j++){
         //          t.localDepthBuffer[j][i] = 0;
         //          t.localColorBuffer[j][i] = 0xFFFF0000;
         //       }
         //    }
         //    t.localColorBuffer[0][i] = 0xFFFF0000;
         //    t.localColorBuffer[(int)t.tileWidth-1][i] = 0xFFFF0000;
         // }
   }


    void sampleTextureOnTile(Scene s, Tile t, int t0, int t1, int t2, int px, int py, int currentTriangle, boolean flag, int entityID){
      double x0 = s.globalVertices[t0];
      double y0 = s.globalVertices[t0+1];
      double z0 = s.globalVertices[t0+2];
      double w0 = s.globalVertices[t0+3];

      double u0 = s.globalVertices[t0+4];
      double v0 = s.globalVertices[t0+5];


      double x1 = s.globalVertices[t1];
      double y1 = s.globalVertices[t1+1];
      double z1 = s.globalVertices[t1+2];
      double w1 = s.globalVertices[t1+3];

      double u1 = s.globalVertices[t1+4];
      double v1 = s.globalVertices[t1+5];


      double x2 = s.globalVertices[t2];
      double y2 = s.globalVertices[t2+1];
      double z2 = s.globalVertices[t2+2];
      double w2 = s.globalVertices[t2+3];

      double u2 = s.globalVertices[t2+4];
      double v2 = s.globalVertices[t2+5];



      double denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0-y2);

      double b0 = ((y1-y2)*(px-x2)+(x2-x1)*(py-y2))/denominator;
      double b1 = ((y2-y0)*(px-x2)+(x0-x2)*(py-y2))/denominator;
      double b2 = 1 - b1 - b0;

      u0 *= (1/w0);
      u1 *= (1/w1);
      u2 *= (1/w2);

      v0 *= (1/w0);
      v1 *= (1/w1);
      v2 *= (1/w2);

      z0 *= (1/w0);
      z1 *= (1/w1);
      z2 *= (1/w2);

      double u = b0*u0 + b1*u1 + b2*u2;
      double v = b0*v0 + b1*v1 + b2*v2;
      double z = b0 *z0 + b1*z1 + b2*z2;
      double wInterpolated = b0*(1/w0) + b1*(1/w1) + b2*(1/w2);
      u /= wInterpolated;
      v /= wInterpolated;
      z /= wInterpolated;

      Entity e = s.entities.get(entityID);

      int tileW = (int) t.tileWidth;
      int xLocal = px;
      int yLocal = py + (int)(t.tileLength - t.yOffset);
      int pixel = (yLocal * tileW) + xLocal;

      if(localDepthTest(t,px, py, z)){
         if(e.texture != null){
            int textureWidth = e.texture.getWidth();
            int textureHeight = e.texture.getHeight();
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
            
            t.tileColorBuffer[pixel] = e.texture.getRGB((int)texU, (int)texV);

         
         } else {
            t.tileDepthBuffer[pixel] = (float)z;

            t.tileColorBuffer[pixel] = (flag) ? 0xFFFF0000: 0xFF00FF;
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
      // return (z >= t.localDepthBuffer[xLocal][yLocal]);
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