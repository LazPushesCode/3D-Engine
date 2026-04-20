import java.util.ArrayList;

public class Tile {
    int xOffset, yOffset;
    ArrayList <double []> vectorList;
    ArrayList<int []> visibleTriangleList;
    Tile(int x, int y){
        xOffset = x;
        yOffset = y;
        visibleTriangleList = new ArrayList<>();
    }
}
