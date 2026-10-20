import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

public class Entity {
    //keep
    double[] worldVertices;
    double[] staticVertices;
    int[] indices;

    int[] textureBuffer;
    int textureHeight;
    int textureWidth;
    boolean hasTexture;

    double materialDiffuse;
    double materialSpecular;
    double materialShininess;

    boolean greenScreen;

    BufferedImage texture;
    Matrix transformation;
    double x, y, z;
    double yaw, pitch, roll;
    double xSize, ySize, zSize;
    Quaternion orientation;
    int verticeCount;
    int globalVerticeOffset;
    int globalIndiceOffset;
    int defaultColor = 0xFFFFFF;

    Entity parent;
    HashMap<Integer, Entity> children;

    int ID;
    int childrenCount;


    //Animation data
    int animationID;
    int indexID;
    long prevTime;
    int currentKeyFrame;

    static final int TRACK_STRIDE = 4;
    static final int TRACK_ANIMATIONID = 0;
    static final int TRACK_CURRENT_FRAME = 1;
    static final int TRACK_GROUP_OFFSET= 2;
    static final int TRACK_VERTICE_COUNT = 3;

    int[] trackData; // holds animation id, current frame, group offset (start) and vertice count (end)
    double[] trackPrevTimes; 
    int[] trackIndices;

    int currentTrackIndiceSize;
    int maxTrackIndiceSize;
    int trackCount;
    

    static final int INITIAL_MESH_STRIDE = 6;
    static final int LOCAL_MESH_STRIDE = 9;

    boolean backFaceCulling;
    boolean prioritize;
    boolean affectedByLight;
    


    Entity(){
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
        parent = null;
    }
    Entity(double [] vertices, int [] ind, Matrix d){
        vertices = bakeNormalVectors(vertices, ind);
        initializeArrays(vertices, ind);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
    }
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
        greenScreen = false;
        backFaceCulling = true;
        prioritize = false;
        affectedByLight = true;
        childrenCount = 0;
        verticeCount = 0;
        currentTrackIndiceSize = 0;
        trackCount = 0;

        prevTime = 0;
        currentKeyFrame = 0;

        children = new HashMap<>();
    }
    void setMaterialValues(double diff, double spec, double shiny){
        materialDiffuse = diff;
        materialSpecular = spec;
        materialShininess = shiny;
    }
    void updateTransformations(Matrix parentMatrix){
        Matrix local = Matrix.Translate(x,y,z)
        .multiply(Matrix.Rotate(orientation))
        .multiply(Matrix.Scale(xSize, ySize, zSize));
        this.transformation = (parentMatrix != null) ? parentMatrix.multiply(local) : local;
        for(Entity child : children.values()){
            child.updateTransformations(this.transformation);
        }
    }
    void applyTexture(Texture texture){
        textureHeight = texture.height;
        textureWidth = texture.width;
        textureBuffer = texture.buffer;
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
    void initializeArrays(double [] givenVertices, int [] givenIndices){
        int vc = givenVertices.length/9;
        staticVertices = new double[givenVertices.length];
        System.arraycopy(givenVertices, 0, staticVertices, 0, givenVertices.length);
        // staticVertices = givenVertices;
        worldVertices = givenVertices;
        indices = givenIndices;
        trackData = new int[TRACK_STRIDE*vc];
        trackPrevTimes = new double[vc];
        trackIndices = new int[vc];
        maxTrackIndiceSize = vc;
    }
    void addTrackData(int animationID){
        // trackData[activeTrackCount] = animationID;
        // trackData[TRACK_CURRENT_FRAME] = 0;
        // trackData[TRACK_GROUP_OFFSET] = 0;
    }
    void addTrackIndices(int[] givenIndices){
        if(currentTrackIndiceSize + givenIndices.length > maxTrackIndiceSize){
            // increase array size
            maxTrackIndiceSize *= 2;
            int[] newTrackIndices = new  int[maxTrackIndiceSize];
            System.arraycopy(trackIndices, 0, newTrackIndices, 0, currentTrackIndiceSize);
            trackIndices = newTrackIndices;
        }
        System.arraycopy(givenIndices, 0, trackIndices, currentTrackIndiceSize, givenIndices.length);
        int index = trackCount * TRACK_STRIDE;
        trackData[index] = 0;
        trackData[index+TRACK_CURRENT_FRAME] = 0;
        trackData[index+TRACK_GROUP_OFFSET] = currentTrackIndiceSize;
        trackData[index+TRACK_VERTICE_COUNT] = givenIndices.length;
        currentTrackIndiceSize += givenIndices.length;
        trackCount++;
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
        // System.out.println("verticeS: " + Arrays.toString(gridVertices));
        // System.out.println("indices: " + Arrays.toString(gridIndices));
        gridVertices = bakeNormalVectors(gridVertices, gridIndices);
        initializeArrays(gridVertices, gridIndices);
        initializeVariables();
        transformation = Matrix.identity();
        hasTexture = false;
    }
    void cubeMesh(boolean inverted){
        double [] cubeVertices = {
            //top
            0.5,0.5,0.5,1, 1, 0,//0
            -0.5,0.5,0.5,1, 0, 0,//1
            0.5,0.5,-0.5,1, 1, 1,//2
            -0.5,0.5,-0.5,1, 0, 1,//3

            //front
            0.5,0.5,-0.5,1, 1, 0,//4
            -0.5,0.5,-0.5,1, 0, 0,//5
            0.5, -0.5, -0.5,1, 1, 1,//6
            -0.5,-0.5,-0.5,1, 0, 1,//7

            //right
            0.5,0.5,0.5,1, 1, 0,//8
            0.5,0.5,-0.5,1, 0, 0,//9
            0.5,-0.5,0.5,1, 1, 1,//10
            0.5,-0.5,-0.5,1, 0, 1,//11

            //left
            -0.5,0.5,0.5,1, 0, 0,//12
            -0.5,0.5,-0.5,1, 1, 0,//13
            -0.5,-0.5,0.5,1, 0, 1,//14
            -0.5,-0.5,-0.5,1, 1, 1,//15

            //back
            0.5,0.5,0.5,1, 1, 0,//16
            -0.5,0.5,0.5,1, 0, 0,//17
            0.5,-0.5,0.5,1, 1, 1,//18
            -0.5,-0.5,0.5,1, 0, 1,//19

            //bottom
            0.5,-0.5,0.5,1, 1, 0,//20
            0.5, -0.5, -0.5,1, 1, 1,//21
            -0.5,-0.5, 0.5,1, 0, 0,//22
            -0.5,-0.5,-0.5,1, 0, 1,//23
            
        };
        // int [] cubeIndices = {
        //     //top
        //     0, 2, 1,
        //     2, 3, 1,

        //     //front
        //     5, 4, 6,
        //     5, 6, 7,

        //     //right
        //     9, 8, 10,
        //     9, 10, 11,

        //     //left
        //     12, 13, 15,
        //     12, 15, 14,

        //     //back
        //     16, 17, 19,
        //     16, 19, 18,

        //     //bottom
        //     20, 22, 23,
        //     20, 23, 21
        // };
        int[] cubeIndices = cubeIndiceDirection(inverted);
        cubeVertices = bakeNormalVectors(cubeVertices, cubeIndices);
        
        initializeArrays(cubeVertices, cubeIndices);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
    }
    int[] cubeIndiceDirection(boolean flag){
        if(!flag){
            return new int[] {
            //top
                0, 2, 1,
                2, 3, 1,

                //front
                5, 4, 6,
                5, 6, 7,

                //right
                9, 8, 10,
                9, 10, 11,

                //left
                12, 13, 15,
                12, 15, 14,

                //back
                16, 17, 19,
                16, 19, 18,

                //bottom
                20, 22, 23,
                20, 23, 21
            };
        } else {
            return new int[] {
            //top
                0, 1, 2,
                2, 1, 3,

                //front
                5, 6, 4,
                5, 7, 6,

                //right
                9, 10, 8,
                9, 11, 10,

                //left
                12, 15, 13,
                12, 14, 15,

                //back
                16, 19, 17,
                16, 18, 19,

                //bottom
                20, 23, 22,
                20, 21, 23
            };
        }
    }
    void squareMesh(){
        double[] squareVertices = {
            -0.5, -0.5, 0, 1, 1, 0,
            -0.5, 0.5, 0, 1, 0, 0,
            0.5, 0.5, 0, 1, 1, 0,
            0.5, -0.5, 0, 1, 0, 1
        };
        int[] squareIndices = {
            0, 1, 2,
            0, 2, 3
        };
        squareVertices = bakeNormalVectors(squareVertices, squareIndices);
        initializeArrays(squareVertices, squareIndices);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
    }
    void squareMeshWithBoundingBox(int uStart, int uEnd, int vStart, int vEnd){
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
        squareVertices = bakeNormalVectors(squareVertices, squareIndices);
        initializeArrays(squareVertices, squareIndices);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
    }
    void sphereMesh(int m, int n, double outerRadius, double innerRadius, int uStart, int uEnd, int vStart, int vEnd){
        int verticeCount = (m+1)*(n+1);
        double[] sphereVertices = new double[((verticeCount)*2* INITIAL_MESH_STRIDE)];
        int verticesIndex = 0;
        for(double r : new double[]{innerRadius, outerRadius}){
            for(int i = 0; i <= m; i++){
                double fy = ((double)i / (double)m);
                for(int j = 0; j <= n; j++){
                    double fx = ((double)j / (double)n);
                    double pihalf = -((double)Math.PI/2);
                    double longtitude = -((double)(Math.PI/2)) + fy * Math.PI;
                    double latitude = fx * 2 * Math.PI;
                    double x = r*Math.cos(longtitude)  * Math.cos(latitude);
                    double y = r*Math.sin(longtitude);
                    double z = r*Math.cos(longtitude) * Math.sin(latitude);
                    sphereVertices[verticesIndex] = x;
                    sphereVertices[verticesIndex+1] = y;
                    sphereVertices[verticesIndex+2] = z;
                    sphereVertices[verticesIndex+3] = 1;
                    // sphereVertices[verticesIndex+4] = fx;
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
                boolean insideU = (j >= uStart && j < uEnd);
                boolean insideV = (i >= vStart && i < vEnd);
                if(insideU && insideV){
                    continue;
                }
                int p0 = i * (n+1) + j;
                int p1 = (i+1) * (n+1) + j;
                int p2 = p1 + 1;
                int p3 = p0 + 1;

                sphereIndices[indicesIndex] = p0;
                sphereIndices[indicesIndex+1] = p3;
                sphereIndices[indicesIndex+2] = p1;

                sphereIndices[indicesIndex+3] = p1;
                sphereIndices[indicesIndex+4] = p3;
                sphereIndices[indicesIndex+5] = p2; 

                sphereIndices[indicesIndex+indiceAllocation] = p0+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+1] = p1+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+2] = p3+verticeCount;

                sphereIndices[indicesIndex+indiceAllocation+3] = p1+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+4] = p2+verticeCount;
                sphereIndices[indicesIndex+indiceAllocation+5] = p3+verticeCount; 

                indicesIndex += 6;
            }
        }
        sphereIndices = Arrays.copyOf(sphereIndices, indicesIndex*2);
        sphereVertices = bakeNormalVectors(sphereVertices, sphereIndices);
        initializeArrays(sphereVertices, sphereIndices);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
    }
    void generateFlatDynamicMesh(int row, int col){
        double xCurr = -0.5;
        double zCurr = -0.5;
        double height = 1 / (double)(row-1);
        double width = 1 / (double)(col-1);

        int indIdx = 0;
        double[] meshVertices = new double[(int)(row*col)*INITIAL_MESH_STRIDE];
        int[] meshIndices = new int[(int)((row-1)*(col-1))*6];
        for(int i = 0; i < row; i++){
            int rowOffset = ((i*col));
            for(int j = 0; j < col; j++){
                int index = (rowOffset+j)*INITIAL_MESH_STRIDE;
                meshVertices[index] = xCurr; meshVertices[index+1] = 0; meshVertices[index+2] = zCurr; meshVertices[index+3] = 1; meshVertices[index+4] = 0; meshVertices[index+5] = 0;
                zCurr += width;
            }
            xCurr += height;
            zCurr = -0.5;
        }
        int vertCount = row*col;
        int vertIdx = 0;
        for(int i = 0; i < row-1; i++){
            for(int j = 0; j < col-1; j++){
                meshIndices[indIdx++]= vertIdx; meshIndices[indIdx++] = vertIdx+1; meshIndices[indIdx++] = vertIdx+7;
                meshIndices[indIdx++]= vertIdx+7; meshIndices[indIdx++] = vertIdx+6; meshIndices[indIdx++] = vertIdx;
                vertIdx++;
            }
            vertIdx++;
        }
        meshVertices = bakeNormalVectors(meshVertices, meshIndices);
        initializeArrays(meshVertices, meshIndices);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
        
    }
    double[] bakeNormalVectors(double [] vertices, int[] indices){
        int indicesLength = indices.length;
        int verticesLength = vertices.length;
        int size = verticesLength + (verticesLength/6)*3;
        double[] newVertices = new double[size];

        int currentPosition = 0;
        
        for(int i = 0; i < indicesLength; i+=3){
            int p0 = indices[i]*6;
            int p1 = indices[i+1]*6;
            int p2 = indices[i+2]*6;

            int np0 = (p0/INITIAL_MESH_STRIDE)*LOCAL_MESH_STRIDE;
            int np1 = (p1/INITIAL_MESH_STRIDE)*LOCAL_MESH_STRIDE;
            int np2 = (p2/INITIAL_MESH_STRIDE)*LOCAL_MESH_STRIDE;

            double x0 = vertices[p0];
            double y0 = vertices[p0+1];
            double z0 = vertices[p0+2];
            double w0 = vertices[p0+3];
            double u0 = vertices[p0+4];
            double v0 = vertices[p0+5];

            double x1 = vertices[p1];
            double y1 = vertices[p1+1];
            double z1 = vertices[p1+2];
            double w1 = vertices[p1+3];
            double u1 = vertices[p1+4];
            double v1 = vertices[p1+5];

            double x2 = vertices[p2];
            double y2 = vertices[p2+1];
            double z2 = vertices[p2+2];
            double w2 = vertices[p2+3];
            double u2 = vertices[p2+4];
            double v2 = vertices[p2+5];

            double ax = x1-x0;
            double ay = y1-y0;
            double az = z1-z0;

            double bx = x2-x0;
            double by = y2-y0;
            double bz = z2-z0;

            double nx = (ay * bz) - (az * by);
            double ny = (az * bx) - (ax * bz);
            double nz = (ax * by) - (ay * bx);


            double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length > 1e-8) {
                nx /= length;
                ny /= length;
                nz /= length;
            } else {
                // Default fallback normal if triangle is degenerate
                nx = 0; ny = 0; nz = 1;
            }
            newVertices[np0] = x0;
            newVertices[np0+1] = y0;
            newVertices[np0+2] = z0;
            newVertices[np0+3] = w0;
            newVertices[np0+4] = u0;
            newVertices[np0+5] = v0;
            newVertices[np0+6] = nx;
            newVertices[np0+7] = ny;
            newVertices[np0+8] = nz;

            newVertices[np1] = x1;
            newVertices[np1+1] = y1;
            newVertices[np1+2] = z1;
            newVertices[np1+3] = w1;
            newVertices[np1+4] = u1;
            newVertices[np1+5] = v1;
            newVertices[np1+6] = nx;
            newVertices[np1+7] = ny;
            newVertices[np1+8] = nz;

            newVertices[np2] = x2;
            newVertices[np2+1] = y2;
            newVertices[np2+2] = z2;
            newVertices[np2+3] = w2;
            newVertices[np2+4] = u2;
            newVertices[np2+5] = v2;
            newVertices[np2+6] = nx;
            newVertices[np2+7] = ny;
            newVertices[np2+8] = nz;
        }
        return newVertices;
    }
    void triangleMesh(){
        double [] cubeVertices = {
            0.5,0.5,-0.5,1, 1, 0,//0
            -0.5,0.5,-0.5,1, 0, 0,//1
            0.5, -0.5, -0.5,1, 1, 1,//2
        };
        int [] cubeIndices = {
            //top
            0, 2, 1
        };
        initializeArrays(cubeVertices, cubeIndices);
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
    void addChild(Entity child){
        children.put(childrenCount, child);
        child.ID = childrenCount;
        child.parent = this;
        childrenCount++;
    }
    void setGreenScreen(boolean b){
        greenScreen = b;
    }
    Entity setWorldPosition(double givenx, double giveny, double givenz){
        x = givenx;
        y = giveny;
        z = givenz;
        return this;
    }
    Entity resetTransformation(){
        transformation = Matrix.Identity();
        return this;
    }
    Entity translate(double givenx, double giveny, double givenz){
        this.x += givenx;
        this.y += giveny;
        this.z += givenz;
        return this;
    }
    Entity scale(double givenx, double giveny, double givenz){
        this.xSize += givenx;
        this.ySize += giveny;
        this.zSize += givenz;
        return this;
    }
    Entity rotatex(double degree){
        pitch += degree;
        return this;
    }
    Entity rotatey(double degree){
        yaw += degree;
        return this;
    }
    Entity rotatez(double degree){
        roll += degree;
        return this;
    }
    Entity rotatexWorld(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(1, 0, 0, degrees);
        orientation = newOrientation.multiply(orientation).normalize();
        return this;
    }
    Entity rotateyWorld(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 1, 0, degrees);
        orientation = newOrientation.multiply(orientation).normalize();
        return this;
    }
    Entity rotatezWorld(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 0, 1, degrees);
        orientation = newOrientation.multiply(orientation).normalize();
        return this;
    }
    Entity rotatexLocal(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(1, 0, 0, degrees);
        orientation = orientation.multiply(newOrientation).normalize();
        return this;
    }
    Entity rotateyLocal(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 1, 0, degrees);
        orientation = orientation.multiply(newOrientation).normalize();
        return this;
    }
    Entity rotatezLocal(double degrees){
        Quaternion newOrientation = Quaternion.nextOrientation(0, 0, 1, degrees);
        orientation = orientation.multiply(newOrientation).normalize();
        return this;
    }
    void resetFrameData(){
        // System.out.println("viewspace: " + viewSpaceVectors.get(0)[0]);
        resetTransformation();
    }
}
