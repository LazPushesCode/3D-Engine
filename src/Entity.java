import java.awt.image.BufferedImage;

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

    static final int STRIDE = 6;


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
    double[] convertVectorsToWorldSpace(){
        double [] worldVectors = new double[vertices.length];
        for(int v = 0; v < vertices.length; v+=STRIDE){
            for(int i = 0; i < 4; i++){
                worldVectors[v + i] = 0;
                for(int j = 0; j < 4; j++){
                    worldVectors[v + i] += transformation.data[(i*4) + j] * vertices[v + j];
                }
            }
            worldVectors[v+4] = vertices[v+4];
            worldVectors[v+5] = vertices[v+5]; 
        }
        return worldVectors;
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
        initializeArrays(cubeVertices, cubeIndices);
        initializeVariables();
        transformation = Matrix.Identity();
        hasTexture = false;
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
