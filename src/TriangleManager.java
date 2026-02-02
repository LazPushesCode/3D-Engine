import java.util.ArrayList;

public class TriangleManager {
    static void cullTriangles(Entity m, CameraManager c){
        ArrayList<int[]> validIndices = backFaceCulling(m);
        c.convertToClipSpace(m);
        finalizeTriangles(m, validIndices);
    }
    static ArrayList<int[]> backFaceCulling(Entity m){
        ArrayList<int[]> validIndices = new ArrayList<>();
        for(int i = 0; i < m.indices.length; i++){
            if(determineDirection(m.indices[i], m.viewSpaceVectors) <= 0) {
                continue;
            }
            validIndices.add(m.indices[i].clone());
        }
        return validIndices;
    }
    static void finalizeTriangles(Entity m, ArrayList<int[]> validIndices){
        
        ArrayList<int[]> clipTriList = new ArrayList<>();
        ArrayList<int[]> finalTriList = new ArrayList<>();
        for(int i = 0; i < validIndices.size(); i++){
            int outside = 0;
            for(int j = 0; j < validIndices.get(i).length; j++){
                int index = validIndices.get(i)[j];
                double wm = m.finalVectors.get(index)[3];
                double xm = m.finalVectors.get(index)[0];
                double ym = m.finalVectors.get(index)[1];
                double zm = m.finalVectors.get(index)[2];
                if(xm < (-wm) || xm > wm){
                    outside++;
                } else if(ym < (-wm) || ym > wm) {
                    outside++;
                } else if(zm < (-wm)) {
                    outside++;
                }
            }
            if(outside == 3){
                continue;
            }
            if(outside > 0){
                clipTriList.add(validIndices.get(i));
                continue;
            }
            finalTriList.add(validIndices.get(i));
        }
        for(int[] triangle : clipTriList){
            ArrayList<Integer> tri = new ArrayList<>();
            for(int i : triangle){
                tri.add(i);
            }
            tri = clipTriangle(tri, m);
            if(tri.size() < 3) continue;
            for(int i = 1; i < tri.size()-1; i++){
                int[] t = {tri.get(0), tri.get(i), tri.get(i+1)};
                finalTriList.add(t);
            }
        }
        if(!finalTriList.isEmpty()) {
           m.finalIndices = (ArrayList<int[]>) finalTriList.clone();
        }
        for(int i = 0; i < m.finalVectors.size(); i++){
            double wm = m.finalVectors.get(i)[3];
                double xm = m.finalVectors.get(i)[0];
                double ym = m.finalVectors.get(i)[1];
                double zm = m.finalVectors.get(i)[2];
                if(wm != 0){
                    m.finalVectors.get(i)[0] /= wm;
                    m.finalVectors.get(i)[1] /= wm;
                    // m.finalVectors.get(i)[2] /= wm;
                    m.finalVectors.get(i)[3] = 1;
                }
        }
    }
    static double determineDirection(int[] tri, ArrayList<double[]> vtx) {
        double[] p0 = vtx.get(tri[0]);
        double[] p1 = vtx.get(tri[1]);
        double[] p2 = vtx.get(tri[2]);

        double[] e1 = {
            p1[0] - p0[0],
            p1[1] - p0[1],
            p1[2] - p0[2]
        };

        double[] e2 = {
            p2[0] - p0[0],
            p2[1] - p0[1],
            p2[2] - p0[2]
        };

        double[] n = {
            e1[1]*e2[2] - e1[2]*e2[1],
            e1[2]*e2[0] - e1[0]*e2[2],
            e1[0]*e2[1] - e1[1]*e2[0]
        };
        double[] v = {
            -p0[0],
            -p0[1],
            -p0[2]
        };
        return n[0]*v[0] + n[1]*v[1] + n[2]*v[2];
    }
    static ArrayList<Integer> clipTriangle(ArrayList<Integer> v, Entity m){
        ArrayList<Integer> vertices = v;
        for(int p = 0; p < 5; p++){
            ArrayList<Integer> output = new ArrayList<>();
            for(int i = 0; i < vertices.size(); i++){
                    int v1 = vertices.get(i);
                    int v2 = vertices.get((i+1) % vertices.size());
                    boolean e1 = isInPlane(p, m.finalVectors.get(v1));
                    boolean e2 = isInPlane(p, m.finalVectors.get(v2));
                    if(e1 && e2){
                        output.add(v2);
                    } else if(e1){
                        m.finalVectors.add(calculateIntersection(m.finalVectors.get(v1), m.finalVectors.get(v2), p));
                        output.add(m.finalVectors.size()-1);
                    } else if(e2){
                        //add intersection and second
                        m.finalVectors.add(calculateIntersection(m.finalVectors.get(v1), m.finalVectors.get(v2), p));
                        output.add(m.finalVectors.size()-1);
                        output.add(v2);
                    } 
                }
                if(output.isEmpty()){
                    System.out.println("output is emtpy");
                    return output;
                }
                vertices = output;
            }
        return vertices;
    }
    static boolean isInPlane(int plane, double[] v){
        switch (plane) {
            case 0:
                return ((v[0] + v[3]) >= 0);
            case 1:
                return ((v[3] - v[0]) >= 0);
            case 2:
                return ((v[1] + v[3]) >= 0);
            case 3:
                return ((v[3] - v[1]) >= 0);
            case 4:
                return (v[2] >= 0);
            default:
                throw new AssertionError();
        }
    }
    static double[] calculateIntersection(double[] p1, double[] p2, int plane){
        double fp1;
        double fp2;
        double t;
        switch (plane) {
            case 0:
                fp1 = p1[3] + p1[0]; 
                fp2 = p2[3] + p2[0];
                break;
            case 1:
                fp1 = p1[3] - p1[0]; 
                fp2 = p2[3] - p2[0];
                break;
            case 2:
                fp1 = p1[3] + p1[1]; 
                fp2 = p2[3] + p2[1];
                break;
            case 3:
                fp1 = p1[3] - p1[1]; 
                fp2 = p2[3] - p2[1];
                break;
            case 4:
                fp1 = p1[3] + p1[2]; 
                fp2 = p2[3] + p2[2];
                break;
            case 5:
                fp1 = p1[3] - p1[2]; 
                fp2 = p2[3] - p2[2];
                break;
            default:
                throw new AssertionError();
        }
        t = fp1/(fp1 - fp2);
        double[] intersection = new double[4];
        for(int i = 0; i < 4; i++){
            intersection[i] = p1[i] + t*(p2[i] - p1[i]);
        }
        return intersection;
    }
}
