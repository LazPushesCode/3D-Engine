import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

public class Entity {
    //keep
    double[] vertices;
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
    int globalVerticeOffset;
    int globalIndiceOffset;
    int defaultColor = 0xFFFFFF;

    Entity parent;
    HashMap<Integer, Entity> children;
    HashMap<Integer, int[]> skeleton;

    int ID;
    int childrenCount;

    static final int INITIAL_MESH_STRIDE = 6;
    static final int LOCAL_MESH_STRIDE = 9;


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
        childrenCount = 0;
        children = new HashMap<>();
    }
    void applyTransformationValues(){

        Matrix T = Matrix.Translate(x, y, z);
        Matrix R = Matrix.Rotate(orientation);
        Matrix S = Matrix.Scale(xSize, ySize, zSize);
        transformation = T.multiply(R.multiply(S));

        // transformation = Matrix.Translate(x, y, z)
        // .multiply(Matrix.Rotate(orientation)
        // .multiply(Matrix.Scale(xSize, ySize, zSize)));

        if(parent != null){
            transformation = parent.transformation.multiply(transformation);
        }
    }
    void applyTexture(Texture texture){
        textureHeight = texture.height;
        textureWidth = texture.width;
        textureBuffer = texture.buffer;
        hasTexture = true;
    }
    void applyMP4(VideoDecoder vd){
        textureHeight = vd.getHeight();
        textureWidth = vd.getWidth();
        textureBuffer = vd.getFrameBuffer();
        hasTexture = true;
    }
    void applyNewMP4Frame(int[] buffer){
        textureBuffer = buffer;
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
        vertices = givenVertices;
        indices = givenIndices;
    }
    void cubeMesh(){
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
            -0.5,-0.5,0.5,1, 0, 0,//22
            -0.5,-0.5,-0.5,1, 0, 1,//23
            
        };
        int [] cubeIndices = {
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
        cubeVertices = bakeNormalVectors(cubeVertices, cubeIndices);
        initializeArrays(cubeVertices, cubeIndices);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
    }
    void sphereMesh(int m, int n){
        int offset = 6;
        double[] sphereVertices = new double[(((m+1) * (n+1))* offset)];
        int verticesIndex = 0;
        for(int i = 0; i <= m; i++){
            double fy = ((double)i / (double)m);
            for(int j = 0; j <= n; j++){
                double fx = ((double)j / (double)n);
                double pihalf = -((double)Math.PI/2);
                double longtitude = -((double)(Math.PI/2)) + fy * Math.PI;
                double latitude = fx * 2 * Math.PI;
                double x = Math.cos(longtitude)  * Math.cos(latitude);
                double y = Math.sin(longtitude);
                double z = Math.cos(longtitude) * Math.sin(latitude);
                sphereVertices[verticesIndex] = x;
                sphereVertices[verticesIndex+1] = y;
                sphereVertices[verticesIndex+2] = z;
                sphereVertices[verticesIndex+3] = 1;
                sphereVertices[verticesIndex+4] = fx;
                sphereVertices[verticesIndex+5] = fy;
                verticesIndex+=offset;
            }
        }
        int[] sphereIndices = new int[m * n * 2 * 3];
        int indicesIndex = 0;
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
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

                indicesIndex += 6;
            }
        }
        sphereVertices = bakeNormalVectors(sphereVertices, sphereIndices);
        initializeArrays(sphereVertices, sphereIndices);
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

            double magnitude = Math.sqrt((nx*nx) + (ny*ny) + (nz*nz));

            nx /= magnitude;
            ny /= magnitude;
            nz /= magnitude;

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
        System.out.println(Arrays.toString(cubeVertices));
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
            System.out.println("f: " + frontIndex + " b: " + backIndex);
            shapeIndices[frontIndex] = 0;
            shapeIndices[frontIndex+1] = i + 1;
            shapeIndices[frontIndex+2] = i + 2;
            shapeIndices[backIndex] = vertCount;
            shapeIndices[backIndex+1] = i+vertCount+2;
            shapeIndices[backIndex+2] = i+vertCount+1;
        }  
        System.out.println("indices; " + Arrays.toString(shapeIndices) + " filled: " + totalFrontAndBackTriangles);
        int a = 0, b = vertCount, c = vertCount+1, d = 1;
        System.out.println("blueprint: " + Arrays.toString(blueprint));
        for(int i = 0; i < vertCount; i++){
            System.out.println("a: " + a + " b: " +b + " c: " + c + " d: " + d);
            System.arraycopy(blueprint, a*INITIAL_MESH_STRIDE, shapeVertices, verticeIndex, INITIAL_MESH_STRIDE);
            System.arraycopy(blueprint, b*INITIAL_MESH_STRIDE, shapeVertices, verticeIndex+6, INITIAL_MESH_STRIDE);
            System.arraycopy(blueprint, c*INITIAL_MESH_STRIDE, shapeVertices, verticeIndex+12, INITIAL_MESH_STRIDE);
            System.arraycopy(blueprint, d*INITIAL_MESH_STRIDE, shapeVertices, verticeIndex+18, INITIAL_MESH_STRIDE);
            
            shapeIndices[indiceIndex] = currVertex;
            shapeIndices[indiceIndex+1] = currVertex+1;
            shapeIndices[indiceIndex+2] = currVertex+2;
            shapeIndices[indiceIndex+3] = currVertex;
            shapeIndices[indiceIndex+4] = currVertex+2;
            shapeIndices[indiceIndex+5] = currVertex+3;
            a++;
            b++;
            c = (c < doubleVerticeCount-1) ? c + 1 : vertCount;
            d = (d < vertCount-1) ? d + 1 : 0;
            indiceIndex += 6;
            verticeIndex += 24;
            currVertex += 4;
        }
        System.out.println("indiceIndex: " +indiceIndex + " indices: " + Arrays.toString(shapeIndices));
        System.out.println("vertices: " + Arrays.toString(shapeVertices));
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
