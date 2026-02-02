import java.util.ArrayList;
public class Entity {
    double[][] objectSpaceVectors;
    int [][] indices;

    double[][] worldSpaceVectors;

    ArrayList<double[]> viewSpaceVectors;
    ArrayList<double[]> finalVectors;
    ArrayList<int[]> finalIndices;


    Matrix transformation;

    Entity(){

    }
    Entity(double [][] vertices, int [][] ind, Matrix m){
        initializeVectorSpaces(vertices);
        this.indices = ind;
        this.transformation = m;
        initializeLists();
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
    void initializeVectorSpaces(double [][] vertices){
        objectSpaceVectors = new double[vertices.length][vertices[0].length+1];
        worldSpaceVectors = new double[vertices.length][vertices[0].length+1];
        for(int i = 0; i < vertices.length; i++){
            for(int j = 0; j < vertices[i].length; j++){
                objectSpaceVectors[i][j] = vertices[i][j];
            }
            objectSpaceVectors[i][3] = 1;
        }
    }
    void initializeLists(){
        viewSpaceVectors = new ArrayList<>();
        finalVectors = new ArrayList<>();
        finalIndices = new ArrayList<>();
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
        this.indices = cubeIndices;
        initializeLists();
        transformation = Matrix.Identity();
    }
    Entity setWorldPosition(double x, double y, double z){
        transformation = Matrix.translate(x, y, z);
        return this;
    }
    Entity resetTransformation(){
        transformation = Matrix.Identity();
        return this;
    }
    Entity translate(double x, double y, double z){
        transformation = transformation.multiply(Matrix.translate(x, y, z));
        return this;
    }
    Entity scale(double x, double y, double z){
        transformation = transformation.multiply(Matrix.scale(x, y, z));
        return this;
    }
    Entity rotatex(double degree){
        transformation = transformation.multiply(Matrix.rotatex(degree));
        return this;
    }
    Entity rotatey(double degree){
        transformation = transformation.multiply(Matrix.rotatey(degree));
        return this;
    }
    Entity rotatez(double degree){
        transformation = transformation.multiply(Matrix.rotatez(degree));
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
                    } else if(finalVectors.get(jPosition)[1] == finalVectors.get(kPosition)[1]){
                        if(finalVectors.get(jPosition)[0] < finalVectors.get(kPosition)[0]) {
                           swapVertices(i, j, k);
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
    void invertObject(){
        for(int i = 0; i < objectSpaceVectors.length; i++){
            objectSpaceVectors[i][1] *= -1;
        }
    }
    void resetFrameData(){
        viewSpaceVectors.clear();
        finalVectors.clear();
        finalIndices.clear();
    }
}
