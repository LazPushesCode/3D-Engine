import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Scene {
    HashMap<Integer, Entity> entities;
    HashMap<Integer, CameraManager> cameras;
    HashMap<Integer, Light> lights;
    

    static final int STRIDE = 13;

    static final int X_OFFSET = 0;
    static final int Y_OFFSET = 1;
    static final int Z_OFFSET = 2;
    static final int W_OFFSET = 3;
    static final int U_OFFSET = 4;
    static final int V_OFFSET = 5;
    static final int WX_OFFSET = 6;
    static final int WY_OFFSET = 7;
    static final int WZ_OFFSET = 8;
    static final int WW_OFFSET = 9;
    static final int NX_OFFSET = 10;
    static final int NY_OFFSET = 11;
    static final int NZ_OFFSET = 12;

    static final int PROCESSED_STRIDE = 3;

    static final int LIGHT_STRIDE = 11;

    int entityCount;
    int cameraCount;
    int lightCount;

    int cameraFocused;

    double xLightPos;
    double yLightPos;
    double zLightPos;
    double xLightDir;
    double yLightDir;
    double zLightDir;
    double yaw, pitch;
    double lightIntensity;

    int type;

    double ambience;

    double[] globalVertices;
    int[] globalIndices;
    int [] processedIndices;
    int initialVerticesSize;
    int initialIndicesSize;
    int currentIndicesSize;
    int processedIndicesSize;
    int currentVerticesSize;

    double[] lightBuffer;

    int [] threadEntityOffsets;
    int threadCount;
    ExecutorService entityPool;

    Scene(){
        entities = new HashMap<>();
        cameras = new HashMap<>();
        lights = new HashMap<>(); 
        cameraFocused = 0;
        entityCount = 0;
        cameraCount = 0;
        initialVerticesSize = 0;
        initialIndicesSize = 0;
        currentVerticesSize = 0;
        currentIndicesSize = 0;
        processedIndicesSize = 0;
        threadCount = Runtime.getRuntime().availableProcessors();
        threadEntityOffsets = new int[threadCount];
        entityPool = Executors.newFixedThreadPool(threadCount);

        xLightPos = 0;
        yLightPos = 0;
        zLightPos = 0;
        xLightDir = 0;
        yLightDir = 0;
        zLightDir = 0;
        type = 0;
        ambience = 0;
    }
    void addEntity(Entity et){
        entities.put(entityCount, et);
        et.ID = entityCount;
        entityCount++;
    }
    void addEntityChildren(Entity e){
        for(Entity c : e.children.values()){
            addEntity(c);
        }
    }
    void addLight(Light lt){
        lights.put(lightCount, lt);
        lt.ID = lightCount;
        lightCount++;
        lightBuffer = new double[LIGHT_STRIDE * lightCount];
    }
    void bindVertices(){
        initialVerticesSize = calculateVerticeCount();
        globalVertices = new double[initialVerticesSize* 2];
        initialIndicesSize = calculateTriangleCount();

        // initialIndicesSize = entities.size() * 120;

        globalIndices = new int[initialIndicesSize];
        processedIndices = new int[initialIndicesSize*2];
    }
    int calculateVerticeCount(){
        int sum = 0;
        for(Entity e: entities.values()){
            int verticeCount = e.vertices.length / 6;
            sum += verticeCount * STRIDE;
        }
        return sum;
    }
    int calculateTriangleCount(){
        int sum = 0;
        for(Entity e : entities.values()){
            int indiceCount = e.indices.length / 3;
            sum += indiceCount * 4;
        }
        return sum;
    }
    void resetSceneFrameData(){
        processedIndicesSize = 0;
        currentVerticesSize = 0;
    }
    void initializeGlobalIndices(){
    int currentIndicesIndex = 0;
    int vertexOffsetAccumulator = 0;
    
    for(Map.Entry<Integer, Entity> entry : entities.entrySet()){
        int ID = entry.getKey();
        Entity e = entry.getValue();
        
        e.globalVerticeOffset = vertexOffsetAccumulator;
        
        int indicesLength = e.indices.length;
        int index = currentIndicesIndex;
        for(int i = 0; i < indicesLength; i+=3){
            globalIndices[index]   = e.indices[i] + vertexOffsetAccumulator;
            globalIndices[index+1] = e.indices[i+1] + vertexOffsetAccumulator;
            globalIndices[index+2] = e.indices[i+2] + vertexOffsetAccumulator;
            globalIndices[index+3] = ID;
            index += 4;
        }
        vertexOffsetAccumulator += e.vertices.length / Entity.LOCAL_MESH_STRIDE;
        
        currentIndicesIndex = index;
    }
    currentIndicesSize = currentIndicesIndex;
}
void assignEntitiesToThreads(){
        int batch = (int) Math.ceil((double) entityCount / threadCount);
        int currOffset = entityCount;
        for(int i = threadCount-1; i >= 0; i--){
            threadEntityOffsets[i] = currOffset;
            currOffset -= batch;
            if(currOffset < 0 ) currOffset = 0;
        }
    }
    void entityConversions(){
        currentVerticesSize = 0;
        for(Entity e : entities.values()){
            currentVerticesSize += (e.vertices.length/Entity.LOCAL_MESH_STRIDE)*STRIDE;
        }
        CountDownLatch latch = new CountDownLatch(threadCount);
        CameraManager cm = cameras.get(cameraFocused);
        int start = 0;
        for(int i = 0; i < threadCount; i++){
            final int threadStart = start;
            int end = threadEntityOffsets[i];
            entityPool.execute(() -> {
                try {
                    for(int j = threadStart; j < end; j++){
                        transformEntity(j, cm);
                    }
                } finally {
                    latch.countDown();
                }
            });
            start = threadEntityOffsets[i];
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    void transformEntity(int id, CameraManager cm){
        Entity e = entities.get(id);
        e.applyTransformationValues();

            Matrix t = e.transformation;
            Matrix cameraMatrix = cm.combinedMatrix;
            double[] tData = t.data;
            double[] cData = cameraMatrix.data;
            double[] localMesh = e.vertices;
            int start = e.globalVerticeOffset * STRIDE;
            int end = start + (localMesh.length/Entity.LOCAL_MESH_STRIDE)*STRIDE;
            int index = 0;
            for(int v = start; v < end; v+= STRIDE){
                double x = localMesh[index];
                double y = localMesh[index+Y_OFFSET];
                double z = localMesh[index+Z_OFFSET];
                double w = localMesh[index+W_OFFSET];

                double nx = localMesh[index+6];
                double ny = localMesh[index+7];
                double nz = localMesh[index+8];

                double wxNorm = tData[0]*nx + tData[1]*ny + tData[2]*nz;
                double wyNorm = tData[4]*nx + tData[5]*ny + tData[6]*nz;
                double wzNorm = tData[8]*nx + tData[9]*ny + tData[10]*nz;

                double wx = tData[0]*x  + tData[1]*y  + tData[2]*z  + tData[3]*w;
                double wy = tData[4]*x  + tData[5]*y  + tData[6]*z  + tData[7]*w;
                double wz = tData[8]*x  + tData[9]*y  + tData[10]*z + tData[11]*w;
                double ww = tData[12]*x + tData[13]*y + tData[14]*z + tData[15]*w;

                globalVertices[v]   = cData[0]*wx  + cData[1]*wy  + cData[2]*wz  + cData[3]*ww;
                globalVertices[v+Y_OFFSET] = cData[4]*wx  + cData[5]*wy  + cData[6]*wz  + cData[7]*ww;
                globalVertices[v+Z_OFFSET] = cData[8]*wx  + cData[9]*wy  + cData[10]*wz + cData[11]*ww;
                globalVertices[v+W_OFFSET] = cData[12]*wx + cData[13]*wy + cData[14]*wz + cData[15]*ww;
                
                globalVertices[v+U_OFFSET] = localMesh[index+U_OFFSET];
                globalVertices[v+V_OFFSET] = localMesh[index+V_OFFSET];


                globalVertices[v+WX_OFFSET] = wx;
                globalVertices[v+WY_OFFSET] = wy;
                globalVertices[v+WZ_OFFSET] = wz;
                globalVertices[v+WW_OFFSET] = ww;

                globalVertices[v+NX_OFFSET] = wxNorm;
                globalVertices[v+NY_OFFSET] = wyNorm;
                globalVertices[v+NZ_OFFSET] = wzNorm;
                
                index += Entity.LOCAL_MESH_STRIDE;
            }
    }
    void perspectiveDivideVectors(){
        for(int i = 0; i < currentVerticesSize; i+=STRIDE){
            double w = globalVertices[i + W_OFFSET];
            if(w != 0){
                globalVertices[i] /= globalVertices[i+W_OFFSET];
                globalVertices[i+Y_OFFSET] /= globalVertices[i+W_OFFSET];
                globalVertices[i+Z_OFFSET] /= globalVertices[i+W_OFFSET];
            }
        }
    }
    void convertToNDC(WindowManager wm, TileManager tm){
        int width = wm.width;
        int length = wm.length;
        for(int i = 0; i < currentVerticesSize; i+= STRIDE){
            globalVertices[i] = (1+globalVertices[i]) * 0.5 * width;
            globalVertices[i+Y_OFFSET] = ((1-globalVertices[i+Y_OFFSET]) * 0.5 * length);
        }
    }
    void addCamera(CameraManager cm){
        cameras.put(cameraCount, cm);
        cameraCount++;
    }
    public void addProcessedTriangle(int v0, int v1, int v2, int entityID){
        v0 *= STRIDE;
        v1 *= STRIDE;
        v2 *= STRIDE;
        processedIndices[processedIndicesSize++] = v0;
        processedIndices[processedIndicesSize++] = v1;
        processedIndices[processedIndicesSize++] = v2;
        processedIndices[processedIndicesSize++] = entityID;
        
        // calculateTriangleLightLevel(v0, v1, v2);
    }
    void fillLightBuffer(){
        int currentLight = 0;
        for(Light l : lights.values()){
            int index = currentLight * LIGHT_STRIDE;
            lightBuffer[index] = l.x;
            lightBuffer[index+1] = l.y;
            lightBuffer[index+2] = l.z;
            lightBuffer[index+3] = l.xDir;
            lightBuffer[index+4] = l.yDir;
            lightBuffer[index+5] = l.zDir;
            lightBuffer[index+6] = l.intensity;
            lightBuffer[index+7] = l.type;
            lightBuffer[index+8] = l.r;
            lightBuffer[index+9] = l.g;
            lightBuffer[index+10] = l.b;
            currentLight++;
        }
    }
    public void setLight(int t, double intensity, double x, double y, double z, double lightYaw, double lightPitch, double a){
        xLightPos = x;
        yLightPos = y;
        zLightPos = z;
        yaw = lightYaw;
        pitch = lightPitch;
        ambience = a;
        lightIntensity = intensity;
        type = t;
    }
    void applyLightRotations(){
        for(Light l : lights.values()){
            l.applyRotation();
        }
    }
    void LightYawRotate(double y){
        yaw += y;
    }
    void lightPitchRotate(double p){
        pitch += p;
    }
    public void printSceneData(){
        // System.out.println("current Vertices size: " + currentVerticesSize);
        // System.out.println("current indices size: " + currentIndicesSize);
        // System.out.println("processed indices size: " + processedIndicesSize);
        // System.out.println(Arrays.toString(globalIndices));
        System.out.println(Arrays.toString(globalVertices));
    }
}
