import javax.swing.*;
public class WindowManager {
    JFrame frame = new JFrame("");
    PixelPanel panel;
    int width;
    int length;
    int [][] colorBuffer;
    double [][] depthBuffer;
    boolean displayTilesOnScreen;
    boolean errorOccured;
    WindowManager(int windowWidth, int windowHeight){
        frame.setSize(windowWidth, windowHeight);
        width = windowWidth;
        length = windowHeight;
        panel = new PixelPanel(width,length);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        colorBuffer = new int[width][length];
        depthBuffer = new double[width][length];
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
        for(int i = 0; i < width; i++){
            for(int j = 0; j < length; j++){
                if(colorBuffer[i][j] != 0){
                    panel.setPixel(i,j, colorBuffer[i][j]);
                }
            }
        }
        populateBuffers();
        panel.repaint();
    }
    void addInputListener(InputManager input){
      panel.addKeyListener(input);
    }
    void convertToNDC(Entity m, TileManager tm){
        for(int i = 0; i < m.finalVectors.size(); i++){
            double x = m.finalVectors.get(i)[0];
            double y = m.finalVectors.get(i)[1];
            m.finalVectors.get(i)[0] = (1+x) * 0.5 * width;
            m.finalVectors.get(i)[1] = ((1-y) * 0.5 * length);
        }
        m.sortVertices();
        try{
         tm.checkTriangleExistanceInTiles(m, this);
        } catch (Exception e){
         e.printStackTrace();
        }
    }
   boolean inScreenBounds(int x, int y){
        if(y < 0 || y >= length) return false;
        if(x < 0 || x >= width) return false;
        return true;
    }
    boolean inTileBounds(Tile t, int x, int y){
      if(x < 0 || x >= t.tileWidth)return false;
      if(y < 0 || y >= t.tileLength)return false;
      return true;
    }
    void populateBuffers(){
        for(int i = 0; i < width; i++){
            for(int j = 0; j < length; j++){
                colorBuffer[i][j] = 0;
                depthBuffer[i][j] = 0;
            }
        }
    }
    void clearScreen(){
        panel.clear(0xFF000000); // opaque black
    }
    void renderTile(Tile t, Scene s, CameraManager c){
      //depth test on indice points
      // System.out.println("tile visible triangles: " + Arrays.toString(t.visibleIndices));
      for (int i = 0; i < t.indicesCount; i++) {
         int v = t.visibleIndices[i];
         int x = (int) s.globalVertices[v];
         int y = (int) s.globalVertices[v+1];
         double z = s.globalVertices[v+2];
         if(localDepthTest(t, x, y, z)){
            t.localDepthBuffer[x + (int)(t.tileWidth - t.xOffset)]
            [y + (int)(t.tileLength - t.yOffset)] = z;
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
    void oldRenderTile(CameraManager c, Tile t, Scene s){
      for (double[] vec : t.vectorList.values()) {
         if (vec[1] <= this.length && vec[1] >= -(this.length)) {
            int x = (int)vec[0];
            int y = (int)vec[1];
            double z = vec[2];
            // if(depthTest(x, y, z)){
            //    this.depthBuffer[x][y] = z;
            // }
         }
      }
      for(int i = 0; i < t.visibleTriangleList.size(); i++){
         int pos1 = t.visibleTriangleList.get(i)[0];
         int pos2 = t.visibleTriangleList.get(i)[1];
         int pos3 = t.visibleTriangleList.get(i)[2];
         int[] xValues1 = interpolate(
            (int)t.vectorList.get(pos1)[0],
            (int)t.vectorList.get(pos1)[1],
            (int)t.vectorList.get(pos2)[0],
            (int)t.vectorList.get(pos2)[1]);
         int[] xValues2 = interpolate(
            (int)t.vectorList.get(pos2)[0],
            (int)t.vectorList.get(pos2)[1],
            (int)t.vectorList.get(pos3)[0],
            (int)t.vectorList.get(pos3)[1]);
         int[] xValues3 = interpolate(
            (int)t.vectorList.get(pos1)[0],
            (int)t.vectorList.get(pos1)[1],
            (int)t.vectorList.get(pos3)[0],
            (int)t.vectorList.get(pos3)[1]);
         
         int[] combinedArray = new int[xValues1.length + xValues2.length];
         try{
         System.arraycopy(xValues1, 0, combinedArray, 0, xValues1.length);
         System.arraycopy(xValues2, 0, combinedArray, xValues1.length, xValues2.length);
        int left = decideWhichIsLeft(combinedArray, xValues3);
         if(left == 0){
            // drawTileLines((int)t.vectorList.get(pos1)[1], (int)t.vectorList.get(pos3)[1], combinedArray, xValues3, t.vectorList.get(pos1)[2], t, i, s);
            // drawTileLines((int)s.globalVertices[pos1+1], (int)s.globalVertices[pos3+1], combinedArray, xValues3, s.globalVertices[pos1+2], t, i, s);
         } else {
            // drawTileLines((int)t.vectorList.get(pos1)[1], (int)t.vectorList.get(pos3)[1], xValues3, combinedArray, t.vectorList.get(pos1)[2], t, i, s);
            // drawTileLines((int)s.globalVertices[pos1+1], (int)s.globalVertices[pos3+1], xValues3, combinedArray, s.globalVertices[pos1+2], t, i, s);
         }
         } catch (Exception e){
            System.out.println("error: ");
            e.printStackTrace();
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
                     // if(localDepthTest(t, j, i+yStart, t.visibleIndices[currentIndice])){
                        // sampleTextureOnTile(s, t, s.entities.get(t.modelTextureID.get(currentIndice)), 
                        //    t.visibleTriangleList.get(currentIndice)[0], 
                        //    t.visibleTriangleList.get(currentIndice)[1], 
                        //    t.visibleTriangleList.get(currentIndice)[2],
                        //    j, i+yStart, currentIndice, ((j==xLeftValues[i] || j == xRightValues[i]-1)));
                        sampleTextureOnTile(s, t, 
                           t.visibleIndices[currentIndice], 
                           t.visibleIndices[currentIndice+1], 
                           t.visibleIndices[currentIndice+2],
                           j, i+yStart, currentIndice, ((j==xLeftValues[i] || j == xRightValues[i]-1)), t.visibleIndices[currentIndice+3]);

                           // this.colorBuffer[j][i+yStart] = ((j==xLeftValues[i] || j == xRightValues[i]-1)) ? 0xFFFF0000 : 0xFFFFFFFF;
                           // t.localColorBuffer[(j + (int)(t.tileWidth - t.xOffset))][i+yStart+(int)(t.tileLength - t.yOffset)] = ((j == xLeftValues[i] || j == xRightValues[i])) ? 0xFFFF0000 : 0xFFFFFFFF;
                     // } 
                  } catch (Exception e) {
                     //  System.out.println("j: " + j + " and after calc: " + (j - (int)(t.xOffset-t.tileWidth)) + " i: " + i + " with xOffset: " + t.xOffset);
                     e.printStackTrace();
                  }
         } 
      }
      // shows tile border
      if(!displayTilesOnScreen) return;
      for(int i = 0; i < t.tileLength; i++){
         if(i == 0 || i == (t.tileLength-1)){
            for(int j = 0; j < t.tileWidth; j++){
               t.localDepthBuffer[j][i] = 0;
               t.localColorBuffer[j][i] = 0xFFFF0000;
            }
         }
         t.localColorBuffer[0][i] = 0xFFFF0000;
         t.localColorBuffer[(int)t.tileWidth-1][i] = 0xFFFF0000;
      }
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
            int xLocal = px + (int)(t.tileWidth - t.xOffset);
            int yLocal = py + (int)(t.tileLength - t.yOffset);

            if (xLocal < 0 || xLocal >= (int)t.tileWidth || yLocal < 0 || yLocal >= (int)t.tileLength) {
               return; 
            }
            t.localDepthBuffer[xLocal][yLocal] = z;
            
            t.localColorBuffer[xLocal][yLocal] = e.texture.getRGB((int)texU, (int)texV);
         
         } else {
            int xLocal = px + (int)(t.tileWidth - t.xOffset);
            int yLocal = py + (int)(t.tileLength - t.yOffset);
            t.localDepthBuffer[xLocal][yLocal] = z;
            t.localColorBuffer[xLocal][yLocal] =  (flag) ? 0xFFFF0000: 0xFF00FF;
         }
      }
   }
   void combineColorBuffers(TileManager tm){
      int iStart = 0;
      int jStart = 0;
      for(Tile t : tm.tiles){
         for(int i = 0; i < t.tileWidth; i++){
            for(int j = 0; j < t.tileLength; j++){
               colorBuffer[i+iStart][j+jStart] = t.localColorBuffer[i][j];
            }
         }
         iStart += t.tileWidth;
         if(iStart >= width){
            iStart = 0;
            jStart += t.tileLength;
         }
      }
   }
   //outdated
   // void sampleTexture(Entity e, int t0, int t1, int t2, int px, int py, int currentTriangle, boolean flag){
   //    double x0 = e.finalVectors.get(t0)[0];
   //    double y0 = e.finalVectors.get(t0)[1];
   //    double z0 = e.finalVectors.get(t0)[2];
   //    double w0 = e.finalVectors.get(t0)[3];

   //    double u0 = e.finalTextureMapping.get(currentTriangle)[0][0];
   //    double v0 = e.finalTextureMapping.get(currentTriangle)[0][1];

   //    double x1 = e.finalVectors.get(t1)[0];
   //    double y1 = e.finalVectors.get(t1)[1];
   //    double z1 = e.finalVectors.get(t1)[2];
   //    double w1 = e.finalVectors.get(t1)[3];

   //    double u1 = e.finalTextureMapping.get(currentTriangle)[1][0];
   //    double v1 = e.finalTextureMapping.get(currentTriangle)[1][1];

   //    double x2 = e.finalVectors.get(t2)[0];
   //    double y2 = e.finalVectors.get(t2)[1];
   //    double z2 = e.finalVectors.get(t2)[2];
   //    double w2 = e.finalVectors.get(t2)[3];

   //    double u2 = e.finalTextureMapping.get(currentTriangle)[2][0];
   //    double v2 = e.finalTextureMapping.get(currentTriangle)[2][1];

   //    double denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0-y2);

   //    double b0 = ((y1-y2)*(px-x2)+(x2-x1)*(py-y2))/denominator;
   //    double b1 = ((y2-y0)*(px-x2)+(x0-x2)*(py-y2))/denominator;
   //    double b2 = 1 - b1 - b0;

   //    u0 *= (1/w0);
   //    u1 *= (1/w1);
   //    u2 *= (1/w2);

   //    v0 *= (1/w0);
   //    v1 *= (1/w1);
   //    v2 *= (1/w2);

   //    z0 *= (1/w0);
   //    z1 *= (1/w1);
   //    z2 *= (1/w2);

   //    double u = b0*u0 + b1*u1 + b2*u2;
   //    double v = b0*v0 + b1*v1 + b2*v2;
   //    double z = b0 *z0 + b1*z1 + b2*z2;
   //    double wInterpolated = b0*(1/w0) + b1*(1/w1) + b2*(1/w2);

   //    u /= wInterpolated;
   //    v /= wInterpolated;
   //    z /= wInterpolated;

   //    if(depthTest(px, py, z)){
   //       if(e.texture != null){
   //          int width = e.texture.getWidth();
   //          int height = e.texture.getHeight();
   //          u *= e.texture.getWidth();
   //          v *= e.texture.getHeight();
   //          if(u >= width || v >= height) return;
   //          if(u < 0 || v < 0) return;
   //          this.depthBuffer[px][py] = z;
   //          try{
   //             this.colorBuffer[px][py] = e.texture.getRGB((int)u, (int)v);
   //          } catch(Exception t){
   //             System.out.println("out of bounds: ("+(int)u+", "+(int)v+")");
   //          }
   //       } else {
   //          this.depthBuffer[px][py] = z;
   //          this.colorBuffer[px][py] = (flag) ? 0xFF00FF : e.defaultColor;
   //       }
   //    }
   // }
   boolean localDepthTest(Tile t, int x, int y, double z){
      int xLocal = x + (int)(t.tileWidth - t.xOffset);
      int yLocal = y + (int)(t.tileLength - t.yOffset);
      if(!inTileBounds(t, xLocal, yLocal))return false;
      return (z >= t.localDepthBuffer[xLocal][yLocal]);
   }
    boolean depthTest(int x, int y, double z){
      if(!inScreenBounds(x, y)) return false;
      return (z <= this.depthBuffer[x][y]);
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