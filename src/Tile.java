import java.util.Arrays;
public class Tile {
    double xOffset, yOffset, tileWidth, tileLength;
    int[] visibleIndices;
    int indicesCount;


    //remove
    int [][] localColorBuffer;
    float [][] localDepthBuffer;

    Tile(double x, double y, double w, double l, int count){
        xOffset = x;
        yOffset = y;
        tileWidth = w;
        tileLength = l;
        indicesCount = 0;
        visibleIndices = new int[count];

        localColorBuffer = new int[(int)tileWidth][(int)tileLength];
        localDepthBuffer = new float[(int)tileWidth][(int)tileLength];

        for (int i = 0; i < (int)tileWidth; i++) {
            java.util.Arrays.fill(localColorBuffer[i], 0); 
            java.util.Arrays.fill(localDepthBuffer[i], 0); 
        }
    }
    void emptyTileData(){
        indicesCount = 0;

        for (int i = 0; i < (int)tileWidth; i++) {
            java.util.Arrays.fill(localColorBuffer[i], 0); 
            java.util.Arrays.fill(localDepthBuffer[i], 0); 
        }
    }
    void displayData(){
        System.out.println(Arrays.toString(visibleIndices));
    }
}
