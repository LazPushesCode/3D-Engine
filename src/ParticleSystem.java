
import static java.lang.Math.random;

public class ParticleSystem {
    double[] vertices; 
    double[] projected; 
    boolean[] visible;
    int[] baseSize;
    int[] dynamicSize;

    double[] riseSpeed;

    int particleCount;
    int color;
    int ID;

    int r; int g; int b; 

    final static int VERTICE_STRIDE = 4;

    void initializeSystem(int x, int y, int z, int count, int size, int xDisp, int yDisp, int zDisp){
        particleCount = count;
        vertices = new double[count*VERTICE_STRIDE];
        projected = new double[count*3];
        visible = new boolean[count];
        baseSize = new int[count];
        dynamicSize = new int[count];
        riseSpeed = new double[count];
        color = 0xFFFFFFFF;
        for(int i = 0; i < count; i++){
            int index = i * VERTICE_STRIDE;
            vertices[index] =  (Math.random()-0.5)*xDisp+x;
            vertices[index+1] = (Math.random()-0.5)*yDisp+y;
            vertices[index+2] = (Math.random()-0.5)*zDisp+z;
            vertices[index+3] = 1;
            riseSpeed[i] = random() * 0.008;
            baseSize[i] = size;
        }
    }
    void setColor(int tr, int tg, int tb){
        r = tr; g = tg; b = tb;
    }
    void convertToClipSpace(Camera cm, int windowWidth, int windowLength){
        Matrix cameraMatrix = cm.combinedMatrix;
        double[] cData = cameraMatrix.data;
        // System.arraycopy(vertices,0, vertBuffer, 0, particleCount*VERTICE_STRIDE);
        for(int i = 0; i < particleCount; i++){
            int index = i*VERTICE_STRIDE;
            double x = vertices[index];
            double y = vertices[index+1];
            double z = vertices[index+2];
            double w = vertices[index+3];
            double cx = cData[0]*x + cData[1]*y + cData[2]*z + cData[3]*w;
            double cy = cData[4]*x + cData[5]*y + cData[6]*z + cData[7]*w;
            double cz = cData[8]*x + cData[9]*y + cData[10]*z + cData[11]*w;
            double cw = cData[12]*x + cData[13]*y + cData[14]*z + cData[15]*w;
            if(cw <= 0.1){
                visible[i] = false;
                continue;
            }
            double invCw = 1 / cw;
            cx *= invCw; cy *= invCw;
            if(cx < -1 || cx > 1 || cy < -1 || cy > 1){
                visible[i] = false;
                continue;
            }
            int pIndex = i * 3;
            projected[pIndex] = (int) ((cx+1) * 0.5 * windowWidth);
            projected[pIndex+1] = (int) ((1-cy) * 0.5 * windowLength);
            projected[pIndex+2] = cz * invCw;
            visible[i] = true;
            dynamicSize[i] = (int) Math.max(0, (baseSize[i] / cw));
        }
    }
    void particlesRise(){
        for(int i = 0; i < particleCount; i++){
            int index = i * VERTICE_STRIDE;
            vertices[index+1] += riseSpeed[i];
            if(vertices[index+1] > 8) vertices[index+1]=-8;
        }
    }
    void setColor(int givenColor){
        color = givenColor;
    }
}
