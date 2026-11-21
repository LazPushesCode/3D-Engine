import java.util.Arrays;
public class Tile {
    double xOffset; // Offset in the range of 0 to tileWidth, acting as start x position when rendering tile
    double  yOffset; // Offset in the range of 0 to tileLength, acting as start y position when rendering tile
    double  tileWidth; // Width of tile 
    double  tileLength; // Length of tile
    int[] visibleIndices; // List of indices that are in the bounds of the tile's bounding box
    int indicesCount; // Number of visible indices

    int[] tileColorBuffer; // List of pixel colors (packed as RGB). Dimension: tileWidth * tileLength
    float[] tileDepthBuffer; // List of z depth values, pertaining to their assigned pixel: (x*y). Dimension: tileWidth * tileLength
    int[] tilePrioritize; // List of integers either 0 or 1, where 1 is to prioritize rendering this pixel over all future pixels. dimension: tileLength * tileLength

    int ID; // tile ID provided by TileManager

    // Initializes Tile and its values
    // Takes given ID provided by tileManager, as well as xOffset, yOffset, width, length, and allocated space for indice buffer
    Tile(int tileID, double x, double y, double w, double l, int allocatedIndiceSpace){
        xOffset = x;
        yOffset = y;
        tileWidth = w;
        tileLength = l;
        indicesCount = 0;
        visibleIndices = new int[allocatedIndiceSpace];
        ID = tileID;

        tileColorBuffer = new int[(int)tileWidth * (int)tileLength];
        tileDepthBuffer = new float[(int)tileWidth * (int)tileLength];
        tilePrioritize = new int[(int)tileWidth * (int)tileLength];

        java.util.Arrays.fill(tileColorBuffer, 0xFF000000); // Fill color buffer pixels with opaque black RGB
        java.util.Arrays.fill(tileDepthBuffer, -Float.MAX_VALUE); // Fill with lowest possible depth value
        java.util.Arrays.fill(tilePrioritize, 0); // Fill with 0 (if 1 then prioritize pixel)
    }
    // Resets all buffers and indiceCount. Occurs every frame 
    void emptyTileData(){
        indicesCount = 0; 
       java.util.Arrays.fill(tileColorBuffer, 0xFF000000);
       java.util.Arrays.fill(tileDepthBuffer, -Float.MAX_VALUE);
       java.util.Arrays.fill(tilePrioritize, 0);
    }
    void displayData(){
        System.out.println(Arrays.toString(visibleIndices));
    }
}
