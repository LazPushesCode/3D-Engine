public class RenderManager{   
   
   static void renderOnScreen(CameraManager c, WindowManager w, Entity m){
      for(int i = 0; i < m.finalVectors.size(); i++){
         if(m.finalVectors.get(i)[1] <= w.length && m.finalVectors.get(i)[1] >= -(w.length)){
            int x = (int)m.finalVectors.get(i)[0];
            int y = (int)m.finalVectors.get(i)[1];
            int z = (int)m.finalVectors.get(i)[2];
            if(depthTest(w, x, y, z)){
               w.depthBuffer[x][y] = z;
               w.colorBuffer[x][y] = 0xFFFF0000;
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
            drawLines((int)m.finalVectors.get(pos1)[1], (int)m.finalVectors.get(pos3)[1], combinedArray, xValues3, w, (int)m.finalVectors.get(pos1)[2]);
         } else {
            drawLines((int)m.finalVectors.get(pos1)[1], (int)m.finalVectors.get(pos3)[1], xValues3, combinedArray, w, (int)m.finalVectors.get(pos1)[2]);
         }
         } catch (Exception e){
            System.out.println("error: ");
            e.printStackTrace();
         }
      }
   }
   static void drawLines(int yStart, int yEnd, int[] xLeftValues, int[] xRightValues, WindowManager w, int z){
      int length = Math.abs(yEnd - yStart);
      if(xLeftValues.length != xRightValues.length) return;
      for(int i = 0; i < length; i++){
         for(int j = xLeftValues[i]; j < xRightValues[i]; j++){
            try {
               if(depthTest(w, j, i+yStart, z)){
                  w.depthBuffer[j][i+yStart] = z;
                  w.colorBuffer[j][i+yStart] = ((j==xLeftValues[i] || j == xRightValues[i]-1)) ? 0xFFFF0000 : 0xFFFFFFFF;
               } 
            } catch (Exception e) {
               System.out.println("error in drawing");
            }
         }
      }
   }
   static boolean depthTest(WindowManager w, int x, int y, int z){
      if(!w.inScreenBounds(x, y)) return false;
      return (z <= w.depthBuffer[x][y]);
   }
   static int[] interpolate(int x1, int y1, int x2, int y2) {
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

   static int decideWhichIsLeft(int[] array1, int[] array2){
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