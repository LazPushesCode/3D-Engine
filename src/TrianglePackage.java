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
}
