import java.util.ArrayList;
import java.util.HashMap;
public class Tile {
    double xOffset, yOffset, tileWidth, tileLength;
    HashMap <Integer, double []> vectorList;
    ArrayList<int []> visibleTriangleList;
    ArrayList<double[][]> textureMapping;
    ArrayList<Integer> modelTextureID;

    int [][] localColorBuffer;
    double [][] localDepthBuffer;

    Tile(double x, double y, double w, double l){
        xOffset = x;
        yOffset = y;
        tileWidth = w;
        tileLength = l;
        visibleTriangleList = new ArrayList<>();
        vectorList = new HashMap<>();
        textureMapping = new ArrayList<>();
        modelTextureID = new ArrayList<>();
        localColorBuffer = new int[(int)tileWidth][(int)tileLength];
        localDepthBuffer = new double[(int)tileWidth][(int)tileLength];
        for (int i = 0; i < (int)tileWidth; i++) {
            java.util.Arrays.fill(localColorBuffer[i], 0); 
            java.util.Arrays.fill(localDepthBuffer[i], 10000); 
        }
    }
    void emptyTileData(){
        vectorList.clear();
        visibleTriangleList.clear();
        textureMapping.clear();
        modelTextureID.clear();
        for (int i = 0; i < (int)tileWidth; i++) {
            java.util.Arrays.fill(localColorBuffer[i], 0); 
            java.util.Arrays.fill(localDepthBuffer[i], 100000); 
        }
    }
    void displayTileData(){
        for(int[] triangle : visibleTriangleList){
            for(int v : triangle){
                System.out.print(" v: " + v);
                System.out.print(" x: " + vectorList.get(v)[0] + " y: " + vectorList.get(v)[1]);
            }   
            System.out.println();
        }
    }
}
