import java.util.ArrayList;

public class TriangleManager {
    static void cullTriangles(Entity m, CameraManager c){
        ArrayList<int[]> validIndices = new ArrayList<>();
        ArrayList<double[][]> validTextures = new ArrayList<>();
        //backface culling
        for(int i = 0; i < m.indices.length; i++){
            if(determineDirection(m.indices[i], m.viewSpaceVectors) <= 0) {
                continue;
            }
            validIndices.add(m.indices[i].clone());
            validTextures.add(m.textureMapping[i].clone());
        }
        c.convertToClipSpace(m);
        finalizeTriangles(m, validIndices, validTextures);
    }
    static void finalizeTriangles(Entity m, ArrayList<int[]> validIndices, ArrayList<double[][]> validTextures){
        
        ArrayList<int[]> clipTriList = new ArrayList<>();
        ArrayList<int[]> finalTriList = new ArrayList<>();
        ArrayList<double[][]> clipTextureList = new ArrayList<>();
        ArrayList<double[][]> finalTextureList = new ArrayList<>();

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
                clipTextureList.add(validTextures.get(i));
                continue;
            }
            finalTriList.add(validIndices.get(i));
            finalTextureList.add(validTextures.get(i));
        }
        for(int i = 0; i < clipTriList.size(); i++){
            ArrayList<Integer> tri = new ArrayList<>();
            for(int j = 0; j < 3; j++){
                tri.add(clipTriList.get(i)[j]);
            }
            TrianglePackage tp = clipTriangle(tri, m, clipTextureList.get(i));
            if(tp.vertices.size() < 3) continue;
            for(int j = 1; j < tp.vertices.size()-1; j++){
                int[] t = {tp.vertices.get(0), tp.vertices.get(j), tp.vertices.get(j+1)};
                finalTriList.add(t);
                finalTextureList.add(new double[][] {tp.uvs.get(0), tp.uvs.get(j), tp.uvs.get(j+1)});
            }
        }
        if(!finalTriList.isEmpty()) {
           m.finalIndices = (ArrayList<int[]>) finalTriList.clone();
           m.finalTextureMapping = (ArrayList<double[][]>) finalTextureList.clone();
        }
        for(int i = 0; i < m.finalVectors.size(); i++){
            double wm = m.finalVectors.get(i)[3];
            double xm = m.finalVectors.get(i)[0];
            double ym = m.finalVectors.get(i)[1];
            double zm = m.finalVectors.get(i)[2];
            if(wm != 0){
                m.finalVectors.get(i)[0] /= wm;
                m.finalVectors.get(i)[1] /= wm;
                m.finalVectors.get(i)[2] /= wm;
                // m.finalVectors.get(i)[3] = 1;
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
    static TrianglePackage clipTriangle(ArrayList<Integer> t, Entity m, double[][] textureCords){
        TrianglePackage tp = new TrianglePackage();
        tp.vertices = t;
        for(int i = 0; i < textureCords.length; i++){
            tp.uvs.add(textureCords[i].clone());
        }
        for(int p = 0; p < 5; p++){
            ArrayList<Integer> pointOutput = new ArrayList<>();
            ArrayList<double[]> uvOutput = new ArrayList<>();
            for(int i = 0; i < tp.vertices.size(); i++){
                    int t1 = tp.vertices.get(i);
                    int t2 = tp.vertices.get((i+1) % tp.vertices.size());
                    double[] uv1 = tp.uvs.get(i);
                    double[] uv2 = tp.uvs.get((i+1) % tp.uvs.size());
                    boolean e1 = isInPlane(p, m.finalVectors.get(t1));
                    boolean e2 = isInPlane(p, m.finalVectors.get(t2));
                    
                    //when cliping a triangle, we need to ensure the uv mappings remain consistent
                    if(e1 && e2){
                        pointOutput.add(t2);
                        uvOutput.add(tp.uvs.get((i+1) % tp.uvs.size()).clone());
                    } else if(e1){
                        double[] result = calculateIntersection(m.finalVectors.get(t1), m.finalVectors.get(t2), uv1, uv2, p);
                        m.finalVectors.add(new double[]{result[0],result[1], result[2], result[3]});
                        pointOutput.add(m.finalVectors.size()-1);
                        uvOutput.add(new double[]{result[4], result[5]});
                    } else if(e2){
                        double[] result = calculateIntersection(m.finalVectors.get(t1), m.finalVectors.get(t2), uv1, uv2, p);
                        m.finalVectors.add(new double[]{result[0],result[1], result[2], result[3]});
                        pointOutput.add(m.finalVectors.size()-1);
                        uvOutput.add(new double[]{result[4], result[5]});
                        pointOutput.add(t2);
                        uvOutput.add(tp.uvs.get((i+1) % tp.uvs.size()).clone());
                    } 
                }
                if(pointOutput.isEmpty()){
                    System.out.println("output is empty");
                    return null;
                }
                tp.vertices = pointOutput;
                tp.uvs = uvOutput;
            }
        return tp;
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
    static double[] calculateIntersection(double[] p1, double[] p2, double[] uv1, double[] uv2, int plane){
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
        double[] intersection = new double[6];
        for(int i = 0; i < 4; i++){
            intersection[i] = p1[i] + t*(p2[i] - p1[i]);
        }
        intersection[4] = uv1[0] + t*(uv2[0] - uv1[0]);
        intersection[5] = uv1[1] + t*(uv2[1] - uv1[1]);
        return intersection;
    }
}
