
import static java.lang.Math.random;
import java.util.Arrays;



public class EffectManager {
    static double a[];
    static double b[];
    static double neighbors[];
    static int neighborStrides[];
    static int verticeCount;

    EffectManager(){

    }
    static void populateBuffers(int k){
        a = new double[32];
        b = new double[32];
        neighbors = new double[k*2];
        neighborStrides = new int[k];
    }
    static Entity generateGlassSphere(int numSeeds, int kNeighbors){
        Entity glassSphere = new Entity();
        double xImpact = 0.5;
        double yImpact=  0.5;
        double maxRadius = 0.15;
        double lambda = 0.5;
        populateBuffers(kNeighbors);
        double[] seedVertices = new double[numSeeds * 2];
        seedVertices[0] = xImpact;
        seedVertices[1] = yImpact;
        int index = 2;
        
        for(int i = 1; i < numSeeds; i++){
            double theta = random() * Math.PI*2;
            double r = Math.pow(random(), 2) * maxRadius;
            double x = xImpact + r * Math.cos(theta);
            double y = yImpact + r * Math.sin(theta);
            // double x = Math.min(1, Math.max(0,xImpact + r * Math.cos(theta)));
            // double y = Math.min(1, Math.max(0, yImpact + r * Math.sin(theta)));
            seedVertices[index] = x;
            seedVertices[index+1] = y;
            index += 2;
        }

        Integer[] sortedIndices = new Integer[numSeeds];
        for(int i = 0; i < numSeeds; i++){
            sortedIndices[i] = i;
        }
        Arrays.sort(sortedIndices, (a,  b) ->{
            double ax = seedVertices[a * 2];
            double ay = seedVertices[a*2+1];
            double bx = seedVertices[b*2];
            double by = seedVertices[b*2+1];
            double dxa = ax - xImpact;
            double dya = ay - yImpact;
            double dxb = bx - xImpact;
            double dyb = by - yImpact;
            double distSqrA = (dxa * dxa) + (dya * dya);
            double distSqrB = (dxb * dxb) + (dyb * dyb);
            return Double.compare(distSqrA, distSqrB);
        });
        
        for(int i = 0; i < numSeeds; i++){
            int n = sortedIndices[i];
            findNeighbors(seedVertices,n, numSeeds, kNeighbors);
            calculateShardVerticesS(seedVertices, n, kNeighbors);
            Entity shard = projectUVCordsToSphere(seedVertices, n);
            if(shard != null){
                glassSphere.addChild(shard);
            }
        }
        return glassSphere;
        // return new Entity();
    }
    static void calculateShardVerticesS(double[] seedVertices, int seedIndex, int kNeighbors){
        Arrays.fill(a, 0);
        Arrays.fill(b, 0);
        initializeBoundingBox();
        int count = 4;
        double sx = seedVertices[seedIndex * 2];
        double sy = seedVertices[seedIndex * 2 + 1];
        int newCount = 0;
        for(int i = 0; i < kNeighbors; i++){
            Arrays.fill(b,0);
            int nIndex = neighborStrides[i]*2;
            double nx = seedVertices[nIndex];
            double ny = seedVertices[nIndex + 1];
            double mx = (sx+nx)*0.5;
            double my = (sy+ny)*0.5;
            double xnorm = nx - sx;
            double ynorm = ny - sy;
            double c = -(xnorm*mx + ynorm*my);
            newCount = 0;
            for(int j = 0; j < count; j++){
                int prevIndex = (j + count - 1) % count;
                
                double px = a[prevIndex*2];
                double py = a[prevIndex*2+1];
                double cx = a[j*2];
                double cy = a[j*2+1];

                double cres = (xnorm*cx + ynorm*cy)+c;
                double pres = (xnorm*px + ynorm*py)+c;
                boolean cInside = (cres<=0);
                boolean pInside = (pres<=0);
                if(pInside != cInside){
                    double t = pres / (pres-cres);
                    double x = px + t * (cx - px);
                    double y = py + t * (cy - py);
                    b[newCount * 2] = x;
                    b[newCount * 2 + 1] = y;
                    newCount++;
                }
                if(cInside){
                    b[newCount*2]=cx;
                    b[newCount*2+1]=cy;
                    newCount++;
                }
            }
            double[]temp = a;
            a = b;
            b = temp;     
            count = newCount;
            if(count == 0)break;
        }
        verticeCount = newCount;
        // System.out.println("vertices: " + Arrays.toString(a) + " count: " + newCount);
    }
    static Entity projectUVCordsToSphere(double[] seedVertices, int seedIndex) {
       if (verticeCount < 3) {
            return null;
        }

        double r = 0.5;
        double pi = Math.PI;

        int vertStride = Entity.INITIAL_MESH_STRIDE;
        int indStride = 3;
        double[] vertices = new double[verticeCount * vertStride];

        double cx =0, cy = 0, cz = 0;

        for (int i = 0; i < verticeCount; i++) {
            double u = a[i * 2];
            double v = a[i * 2 + 1];

            double theta = (u - 0.5) * (2.0 * pi);
            double phi   = v * pi;

            double sinPhi = Math.sin(phi);
            double px = r * sinPhi * Math.cos(theta);
            double py = r * sinPhi * Math.sin(theta);
            double pz = r * Math.cos(phi);

            cx += px;
            cy += py;
            cz += pz;

            int index = i * vertStride;
            vertices[index]     = px;
            vertices[index + 1] = py;
            vertices[index + 2] = pz;
            vertices[index + 3] = 1.0;
            vertices[index + 4] = u;
            vertices[index + 5] = v;
        }

        cx /= verticeCount;
        cy /= verticeCount;
        cz /= verticeCount;
        for(int i = 0; i < verticeCount; i++){
            int index = i*vertStride;
            vertices[index] -= cx;
            vertices[index+1] -= cy;
            vertices[index+2] -= cz;
        }

        int numTriangles = verticeCount - 2;
        int[] indices = new int[numTriangles * indStride];
        for (int j = 0; j < numTriangles; j++) {
            int index = j * indStride;
            indices[index]     = 0;
            indices[index + 1] = j + 2;
            indices[index + 2] = j + 1; 
        }
        
        
        Entity shard = new Entity(vertices, indices, Matrix.Identity());

        shard.setWorldPosition(cx, cy, cz);

        return shard;
    }
    static void initializeBoundingBox(){
        double minX = 0.35, maxX = 0.65;
        double minY = 0.35, maxY = 0.65;

        a[0] = minX; a[1] = minY;
        a[2] = minX; a[3] = maxY;
        a[4] = maxX; a[5] = maxY;
        a[6] = maxX; a[7] = minY;
    }
    static void findNeighbors(double[] seedVertices, int seedIndex, int seeds, int kNeighbors) {
        Arrays.fill(neighborStrides, 0);
        double cx = seedVertices[seedIndex * 2];
        double cy = seedVertices[seedIndex * 2 + 1];
        int count = 0;

        for (int i = 0; i < seeds; i++) {
            if (i == seedIndex) continue;

            if (count < kNeighbors) {
                neighborStrides[count] = i;
                count++;
                if (count == kNeighbors) {
                    insertionSort(seedVertices, cx, cy, kNeighbors);
                }
            } else {
                checkAndInsertionSort(seedVertices, cx, cy, kNeighbors, i);
            }
        }
        if (count < kNeighbors) {
            insertionSort(seedVertices, cx, cy, count);
        }

        // System.out.println("neighborstrides: " + Arrays.toString(neighborStrides));
    }
    static double getDistance(double x0, double y0, double x1, double y1){
        double dx = x1 - x0;
        double dy = y1 - y0;
        return (dx * dx) + (dy * dy);
    }
    static void insertionSort(double[] seedVertices, double cx, double cy, int kNeighbors) {
    for (int i = 1; i < kNeighbors; i++) {
        for (int j = i; j > 0; j--) {
            int current = neighborStrides[j];
            int prev = neighborStrides[j - 1];

            double distCurrent = getDistance(cx, cy, seedVertices[current * 2], seedVertices[current * 2 + 1]);
            double distPrev = getDistance(cx, cy, seedVertices[prev * 2], seedVertices[prev * 2 + 1]);

            if (distCurrent < distPrev) {
                neighborStrides[j] = prev;
                neighborStrides[j - 1] = current;
            } else {
                break; 
            }
        }
    }
}

static void checkAndInsertionSort(double[] seedVertices, double cx, double cy, int kNeighbors, int newIndex) {
    int maxStride = neighborStrides[kNeighbors - 1];
    double maxDist = getDistance(cx, cy, seedVertices[maxStride * 2], seedVertices[maxStride * 2 + 1]);
    double newDist = getDistance(cx, cy, seedVertices[newIndex * 2], seedVertices[newIndex * 2 + 1]);

    if (newDist >= maxDist) return;
    neighborStrides[kNeighbors - 1] = newIndex;
    for (int j = kNeighbors - 1; j > 0; j--) {
        int current = neighborStrides[j];
        int prev = neighborStrides[j - 1];

        double distCurrent = getDistance(cx, cy, seedVertices[current * 2], seedVertices[current * 2 + 1]);
        double distPrev = getDistance(cx, cy, seedVertices[prev * 2], seedVertices[prev * 2 + 1]);

        if (distCurrent < distPrev) {
            neighborStrides[j] = prev;
            neighborStrides[j - 1] = current;
        } else {
            break;
        }
    }
}
static double getDistanceSq(double ix, double iy, double iz, double x, double y, double z){
    double dx = x - ix;
    double dy = y - iy;
    double dz = z - iz;
    return (dx*dx) + (dy*dy) + (dz*dz);
}
static double invSqrt(double x) {
    double xhalf = 0.5d * x;
    long i = Double.doubleToLongBits(x);
    i = 0x5fe6eb50c7aa19f9L - (i >> 1); 
    x = Double.longBitsToDouble(i);
    x = x * (1.5d - xhalf * x * x);    
  return x;
}



}
