import java.util.ArrayList;
public class TileManager {
    ArrayList<Tile> tiles;
    double tileWidth, tileLength;
    int numRows, numCols;
    TileManager(){
        tiles = new ArrayList();
    }
    void allocateTiles(int rows, int cols, WindowManager wm, Scene s){
        if(!tiles.isEmpty()){
            tiles.clear();
        }
        numRows = rows;
        numCols = cols;
        tileWidth = wm.width/4;
        tileLength = wm.length/2;
        double xOffset = tileWidth;
        double yOffset = tileLength;
        for(int i = 0; i < 8; i++){
            tiles.add(new Tile(xOffset, yOffset, tileWidth, tileLength, s.entityCount*40*2));
            xOffset += tileWidth;
            if(xOffset > wm.width){
                xOffset = tileWidth;
                yOffset += tileLength;
            }
        }
    }
    void assignTrianglesToTiles(Scene s, WindowManager wm){
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
                y = s.globalVertices[v+1];
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
                for(int c = minCol; c <= maxCol; c++){
                    int t = (numCols * r) + c;
                    int triangleIndex = tiles.get(t).indicesCount;
                    tiles.get(t).visibleIndices[triangleIndex] = s.processedIndices[i];
                    tiles.get(t).visibleIndices[triangleIndex+1] = s.processedIndices[i+1];
                    tiles.get(t).visibleIndices[triangleIndex+2] = s.processedIndices[i+2];
                    tiles.get(t).visibleIndices[triangleIndex+3] = s.processedIndices[i+3];
                    tiles.get(t).indicesCount += 4;
                }
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
                if(x > xMax) xMax = x;
                if(x < xMin) xMin = x;
                if(y > yMax) yMax = y;
                if(y < yMin) yMin = y;
            }
            // int minCol = (int) (xMin/this.tileWidth);
            int minCol = Math.max(0, (int)(xMin/this.tileWidth));
            int maxCol = Math.min(numCols - 1, (int)(xMax/this.tileWidth));
            int minRow = Math.max(0,(int)(yMin/this.tileLength));
            int maxRow = Math.min(numRows - 1, (int)(yMax/this.tileLength));
            for(int r = minRow; r <= maxRow; r++){
                for(int c = minCol; c <= maxCol; c++){
                    int index = (numCols * r) + c;
                    int[] indices = e.finalIndices.get(i);
                    tiles.get(index).visibleTriangleList.add(indices);
                    tiles.get(index).textureMapping.add(e.finalTextureMapping.get(i));
                    tiles.get(index).modelTextureID.add(e.ID);
                    for (int indice : indices) {
                        tiles.get(index).vectorList.put(indice, e.finalVectors.get(indice));   
                    }
                }
            }
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
