

public class TriangleManager {

    static final int INDICE_STRIDE = 4;

    static void cullOperations(Scene s){
        int validIndices[] = new int[s.currentIndicesSize];
        int currentIndex = 0;
        int trianglesCulled = 0;
        for(int i = 0; i < s.currentIndicesSize; i+=4){
            int p0 = s.globalIndices[i];
            int p1 = s.globalIndices[i+1];
            int p2 = s.globalIndices[i+2];
            int entityID = s.globalIndices[i+3];
            if(determineDirection(s.globalVertices, p0, p1, p2) <= 0){
                trianglesCulled++;
                continue;
            }
            validIndices[currentIndex] = p0;
            validIndices[currentIndex+1] = p1;
            validIndices[currentIndex+2] = p2;
            validIndices[currentIndex+3] = entityID;
            currentIndex+=4;
        }
        // s.convertVerticesToClipSpace();
        processValidIndices(s, validIndices, currentIndex);
    }
    static void processValidIndices(Scene s, int[] validIndices, int validIndicesSize){
        for(int i = 0; i < validIndicesSize; i+=4){
            boolean discard = false;
            boolean clip = false;
            for(int p = 0; p < 6; p++){
                int pointsOutsidePlane = 0;
                for(int j = i; j < (i+4); j++){
                    int index = validIndices[j]*Scene.STRIDE;
                    double xm = s.globalVertices[index];
                    double ym = s.globalVertices[index+1];
                    double zm = s.globalVertices[index+2];
                    double wm = s.globalVertices[index+3];
                    switch(p){
                        case 0: // outside left plane
                            if(xm < -wm) {
                                pointsOutsidePlane++;
                            }
                            break;
                        case 1: // outside right plane
                            if(xm > wm) {
                                pointsOutsidePlane++;
                            }
                            break;
                        case 2: // outside bottom plane
                            if(ym < -wm) {
                                pointsOutsidePlane++;
                            }
                            break;
                        case 3: // outside top plane
                            if(ym > wm) {
                                pointsOutsidePlane++;
                            }
                            break;
                        case 4: // outside near plane
                            if(zm < -wm) {
                                pointsOutsidePlane++;
                            }
                            break;
                        case 5: // outside far plane
                            if(zm < wm) {
                                pointsOutsidePlane++;
                            }
                            break;
                    }
                }
                if(pointsOutsidePlane > 3){
                    discard = true;
                    break;
                }
                if(pointsOutsidePlane > 0){
                    clip = true;
                }
            }
            if(discard){
                continue;
            }
            if(clip){
                clipTriangle(s, validIndices[i], validIndices[i+1], validIndices[i+2], validIndices[i+3]);
                continue;
            }
            s.addProcessedTriangle(validIndices[i], validIndices[i+1], validIndices[i+2], validIndices[i+3]);
        }
        s.perspectiveDivideVectors();
    }
    static double determineDirection(double[] vectors, int p0, int p1, int p2){
        int stride = Scene.STRIDE;
        p0 *= stride;
        p1 *= stride;
        p2 *= stride;
        
        double e1x = vectors[p1]-vectors[p0];
        double e1y = vectors[p1+1] - vectors[p0+1];
        double e1z = vectors[p1+2] - vectors[p0+2];

        double e2x = vectors[p2] - vectors[p0];
        double e2y = vectors[p2+1] - vectors[p0+1];
        double e2z = vectors[p2+2] - vectors[p0+2];

        double nx = e1y * e2z - e1z * e2y;
        double ny = e1z * e2x - e1x * e2z;
        double nz = e1x * e2y - e1y * e2x; 

        double vx = -vectors[p0];
        double vy = -vectors[p0 + 1];
        double vz = -vectors[p0 + 2];

        return (nx * vx) + (ny * vy) + (nz * vz);
    }
    static void clipTriangle(Scene s, int v0, int v1, int v2, int entityID){
        
        int[] input = new int[12];
        int[] output = new int[12];

        input[0] = v0;
        input[1] = v1;
        input[2] = v2;
        int inputSize = 3;

        for (int p = 0; p < 5; p++) {
            int outputSize = 0;
            if(inputSize == 0) return;
            for(int i = 0; i < inputSize; i++){
                int t1 = input[i];
                int t2 = input[(i+1) % inputSize];

                boolean e1 = isInPlane(p, s.globalVertices, t1);
                boolean e2 = isInPlane(p, s.globalVertices, t2);

                if(e1 && e2){
                    output[outputSize++] = t2;
                } else if(e1){
                    output[outputSize++] = intersectAndStore(s, t1, t2, p);
                } else if(e2){
                    output[outputSize++] = intersectAndStore(s, t1, t2, p);
                    output[outputSize++] = t2;
                }
            }
        int[] temp = input;
        input = output;
        output = temp;
        inputSize = outputSize;
        }
        distributeIndices(s, input, inputSize, entityID);
    }
    static void distributeIndices(Scene s, int[] input, int inputSize, int entityID){
        if(inputSize < 3) return;
        for(int i = 1; i < inputSize-1; i++){
            s.addProcessedTriangle(input[0], input[i], input[i+1], entityID);
        }
    }
    static boolean isInPlane(int plane, double[] vertices, int indice){
        indice *= Scene.STRIDE;
        double x = vertices[indice];
        double y = vertices[indice+1];
        double z = vertices[indice+2];
        double w = vertices[indice+3];

         switch (plane) {
            case 0:
                return ((x + w) >= 0);
            case 1:
                return ((w - x) >= 0);
            case 2:
                return ((y + w) >= 0);
            case 3:
                return ((w - y) >= 0);
            case 4:
                return (z >= 0);
            case 5: 
                return ((w - z) >= 0);
            default:
                throw new AssertionError();
        }
    }
    static int intersectAndStore(Scene s, int p1, int p2, int plane){
        int scene = Scene.STRIDE;
        p1 *= scene;
        p2 *= scene;
        double fp1= 0;
        double fp2 = 0;
        switch (plane) {
            case 0:
                fp1 = s.globalVertices[p1+3] + s.globalVertices[p1]; 
                fp2 = s.globalVertices[p2+3] + s.globalVertices[p2];
                break;
            case 1:
                fp1 = s.globalVertices[p1 + 3] - s.globalVertices[p1];
                fp2 = s.globalVertices[p2 + 3] - s.globalVertices[p2];
                break;
            case 2:
                fp1 = s.globalVertices[p1 + 3] + s.globalVertices[p1 + 1];
                fp2 = s.globalVertices[p2 + 3] + s.globalVertices[p2 + 1];
                break;
            case 3:
                fp1 = s.globalVertices[p1 + 3] - s.globalVertices[p1 + 1]; 
                fp2 = s.globalVertices[p2 + 3] - s.globalVertices[p2 + 1];
                break;
            case 4:
                fp1 = s.globalVertices[p1 + 3] + s.globalVertices[p1 + 2]; 
                fp2 = s.globalVertices[p2 + 3] + s.globalVertices[p2 + 2];
                break;
            case 5:
                fp1 = s.globalVertices[p1 + 3] - s.globalVertices[p1 + 2]; 
                fp2 = s.globalVertices[p2 + 3] - s.globalVertices[p2 + 2];
                break;
        }
        double t = fp1 / (fp1 - fp2);
        int newIndice = s.currentVerticesSize;
        for(int i = 0; i < 4; i++){
            s.globalVertices[newIndice + i] = s.globalVertices[p1+i] + t*(s.globalVertices[p2+i] - s.globalVertices[p1+i]);
        }
        double u1 = s.globalVertices[p1+4];
        double v1 = s.globalVertices[p1+5];

        double u2 = s.globalVertices[p2+4];
        double v2 = s.globalVertices[p2+5];

        s.globalVertices[newIndice+4] = u1 + t*(u2 - u1);
        s.globalVertices[newIndice+5] = v1 + t*(v2 - v1);
        s.currentVerticesSize += 6;
        return (newIndice/scene);
    }
}
