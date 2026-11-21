import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

public class Entity {

    double[] worldVertices; 
    double[] staticVertices; // Keeps original vertice state
    int[] indices; // List  of indices.  Every 3 indices = 1 Triangle

    int[] textureBuffer; // Holds texture pixel colors (textureHeight x textureWidth)
    int textureHeight;
    int textureWidth;
    boolean hasTexture; 

    int[] normalMapBuffer; 
    int normalMapHeight;
    int normalMapWidth;
    boolean hasNormalMap;

    double materialDiffuse; // Uniform scattering of light across surface
    double materialSpecular; // Controls how light reflects off the surface
    double materialShininess; // Determines the shininess of the surface

    boolean greenScreen; // If true, does not render green coloring belonging to entity
    boolean transparent; // If true, entity becomes see through dependingo n alpha level
    double alpha; // Determines level of opacity

    BufferedImage texture; 
    BufferedImage normalMap;
    Matrix transformation; // Translates, scales, and rotates the entity
    double x, y, z; // Determines world position
    double yaw, pitch, roll; // Determines rotations around its local space
    double xSize, ySize, zSize; // Determines scale of entity
    Quaternion orientation; // Determines spatial orientation and rotation of entity
    int verticeCount; 
    int globalVerticeOffset; // Start position of entity vertices inside scene' global vertice array
    int globalIndiceOffset; // Start position of entity indices inside scene's global indice array
    int defaultColor = 0xFFFFFF; // If entity has no texture, defaults to this color

    Entity parent; // Entity inherits parent's transformation
    HashMap<Integer, Entity> children; // Children inherit entity's transformation

    int ID; // Entity ID given by scene class
    int childrenCount; 

    int animationID; // Determines the animation to play on entity
    int indexID; // Entity's animation ID given by AnimationManager
    long prevTime; // Determines how much animation time has passed
    int currentKeyFrame; // Current key frame animation timeline is on

    static final int TRACK_STRIDE = 4; // Size of single entity animation memory
    static final int TRACK_ANIMATIONID = 0;
    static final int TRACK_CURRENT_FRAME = 1;
    static final int TRACK_GROUP_OFFSET= 2;
    static final int TRACK_VERTICE_COUNT = 3;

    int[] trackData; // Holds animation id, current frame, group offset (start) and vertice count (end)
    double[] trackPrevTimes; // Holds prev times
    int[] trackIndices; // Indice groups which can have animations applied to them

    int currentTrackIndiceSize; // How many indexes have been filled in trackIndices (max is maxTrackIndiceSize)
    int maxTrackIndiceSize; // Max size of trackIndices (if reach max then reallocate)
    int trackCount; 
    

    static final int INITIAL_MESH_STRIDE = 6; // Initial vector size (x, y, z, w, u, v)
    static final int LOCAL_MESH_STRIDE = 12; // Vector size after baking normal and tangent vectors (x, y, z, w, u, v, nx, ny, nz, tx, ty, tz)
    // static final int LOCAL_MESH_STRIDE = 9;

    boolean backFaceCulling; // Determines if entity undergoes backface culling operations
    boolean prioritize; // Determines if entity is prioritized to render in front of other entities regardless of depth
    boolean affectedByLight; // Determines if entity undergoes pixel lighting operations
    
    // Initializes entity and variables. Requires secondary mesh generation to create mesh data
    Entity(){
        initializeVariables();
        transformation = Matrix.Identity();
    }
    // Manually input mesh data and transformation
    Entity(double [] vertices, int [] ind, Matrix d){
        vertices = MeshGenerator.bakeNormalVectors(vertices, ind);
        initializeArrays(vertices, ind);
        initializeVariables();
        transformation = Matrix.Identity();
    }
    // Initializes all entity variables used in the pipeline
    void initializeVariables(){
        x = 0;
        y = 0;
        z = 0;
        yaw = 0;
        pitch = 0;
        roll = 0;
        xSize = 1;
        ySize = 1;
        zSize = 1;
        orientation = new Quaternion();
        materialDiffuse = 0.8;
        materialSpecular = 0.5;
        materialShininess = 64;
        alpha = 1.0;
        greenScreen = false;
        transparent = false;
        backFaceCulling = true;
        prioritize = false;
        affectedByLight = true;
        hasTexture = false;
        hasNormalMap = false;
        childrenCount = 0;
        verticeCount = 0;
        currentTrackIndiceSize = 0;
        trackCount = 0;
        parent = null;
        prevTime = 0;
        currentKeyFrame = 0;

        children = new HashMap<>();
    }
    // Sets entity material values including diffuse, specular, and shininess
    void setMaterialValues(double diff, double spec, double shiny){
        materialDiffuse = diff;
        materialSpecular = spec;
        materialShininess = shiny;
    }
    // Updates entity transformation. Applies parent transformation onto entity and applies entity transformations onto children
    void updateTransformations(Matrix parentMatrix){
        Matrix local = Matrix.Translate(x,y,z)
        .multiply(Matrix.Rotate(orientation))
        .multiply(Matrix.Scale(xSize, ySize, zSize));
        this.transformation = (parentMatrix != null) ? parentMatrix.multiply(local) : local;
        for(Entity child : children.values()){
            child.updateTransformations(this.transformation);
        }
    }
    // Sets texture variables based on given texture
    void applyTexture(Texture t){
        textureHeight = t.height;
        textureWidth = t.width;
        textureBuffer = t.buffer;
        hasTexture = true;
    }
    void setTexture(BufferedImage t){
        try{
            texture = t;
            textureHeight = texture.getHeight();
            textureWidth = texture.getWidth();
            textureBuffer = new int[textureHeight * textureWidth];
            texture.getRGB(0, 0, textureWidth, textureHeight, textureBuffer, 0, textureWidth);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    // Sets normal map variables based on given texture
    void applyNormalMap(Texture t){
        normalMapHeight = t.height;
        normalMapWidth = t.width;
        normalMapBuffer = t.buffer;
        hasNormalMap = true;
    }
    void setNormalMap(BufferedImage t){
        try{
            normalMap = t;
            normalMapHeight = normalMap.getHeight();
            normalMapWidth = normalMap.getWidth();
            normalMapBuffer = new int[normalMapHeight * normalMapWidth];
            normalMap.getRGB(0, 0, normalMapWidth, normalMapHeight, normalMapBuffer, 0, normalMapWidth);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    // Initializes all buffers and attached variables
    void initializeArrays(double [] givenVertices, int [] givenIndices){
        int vc = givenVertices.length/9;
        staticVertices = new double[givenVertices.length];
        System.arraycopy(givenVertices, 0, staticVertices, 0, givenVertices.length);
        worldVertices = givenVertices;
        indices = givenIndices;
        trackData = new int[TRACK_STRIDE*vc];
        trackPrevTimes = new double[vc];
        trackIndices = new int[vc];
        maxTrackIndiceSize = vc;
    }
    // Adds given indices to trackIndices buffer
    void addTrackIndices(int[] givenIndices){
        if(currentTrackIndiceSize + givenIndices.length > maxTrackIndiceSize){ //reallocate if estimated size is greater than max
            maxTrackIndiceSize *= 2;
            int[] newTrackIndices = new  int[maxTrackIndiceSize];
            System.arraycopy(trackIndices, 0, newTrackIndices, 0, currentTrackIndiceSize);
            trackIndices = newTrackIndices;
        }
        System.arraycopy(givenIndices, 0, trackIndices, currentTrackIndiceSize, givenIndices.length);
        int index = trackCount * TRACK_STRIDE; // Gives starting index of availble slot in trackIndices

        // Place track data into array using index and offsets
        trackData[index] = 0;
        trackData[index+TRACK_CURRENT_FRAME] = 0;
        trackData[index+TRACK_GROUP_OFFSET] = currentTrackIndiceSize;
        trackData[index+TRACK_VERTICE_COUNT] = givenIndices.length;
        currentTrackIndiceSize += givenIndices.length;
        trackCount++;
    }
    // Assigns cube vertices and indice data to Entity's buffers
    void cubeMesh(boolean inverted){ 
        // Use MeshGenerator to procedurally generate cube data
        double[] cubeVertices = MeshGenerator.generateCubeVertices();
        int[] cubeIndices = MeshGenerator.generateCubeIndices(inverted);
        cubeVertices = MeshGenerator.bakeNormalVectors(cubeVertices, cubeIndices);
        
        initializeArrays(cubeVertices, cubeIndices);
        initializeVariables();
        transformation = Matrix.Identity();
    }
    // Assigns square vertices and indice data to Entity's buffers
    void squareMesh(){
        // Use MeshGenerator to procedurally generate square
        double[] squareVertices = MeshGenerator.generateSquareVertices();
        int[] squareIndices = MeshGenerator.generateSquareIndices();
        squareVertices = MeshGenerator.bakeNormalVectors(squareVertices, squareIndices);
        initializeArrays(squareVertices, squareIndices);
        initializeVariables();
        transformation = Matrix.Identity();
    }
    // Similar to squareMesh(), but creates bounding box from (uStart, uEnd) to (vStart, vEnd)
    void squareMeshWithBoundingBox(int uStart, int uEnd, int vStart, int vEnd){

        // Normalizes uv cords to range [-0.5, 0.5]
        double uMin = -0.5 + ((double)uStart / 100);
        double uMax = -0.5 + ((double)uEnd / 100);
        double vMin = -0.5 + ((double)vStart / 100);
        double vMax = -0.5 + ((double)vEnd / 100);
        double[] squareVertices = {
            //top
            -0.5, 0.5,  0, 1,  0.0, 1.0,
            -0.5, vMax, 0, 1,  0.0, vMax + 0.5,
            0.5, vMax, 0, 1,  1.0, vMax + 0.5,
            0.5, 0.5,  0, 1,  1.0, 1.0,

            // mid
            -0.5, vMax, 0, 1,  0.0, vMax + 0.5,
            -0.5, vMin, 0, 1,  0.0, vMin + 0.5,
            uMin, vMin, 0, 1,  uMin + 0.5, vMin + 0.5,
            uMin, vMax, 0, 1,  uMin + 0.5, vMax + 0.5,

            uMax, vMax, 0, 1,  uMax + 0.5, vMax + 0.5,
            uMax, vMin, 0, 1,  uMax + 0.5, vMin + 0.5,
            0.5, vMin, 0, 1,  1.0, vMin + 0.5,
            0.5, vMax, 0, 1,  1.0, vMax + 0.5,

            //  bottom
            -0.5, vMin,  0, 1,  0.0, vMin + 0.5,
            -0.5, -0.5,  0, 1,  0.0, 0.0,
            0.5, -0.5,  0, 1,  1.0, 0.0,
            0.5, vMin,  0, 1,  1.0, vMin + 0.5
        };
        int[] squareIndices = {
            0, 1, 3,
            1, 2, 3,

            4, 5, 7,
            5, 6, 7,

            8, 9, 11,
            9, 10, 11,

            12, 13, 15,
            13, 14, 15
        };
        squareVertices = MeshGenerator.bakeNormalVectors(squareVertices, squareIndices);
        initializeArrays(squareVertices, squareIndices);
        initializeVariables();
        transformation = Matrix.Identity();
    }
    // Assigns sphere vertices and indice data to Entity's buffers
    // Takes m (rows) and n (columns) to create m x n indices. 
    // Creates bounding box from (uStart, uEnd) to (vStart, vEnd) on sphere
    void sphereMesh(int m, int n, double outerRadius, double innerRadius, int uStart, int uEnd, int vStart, int vEnd){
        int indiceCount = (m+1)*(n+1); 
        double[] sphereVertices = new double[((indiceCount)*2* INITIAL_MESH_STRIDE)];
        int verticesIndex = 0;
        for(double r : new double[]{innerRadius, outerRadius}){
            for(int i = 0; i <= m; i++){ // Iterate through rows

                double fy = ((double)i / (double)m); // i positioning in m range

                for(int j = 0; j <= n; j++){ // Iterate through columns

                    double fx = ((double)j / (double)n); // j positioning in n range

                    double pihalf = -((double)Math.PI/2);

                    // Calculate longtitude and latitude
                    double longtitude = -((double)(Math.PI/2)) + fy * Math.PI;
                    double latitude = fx * 2 * Math.PI;

                    // Calculate spherical coordinates using given formulates
                    double x = r*Math.cos(longtitude)  * Math.cos(latitude);
                    double y = r*Math.sin(longtitude);
                    double z = r*Math.cos(longtitude) * Math.sin(latitude);

                    // Add coordinates to vertice array
                    sphereVertices[verticesIndex] = x;
                    sphereVertices[verticesIndex+1] = y;
                    sphereVertices[verticesIndex+2] = z;
                    sphereVertices[verticesIndex+3] = 1;

                    // Clamp
                    if (fx >= 1.0) fx = 0.99;
                    if (fx <= 0.0) fx = 0.01;

                    sphereVertices[verticesIndex+4] = fx;
                    sphereVertices[verticesIndex+5] = fy;
                    verticesIndex+=INITIAL_MESH_STRIDE;
                }
            }
        }
        int indiceAllocation = m * n * 2 * 3;
        int[] sphereIndices = new int[indiceAllocation*2];
        int indicesIndex = 0;
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                
                // If inside bounding box, skip this ij
                boolean insideU = (j >= uStart && j < uEnd);
                boolean insideV = (i >= vStart && i < vEnd);
                if(insideU && insideV){
                    continue;
                }

                // Assign 4 indices
                int p0 = i * (n+1) + j;
                int p1 = (i+1) * (n+1) + j;
                int p2 = p1 + 1;
                int p3 = p0 + 1;

                // Add indices as 2 triangles (interior)
                sphereIndices[indicesIndex] = p0;
                sphereIndices[indicesIndex+1] = p3;
                sphereIndices[indicesIndex+2] = p1;

                sphereIndices[indicesIndex+3] = p1;
                sphereIndices[indicesIndex+4] = p3;
                sphereIndices[indicesIndex+5] = p2; 

                // Add triangles as exterior
                sphereIndices[indicesIndex+indiceAllocation] = p0+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+1] = p1+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+2] = p3+verticeCount;

                sphereIndices[indicesIndex+indiceAllocation+3] = p1+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+4] = p2+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+5] = p3+verticeCount; 

                indicesIndex += 6; // Increment by two triangles (6 indices)
            }
        }
        sphereIndices = Arrays.copyOf(sphereIndices, indicesIndex*2);
        sphereVertices = MeshGenerator.bakeNormalVectors(sphereVertices, sphereIndices);
        initializeArrays(sphereVertices, sphereIndices);
        initializeVariables();
        transformation = Matrix.Identity();
    }
    void generateFlatDynamicMesh(int row, int col){
        double xCurr = -0.5;
        double zCurr = -0.5;
        double height = 1 / (double)(row-1); // Increment to add to xCurr
        double width = 1 / (double)(col-1); // Increment to add to zCurr

        int indIdx = 0;
        double[] meshVertices = new double[(int)(row*col)*INITIAL_MESH_STRIDE];
        int[] meshIndices = new int[(int)((row-1)*(col-1))*6];
        for(int i = 0; i < row; i++){
            int rowOffset = ((i*col)); 
            for(int j = 0; j < col; j++){
                int index = (rowOffset+j)*INITIAL_MESH_STRIDE;
                meshVertices[index] = xCurr; 
                meshVertices[index+1] = 0; 
                meshVertices[index+2] = zCurr; 
                meshVertices[index+3] = 1; 
                meshVertices[index+4] = 0; 
                meshVertices[index+5] = 0;
                zCurr += width;
            }
            xCurr += height; 
            zCurr = -0.5;
        }
        int vertIdx = 0;
        // Stitch indices together
        for(int i = 0; i < row-1; i++){
            for(int j = 0; j < col-1; j++){
                meshIndices[indIdx++]= vertIdx; meshIndices[indIdx++] = vertIdx+1; meshIndices[indIdx++] = vertIdx+7;
                meshIndices[indIdx++]= vertIdx+7; meshIndices[indIdx++] = vertIdx+6; meshIndices[indIdx++] = vertIdx;
                vertIdx++;
            }
            vertIdx++;
        }
        meshVertices = MeshGenerator.bakeNormalVectors(meshVertices, meshIndices);
        initializeArrays(meshVertices, meshIndices);
        initializeVariables();
        transformation = Matrix.Identity();
    }
    void triangleMesh(){
        double[] triangleVertices = MeshGenerator.generateTriangleVertices();
        int [] triangleIndices = MeshGenerator.generateTriangleIndices();
        initializeArrays(triangleVertices, triangleIndices);
        initializeVariables();
        transformation = Matrix.Identity();
    }
    static Entity generateFlat3DShape(double[] blueprint, int vertCount){
        int totalVertices = (vertCount+vertCount+(4*vertCount));
        int doubleVerticeCount = vertCount*2;
        int totalSideTriangles = (doubleVerticeCount)*3;
        int totalFrontAndBackTriangles = (vertCount - 2) * 3 * 2;
        double[] shapeVertices = new  double[totalVertices*INITIAL_MESH_STRIDE];
        int[] shapeIndices = new int[totalSideTriangles + totalFrontAndBackTriangles];
        int triCount = vertCount - 2;
        int verticeIndex = 0;
        int currVertex = 0;
        int indiceIndex = 0;
        int halfway = vertCount * INITIAL_MESH_STRIDE;
        System.arraycopy(blueprint, 0, shapeVertices, 0, halfway);
        System.arraycopy(blueprint, halfway, shapeVertices, halfway, halfway);
        currVertex = (vertCount + vertCount);
        verticeIndex = currVertex*INITIAL_MESH_STRIDE;
        indiceIndex= (totalFrontAndBackTriangles);
        for(int i = 0; i < triCount; i++){
            int frontIndex = i * 3;
            int backStride = (i+triCount);
            int backIndex = backStride * 3;
            shapeIndices[frontIndex] = 0;
            shapeIndices[frontIndex+1] = i + 2;
            shapeIndices[frontIndex+2] = i + 1;
            shapeIndices[backIndex] = vertCount;
            shapeIndices[backIndex+1] = i+vertCount+1;
            shapeIndices[backIndex+2] = i+vertCount+2;
        }  
        int a = 0, b = vertCount, c = vertCount+1, d = 1;
        for(int i = 0; i < vertCount; i++){
            int s = INITIAL_MESH_STRIDE;
            System.arraycopy(blueprint, a*s, shapeVertices, verticeIndex,       s);
            System.arraycopy(blueprint, b*s, shapeVertices, verticeIndex + s,   s);
            System.arraycopy(blueprint, c*s, shapeVertices, verticeIndex + 2*s, s);
            System.arraycopy(blueprint, d*s, shapeVertices, verticeIndex + 3*s, s);

            shapeIndices[indiceIndex] = currVertex;
            shapeIndices[indiceIndex+1] = currVertex+2;
            shapeIndices[indiceIndex+2] = currVertex+1;
            shapeIndices[indiceIndex+3] = currVertex;
            shapeIndices[indiceIndex+4] = currVertex+3;
            shapeIndices[indiceIndex+5] = currVertex+2;
            a++;
            b++;
            c = (c < doubleVerticeCount-1) ? c + 1 : vertCount;
            d = (d < vertCount-1) ? d + 1 : 0;
            indiceIndex += 6;
            verticeIndex += 4*s;
            currVertex += 4;
        }
        return new Entity(shapeVertices, shapeIndices, Matrix.identity());
    }
    void generateGrid(int rows, int columns, int depth){
        double rowIterator = (double) 1 / rows;
        double colIterator = (double) 1 / columns;
        double depthIterator = (double) 1 / depth;
        int rowVerticeMemory = rows*10*4*INITIAL_MESH_STRIDE;
        int colVerticeMemory = columns*10*4*INITIAL_MESH_STRIDE;
        int depthVerticeMemory = depth*10*4*INITIAL_MESH_STRIDE; 
        int rowIndiceMemory = rows*10*6;
        int colIndiceMemory = columns*10*6;
        int depthIndiceMemory = depth*10*6;
        double[] gridVertices = new double[rowVerticeMemory+colVerticeMemory+depthVerticeMemory];
        int[] gridIndices = new int[rowIndiceMemory+colIndiceMemory+ depthIndiceMemory];
        int vertIdx = 0;
        int indiceIdx = 0;
        double xCurr = -0.5;
        double yCurr = -0.5;
        double zCurr = -0.5;
        double thickness = 0.001;
        for(int i = 0; i < depth; i++){
            for(int j = 0; j < rows; j++){
                int baseVert = vertIdx / INITIAL_MESH_STRIDE;

                gridVertices[vertIdx++] = -0.5;
                gridVertices[vertIdx++] = yCurr;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = -0.5;
                gridVertices[vertIdx++] = yCurr + thickness;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = 0.5;
                gridVertices[vertIdx++] = yCurr + thickness;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = 0.5;
                gridVertices[vertIdx++] = yCurr;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;


                gridIndices[indiceIdx++] = baseVert;
                gridIndices[indiceIdx++] = baseVert+1;
                gridIndices[indiceIdx++] = baseVert+2;

                gridIndices[indiceIdx++] = baseVert;
                gridIndices[indiceIdx++] = baseVert+2;
                gridIndices[indiceIdx++] = baseVert+3;

                yCurr+= rowIterator;
            }
            zCurr += depthIterator;
            yCurr = -0.5;
        }
        zCurr = -0.5;
        for(int i = 0; i < depth; i++){
            for(int j = 0; j < columns; j++){
                int baseVert = vertIdx / INITIAL_MESH_STRIDE;

                gridVertices[vertIdx++] = xCurr;
                gridVertices[vertIdx++] = -0.5;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = xCurr;
                gridVertices[vertIdx++] = 0.5;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = xCurr+thickness;
                gridVertices[vertIdx++] = 0.5;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = xCurr+thickness;
                gridVertices[vertIdx++] = -0.5;
                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;


                gridIndices[indiceIdx++] = baseVert;
                gridIndices[indiceIdx++] = baseVert+1;
                gridIndices[indiceIdx++] = baseVert+2;

                gridIndices[indiceIdx++] = baseVert;
                gridIndices[indiceIdx++] = baseVert+2;
                gridIndices[indiceIdx++] = baseVert+3;

                xCurr+= colIterator;
            }
            zCurr += depthIterator;
            xCurr = -0.5;
        }
        yCurr = -0.5;
        zCurr = -0.5;
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < depth; j++){
                int baseVert = vertIdx / INITIAL_MESH_STRIDE;

                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = yCurr;
                gridVertices[vertIdx++] = -0.5;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = zCurr+thickness;
                gridVertices[vertIdx++] = yCurr;
                gridVertices[vertIdx++] = -0.5;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = zCurr+thickness;
                gridVertices[vertIdx++] = yCurr;
                gridVertices[vertIdx++] = 0.5;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;

                gridVertices[vertIdx++] = zCurr;
                gridVertices[vertIdx++] = yCurr;
                gridVertices[vertIdx++] = 0.5;
                gridVertices[vertIdx++] = 1;
                gridVertices[vertIdx++] = 0;
                gridVertices[vertIdx++] = 0;


                gridIndices[indiceIdx++] = baseVert;
                gridIndices[indiceIdx++] = baseVert+1;
                gridIndices[indiceIdx++] = baseVert+2;

                gridIndices[indiceIdx++] = baseVert;
                gridIndices[indiceIdx++] = baseVert+2;
                gridIndices[indiceIdx++] = baseVert+3;

                zCurr+= depthIterator;
            }
            yCurr += rowIterator;
            zCurr = -0.5;
        }
        gridVertices = MeshGenerator.bakeNormalVectors(gridVertices, gridIndices);
        initializeArrays(gridVertices, gridIndices);
        initializeVariables();
        transformation = Matrix.identity();
    }
    // Adds given entity to this entity's list of children
    void addChild(Entity child){
        children.put(childrenCount, child);
        child.ID = childrenCount;
        child.parent = this;
        childrenCount++;
    }
    // Sets green screen boolean to given value
    void setGreenScreen(boolean b){
        greenScreen = b;
    }
    // Sets transparent boolean to given value
    void setTransparent(boolean b){
        transparent = b;
    }
    // Sets alpha value to given value
    void setAlpha(double v){
        alpha = v;
    }
    // Sets x, y, and z world positions to given values
    Entity setWorldPosition(double givenx, double giveny, double givenz){
        x = givenx;
        y = giveny;
        z = givenz;
        return this;
    }
    // Reverts transformation to identity matrix
    Entity resetTransformation(){
        transformation = Matrix.Identity();
        return this;
    }
    // Moves entity linearly by given values in world space
    Entity translate(double givenx, double giveny, double givenz){
        this.x += givenx;
        this.y += giveny;
        this.z += givenz;
        return this;
    }
    // Scales entity size by given values in world space
    Entity scale(double givenx, double giveny, double givenz){
        this.xSize += givenx;
        this.ySize += giveny;
        this.zSize += givenz;
        return this;
    }
    // Rotates entity around world x-axis
    Entity rotatexWorld(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(1, 0, 0, degrees);
        orientation = newOrientation.multiply(orientation).normalize();
        return this;
    }
    // Rotates entity around world y-axis
    Entity rotateyWorld(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 1, 0, degrees);
        orientation = newOrientation.multiply(orientation).normalize();
        return this;
    }
    // Rotates entity around world z-axis
    Entity rotatezWorld(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 0, 1, degrees);
        orientation = newOrientation.multiply(orientation).normalize();
        return this;
    }
    // Rotates entity around local x-axis
    Entity rotatexLocal(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(1, 0, 0, degrees);
        orientation = orientation.multiply(newOrientation).normalize();
        return this;
    }
    // Rotates entity around local y-axis
    Entity rotateyLocal(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 1, 0, degrees);
        orientation = orientation.multiply(newOrientation).normalize();
        return this;
    }
    // Rotates entity around local z-axis
    Entity rotatezLocal(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 0, 1, degrees);
        orientation = orientation.multiply(newOrientation).normalize();
        return this;
    }
    // Outdated rotation around x-axis
    Entity rotatex(double degree){
        pitch += degree;
        return this;
    }
    // Outdated rotation around y-axis
    Entity rotatey(double degree){
        yaw += degree;
        return this;
    }
    // Outdated rotation around z-axis
    Entity rotatez(double degree){
        roll += degree;
        return this;
    }
    void resetFrameData(){
        // System.out.println("viewspace: " + viewSpaceVectors.get(0)[0]);
        resetTransformation();
    }
}
