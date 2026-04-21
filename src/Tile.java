import java.util.ArrayList;
import java.util.HashMap;
public class Tile {
    double xOffset, yOffset, tileWidth, tileLength;
    HashMap <Integer, double []> vectorList;
    ArrayList<int []> visibleTriangleList;
    ArrayList<double[][]> textureMapping;
    ArrayList<Integer> modelTextureID;

    Tile(double x, double y, double w, double l){
        xOffset = x;
        yOffset = y;
        tileWidth = w;
        tileLength = l;
        visibleTriangleList = new ArrayList<>();
        vectorList = new HashMap<>();
        textureMapping = new ArrayList<>();
        modelTextureID = new ArrayList<>();
    }
    void emptyTileData(){
        vectorList.clear();
        visibleTriangleList.clear();
        textureMapping.clear();
        modelTextureID.clear();
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
