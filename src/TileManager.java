import java.util.ArrayList;
public class TileManager {
    ArrayList<Tile> tiles;
    double tileWidth, tileLength;
    int numRows, numCols;
    TileManager(){
        tiles = new ArrayList();
    }
    void allocateTiles(int rows, int cols, WindowManager wm){
        if(!tiles.isEmpty()){
            tiles.clear();
        }
        numRows = rows;;
        numCols = cols;
        tileWidth = wm.width/4;
        tileLength = wm.length/2;
        double xOffset = tileWidth;
        double yOffset = tileLength;
        for(int i = 0; i < 8; i++){
            tiles.add(new Tile(xOffset, yOffset, tileWidth, tileLength));
            xOffset += tileWidth;
            if(xOffset > wm.width){
                xOffset = tileWidth;
                yOffset += tileLength;
            }
        }
    }
    void checkTriangleExistanceInTiles(Entity e, WindowManager wm){
        for(int i = 0; i < e.finalIndices.size(); i++){
            double xMin = wm.width;
            double xMax = 0;
            double yMin = wm.length;
            double yMax = 0;
            double x = 0;
            double y = 0;
            int v = 0;
            for(int j = 0; j < e.finalIndices.get(i).length; j++){
                v = e.finalIndices.get(i)[j];
                x = e.finalVectors.get(v)[0];
                y = e.finalVectors.get(v)[1];
                if(x > wm.width) continue;
                if(y > wm.length) continue;
                if(x > xMax) xMax = x;
                if(x < xMin) xMin = x;
                if(y > yMax) yMax = y;
                if(y < yMin) yMin = y;
            }
            int minCol = (int) (xMin/this.tileWidth);
            int maxCol = Math.min(numCols - 1, (int)(xMax/this.tileWidth));
            int minRow = (int) (yMin/this.tileLength);
            int maxRow = Math.min(numRows - 1, (int)(yMax/this.tileLength));

            for(int r = minRow; r <= maxRow; r++){
                for(int c = minCol; c <= maxCol; c++){
                    int[] indices = e.finalIndices.get(i);
                    tiles.get((numCols*r) + c).visibleTriangleList.add(indices);
                    for (int indice : indices) {
                        tiles.get((numCols*r) + c).vectorList.put(indice, e.finalVectors.get(indice));   
                    }
                }
            }
        }
    }
    
    void emptyTiles(){
        for(int i = 0; i < tiles.size(); i++){
            tiles.get(i).emptyTileData();
        }
    }
}
