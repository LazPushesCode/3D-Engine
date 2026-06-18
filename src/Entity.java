import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Entity {
    //keep
    double[] vertices;
    int[] indices;

    int[] textureBuffer;
    int textureHeight;
    int textureWidth;
    boolean hasTexture;

    BufferedImage texture;
    Matrix transformation;
    double x, y, z;
    double yaw, pitch, roll;
    double xSize, ySize, zSize;
    int globalVerticeOffset;
    int globalIndiceOffset;
    int defaultColor = 0xFFFFFF;

    //remove

    int ID;

    static final int INITIAL_MESH_STRIDE = 6;
    static final int LOCAL_MESH_STRIDE = 9;


    Entity(){

    }
    Entity(double [] vertices, int [] ind, Matrix m, Matrix d){
        initializeVariables();
        this.transformation = d;
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
    }
    void applyTransformationValues(){
        transformation = Matrix.Translate(x,y,z)
        .multiply(Matrix.Rotatex(yaw)
        .multiply(Matrix.Rotatey(pitch))
        .multiply(Matrix.Rotatez(roll))
        .multiply(Matrix.Scale(xSize,ySize,zSize)));
    }
    void applyTexture(Texture texture){
        textureHeight = texture.height;
        textureWidth = texture.width;
        // textureBuffer = new int[textureHeight * textureWidth];
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
        System.out.println("before: " + Arrays.toString(cubeVertices));
        cubeVertices = bakeNormalVectors(cubeVertices, cubeIndices);
         System.out.println("after: " + Arrays.toString(cubeVertices));
        initializeArrays(cubeVertices, cubeIndices);
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
    void resetFrameData(){
        // System.out.println("viewspace: " + viewSpaceVectors.get(0)[0]);
        resetTransformation();
    }
}
