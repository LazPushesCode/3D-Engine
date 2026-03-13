import javax.swing.*;
public class WindowManager {
    JFrame frame = new JFrame("");
    PixelPanel panel;
    int width;
    int length;
    int [][] colorBuffer;
    double [][] depthBuffer;
    WindowManager(int windowWidth, int windowHeight){
        frame.setSize(windowWidth, windowHeight);
        width = windowWidth;
        length = windowHeight;
        panel = new PixelPanel(width,length);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        colorBuffer = new int[width][length];
        depthBuffer = new double[width][length];
        populateBuffers();
        frame.add(panel);
    }
    void openWindow(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
    void updateScreen(){
        for(int i = 0; i < width; i++){
            for(int j = 0; j < length; j++){
                if(colorBuffer[i][j] != 0){
                    panel.setPixel(i,j, (int)colorBuffer[i][j]);
                }
            }
        }
        populateBuffers();
        panel.repaint();
    }
    void addInputListener(InputManager input){
      panel.addKeyListener(input);
    }
    void convertToNDC(Entity m){
        for(int i = 0; i < m.finalVectors.size(); i++){
            double x = m.finalVectors.get(i)[0];
            double y = m.finalVectors.get(i)[1];
            m.finalVectors.get(i)[0] = (1+x) * 0.5 * width;
            m.finalVectors.get(i)[1] = ((1-y) * 0.5 * length);
        }
        m.sortVertices();
    }
    boolean inScreenBounds(int x, int y){
        if(y < 0 || y >= length) return false;
        if(x < 0 || x >= width) return false;
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

    void renderOnScreen(CameraManager c, Entity m){
      for(int i = 0; i < m.finalVectors.size(); i++){
         if(m.finalVectors.get(i)[1] <= this.length && m.finalVectors.get(i)[1] >= -(this.length)){
            int x = (int)m.finalVectors.get(i)[0];
            int y = (int)m.finalVectors.get(i)[1];
            double z = m.finalVectors.get(i)[2];
            if(depthTest(x, y, z)){
               this.depthBuffer[x][y] = z;
            }
         }
      }
      for(int i = 0; i < m.finalIndices.size(); i++){
         int pos1 = m.finalIndices.get(i)[0];
         int pos2 = m.finalIndices.get(i)[1];
         int pos3 = m.finalIndices.get(i)[2];
         int[] xValues1 = interpolate(
            (int)m.finalVectors.get(pos1)[0],
            (int)m.finalVectors.get(pos1)[1],
            (int)m.finalVectors.get(pos2)[0],
            (int)m.finalVectors.get(pos2)[1]);
         int[] xValues2 = interpolate(
            (int)m.finalVectors.get(pos2)[0],
            (int)m.finalVectors.get(pos2)[1],
            (int)m.finalVectors.get(pos3)[0],
            (int)m.finalVectors.get(pos3)[1]);
         int[] xValues3 = interpolate(
            (int)m.finalVectors.get(pos1)[0],
            (int)m.finalVectors.get(pos1)[1],
            (int)m.finalVectors.get(pos3)[0],
            (int)m.finalVectors.get(pos3)[1]);
         

         int[] combinedArray = new int[xValues1.length + xValues2.length];
         try{
         System.arraycopy(xValues1, 0, combinedArray, 0, xValues1.length);
         System.arraycopy(xValues2, 0, combinedArray, xValues1.length, xValues2.length);
        int left = decideWhichIsLeft(combinedArray, xValues3);
         if(left == 0){
            drawLines((int)m.finalVectors.get(pos1)[1], (int)m.finalVectors.get(pos3)[1], combinedArray, xValues3, m.finalVectors.get(pos1)[2], m, i);
         } else {
            drawLines((int)m.finalVectors.get(pos1)[1], (int)m.finalVectors.get(pos3)[1], xValues3, combinedArray, m.finalVectors.get(pos1)[2], m, i);
         }
         } catch (Exception e){
            System.out.println("error: ");
            e.printStackTrace();
         }
      }
   }
    void drawLines(int yStart, int yEnd, int[] xLeftValues, int[] xRightValues, double z, Entity m, int currentIndice){
      int length = Math.abs(yEnd - yStart);
      if(xLeftValues.length != xRightValues.length) return;
      for(int i = 0; i < length; i++){
         for(int j = xLeftValues[i]; j < xRightValues[i]; j++){
            try {
               // if(depthTest(j, i+yStart, z)){
                  sampleTexture(m, 
                     m.finalIndices.get(currentIndice)[0], 
                     m.finalIndices.get(currentIndice)[1], 
                     m.finalIndices.get(currentIndice)[2],
                     j, i+yStart, currentIndice, ((j==xLeftValues[i] || j == xRightValues[i]-1)));
                     // this.colorBuffer[j][i+yStart] = ((j==xLeftValues[i] || j == xRightValues[i]-1)) ? 0xFFFF0000 : 0xFFFFFFFF;
               // } 
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }
   }
   void sampleTexture(Entity e, int t0, int t1, int t2, int px, int py, int currentTriangle, boolean flag){
      // if(t0 == 2){
      //    if(e.finalTextureMappings.get(t0)[0] != e.finalTextureMapping.get(currentTriangle)[0][0]){
      //       System.out.println("inaccuracies: in tri: " + currentTriangle + " " + (e.finalTextureMappings.get(t0)[0]) + " " + (e.finalTextureMapping.get(currentTriangle)[0][0]));
      //    }
      // }
      double x0 = e.finalVectors.get(t0)[0];
      double y0 = e.finalVectors.get(t0)[1];
      double z0 = e.finalVectors.get(t0)[2];
      double w0 = e.finalVectors.get(t0)[3];

      double u0 = e.finalTextureMapping.get(currentTriangle)[0][0];
      double v0 = e.finalTextureMapping.get(currentTriangle)[0][1];

      double x1 = e.finalVectors.get(t1)[0];
      double y1 = e.finalVectors.get(t1)[1];
      double z1 = e.finalVectors.get(t1)[2];
      double w1 = e.finalVectors.get(t1)[3];

      double u1 = e.finalTextureMapping.get(currentTriangle)[1][0];
      double v1 = e.finalTextureMapping.get(currentTriangle)[1][1];

      double x2 = e.finalVectors.get(t2)[0];
      double y2 = e.finalVectors.get(t2)[1];
      double z2 = e.finalVectors.get(t2)[2];
      double w2 = e.finalVectors.get(t2)[3];

      double u2 = e.finalTextureMapping.get(currentTriangle)[2][0];
      double v2 = e.finalTextureMapping.get(currentTriangle)[2][1];

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

      if(depthTest(px, py, z)){
         if(e.texture != null){
            int width = e.texture.getWidth();
            int height = e.texture.getHeight();
            u *= e.texture.getWidth();
            v *= e.texture.getHeight();
            if(u >= width || v >= height) return;
            if(u < 0 || v < 0) return;
            this.depthBuffer[px][py] = z;
            try{
               this.colorBuffer[px][py] = e.texture.getRGB((int)u, (int)v);
            } catch(Exception t){
               System.out.println("out of bounds: ("+(int)u+", "+(int)v+")");
            }
         } else {
            this.depthBuffer[px][py] = z;
            this.colorBuffer[px][py] = (flag) ? 0xFFFF0000 : 0xFFFFFFFF;
         }
      }
   }
    boolean depthTest(int x, int y, double z){
      if(!inScreenBounds(x, y)) return false;
      return (z >= this.depthBuffer[x][y]);
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