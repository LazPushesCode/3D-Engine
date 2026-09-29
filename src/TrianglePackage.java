import java.util.ArrayList;
public class TrianglePackage {
    ArrayList<Integer> vertices;
    ArrayList<double[]> uvs;

    TrianglePackage(){
        vertices = new ArrayList<>();
        uvs = new ArrayList<>();
    }
    void insertVertice(int givenV){
        vertices.add(givenV);
    }   
    void insertUV(double[] givenUV){
        uvs.add(givenUV);
    }
    void displayPackageInformation(){
        System.out.println("====================");
        for(int i = 0; i < vertices.size(); i++){
            System.out.println("v: " + vertices.get(i) + " uv: " + uvs.get(i)[0] + ", " + uvs.get(i)[1]);
        }
    }
}
