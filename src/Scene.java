import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Scene {
    HashMap<Integer, Entity> entities;
    HashMap<Integer, Entity> parents;
    HashMap<Integer, Camera> cameras;
    HashMap<Integer, Light> lights;

    Camera activeCamera;


    HashMap<Integer, ParticleSystem> particleSystems;
    
    

    static final int STRIDE = 16; // GlobalVertice memory stride (16 values per indice)
    // static final int STRIDE = 13;

    // Indice values
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
    static final int TX_OFFSET = 13;
    static final int TY_OFFSET = 14;
    static final int TZ_OFFSET = 15;

    
    static final int INDICE_STRIDE = 4; // Indice Stride (holds 3 triangle indices and entityID) ]

    static final int LIGHT_STRIDE = 15; 

    int entityCount;
    int cameraCount;
    int lightCount;
    int parentCount;
    int particleSystemCount;

    int cameraFocused;

    // Main scene lighting (outdated)
    double xLightPos;
    double yLightPos;
    double zLightPos;
    double xLightDir;
    double yLightDir;
    double zLightDir;
    double yaw, pitch;
    double lightIntensity;
    int type;

    double ambience; // Overall brightness of scene

    double[] globalVertices; // Holds all vertices of entities owned by scene
    int[] globalIndices; // Holds all indices of entities owned by scene
    int [] processedIndices; // Holds indices that have passed culling operations
    int initialVerticesSize; // Max vertice space (reallocate if needed)
    int initialIndicesSize; // Max indice space (reallocate if needed)
    int currentIndicesSize; // Number of space used up in indice buffer 
    int processedIndicesSize; 
    int currentVerticesSize; // Number of space used up in vertice buffer

    double[] lightBuffer; // Holds all light data values

    int [] threadEntityOffsets; // Holds range of entity ID's in each slot 
    int threadCount; 
    ExecutorService entityPool;
    
    // Initialize Scene class
    Scene(){
        entities = new HashMap<>();
        cameras = new HashMap<>();
        lights = new HashMap<>(); 
        parents = new HashMap<>();
        particleSystems = new HashMap<>();
        // lightsList = new HashMap<>();
        cameraFocused = 0;
        entityCount = 0;
        cameraCount = 0;
        parentCount = 0;
        particleSystemCount = 0;
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
    // Adds Entity to entity HashMap
    void addEntity(Entity et){
        entities.put(entityCount, et);
        et.ID = entityCount;
        entityCount++;
        for(Entity c: et.children.values()){
            addEntity(c);
        }
        // If entity has no parent, then it is a parent
        // If entity is a parent, add to parent list
        if(et.parent == null){
            parents.put(parentCount, et);
            parentCount++;
        }
    } 
    // Adds Light to light HashMap
    void addLight(Light lt){
        lights.put(lightCount, lt);
        lt.ID = lightCount;
        lightCount++;
        lightBuffer = new double[LIGHT_STRIDE * lightCount];
    }
    // Adds camera to camera hashmap
    void addCamera(Camera cm){
        cameras.put(cameraCount, cm);
        cameraCount++;
    }
    // Add Particle System to particleSystem HashMap
    void addParticleSystem(ParticleSystem ps){
        particleSystems.put(particleSystemCount, ps);
        ps.ID = particleSystemCount;
        particleSystemCount++;
    }
    // Occurs once during startup
    // Calculates total number of vertices and indices needed to initialize buffers
    void bindVertices(){
        initialVerticesSize = calculateVerticeCount()*10;
        globalVertices = new double[initialVerticesSize];
        currentVerticesSize = initialVerticesSize;
        initialIndicesSize = calculateTriangleCount()*2;

        globalIndices = new int[initialIndicesSize];
        processedIndices = new int[initialIndicesSize];
    }
    void reallocateGlobalVerticeMemory(){
        initialVerticesSize *= 4;
        double[] temp = new double[initialVerticesSize];
        System.arraycopy(globalVertices, 0, temp, 0, globalVertices.length);
        globalVertices = temp;
    }
    // Obtains total number of vertices and returns vertices * STRIDE for total space count needed
    int calculateVerticeCount(){
        int sum = 0; // Sum will equal the total amount of vertices in the scene
        for(Entity e: entities.values()){
            if(e.worldVertices == null) continue;
            int calculatedVerticeCount = e.worldVertices.length / Entity.INITIAL_MESH_STRIDE; // Divide by initial mesh stride to get its vertice count
            e.verticeCount = calculatedVerticeCount;
            sum += calculatedVerticeCount; 
        }
        return sum*STRIDE;
    }
    // Obtains total number of indices and returns space needed for the buffer
    int calculateTriangleCount(){
        int sum = 0;
        for(Entity e : entities.values()){
            if(e.indices == null) continue;
            int triangleCount = e.indices.length / 3; // Divide buffer length by 3 to obtain triangle count 
            sum += triangleCount + e.indices.length; // Add count and length to add 1 array index to every triangle.
            // Each triangle has increased from 3 array indexes (3 indices) to 4 now appending the entityID to each
        }
        return sum;
    }
    // Reverts temporary buffer sizes back to 0 (occurs at each frame)
    void resetSceneFrameData(){
        processedIndicesSize = 0;
        // currentVerticesSize = 0;
    }
    // Occurs once during settup alongside bindVertices()
    void initializeGlobalIndices(){
        int currentIndicesIndex = 0;
        int vertexOffsetAccumulator = 0;
        
        for(Map.Entry<Integer, Entity> entry : entities.entrySet()){
            int ID = entry.getKey();
            Entity e = entry.getValue();
            if(e.worldVertices == null || e.indices == null) continue;
            e.globalVerticeOffset = vertexOffsetAccumulator; // Entity given direct access to its place globalIndice buffer
            
            int indicesLength = e.indices.length;
            int index = currentIndicesIndex; // Beginning of entity's spot in the globalIndice buffer
            for(int i = 0; i < indicesLength; i+=3){ // Iterate through entity's indice buffer
                // Add each indice value to globalIndice and add vertex offset
                globalIndices[index]   = e.indices[i] + vertexOffsetAccumulator;
                globalIndices[index+1] = e.indices[i+1] + vertexOffsetAccumulator;
                globalIndices[index+2] = e.indices[i+2] + vertexOffsetAccumulator;
                globalIndices[index+3] = ID;
                index += INDICE_STRIDE;
            }
            vertexOffsetAccumulator += e.worldVertices.length / Entity.LOCAL_MESH_STRIDE; // Increment offset by entity's vertice count
            
            currentIndicesIndex = index; // Set new start
        }
        currentIndicesSize = currentIndicesIndex; // Set size of Indices
    }
    // Divides entities up among given threads, accelerating transformation computation
    void assignEntitiesToThreads(){
        int batch = (int) Math.ceil((double) entityCount / threadCount); // Each thread receives the same number of entities in a batch
        int currOffset = entityCount;
        for(int i = threadCount-1; i >= 0; i--){ // Iterate through each thread
            threadEntityOffsets[i] = currOffset; // Store range of entitiess that ith thread will compute
            currOffset -= batch; 
            if(currOffset < 0 ) currOffset = 0;
        }
    }
    // Updates all parent transformations
    void updateEntityTransformations(){
        for(Entity e : parents.values()){
            e.updateTransformations(null); // Recursively updates parent and all its childrens transformations
        }
    }
    // Converts all of scene's entities from local -> world -> view -> clip space
    void entityConversions(){
        CountDownLatch latch = new CountDownLatch(threadCount);
        int start = 0;

        // Begin multi-threading process to transform entities
        for(int i = 0; i < threadCount; i++){
            final int threadStart = start;
            int end = threadEntityOffsets[i];
            entityPool.execute(() -> {
                try {
                    for(int j = threadStart; j < end; j++){
                        transformEntity(j, activeCamera); // Each thread transforms their jth entity
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
    // Converts given entity from local all the way to clip space
    // Parameters include entity id and camera
    void transformEntity(int id, Camera cm){
        Entity e = entities.get(id); // Retrieve entity using id
            if(e.worldVertices == null) return;
            Matrix t = e.transformation; // Reference to entity's matrix transformation
            Matrix cameraMatrix = cm.combinedMatrix; // Reference to camera's matrix (view * projection)
            double[] tData = t.data; // Double array containing entity matrix transformation values
            double[] cData = cameraMatrix.data; // Double array containing camera matrix transformation values
            double[] localMesh = e.worldVertices; // Double array containing entity world space vertice data
            
            int start = e.globalVerticeOffset * STRIDE; // Begin at entity's position in globalVertices array
            int end = start + (localMesh.length/Entity.LOCAL_MESH_STRIDE)*STRIDE; // End of entity's memory in globalVertices array
            int index = 0;
            for(int v = start; v < end; v+= STRIDE){ // Iterate from start to end of entity's globalVertice positioning
                // World x, y, and z positioning.
                double x = localMesh[index]; 
                double y = localMesh[index+Y_OFFSET];
                double z = localMesh[index+Z_OFFSET];
                double w = localMesh[index+W_OFFSET]; // Value used for perspective division

                // Normal Vector
                double nx = localMesh[index+6]; 
                double ny = localMesh[index+7];
                double nz = localMesh[index+8];

                // Transform normal vector to world normal vector position
                double wxNorm = tData[0]*nx + tData[1]*ny + tData[2]*nz;
                double wyNorm = tData[4]*nx + tData[5]*ny + tData[6]*nz;
                double wzNorm = tData[8]*nx + tData[9]*ny + tData[10]*nz;

                // Transform previous world vectors to new world vectors (could remain the same)
                double wx = tData[0]*x  + tData[1]*y  + tData[2]*z  + tData[3]*w;
                double wy = tData[4]*x  + tData[5]*y  + tData[6]*z  + tData[7]*w;
                double wz = tData[8]*x  + tData[9]*y  + tData[10]*z + tData[11]*w;
                double ww = tData[12]*x + tData[13]*y + tData[14]*z + tData[15]*w;

                // Convert vertices to clip space by applying camera's matrix on vertices
                globalVertices[v]   = cData[0]*wx  + cData[1]*wy  + cData[2]*wz  + cData[3]*ww; 
                globalVertices[v+Y_OFFSET] = cData[4]*wx  + cData[5]*wy  + cData[6]*wz  + cData[7]*ww; 
                globalVertices[v+Z_OFFSET] = cData[8]*wx  + cData[9]*wy  + cData[10]*wz + cData[11]*ww;
                globalVertices[v+W_OFFSET] = cData[12]*wx + cData[13]*wy + cData[14]*wz + cData[15]*ww;
                
                // UV retains original values in range [0,1]
                globalVertices[v+U_OFFSET] = localMesh[index+U_OFFSET];
                globalVertices[v+V_OFFSET] = localMesh[index+V_OFFSET];

                // Add calculated vertices to globalVertices
                globalVertices[v+WX_OFFSET] = wx;
                globalVertices[v+WY_OFFSET] = wy;
                globalVertices[v+WZ_OFFSET] = wz;
                globalVertices[v+WW_OFFSET] = ww;
                globalVertices[v+NX_OFFSET] = wxNorm;
                globalVertices[v+NY_OFFSET] = wyNorm;
                globalVertices[v+NZ_OFFSET] = wzNorm;
                
                index += Entity.LOCAL_MESH_STRIDE; // Go to next vertice
            }
    }
    // Divides vertice x, y, and z by its w value
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
    // Converts from pixel in the range of height and width to between -1 and 1. Used for clipping before rendering to screen
    void convertToNDC(WindowManager wm, TileManager tm){
        int width = wm.width;
        int length = wm.length;
        for(int i = 0; i < currentVerticesSize; i+= STRIDE){
            globalVertices[i] = (1+globalVertices[i]) * 0.5 * width; // X ndc cord
            globalVertices[i+Y_OFFSET] = ((1-globalVertices[i+Y_OFFSET]) * 0.5 * length); // Y ndc cord
        }
    }
    // Adds 3 indices (forming triangle) and ID as parameters to processedIndices buffer if passes all culling tests
    public void addProcessedTriangle(int v0, int v1, int v2, int entityID){
        v0 *= STRIDE;
        v1 *= STRIDE;
        v2 *= STRIDE;
        processedIndices[processedIndicesSize++] = v0;
        processedIndices[processedIndicesSize++] = v1;
        processedIndices[processedIndicesSize++] = v2;
        processedIndices[processedIndicesSize++] = entityID;
    }
    // Refills lightBuffer with given light values. Occurs every frame
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
            lightBuffer[index+8] = l.innerCutOff;
            lightBuffer[index+9] = l.outerCutOff;
            lightBuffer[index+10] = l.linearAttenuation;
            lightBuffer[index+11] = l.quadraticAttenuation;
            lightBuffer[index+12] = l.r;
            lightBuffer[index+13] = l.g;
            lightBuffer[index+14] = l.b;
            currentLight++;
        }
    }
    // Set the values of given Light
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
    // Applies rotation matrix on all lights
    void applyLightRotations(){
        for(Light l : lights.values()){
            l.applyRotation();
        }
    }
    // Converts particleSystems from local -> clip space
    void convertParticlesToClipSpace(Camera cm, int windowWidth, int windowLength){
        for(ParticleSystem ps : particleSystems.values()){
            ps.convertToClipSpace(cm, windowWidth, windowLength);
        }
    }
    void LightYawRotate(double y){
        yaw += y;
    }
    void lightPitchRotate(double p){
        pitch += p;
    }
    // Debugging
    public void printSceneData(){
        System.out.println("current Vertices size: " + currentVerticesSize);
        System.out.println("current indices size: " + currentIndicesSize);
        System.out.println("processed indices size: " + processedIndicesSize);
        System.out.println(Arrays.toString(globalIndices));
        System.out.println(Arrays.toString(globalVertices));
    }
}
