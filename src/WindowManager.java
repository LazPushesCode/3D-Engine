import javax.swing.*;

public class WindowManager {
    JFrame frame = new JFrame("");
    PixelPanel panel;
    int width;
    int length;

    int[] windowColorBuffer;

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
      int y_offset = Scene.Y_OFFSET;
      int z_offset = Scene.Z_OFFSET;
      int w_offset = Scene.W_OFFSET;
      int u_offset = Scene.U_OFFSET;
      int v_offset = Scene.V_OFFSET;
      int wx_offset = Scene.WX_OFFSET;
      int wy_offset = Scene.WY_OFFSET;
      int wz_offset = Scene.WZ_OFFSET;
      int ww_offset = Scene.WW_OFFSET;
      int nx_offset = Scene.NX_OFFSET;
      int ny_offset = Scene.NY_OFFSET;
      int nz_offset = Scene.NZ_OFFSET;

      for(int i = 0; i < t.indicesCount; i+=4){
         int pos0 = t.visibleIndices[i];
         int pos1 = t.visibleIndices[i+1];
         int pos2 = t.visibleIndices[i+2];
         int entityID = t.visibleIndices[i+3];

         double x0 = s.globalVertices[pos0];
         double y0 = s.globalVertices[pos0+y_offset];
         double z0 = s.globalVertices[pos0+z_offset];
         double w0 = s.globalVertices[pos0+w_offset];
         double u0 = s.globalVertices[pos0+u_offset];
         double v0 = s.globalVertices[pos0+v_offset];
         double wx0 = s.globalVertices[pos0+wx_offset];
         double wy0 = s.globalVertices[pos0+wy_offset];
         double wz0 = s.globalVertices[pos0+wz_offset];
         double ww0 = s.globalVertices[pos0+ww_offset];
         double nx0 = s.globalVertices[pos0+nx_offset]; 
         double ny0 = s.globalVertices[pos0+ny_offset];
         double nz0 = s.globalVertices[pos0+nz_offset];

         double x1 = s.globalVertices[pos1];
         double y1 = s.globalVertices[pos1+y_offset];
         double z1 = s.globalVertices[pos1+z_offset];
         double w1 = s.globalVertices[pos1+w_offset];
         double u1 = s.globalVertices[pos1+u_offset];
         double v1 = s.globalVertices[pos1+v_offset];
         double wx1 = s.globalVertices[pos1+wx_offset];
         double wy1 = s.globalVertices[pos1+wy_offset];
         double wz1 = s.globalVertices[pos1+wz_offset];
         double ww1 = s.globalVertices[pos1+ww_offset];
         double nx1 = s.globalVertices[pos1+nx_offset]; 
         double ny1 = s.globalVertices[pos1+ny_offset];
         double nz1 = s.globalVertices[pos1+nz_offset];

         double x2 = s.globalVertices[pos2];
         double y2 = s.globalVertices[pos2+y_offset];
         double z2 = s.globalVertices[pos2+z_offset];
         double w2 = s.globalVertices[pos2+w_offset];
         double u2 = s.globalVertices[pos2+u_offset];
         double v2 = s.globalVertices[pos2+v_offset];
         double wx2 = s.globalVertices[pos2+wx_offset];
         double wy2 = s.globalVertices[pos2+wy_offset];
         double wz2 = s.globalVertices[pos2+wz_offset];
         double ww2 = s.globalVertices[pos2+ww_offset];
         double nx2 = s.globalVertices[pos2+nx_offset]; 
         double ny2 = s.globalVertices[pos2+ny_offset];
         double nz2 = s.globalVertices[pos2+nz_offset];

         Entity e = s.entities.get(entityID);
         int[] textureBuffer = e.textureBuffer;

         if (y0 > y1) { 
               double tx=x0, ty=y0, tz=z0, tw=w0, tu=u0, tv=v0, tnx = nx0, tny = ny0, tnz = nz0, twx = wx0, twy = wy0, twz = wz0, tww = ww0;
               x0=x1; y0=y1; z0=z1; w0=w1; u0=u1; v0=v1; nx0 = nx1; ny0 = ny1; nz0 = nz1; wx0 = wx1; wy0 = wy1; wz0 = wz1; ww0 = ww1;
               x1=tx; y1=ty; z1=tz; w1=tw; u1=tu; v1=tv; nx1 = tnx; ny1 = tny; nz1 = tnz; wx1 = twx; wy1 = twy; wz1 = twz; ww1 = tww;
            }
            if (y0 > y2) { 
               double tx=x0, ty=y0, tz=z0, tw=w0, tu=u0, tv=v0, tnx = nx0, tny = ny0, tnz = nz0, twx = wx0, twy = wy0, twz = wz0, tww = ww0;
               x0=x2; y0=y2; z0=z2; w0=w2; u0=u2; v0=v2; nx0 = nx2; ny0 = ny2; nz0 = nz2; wx0 = wx2; wy0 = wy2; wz0 = wz2; ww0 = ww2;
               x2=tx; y2=ty; z2=tz; w2=tw; u2=tu; v2=tv; nx2 = tnx; ny2 = tny; nz2 = tnz; wx2 = twx; wy2 = twy; wz2 = twz; ww2 = tww;
            }
            if (y1 > y2) { 
               double tx=x1, ty=y1, tz=z1, tw=w1, tu=u1, tv=v1, tnx = nx1, tny = ny1, tnz = nz1, twx = wx1, twy = wy1, twz = wz1, tww = ww1;
               x1=x2; y1=y2; z1=z2; w1=w2; u1=u2; v1=v2; nx1 = nx2; ny1 = ny2; nz1 = nz2; wx1 = wx2; wy1 = wy2; wz1 = wz2; ww1 = ww2;
               x2=tx; y2=ty; z2=tz; w2=tw; u2=tu; v2=tv; nx2 = tnx; ny2 = tny; nz2 = tnz; wx2 = twx; wy2 = twy; wz2 = twz; ww2 = tww;
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

         wx0 *= (1/w0);
         wy0 *= (1/w0);
         wz0 *= (1/w0);

         wx1 *= (1/w1);
         wy1 *= (1/w1);
         wz1 *= (1/w1);

         wx2 *= (1/w2);
         wy2 *= (1/w2);
         wz2 *= (1/w2);


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

                  double wx = b0*wx0 + b1*wx1 + b2*wx2;
                  double wy = b0*wy0 + b1*wy1 + b2*wy2;
                  double wz = b0*wz0 + b1*wz1 + b2*wz2;

                  double nx = b0 * nx0 + b1*nx1 + b2*nx2;
                  double ny = b0 * ny0 + b1*ny1 + b2*ny2;
                  double nz = b0 * nz0 + b1*nz1 + b2*nz2;

                  double u = b0*u0 + b1*u1 + b2*u2;
                  double v = b0*v0 + b1*v1 + b2*v2;
                  double z = b0 *z0 + b1*z1 + b2*z2;

                  double wInterpolated = b0*(1/w0) + b1*(1/w1) + b2*(1/w2);

                  wx /= wInterpolated;
                  wy /= wInterpolated;
                  wz /= wInterpolated;

                  u /= wInterpolated;
                  v /= wInterpolated;
                  z /= wInterpolated;
                  double nMagnitude = Math.sqrt(nx * nx + ny * ny + nz * nz);
                  nx /= nMagnitude;
                  ny /= nMagnitude;
                  nz /= nMagnitude;

                  try {
                     sampleTexture(s, c, t, px, py, wx, wy, wz, nx, ny, nz, u, v, z, textureBuffer, e, ((j==xLeftValues[k] || j == xRightValues[k]-1)));
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
   void sampleTexture(Scene s, CameraManager c, Tile t, int px, int py, double wx, double wy, double wz, double nx, double ny, double nz, double u, double v, double z, int[]textureBuffer, Entity e, boolean flag){
      int tileW = (int) t.tileWidth;
      int xLocal = px;
      int yLocal = py + (int)(t.tileLength - t.yOffset);
      int pixel = (yLocal * tileW) + xLocal;

      boolean directional = (s.type == Light.DIRECTIONAL);
      boolean point = (s.type == Light.POINT);
      boolean spotlight = (s.type == Light.SPOTLIGHT);

      if(localDepthTest(t,px, py, z)){
         if(e.hasTexture){
            int textureWidth = e.textureWidth;
            int textureHeight = e.textureHeight;

            u *= (int)textureWidth;
            v *= (int)textureHeight;
            int texU = Math.max(0, Math.min((int)u, textureWidth - 1));
            int texV = Math.max(0, Math.min((int)v, textureHeight - 1));

            double cx = c.x;
            double cy = c.y;
            double cz = c.z;

            double ax = s.xLightPos;
            double ay = s.yLightPos;
            double az = s.zLightPos;

            double dx = s.xLightDir;
            double dy = s.yLightDir;
            double dz = s.zLightDir;

            double lx;
            double ly;
            double lz;
            if(directional){
               lx = dx;
               ly = dy;
               lz = dz;
            } else {
               lx = ax - wx;
               ly = ay - wy;
               lz = az - wz;
            }

            double lMagnitude = Math.sqrt(lx*lx + ly*ly + lz*lz);
            if(lMagnitude > 0){
               lx /= lMagnitude;
               ly /= lMagnitude;
               lz /= lMagnitude;
            }

            double attenuation = 0;

            if(!directional){
               double constant = 1.0;
               double linear = 0.05;
               double quadratic = 0.02;
               attenuation = (1.0)/(constant + (linear * lMagnitude) + (quadratic * lMagnitude * lMagnitude));
            }

            double vx = cx - wx;
            double vy = cy - wy;
            double vz = cz - wz;

            double vMagnitude = Math.sqrt(vx*vx + vy*vy + vz*vz);
            if(vMagnitude > 0){
               vx /= vMagnitude;
               vy /= vMagnitude;
               vz /= vMagnitude; 
            }

            double hx = lx + vx;
            double hy = ly + vy;
            double hz = lz + vz;

            double magnitude = Math.sqrt(hx*hx + hy*hy + hz*hz);
            if(magnitude > 0){
               hx = hx/magnitude;
               hy = hy/magnitude;
               hz = hz/magnitude;
            }

            double lightIntensity = s.ambience;

            
            if(u < 0 || v < 0) {
               return;
            }
            if (xLocal < 0 || xLocal >= (int)t.tileWidth || yLocal < 0 || yLocal >= (int)t.tileLength) {
               return; 
            }


            int color = textureBuffer[(texV * textureWidth) + texU];
            
            int r = (int)(((color >> 16) & 0xFF));
            int g = (int)(((color >> 8) & 0xFF));
            int b = (int)((color & 0xFF));

            double ambientR = r * lightIntensity;
            double ambientG = g * lightIntensity;
            double ambientB = b * lightIntensity;

            double dotNL = Math.max(0.0, (nx * lx) + (ny * ly) + (nz * lz));
            double dotNH = Math.max(0.0, (nx * hx) + (ny * hy) + (nz * hz));
            double spec = Math.pow(dotNH,1000);

            double diffuseR;
            double diffuseG;
            double diffuseB;
            double specularR;
            double specularG;
            double specularB;

            diffuseR = (r * dotNL);
            diffuseG = (g * dotNL);
            diffuseB = (b * dotNL);
            specularR = (255 * spec);
            specularG = (255 * spec);
            specularB = (255 * spec);

            if(!directional){
               diffuseR *= attenuation;
               diffuseG *= attenuation;
               diffuseB *= attenuation;

               specularR *= attenuation;
               specularG *= attenuation;
               specularB *= attenuation;
            }

            if(spotlight){
               double angle = (dx * -lx) + (dy * -ly ) + (dz * -lz);
               double innerCutOff = 1.0;
               double outerCutOff = .5;
               double epsilon = innerCutOff - outerCutOff;
               double intensity = (angle - outerCutOff) / epsilon;
               double spotIntensity = Math.max(0, Math.min(1.0, intensity));

               diffuseR *= spotIntensity;
               diffuseG *= spotIntensity;
               diffuseB *= spotIntensity;

               specularR *= spotIntensity;
               specularG *= spotIntensity;
               specularB *= spotIntensity;
            }

            int finalR = (int) Math.max(0, Math.min(255, ambientR + diffuseR + specularR));
            int finalG = (int) Math.max(0, Math.min(255, ambientG + diffuseG + specularG));
            int finalB = (int) Math.max(0, Math.min(255, ambientB + diffuseB + specularB));

            color = 0xFF000000 | (finalR << 16) | (finalG << 8) | finalB;

            t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] = color;

         } else {
            t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] =  (flag) ? 0xFFFF0000 : 0xFF00FF;
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