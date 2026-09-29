import java.util.ArrayList;
public class TileManager {
    ArrayList<Tile> tiles;
    int tileCount;
    double tileWidth, tileLength;
    int numRows, numCols;
    TileManager(){
        tiles = new ArrayList();
    }
    void createTiles(int cores, WindowManager wm, Scene s){
        if(!tiles.isEmpty()){
            tiles.clear();
        }
        tileCount = cores;
        tileWidth = wm.width;
        tileLength = Math.ceil(wm.length/cores);
        numRows = cores;
        numCols = 1;
        double xOffset = tileWidth;
        double yOffset = tileLength;
        System.out.println("cores: " + cores +" width: " + tileWidth + " length: " + tileLength);
        for(int i = 0; i < tileCount; i++){
            tiles.add(new Tile(i, xOffset, yOffset, tileWidth, tileLength, s.initialIndicesSize));
            yOffset += tileLength;
        }
    }
    void assignTrianglesToTiles(Scene s, WindowManager wm){
        int y_offset = Scene.Y_OFFSET;
        for(int i = 0; i < s.processedIndicesSize; i+=4){
            double xMin = wm.width;
            double xMax = 0;
            double yMin = wm.length;
            double yMax = 0;
            double x = 0;
            double y = 0;
            int v = 0;
            for(int j = i; j < (i+4); j++){
                v = s.processedIndices[j];
                x = s.globalVertices[v];
                y = s.globalVertices[v+y_offset];
                if(x > xMax) xMax = x;
                if(x < xMin) xMin = x;
                if(y > yMax) yMax = y;
                if(y < yMin) yMin = y;
            }
            int minCol = Math.max(0, (int)(xMin/this.tileWidth));
            int maxCol = Math.min(numCols - 1, (int)(xMax/this.tileWidth));
            int minRow = Math.max(0,(int)(yMin/this.tileLength));
            int maxRow = Math.min(numRows - 1, (int)(yMax/this.tileLength));
            for(int r = minRow; r <= maxRow; r++){
                // for(int c = minCol; c <= maxCol; c++){
                    int t = (numCols * r);
                    int triangleIndex = tiles.get(t).indicesCount;
                    tiles.get(t).visibleIndices[triangleIndex] = s.processedIndices[i];
                    tiles.get(t).visibleIndices[triangleIndex+1] = s.processedIndices[i+1];
                    tiles.get(t).visibleIndices[triangleIndex+2] = s.processedIndices[i+2];
                    tiles.get(t).visibleIndices[triangleIndex+3] = s.processedIndices[i+3];
                    tiles.get(t).indicesCount += 4;
                // }
            }
        }
    }
    void printTileOffsets(){
        for(Tile t : tiles){
            System.out.println(t.xOffset + " " + t.yOffset);
        }
    }
    void displayTileIndiceData(){
        System.out.println("=========================");
        for(int i = 0; i < tiles.size(); i++){
            System.out.println("tile: " + i + " visible indice count: "+tiles.get(i).indicesCount); 
            tiles.get(i).displayData();
        }
    }
    void emptyTiles(){
        for(int i = 0; i < tiles.size(); i++){
            tiles.get(i).emptyTileData();
        }
    }
}
