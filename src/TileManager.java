import java.util.ArrayList;
public class TileManager {
    ArrayList<Tile> tiles; // List of tiles created
    int tileCount; // Number of tiles in tiles ArrayList
    double tileWidth, tileLength; // dimensions of tile (all tiles share same dimension)

    // Tiles are simply just portions of the window containing x * y amount of pixels to be rendered
    // Each tile is responsible for rendering pixels on their portion of the screen, dividing up the workload evenly and accelerating pipeline calculations
    // Individual threads are assigned a tile, allowing for multi-threaded tile rendering to be possible
    
    // Initializes TileManager and tiles list
    TileManager(){ 
        tiles = new ArrayList();
    }

    // Creates tiles and adds to list of tiles
    // Takes number of cores system can spare, WindowManager, and Scene
    void createTiles(int cores, WindowManager wm, int color, Scene s){
        if(!tiles.isEmpty()){
            tiles.clear();
        }
        tileCount = cores; // Each core dedicates itself to rendering one portion of the screen (or tile)
        tileWidth = wm.width; // Width of tile is equal to width of window
        tileLength = Math.ceil(wm.length/cores); // Length of tile is equal to (window length / number of cores)
        double xOffset = tileWidth; // x Start
        double yOffset = tileLength; // yStart
        for(int i = 0; i < tileCount; i++){  // Loop to add tiles to list
            tiles.add(new Tile(i, xOffset, yOffset, tileWidth, tileLength, color, s.initialIndicesSize*2)); 
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
            int minRow = Math.max(0,(int)(yMin/this.tileLength));
            int maxRow = Math.min(tileCount - 1, (int)(yMax/this.tileLength));
            for(int r = minRow; r <= maxRow; r++){
                    int triangleIndex = tiles.get(r).indicesCount;
                    tiles.get(r).visibleIndices[triangleIndex] = s.processedIndices[i];
                    tiles.get(r).visibleIndices[triangleIndex+1] = s.processedIndices[i+1];
                    tiles.get(r).visibleIndices[triangleIndex+2] = s.processedIndices[i+2];
                    tiles.get(r).visibleIndices[triangleIndex+3] = s.processedIndices[i+3];
                    tiles.get(r).indicesCount += 4;
            }
        }
    }
    // Prints visible indices on each tile to terminal
    void displayTileIndiceData(){
        System.out.println("=========================");
        for(int i = 0; i < tiles.size(); i++){
            System.out.println("tile: " + i + " visible indice count: "+tiles.get(i).indicesCount); 
            tiles.get(i).displayData();
        }
    }
    // Removes temporary data values from buffers every frame
    void emptyTiles(){
        for(int i = 0; i < tiles.size(); i++){
            tiles.get(i).emptyTileData();
        }
    }
}
