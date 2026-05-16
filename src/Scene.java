import java.util.HashMap;
import java.util.Map;
public class Scene {
    HashMap<Integer, Entity> entities;
    HashMap<Integer, CameraManager> cameras;

    static final int STRIDE = 6;
    static final int PROCESSED_STRIDE = 3;

    int entityCount;
    int cameraCount;

    int cameraFocused;

    double[] globalVertices;
    int[] globalIndices;
    int [] processedIndices;
    int initialVerticesSize;
    int initialIndicesSize;
    int currentIndicesSize;
    int processedIndicesSize;
    int currentVerticesSize;

    Scene(){
        entities = new HashMap<>();
        cameras = new HashMap<>();
        cameraFocused = 0;
        entityCount = 0;
        cameraCount = 0;
        initialVerticesSize = 0;
        initialIndicesSize = 0;
        currentVerticesSize = 0;
        currentIndicesSize = 0;
        processedIndicesSize = 0;
    }
    void addEntity(Entity et){
        entities.put(entityCount, et);
        et.ID = entityCount;
        entityCount++;
    }
    void bindVertices(){
        initialVerticesSize = entities.size() * 300;
        globalVertices = new double[initialVerticesSize];
        initialIndicesSize = entities.size() * 60;
        globalIndices = new int[initialIndicesSize];
        processedIndices = new int[initialIndicesSize*2];
    }
    void resetSceneFrameData(){
        currentIndicesSize = 0;
        processedIndicesSize = 0;
        currentVerticesSize = 0;
    }
    void transformEntities(){
        for(Entity e : entities.values()){
            e.applyTransformationValues();
        }
    }
    void convertVerticesToWorldSpace(){
        int currentVerticesIndex = 0;
        int currentIndicesIndex = 0;
        for(Map.Entry<Integer, Entity> entry : entities.entrySet()){
            int ID = entry.getKey();
            Entity e = entities.get(ID);

            double[] entityWorldVectors = e.convertVectorsToWorldSpace();
            System.arraycopy(entityWorldVectors, 0, globalVertices, currentVerticesIndex, entityWorldVectors.length);
            
            int offset = currentVerticesIndex / STRIDE;
            e.globalVerticeOffset = offset;
            int indicesLength = e.indices.length;
            
            int index = currentIndicesIndex;

            for(int i = 0; i < indicesLength; i+=3){
                globalIndices[index] = e.indices[i] + offset;
                globalIndices[index+1] = e.indices[i+1] + offset;
                globalIndices[index+2] = e.indices[i+2] + offset;
                globalIndices[index+3] = ID;
                index += 4;
            }
            // for(int i = 0; i < e.indices.length; i++){
            //     globalIndices[currentIndicesIndex+i] = e.indices[i] + offset;
            // }
            // System.arraycopy(e.indices, 0, globalIndices, currentIndicesIndex, e.indices.length);
            currentVerticesIndex += entityWorldVectors.length;
            currentIndicesIndex = index;
        }
        currentIndicesSize =  currentIndicesIndex;
        currentVerticesSize = currentVerticesIndex;
    }
    void convertVerticesToViewSpace(){
        CameraManager cm = cameras.get(cameraFocused);
        double[] vm = cm.viewMatrix.data;
        for(int v = 0; v < currentVerticesSize; v+=STRIDE){
            double x = globalVertices[v];
            double y = globalVertices[v+1];
            double z = globalVertices[v+2];
            double w = globalVertices[v+3];
            globalVertices[v]   = vm[0]*x  + vm[1]*y  + vm[2]*z  + vm[3]*w;
            globalVertices[v+1] = vm[4]*x  + vm[5]*y  + vm[6]*z  + vm[7]*w;
            globalVertices[v+2] = vm[8]*x  + vm[9]*y  + vm[10]*z + vm[11]*w;
            globalVertices[v+3] = vm[12]*x + vm[13]*y + vm[14]*z + vm[15]*w;
        }
    } 
    void convertVerticesToClipSpace(){
        double [] pm = cameras.get(cameraFocused).projectionMatrix.data;
        for(int v = 0; v < currentVerticesSize; v+= STRIDE){
            double x = globalVertices[v];
            double y = globalVertices[v+1];
            double z = globalVertices[v+2];
            double w = globalVertices[v+3];
            globalVertices[v]   = pm[0]*x  + pm[1]*y  + pm[2]*z  + pm[3]*w;
            globalVertices[v+1] = pm[4]*x  + pm[5]*y  + pm[6]*z  + pm[7]*w;
            globalVertices[v+2] = pm[8]*x  + pm[9]*y  + pm[10]*z + pm[11]*w;
            globalVertices[v+3] = pm[12]*x + pm[13]*y + pm[14]*z + pm[15]*w;
        }
    }
    void perspectiveDivideVectors(){
        for(int i = 0; i < currentVerticesSize; i+=STRIDE){
            double w = globalVertices[i + 3];
            if(w != 0){
                globalVertices[i] /= globalVertices[i+3];
                globalVertices[i+1] /= globalVertices[i+3];
                globalVertices[i+2] /= globalVertices[i+3];
            }
        }
    }
    void convertToNDC(WindowManager wm, TileManager tm){
        int width = wm.width;
        int length = wm.length;
        for(int i = 0; i < currentVerticesSize; i+= STRIDE){
            globalVertices[i] = (1+globalVertices[i]) * 0.5 * width;
            globalVertices[i+1] = ((1-globalVertices[i+1]) * 0.5 * length);
        }
    }
    void addCamera(CameraManager cm){
        cameras.put(cameraCount, cm);
        cameraCount++;
    }
    public void addProcessedTriangle(int v0, int v1, int v2, int entityID){
        processedIndices[processedIndicesSize++] = v0 * STRIDE;
        processedIndices[processedIndicesSize++] = v1 * STRIDE;
        processedIndices[processedIndicesSize++] = v2 * STRIDE;
        processedIndices[processedIndicesSize++] = entityID;
    }
    public void printSceneData(){
        // System.out.println("current Vertices size: " + currentVerticesSize);
        // System.out.println("current indices size: " + currentIndicesSize);
        System.out.println("processed indices size: " + processedIndicesSize);
        // System.out.println(Arrays.toString(globalIndices));
        // System.out.println(Arrays.toString(globalVertices));
    }
}
