import javax.swing.*;
public class WindowManager {
    JFrame frame = new JFrame("");
    PixelPanel panel;
    int width;
    int length;
    int [][] colorBuffer;
    float [][] depthBuffer;
    WindowManager(int windowWidth, int windowHeight){
        frame.setSize(windowWidth, windowHeight);
        width = windowWidth;
        length = windowHeight;
        panel = new PixelPanel(width,length);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        colorBuffer = new int[width][length];
        depthBuffer = new float[width][length];
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
                depthBuffer[i][j] = Float.POSITIVE_INFINITY;
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
            int z = (int)m.finalVectors.get(i)[2];
            if(depthTest(x, y, z)){
               this.depthBuffer[x][y] = z;
               this.colorBuffer[x][y] = 0xFFFF0000;
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
            drawLines((int)m.finalVectors.get(pos1)[1], (int)m.finalVectors.get(pos3)[1], combinedArray, xValues3, (int)m.finalVectors.get(pos1)[2]);
         } else {
            drawLines((int)m.finalVectors.get(pos1)[1], (int)m.finalVectors.get(pos3)[1], xValues3, combinedArray, (int)m.finalVectors.get(pos1)[2]);
         }
         } catch (Exception e){
            System.out.println("error: ");
            e.printStackTrace();
         }
      }
   }
    void drawLines(int yStart, int yEnd, int[] xLeftValues, int[] xRightValues, int z){
      int length = Math.abs(yEnd - yStart);
      if(xLeftValues.length != xRightValues.length) return;
      for(int i = 0; i < length; i++){
         for(int j = xLeftValues[i]; j < xRightValues[i]; j++){
            try {
               if(depthTest(j, i+yStart, z)){
                  this.depthBuffer[j][i+yStart] = z;
                  this.colorBuffer[j][i+yStart] = ((j==xLeftValues[i] || j == xRightValues[i]-1)) ? 0xFFFF0000 : 0xFFFFFFFF;
               } 
            } catch (Exception e) {
               System.out.println("error in drawing");
            }
         }
      }
   }
    boolean depthTest(int x, int y, int z){
      if(!inScreenBounds(x, y)) return false;
      return (z < this.depthBuffer[x][y]);
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