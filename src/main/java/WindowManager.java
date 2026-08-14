import javax.swing.JFrame;

public class WindowManager {
    JFrame frame = new JFrame("");
    PixelPanel panel;
    int width;
    int length;

    int[] windowColorBuffer;


    boolean displayTilesOnScreen;
    boolean errorOccured;

    WindowManager(int windowWidth, int windowHeight, int cores){
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
      // populateBuffers();
      panel.repaint();
    }
    void addInputListener(InputManager input){
      panel.addKeyListener(input);
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
         double nx2 = s.globalVertices[pos2+nx_offset]; 
         double ny2 = s.globalVertices[pos2+ny_offset];
         double nz2 = s.globalVertices[pos2+nz_offset];

         Entity e = s.entities.get(entityID);
         int[] textureBuffer = e.textureBuffer;
         double denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
        if (Math.abs(denominator) < 1e-6) continue;
        double invDenom = 1.0 / denominator;
         
         double invw0 = 1 / w0,  invw1 = 1 / w1, invw2 = 1 / w2;
         
         u0 *= invw0; u1 *= invw1; u2 *= invw2;
         v0 *= invw0; v1 *= invw1; v2 *= invw2;
         z0 *= invw0; z1 *= invw1; z2 *= invw2;

         wx0 *= invw0; wy0 *= invw0; wz0 *= invw0;
         wx1 *= invw1; wy1 *= invw1; wz1 *= invw1;
         wx2 *= invw2; wy2 *= invw2; wz2 *= invw2;

         int tileW = (int) t.tileWidth;
        int tileL = (int) t.tileLength;
        int xMinTile = (int) (t.xOffset - tileW);
        int xMaxTile = (int) t.xOffset;
        int yMinTile = (int) (t.yOffset - tileL);
        int yMaxTile = (int) t.yOffset;

        int xMin = Math.max(xMinTile, (int) Math.floor(Math.min(x0, Math.min(x1, x2))));
        int xMax = Math.min(xMaxTile, (int) Math.ceil(Math.max(x0, Math.max(x1, x2))));
        int yMin = Math.max(yMinTile, (int) Math.floor(Math.min(y0, Math.min(y1, y2))));
        int yMax = Math.min(yMaxTile, (int) Math.ceil(Math.max(y0, Math.max(y1, y2))));

         double db0_dx = (y1 - y2) * invDenom;
         double db1_dx = (y2 - y0) * invDenom;

        for (int py = yMin; py < yMax; py++) {
            double startX = xMin + 0.5;
            double currentY = py + 0.5;

            double b0 = ((y1 - y2) * (startX - x2) + (x2 - x1) * (currentY - y2)) * invDenom;
            double b1 = ((y2 - y0) * (startX - x2) + (x0 - x2) * (currentY - y2)) * invDenom;

            for (int px = xMin; px < xMax; px++) {
                double b2 = 1.0 - b0 - b1;

                if (b0 >= 0 && b1 >= 0 && b2 >= 0) {
                    double z = b0 * z0 + b1 * z1 + b2 * z2;
                    double wInterpolated = b0 * invw0 + b1 * invw1 + b2 * invw2;
                    double depth = z / wInterpolated;

                    int xLocal = px - xMinTile;
                    int yLocal = py - yMinTile;

                    if (xLocal >= 0 && xLocal < tileW && yLocal >= 0 && yLocal < tileL) {
                        int pixel = yLocal * tileW + xLocal;

                        if (depth >= t.tileDepthBuffer[pixel]) {
                            double invW = 1.0 / wInterpolated;
                            double wx = (b0 * wx0 + b1 * wx1 + b2 * wx2) * invW;
                            double wy = (b0 * wy0 + b1 * wy1 + b2 * wy2) * invW;
                            double wz = (b0 * wz0 + b1 * wz1 + b2 * wz2) * invW;

                            double nx = b0 * nx0 + b1 * nx1 + b2 * nx2;
                            double ny = b0 * ny0 + b1 * ny1 + b2 * ny2;
                            double nz = b0 * nz0 + b1 * nz1 + b2 * nz2;

                            double u = (b0 * u0 + b1 * u1 + b2 * u2) * invW;
                            double v = (b0 * v0 + b1 * v1 + b2 * v2) * invW;

                            double nMag = invSqrt(nx * nx + ny * ny + nz * nz);
                            nx *= nMag; ny *= nMag; nz *= nMag;

                            sampleTexture(s, c, t, px, py, wx, wy, wz, nx, ny, nz, u, v, depth, textureBuffer, e, false);
                        }
                    }
                }
                b0 += db0_dx;
                b1 += db1_dx;
            }
        }
    }
}
   void sampleTexture(Scene s, CameraManager c, Tile t, int px, int py, double wx, double wy, double wz, double nx, double ny, double nz, double u, double v, double z, int[]textureBuffer, Entity e, boolean flag){
      int tileW = (int) t.tileWidth;
      int xLocal = px;
      int yLocal = py + (int)(t.tileLength - t.yOffset);
      int pixel = (yLocal * tileW) + xLocal;

      // if(localDepthTest(t,px, py, z)){
         if(e.hasTexture){
            int textureWidth = e.textureWidth;
            int textureHeight = e.textureHeight;

            u *= (int)textureWidth;
            v *= (int)textureHeight;
            int texU = Math.max(0, Math.min((int)u, textureWidth - 1));
            int texV = Math.max(0, Math.min((int)v, textureHeight - 1));

            int color = textureBuffer[(texV * textureWidth) + texU];
            
            int r = (int)(((color >> 16) & 0xFF));
            int g = (int)(((color >> 8) & 0xFF));
            int b = (int)((color & 0xFF));
            double ambience = s.ambience;
            double additiveR = r * ambience;
            double additiveG = g * ambience;
            double additiveB = b * ambience;
            

            if(s.lights != null){
               if (e.greenScreen && g > 150 && r < 100 && b < 100) {
                  return;
               }

               boolean directional;
               boolean spotlight;

               double cx = c.x;
               double cy = c.y;
               double cz = c.z;

               double vx = cx - wx;
               double vy = cy - wy;
               double vz = cz - wz;

               double vMagnitude = invSqrt(vx*vx + vy*vy + vz*vz);
               if(vMagnitude > 0){
                  vx *= vMagnitude;
                  vy *= vMagnitude;
                  vz *= vMagnitude; 
               }
               double md = e.materialDiffuse;
               double ms = e.materialSpecular;

               double diffuseR;
               double diffuseG;
               double diffuseB;
               double specularR;
               double specularG;
               double specularB;
               
               int lightCount = s.lightCount;

               if(u < 0 || v < 0)return;

               for(int l = 0; l < lightCount; l++){
                  int index = s.LIGHT_STRIDE * l;
                  double xPos = s.lightBuffer[index + 0];
                  double yPos = s.lightBuffer[index + 1];
                  double zPos = s.lightBuffer[index + 2];
                  double xDir = s.lightBuffer[index + 3];
                  double yDir = s.lightBuffer[index + 4];
                  double zDir = s.lightBuffer[index + 5];
                  double intensity = s.lightBuffer[index + 6];
                  int type = (int) s.lightBuffer[index + 7];

                  directional = (type == Light.DIRECTIONAL);
                  spotlight = (type == Light.SPOTLIGHT);

                  double dx = xDir;
                  double dy = yDir;
                  double dz = zDir;
                  double lx, ly, lz;

                  lx = (directional) ? -dx : (xPos - wx);
                  ly = (directional) ? -dy : (yPos - wy);
                  lz = (directional) ? -dz : (zPos - wz);
                  double lMagnitude = invSqrt(lx*lx + ly*ly + lz*lz);
                  if(lMagnitude > 0){
                     lx *= lMagnitude;
                     ly *= lMagnitude;
                     lz *= lMagnitude;
                  }
                  lMagnitude = 1 / lMagnitude;

                  double dotNL = Math.max(0.0, (nx * lx) + (ny * ly) + (nz * lz));
                  if(dotNL <= 0)continue;

                  double hx = lx + vx;
                  double hy = ly + vy;
                  double hz = lz + vz;

                  double magnitude = invSqrt(hx*hx + hy*hy + hz*hz);
                  if(magnitude > 0){
                     hx *= magnitude;
                     hy *= magnitude;
                     hz *= magnitude;
                  }

                  double dotNH = Math.max(0.0, (nx * hx) + (ny * hy) + (nz * hz));
                  double spec = 0;
                  if(e.materialSpecular > 0 && dotNL > 0){
                     double sp2 = dotNH * dotNH;
                     double sp4 = sp2 * sp2;
                     double sp16 = sp4 * sp4;
                     spec = sp16 * sp16;
                  }

                  double diffFactor = dotNL * md * intensity;
                  double specFactor = spec * ms * intensity;
                  diffuseR = r * diffFactor;
                  diffuseG = g * diffFactor;
                  diffuseB = b * diffFactor;
                  specularR = 255 * specFactor;
                  specularG = 255 * specFactor;
                  specularB = 255 * specFactor;

                  double attenuation = 1;
                  if(!directional){
                     double constant = 1;
                     double linear = 0.05;
                     double quadratic = 0.03;
                     attenuation = (1.0)/(constant + (linear * lMagnitude) + (quadratic * lMagnitude * lMagnitude));
                  }
                  
                  double spotIntensity = 1;
                  if(spotlight){
                     double angle = (dx * -lx) + (dy * -ly ) + (dz * -lz);
                     double innerCutOff = 0.5;
                     double outerCutOff = .4;
                     double epsilon = innerCutOff - outerCutOff;
                     double inten = (angle - outerCutOff) / epsilon;
                     spotIntensity = Math.max(0, Math.min(1.0, inten));
                  }
                  additiveR += (diffuseR + specularR) * attenuation * spotIntensity;
                  additiveG += (diffuseG + specularG) * attenuation * spotIntensity;
                  additiveB += (diffuseB + specularB) * attenuation * spotIntensity;
                  
               }
            }
            int finalR = (int) Math.max(0, Math.min(255, additiveR));
            int finalG = (int) Math.max(0, Math.min(255, additiveG));
            int finalB = (int) Math.max(0, Math.min(255, additiveB));

            color = 0xFF000000 | (finalR << 16) | (finalG << 8) | finalB;

            t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] = color;

         } else {
            t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] =  (flag) ? 0xFFFF0000 : 0xFF00FF;
         }
      // }
   }
   void combineColorBuffers(TileManager tm){
      // System.arraycopy(windowColorBuffer, 0, panel.pixels, 0, windowColorBuffer.length);
      
      for(Tile t : tm.tiles){
         int srcPos = 0;
         int length = t.tileColorBuffer.length;
         int destPos = (int)(t.tileWidth * (t.yOffset - t.tileLength));
         System.arraycopy(t.tileColorBuffer,srcPos, panel.pixels, destPos, length);
      }
   }
   // used external resource
   double invSqrt(double x) {
      double xhalf = 0.5d * x;
      long i = Double.doubleToLongBits(x);
      i = 0x5fe6eb50c7aa19f9L - (i >> 1); 
      x = Double.longBitsToDouble(i);
      x = x * (1.5d - xhalf * x * x);    
      return x;
   }
}