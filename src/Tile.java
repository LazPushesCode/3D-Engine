import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
public class Tile {
    //remove
    HashMap <Integer, double []> vectorList;
    ArrayList<int []> visibleTriangleList;
    ArrayList<double[][]> textureMapping;
    ArrayList<Integer> modelTextureID;

    //keep
    double xOffset, yOffset, tileWidth, tileLength;
    int[] visibleIndices;
    int indicesCount;
    int [][] localColorBuffer;
    double [][] localDepthBuffer;

    Tile(double x, double y, double w, double l, int count){
        xOffset = x;
        yOffset = y;
        tileWidth = w;
        tileLength = l;
        indicesCount = 0;
        visibleIndices = new int[count];
        visibleTriangleList = new ArrayList<>();
        vectorList = new HashMap<>();
        textureMapping = new ArrayList<>();
        modelTextureID = new ArrayList<>();
        localColorBuffer = new int[(int)tileWidth][(int)tileLength];
        localDepthBuffer = new double[(int)tileWidth][(int)tileLength];
        for (int i = 0; i < (int)tileWidth; i++) {
            java.util.Arrays.fill(localColorBuffer[i], 0); 
            java.util.Arrays.fill(localDepthBuffer[i], 0); 
        }
    }
    void emptyTileData(){

        indicesCount = 0;

        vectorList.clear();
        visibleTriangleList.clear();
        textureMapping.clear();
        modelTextureID.clear();
        for (int i = 0; i < (int)tileWidth; i++) {
            java.util.Arrays.fill(localColorBuffer[i], 0); 
            java.util.Arrays.fill(localDepthBuffer[i], 0); 
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
    void displayData(){
        System.out.println(Arrays.toString(visibleIndices));
    }
}
