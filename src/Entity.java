import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Entity {
    //keep
    double[] vertices;
    int[] indices;
    double [] uvMap;
    double [][][] textureMapping; 
    BufferedImage texture;
    Matrix oldTransformation;
    Matrix transformation;
    double x, y, z;
    double yaw, pitch, roll;
    double xSize, ySize, zSize;
    int globalVerticeOffset;
    int globalIndiceOffset;
    int defaultColor = 0xFFFFFF;

    //remove
    double[][] objectSpaceVectors;
    int [][] objectSpaceindices;
    double[][] worldSpaceVectors;
    ArrayList<double[]> viewSpaceVectors;
    ArrayList<double[]> finalVectors;
    ArrayList<int[]> finalIndices;
    ArrayList<double[][]> finalTextureMapping;

    int ID;

    static final int STRIDE = 6;


    Entity(){

    }
    Entity(double [][] vertices, int [][] ind, Matrix m, Matrix d){
        initializeVectorSpaces(vertices);

        initializeVariables();
        initializeTextureMap();

        this.objectSpaceindices = ind;
        this.oldTransformation = m;
        this.transformation = d;
        initializeLists();
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
        oldTransformation = Matrix.translate(x,y,z)
        .multiply2dMatrix(Matrix.rotatex(yaw)
        .multiply2dMatrix(Matrix.rotatey(pitch))
        .multiply2dMatrix(Matrix.rotatez(roll))
        .multiply2dMatrix(Matrix.scale(xSize,ySize,zSize)));
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
    void convertToWorldSpace(){
        for(int k = 0; k < objectSpaceVectors.length; k++){
            for(int i = 0; i < oldTransformation.m.length; i++){
                worldSpaceVectors[k][i] = 0;
                for(int j = 0; j < oldTransformation.m[i].length; j++){
                    worldSpaceVectors[k][i] += oldTransformation.m[i][j] * objectSpaceVectors[k][j];
                } 
            }
        }
    }
    void setTexture(String source){
        try{
            texture = ImageIO.read(new File(source));
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    void initializeArrays(double [] givenVertices, int [] givenIndices){
        vertices = givenVertices;
        indices = givenIndices;
        int currentIndex = 0;
        // for(int i = 0; i < givenVertices.length; i+=STRIDE){
        //     vertices[currentIndex] = givenVertices[i];
        //     vertices[currentIndex+1] = givenVertices[i+1];
        //     vertices[currentIndex+2] = givenVertices[i+2];
        //     vertices[currentIndex+3] = givenVertices[i+3];
        //     vertices[currentIndex+4] = givenVertices[i+4];
        //     vertices[currentIndex+5] = givenVertices[i+5];
        //     currentIndex += STRIDE;
        // }
    }
    void initializeVectorSpaces(double [][] vertices){
        int rowLength = vertices.length;
        int colLength = vertices[0].length+1;

        objectSpaceVectors = new double[rowLength][colLength];
        worldSpaceVectors = new double[rowLength][colLength];
        
        for(int i = 0; i < rowLength; i++){
            for(int j = 0; j < vertices[i].length; j++){
                objectSpaceVectors[i][j] = vertices[i][j];
            }
            objectSpaceVectors[i][3] = 1;
        }
    }
    void initializeTextureMap(){
        textureMapping = new double[objectSpaceindices.length][3][2];
    }
    void initializeLists(){
        viewSpaceVectors = new ArrayList<>();
        finalVectors = new ArrayList<>();
        finalIndices = new ArrayList<>();
        finalTextureMapping = new ArrayList<>();
    }
    void cubeMesh(){
        double [] cubeVertices = {
            // 0.5,0.5,0.5,1, 1, 0,//0
            // -0.5,0.5,0.5,1, 0, 0,//1
            // 0.5,0.5,-0.5,1, 1, 0,//2
            // -0.5,0.5,-0.5,1, 0, 0,//3
            // 0.5,-0.5,0.5,1, 1, 1,//4
            // 0.5, -0.5, -0.5,1, 1, 1,//5
            // -0.5,-0.5,0.5,1, 0, 0,//6
            // -0.5,-0.5,-0.5,1, 0, 1,//7

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

            // // top
            // 1,0,2,
            // 1,2,3,
            // // //front
            // 3,2,5,
            // 3,5,7,
            // // //right
            // 2,0,4,
            // 2,4,5,
            // // // left
            // 6, 1, 7,
            // 7,1,3,
            // // //back
            // 4,0,1,
            // 4,1,6,
            // // //bottom
            // 4,6,5, 
            // 7,5,6 
        };
        double [][] vertices = {
            {0.5,0.5,0.5}, //0
            {-0.5,0.5,0.5}, //1
            {0.5,0.5,-0.5}, //2
            {-0.5,0.5,-0.5}, //3
            {0.5,-0.5,0.5}, //4
            {0.5, -0.5, -0.5}, //5
            {-0.5,-0.5,0.5}, //6
            {-0.5,-0.5,-0.5} //7
        };
        int [][] indices = {
            // top
            {1,0,2},
            {1,2,3},
            //front
            {3,2,5},
            {3,5,7},
            //right
            {2,0,4},
            {2,4,5},
            // left
            {6, 1, 7},
            {7,1,3},
            //back
            {4,0,1},
            {4,1,6},
            //bottom
            {4,6,5}, 
            {7,5,6} 
        };
        initializeArrays(cubeVertices, cubeIndices);

        initializeVectorSpaces(vertices);
        initializeVariables();
        this.objectSpaceindices = indices;
        initializeTextureMap();
        applyTexture(0, new double[][]{{0, 0},{1, 0},{1, 1}}); //top
        applyTexture(1, new double[][]{{0, 0},{1, 1},{0, 1}});
        
        applyTexture(2, new double[][]{{0, 0},{1, 0},{1, 1}}); //front
        applyTexture(3, new double[][]{{0, 0},{1, 1},{0, 1}});
        
        applyTexture(4, new double[][]{{0, 0},{1, 0},{1, 1}}); //right
        applyTexture(5, new double[][]{{0, 0},{1, 1},{0, 1}});
        
        applyTexture(6, new double[][]{{0,1}, {0,0}, {1,1}}); //left
        applyTexture(7, new double[][]{{1,1}, {0,0}, {1,0}});
        
        applyTexture(8, new double[][]{{0,1}, {0,0}, {1,0}}); //back
        applyTexture(9, new double[][]{{0,1}, {1,0}, {1,1}});
        
        applyTexture(10, new double[][]{{1,0}, {0,0}, {0,1}}); //back
        applyTexture(11, new double[][]{{0,1}, {1,1}, {0,0}});
        initializeLists();
        oldTransformation = Matrix.identity();
        transformation = Matrix.Identity();
    }
    void applyTexture(int triangle, double[][] textureCords){
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 2; j++){
                textureMapping[triangle][i][j] = textureCords[i][j];
            }
        }
    }
    Entity setWorldPosition(double givenx, double giveny, double givenz){
        x = givenx;
        y = giveny;
        z = givenz;
        return this;
    }
    Entity resetTransformation(){
        oldTransformation = Matrix.identity();
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
    void sortVertices(){
        for(int i = 0; i < finalIndices.size(); i++){
            for(int j = 0; j < finalIndices.get(i).length; j++){
                for(int k = j+1; k < finalIndices.get(i).length; k++){
                    int jPosition = finalIndices.get(i)[j];
                    int kPosition = finalIndices.get(i)[k];
                    if(finalVectors.get(jPosition)[1] > finalVectors.get(kPosition)[1]){
                        swapVertices(i, j, k);
                        swapUV(i, j, k);
                    } else if(finalVectors.get(jPosition)[1] == finalVectors.get(kPosition)[1]){
                        if(finalVectors.get(jPosition)[0] < finalVectors.get(kPosition)[0]) {
                           swapVertices(i, j, k);
                           swapUV(i, j, k);
                        }
                    }
                }
            }
        }
        
    }
    void swapVertices(int row, int a, int b){
        int temp = finalIndices.get(row)[a];
        finalIndices.get(row)[a] = finalIndices.get(row)[b];
        finalIndices.get(row)[b] = temp;
    }
    void swapUV(int row, int a, int b){
        double[] temp = finalTextureMapping.get(row)[a];
        finalTextureMapping.get(row)[a] = finalTextureMapping.get(row)[b];
        finalTextureMapping.get(row)[b] = temp;
    }
    void invertObject(){
        for(int i = 0; i < objectSpaceVectors.length; i++){
            objectSpaceVectors[i][1] *= -1;
        }
    }
    void resetFrameData(){
        // System.out.println("viewspace: " + viewSpaceVectors.get(0)[0]);
        viewSpaceVectors.clear();
        finalVectors.clear();
        finalIndices.clear();
        finalTextureMapping.clear();
        resetTransformation();
    }
}
