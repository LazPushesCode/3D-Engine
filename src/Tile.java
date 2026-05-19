import java.util.Arrays;
public class Tile {
    double xOffset, yOffset, tileWidth, tileLength;
    int[] visibleIndices;
    int indicesCount;

    int[] tileColorBuffer;
    float[] tileDepthBuffer;


    Tile(double x, double y, double w, double l, int count){
        xOffset = x;
        yOffset = y;
        tileWidth = w;
        tileLength = l;
        indicesCount = 0;
        visibleIndices = new int[count];


        tileColorBuffer = new int[(int)tileWidth * (int)tileLength];
        tileDepthBuffer = new float[(int)tileWidth * (int)tileLength];

        java.util.Arrays.fill(tileColorBuffer, 0);
        java.util.Arrays.fill(tileDepthBuffer, 0);
    }
    void emptyTileData(){
        indicesCount = 0;

        java.util.Arrays.fill(tileColorBuffer, 0);
        java.util.Arrays.fill(tileDepthBuffer, 0);

    }
    void displayData(){
        System.out.println(Arrays.toString(visibleIndices));
    }
}
