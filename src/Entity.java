import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Entity {
    double[][] objectSpaceVectors;
    int [][] indices;
    double [][] textureMappings;
    
    double [][][] textureMapping; 

    double[][] worldSpaceVectors;
    ArrayList<double[]> viewSpaceVectors;

    ArrayList<double[]> finalVectors;
    ArrayList<int[]> finalIndices;
    ArrayList<double[]> finalTextureMappings;

    ArrayList<double[][]> finalTextureMapping;

    BufferedImage texture;

    Matrix transformation;

    double x, y, z;
    double yaw, pitch, roll;
    double xSize, ySize, zSize;

    Entity(){

    }
    Entity(double [][] vertices, int [][] ind, Matrix m){
        initializeVectorSpaces(vertices);
        initializeVariables();
        initializeTextureMap();
        this.indices = ind;
        this.transformation = m;
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
    void convertToWorldSpace(){
        for(int k = 0; k < objectSpaceVectors.length; k++){
            for(int i = 0; i < transformation.m.length; i++){
                worldSpaceVectors[k][i] = 0;
                for(int j = 0; j < transformation.m[i].length; j++){
                    worldSpaceVectors[k][i] += transformation.m[i][j] * objectSpaceVectors[k][j];
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
    void initializeVectorSpaces(double [][] vertices){
        int rowLength = vertices.length;
        int colLength = vertices[0].length+1;

        objectSpaceVectors = new double[rowLength][colLength];
        worldSpaceVectors = new double[rowLength][colLength];
        textureMappings = new double[rowLength][2];
        
        for(int i = 0; i < rowLength; i++){
            textureMappings[i][0] = 0;
            textureMappings[i][1] = 0;
            for(int j = 0; j < vertices[i].length; j++){
                objectSpaceVectors[i][j] = vertices[i][j];
            }
            objectSpaceVectors[i][3] = 1;
        }
    }
    void initializeTextureMap(){
        textureMapping = new double[indices.length][3][2];
    }
    void initializeLists(){
        viewSpaceVectors = new ArrayList<>();
        finalVectors = new ArrayList<>();
        finalIndices = new ArrayList<>();
        finalTextureMappings = new ArrayList<>();
        finalTextureMapping = new ArrayList<>();
    }
    void cubeMesh(){
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
        int [][] cubeIndices = {
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
        initializeVectorSpaces(vertices);
        initializeVariables();
        this.indices = cubeIndices;
        initializeTextureMap();
        textureMappings = new double[][]{
            {1, 1}, //{0.5,0.5,0.5}
            {0.5, 1}, //{-0.5,0.5,0.5}
            {1, 0}, //{0.5,0.5,-0.5}
            {0, 0}, //{-0.5,0.5,-0.5}
            {.5, 1}, //{0.5,-0.5,0.5}
            {1, 1}, //{0.5, -0.5, -0.5}
            {0.5, 0.5}, //{-0.5,-0.5,0.5}
            {0, 1} //{-0.5,-0.5,-0.5}
        };
        for(int i = 0; i < 11; i+= 2){
            applyTexture(i, new double[][]{{0, 0},{1, 0},{1, 1}});
            applyTexture(i+1, new double[][]{{0, 0},{1, 1},{0, 1}});
        }
        initializeLists();
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
        transformation = Matrix.translate(x, y, z);
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
        transformation = transformation.multiply(Matrix.translate(x, y, z));
        return this;
    }
    Entity scale(double givenx, double giveny, double givenz){
        this.xSize += givenx;
        this.ySize += giveny;
        this.zSize += givenz;
        transformation = transformation.multiply(Matrix.scale(xSize, ySize, zSize));
        return this;
    }
    Entity rotatex(double degree){
        pitch += degree;
        transformation = transformation.multiply(Matrix.rotatex(pitch));
        return this;
    }
    Entity rotatey(double degree){
        yaw += degree;
        transformation = transformation.multiply(Matrix.rotatey(yaw));
        return this;
    }
    Entity rotatez(double degree){
        roll += degree;
        transformation = transformation.multiply(Matrix.rotatez(roll));
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
        finalTextureMappings.clear();
        resetTransformation();
    }
}
