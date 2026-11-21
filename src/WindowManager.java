import javax.swing.JFrame;

public class WindowManager {
    JFrame frame = new JFrame("");
    PixelPanel panel; // External panel library used to draw pixels on window
    int width; // Width of window panel
    int length; // Length of window panel

    int[] windowColorBuffer; // Holds final pixel colors to be drawn on screen
    int textureFilteringType; 

    static final int CLOSEST_NEIGHBOR = 0;
    static final int BILINEAR = 1;

    static final int TRIANGLE_STRIDE = 4;
    boolean displayTilesOnScreen; // If true, displays tile bounding boxes on screen
    boolean errorOccured; // If true, error has occurred

   // Intiializes WindowManager and values.
   // Takes window's width and height, as well as cpu cores for parameters
    WindowManager(int windowWidth, int windowHeight, int cores){
        frame.setSize(windowWidth, windowHeight);
        width = windowWidth;
        length = windowHeight;
        panel = new PixelPanel(width,length); 
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        windowColorBuffer = new int[width * length]; // Dimension of window is width * length
        displayTilesOnScreen = false; 
        errorOccured = false;
        populateBuffers();
        frame.add(panel);
    }
    // Tells JFrame library to open window
    void openWindow(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
    // Refreshes screen and updates pixel colors
    void updateScreen(TileManager tm){
      java.util.Arrays.fill(panel.pixels, 0xFF000000); // Reset all pixel colors to black
      for(Tile t : tm.tiles){ // Iterate through each tile
         int srcPos = 0;
         int tileColorBufferLength = t.tileColorBuffer.length; // Length of tile's color buffer
         int destPos = (int)(t.tileWidth * (t.yOffset - t.tileLength)); // tile's starting pixel position
         System.arraycopy(t.tileColorBuffer,srcPos, panel.pixels, destPos, tileColorBufferLength); // copy tile's color buffer into panel's pixel buffer, which panel then draws these colors on screen
      }
      panel.repaint(); // Draw color pixels on screen
    }
    // Initialize input listener to allow system to poll user input.
    void addInputListener(InputManager input){
      panel.addKeyListener(input);
    }
    // Fills windowColorBuffer with 0's acting as a reset
    void populateBuffers(){
      java.util.Arrays.fill(windowColorBuffer, 0);
    }
    // Sets all panel pixel colors back to black
    void clearScreen(){
        panel.clear(0xFF000000); // opaque black
    }
    // Renders all of tile's visible indices on its portion of the window
    void renderTile(Tile t, Scene s, Camera c){
      // if(t.id < 3 || t.id > 12)return;

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
      int tx_offset = Scene.TX_OFFSET;
      int ty_offset = Scene.TY_OFFSET;
      int tz_offset = Scene.TZ_OFFSET;


      // Iterate through visible indices twice inside for loop. Once to process non transparent indices and second to process transparent indices
   
      boolean renderingTransparentObjects = false; 


      for(int i = 0; i < t.indicesCount; i+=TRIANGLE_STRIDE){ // Iterate through all of tile's visible indices

         int entityID = t.visibleIndices[i+3]; // Obtain entity ID from triangle's values
         Entity e = s.entities.get(entityID); // Get entity using entity ID

         // Check if renderingTransparentObjects mode is equal to entity's transparent boolean. We must render non transparent objects first and THEN render transparent ones. 
         // Therefore, we set renderingTransparentObects to true once we've iterated through tile's indices array one (guaranteeing we have rendered all non transparent objects already)
         // Then, we render transparent objects, which are handled differently inside sampleTexture
         if(renderingTransparentObjects == e.transparent){
            // Obtain 3 indices forming triangle
            int pos0 = t.visibleIndices[i];
            int pos1 = t.visibleIndices[i+1];
            int pos2 = t.visibleIndices[i+2];

            // Pos0's vertice values
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
            double tx0 = s.globalVertices[pos0+tx_offset]; 
            double ty0 = s.globalVertices[pos0+ty_offset];
            double tz0 = s.globalVertices[pos0+tz_offset];

            // Pos1's vertice values
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
            double tx1 = s.globalVertices[pos1+tx_offset]; 
            double ty1 = s.globalVertices[pos1+ty_offset];
            double tz1 = s.globalVertices[pos1+tz_offset];

            // Pos2's vertice values
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
            double tx2 = s.globalVertices[pos2+tx_offset]; 
            double ty2 = s.globalVertices[pos2+ty_offset];
            double tz2 = s.globalVertices[pos2+tz_offset];

            
         int[] textureBuffer = e.textureBuffer; // Copy reference to entity's texture buffer
         double denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2); // Calculate area of indices pos0, pos1, and po2
         if (Math.abs(denominator) < 1e-6) continue;
         double invDenom = 1.0 / denominator; // Weights are calculated by dividing areas by the above denominator, so we precompute the invert to prevent needing to divide unnecessarily
            
         double invw0 = 1 / w0,  invw1 = 1 / w1, invw2 = 1 / w2; // We also precompute the inverse of each to prevent unnecessary divisions
            
         //perspective division on vertice values
         u0 *= invw0; u1 *= invw1; u2 *= invw2; 
         v0 *= invw0; v1 *= invw1; v2 *= invw2;
         z0 *= invw0; z1 *= invw1; z2 *= invw2;

         wx0 *= invw0; wy0 *= invw0; wz0 *= invw0;
         wx1 *= invw1; wy1 *= invw1; wz1 *= invw1;
         wx2 *= invw2; wy2 *= invw2; wz2 *= invw2;

         // Pull tile data from t Tile
         int tileW = (int) t.tileWidth;
         int tileL = (int) t.tileLength;
         int xMinTile = (int) (t.xOffset - tileW); // Starting x value of tile
         int xMaxTile = (int) t.xOffset; 
         int yMinTile = (int) (t.yOffset - tileL); // Starting y value of tile
         int yMaxTile = (int) t.yOffset;

         
         int xMin = Math.max(xMinTile, (int) Math.floor(Math.min(x0, Math.min(x1, x2)))); // Calculates the absolute min x point possible with the given points without exceeding tile bounds
         int xMax = Math.min(xMaxTile, (int) Math.ceil(Math.max(x0, Math.max(x1, x2))));  // Calculates the absolute max x point possible with the given points without exceeding tile bounds
         int yMin = Math.max(yMinTile, (int) Math.floor(Math.min(y0, Math.min(y1, y2)))); // Calculates the absolute min y point possible with the given points without exceeding tile bounds
         int yMax = Math.min(yMaxTile, (int) Math.ceil(Math.max(y0, Math.max(y1, y2))));  // Calculates the absoluve max y point possible with the given points without exceeding tile bounds

         double db0_dx = (y1 - y2) * invDenom; 
         double db1_dx = (y2 - y0) * invDenom;

         for (int py = yMin; py < yMax; py++) {// Interpolate layer by layer starting from yMin and ending at yMax
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
                           
                           boolean pass;
                           if(e.prioritize){
                              if(t.tilePrioritize[pixel] == 1){
                                 pass = (depth >= t.tileDepthBuffer[pixel]);
                              } else {
                                 pass = true;
                              }
                           } else {
                              pass = (depth >= t.tileDepthBuffer[pixel] && t.tilePrioritize[pixel] != 1);
                           }
                           if (pass) {
                              double invW = 1.0 / wInterpolated;
                              double wx = (b0 * wx0 + b1 * wx1 + b2 * wx2) * invW;
                              double wy = (b0 * wy0 + b1 * wy1 + b2 * wy2) * invW;
                              double wz = (b0 * wz0 + b1 * wz1 + b2 * wz2) * invW;

                              double nx = b0 * nx0 + b1 * nx1 + b2 * nx2;
                              double ny = b0 * ny0 + b1 * ny1 + b2 * ny2;
                              double nz = b0 * nz0 + b1 * nz1 + b2 * nz2;

                              double tx = b0 * tx0 + b1 * tx1 + b2 * tx2;
                              double ty = b0 * ty0 + b1 * ty1 + b2 * ty2;
                              double tz = b0 * tz0 + b1 * tz1 + b2 * tz2;

                              double u = (b0 * u0 + b1 * u1 + b2 * u2) * invW;
                              double v = (b0 * v0 + b1 * v1 + b2 * v2) * invW;

                              double nMag = invSqrt(nx * nx + ny * ny + nz * nz);
                              nx *= nMag; ny *= nMag; nz *= nMag;

                              if(e.hasNormalMap){
                                 double tMag = invSqrt(tx * tx + ty * ty + tz * tz);
                                 tx *= tMag; ty *= tMag; tz *= tMag;

                                 double bx = ny*tz - nz*ty;
                                 double by = nz*tx - nx*tz;
                                 double bz = nx*ty - ny*tx;
                                 
                                 int normalMapWidth = e.normalMapWidth;
                                 int normalMapHeight = e.normalMapHeight;
                                 double normalMapU = u*normalMapWidth;
                                 double normalMapV = v*normalMapHeight;
                                 int texU = Math.max(0, Math.min((int)normalMapU, normalMapWidth - 1));
                                 int texV = Math.max(0, Math.min((int)normalMapV, normalMapHeight - 1));
                                 int color = e.normalMapBuffer[(texV * normalMapWidth) + texU];
                                 double r = (((color >> 16) & 0xFF) / 255.0) * 2.0 - 1.0;
                                 double g = (((color >> 8)  & 0xFF) / 255.0) * 2.0 - 1.0;
                                 double b = ((color         & 0xFF) / 255.0) * 2.0 - 1.0;
                                 nx = (tx*r) + (bx*g) + (nx*b);
                                 ny = (ty*r) + (by*g) + (ny*b);
                                 nz = (tz*r) + (bz*g) + (nz*b);
                                 double newMag = invSqrt(nx * nx + ny * ny + nz * nz);
                                    nx *= newMag; ny *= newMag; nz *= newMag;
                                  sampleTexture(s, c, t, px, py, wx, wy, wz, nx, ny, nz, u, v, depth, textureBuffer, e, renderingTransparentObjects, false);
                              } else {
                                 sampleTexture(s, c, t, px, py, wx, wy, wz, nx, ny, nz, u, v, depth, textureBuffer, e, renderingTransparentObjects, false);
                              }
                           }
                     }
                  }
                  b0 += db0_dx;
                  b1 += db1_dx;
               }
         }
      }
      if((i+4) == t.indicesCount && !renderingTransparentObjects){
         renderingTransparentObjects = true;
         i = 0;
     }
    }
}
   void sampleTexture(Scene s, Camera c, Tile t, int px, int py, double wx, double wy, double wz, double nx, double ny, double nz, double u, double v, double z, int[]textureBuffer, Entity e, boolean isTransparent, boolean flag){
      int tileW = (int) t.tileWidth;
      int xLocal = px;
      int yLocal = py + (int)(t.tileLength - t.yOffset);
      int pixel = (yLocal * tileW) + xLocal;
      // if(localDepthTest(t,px, py, z)){
         if(e.hasTexture){
            int textureWidth = e.textureWidth;
            int textureHeight = e.textureHeight;
            int r = 0, g = 0, b = 0;
            int color = 0;

            if(textureFilteringType == 1){
               double uTex = u * (textureWidth - 1);
               double vTex = v * (textureHeight - 1);
               int x0 = (int) uTex;
               int y0 = (int) vTex;
               int x1 = Math.min(x0+1, textureWidth-1);
               int y1 = Math.min(y0+1, textureHeight-1);
               double xDist = uTex - x0;
               double yDist = vTex - y0; 

               int col00 = textureBuffer[(y0*textureWidth) + x0];
               int col10 = textureBuffer[(y0*textureWidth) + x1];
               int col01 = textureBuffer[(y1*textureWidth) + x0];
               int col11 = textureBuffer[(y1*textureWidth) + x1];

               int r00 = (col00 >> 16) & 0xFF;
               int r10 = (col10 >> 16) & 0xFF;
               int r01 = (col01 >> 16) & 0xFF;
               int r11 = (col11 >> 16) & 0xFF;

               int g00 = (col00 >> 8) & 0xFF;
               int g10 = (col10 >> 8) & 0xFF;
               int g01 = (col01 >> 8) & 0xFF;
               int g11 = (col11 >> 8) & 0xFF;

               int b00 = (col00) & 0xFF;
               int b10 = (col10) & 0xFF;
               int b01 = (col01) & 0xFF;
               int b11 = (col11) & 0xFF;

               double rTop = (r00*(1-xDist) + r10 * xDist);
               double rBottom = (r01*(1-xDist) + r11 * xDist);
               r = (int)(rTop*(1-yDist) + rBottom*yDist);

               double gTop = (g00 * (1-xDist) + g10 * xDist);
               double gBottom = (g01 * (1-xDist) + g11 * xDist);
               g = (int)(gTop * (1-yDist) + gBottom * yDist);

               double bTop = (b00 * (1-xDist) + b10 * xDist);
               double bBottom = (b01 * (1-xDist) + b11 * xDist);
               b = (int)(bTop * (1-yDist) + bBottom * yDist);


            } else {
               u *= (int)textureWidth;
               v *= (int)textureHeight;


               int texU = Math.max(0, Math.min((int)u, textureWidth - 1));
               int texV = Math.max(0, Math.min((int)v, textureHeight - 1));

               color = textureBuffer[(texV * textureWidth) + texU];
               
               r = (int)(((color >> 16) & 0xFF));
               g = (int)(((color >> 8) & 0xFF));
               b = (int)((color & 0xFF));
            }
            double a = e.alpha;
            double ambience = s.ambience;
            double additiveR, additiveG, additiveB;
            if(e.affectedByLight){
               additiveR =  r * ambience;
               additiveG = g * ambience;
               additiveB = b * ambience;
            
               if(s.lights != null){
                  if (e.greenScreen) {
                     if(g > r * 1.2 && g > b * 1.2 && g > 40){
                        return;
                     }
                     if(g > r && g > b){
                        g = Math.max(r, b);
                     }
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
                     double lightR = s.lightBuffer[index+8];
                     double lightG = s.lightBuffer[index+9];
                     double lightB = s.lightBuffer[index+10];

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
                     diffuseR = r * diffFactor * lightR;
                     diffuseG = g * diffFactor * lightG;
                     diffuseB = b * diffFactor * lightB;
                     specularR = 255 * specFactor * lightR;
                     specularG = 255 * specFactor * lightG;
                     specularB = 255 * specFactor * lightB;

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
            } else {
               if (e.greenScreen) {
                     if(g > r * 1.2 && g > b * 1.2 && g > 40){
                        return;
                     }
                     if(g > r && g > b){
                        g = Math.max(r, b);
                     }
                  }
               additiveR =  r;
               additiveG = g;
               additiveB = b;
            }
            int finalR = (int) Math.max(0, Math.min(255, additiveR));
            int finalG = (int) Math.max(0, Math.min(255, additiveG));
            int finalB = (int) Math.max(0, Math.min(255, additiveB));


            if(isTransparent){
                int bgColor = t.tileColorBuffer[pixel];
               int bgR = (int)(((bgColor >> 16) & 0xFF));
               int bgG = (int)(((bgColor >> 8) & 0xFF));
               int bgB = (int)((bgColor & 0xFF));

               double blendedR = (finalR*a)+(bgR*(1-a)); 
               double blendedG = (finalG*a)+(bgG*(1-a));
               double blendedB = (finalB*a)+(bgB*(1-a));

               color = 0xFF000000 | ((int)blendedR << 16) | ((int)blendedG << 8) | (int)blendedB;
            } else {
               color = 0xFF000000 | (finalR << 16) | (finalG << 8) | finalB;
            }


            if(!isTransparent)t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] = color;
            if(e.prioritize) t.tilePrioritize[pixel] = 1;

         } else {
            t.tileDepthBuffer[pixel] = (float)z;
            t.tileColorBuffer[pixel] =  (flag) ? 0xFFFF0000 : 0xFF00FF;
            if(e.prioritize) t.tilePrioritize[pixel] = 1;
         }
      
   }
   void renderParticlesOnTile(Tile t, Scene s){
      // if(t.id < 3 || t.id > 12)return;
      int tileW = (int) t.tileWidth;
      int tileL = (int) t.tileLength;
      int xMinTile = (int) (t.xOffset - tileW);
      int xMaxTile = (int) t.xOffset;
      int yMinTile = (int) (t.yOffset - tileL);
      int yMaxTile = (int) t.yOffset;
      for(ParticleSystem ps : s.particleSystems.values()){
         
         for(int i = 0; i < ps.particleCount; i++){
            if(!ps.visible[i]) continue;
            int pIndex = i * 3;
            int xCenter = (int) ps.projected[pIndex];
            int yCenter = (int) ps.projected[pIndex+1];
            double pz =  ps.projected[pIndex+2];
            int radius = ps.dynamicSize[i];
            if(xCenter+radius < xMinTile && xCenter-radius >= xMaxTile 
               && yCenter+radius < yMinTile && yCenter-radius >= yMaxTile) continue;
            
            for(int dy = -radius; dy <= radius; dy++){
               int py = yCenter + dy;
               if(py < yMinTile || py >= yMaxTile) continue;
               for(int dx = -radius; dx <= radius; dx++){
                  int px = xCenter + dx;
                  if(px < xMinTile || px >= xMaxTile) continue;
                  int xLocal = px - xMinTile;
                  int yLocal = py - yMinTile;
                  int pixelIndex = yLocal * tileW + xLocal;
                  if(pz >= t.tileDepthBuffer[pixelIndex]){
                     t.tileDepthBuffer[pixelIndex] = (float)pz;
                     int color = 0xFF000000 | (ps.r << 16) | (ps.g << 8) | ps.b;
                     t.tileColorBuffer[pixelIndex] = color;
                  } 
               }
            }
         }
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