import static java.lang.Math.random;
import java.util.Arrays;

public class EffectManager {
    static double a[];
    static double b[];
    static double neighbors[];
    static int neighborStrides[];
    static int verticeCount;


    static double[] boundary;
    static int sides;

    EffectManager() {}

    static void populateBuffers(int k) {
        a = new double[128];
        b = new double[128];
        neighbors = new double[k * 2];
        neighborStrides = new int[k];
    }
    static Entity generateGlassSquare(int numSeeds, int kNeighbors, int boundarySides, double xImpact, double yImpact, double scale, int face, int uStart, int uEnd, int vStart, int vEnd){
        Entity glassSquare = new Entity();
        double uMin = (double) uStart / 100;
        double uMax = (double) uEnd / 100;
        double vMin = (double) vStart / 100;
        double vMax = (double) vEnd / 100;
        double uRadius = (uMax - uMin) * 0.5; 
        double vRadius = (vMax - vMin) * 0.5;
        double uImpact = uMin + xImpact * (uMax-uMin);
        double vImpact = vMin + yImpact * (vMax - vMin);
        sides = boundarySides;
        boundary = new double[sides * 2];
        populateBuffers(kNeighbors);
        populateBoundary(uImpact, vImpact, uRadius, vRadius);
        double[] seedVertices = new double[numSeeds * 2];
        seedVertices[0] = xImpact;
        seedVertices[1] = yImpact;
        int index = 2;
        for(int i = 1; i < numSeeds; i++){
            double theta = random() * 2 * Math.PI;
            double radius = Math.pow(random(), 2);
            double x = uImpact + radius * uRadius * Math.cos(theta);
            double y = vImpact + radius * vRadius * Math.sin(theta);
            seedVertices[index]     = Math.min(uMax, Math.max(uMin, x));
            seedVertices[index + 1] = Math.min(vMax, Math.max(vMin, y));
            index+=2;
        }
        Integer[] sortedIndices = new Integer[numSeeds];
        for (int i = 0; i < numSeeds; i++) {
            sortedIndices[i] = i;
        }
        Arrays.sort(sortedIndices, (s1, s2) -> {
            double ax = seedVertices[s1 * 2] - uImpact;
            double ay = seedVertices[s1 * 2 + 1] - vImpact;
            double bx = seedVertices[s2 * 2] - uImpact;
            double by = seedVertices[s2 * 2 + 1] - vImpact;
            return Double.compare((ax * ax + ay * ay), (bx * bx + by * by));
        });
        for (int i = 0; i < numSeeds; i++) {
            int si = sortedIndices[i];
            findNeighbors(seedVertices, si, numSeeds, kNeighbors);
            calculateShardVertices(seedVertices, si, kNeighbors, uMin, uMax, vMin, vMax);
            
            Entity shard = projectUVCordsToSquare(seedVertices, si, 1, uImpact, vImpact, face, uMin, uMax, vMin, vMax);
            if (shard != null) {
                glassSquare.addChild(shard);
                shard.setMaterialValues(1, 1, 1);
            }
        }
        Entity unshatteredSquare = new Entity();
        unshatteredSquare.squareMeshWithBoundingBox(uStart, uEnd, vStart, vEnd);
        unshatteredSquare.setMaterialValues(1, 1, 1);
        glassSquare.addChild(unshatteredSquare);
        double epsilon = 0.0002;
        Entity gap = fillGapSquare(1, face, uMin-epsilon, uMax+epsilon, vMin-epsilon, vMax+epsilon);
        gap.setMaterialValues(1, 1, 1);
        glassSquare.addChild(gap);
        return glassSquare;
    }
    static Entity generateGlassSphere(int m, int n,int numSeeds, int kNeighbors, double outerRadius, double innerRadius, int boundarySides, double xImpact, double yImpact, int uStart, int uEnd, int vStart, int vEnd) {
        Entity glassSphere = new Entity();
        double uMin = (double) uStart / n;
        double uMax = (double) uEnd / n;
        double vMin = (double) vStart / m;
        double vMax = (double) vEnd / m;
        double uRadius = (uMax - uMin) * 0.5;
        double vRadius = (vMax - vMin) * 0.5;
        sides = boundarySides;
        boundary = new double[sides*2];

        double uImpact = uMin + xImpact * (uMax-uMin);
        double vImpact = vMin + yImpact * (vMax - vMin);
        populateBuffers(kNeighbors);
        populateBoundary(uImpact, vImpact, uRadius, vRadius);

        double[] seedVertices = new double[numSeeds * 2];
        seedVertices[0] = uImpact;
        seedVertices[1] = vImpact;
        int index = 2;

        for (int i = 1; i < numSeeds; i++) {
            double theta = random() * Math.PI * 2.0;
            double r = Math.pow(random(), 2);
            
            double x = uImpact + r * uRadius * Math.cos(theta);
            double y = vImpact + r * vRadius * Math.sin(theta);

            seedVertices[index]     = Math.min(uMax, Math.max(uMin, x));
            seedVertices[index + 1] = Math.min(vMax, Math.max(vMin, y));
            index += 2;
        }

        Integer[] sortedIndices = new Integer[numSeeds];
        for (int i = 0; i < numSeeds; i++) {
            sortedIndices[i] = i;
        }

        Arrays.sort(sortedIndices, (s1, s2) -> {
            double ax = seedVertices[s1 * 2] - uImpact;
            double ay = seedVertices[s1 * 2 + 1] - vImpact;
            double bx = seedVertices[s2 * 2] - uImpact;
            double by = seedVertices[s2 * 2 + 1] - vImpact;
            return Double.compare((ax * ax + ay * ay), (bx * bx + by * by));
        });


        for (int i = 0; i < numSeeds; i++) {
            int si = sortedIndices[i];
            findNeighbors(seedVertices, si, numSeeds, kNeighbors);
            calculateShardVertices(seedVertices, si, kNeighbors, uMin, uMax, vMin, vMax);
            
            Entity shard = projectUVCordsToSphere(seedVertices, si, outerRadius, uImpact, vImpact);
            if (shard != null) {
                glassSphere.addChild(shard);
                shard.setMaterialValues(1, 1, 1);
            }
        }
        
        double epsilon = 0.0002;
        Entity gap = fillGapSphere(outerRadius, uMin-epsilon, uMax+epsilon, vMin-epsilon, vMax+epsilon);
        glassSphere.addChild(gap);
        Entity unshatteredSphere = new Entity();
        unshatteredSphere.sphereMesh(m, n, outerRadius, innerRadius, uStart, uEnd, vStart, vEnd);
        unshatteredSphere.setMaterialValues(1, 1, 1);
        glassSphere.addChild(unshatteredSphere);
        gap.setMaterialValues(1, 1, 1);
        return glassSphere;
    }
    
    static void populateBoundary(double xImpact, double yImpact, double uRadius, double vRadius){
        double angleStep = (2 * Math.PI) / sides;
        for(int i = 0; i < sides; i++){
            double angle = i * angleStep;
            double jitter = 0.85 + (random() * 0.20);
            boundary[i*2] = xImpact + uRadius * jitter * Math.cos(angle);
            boundary[i*2+1] = yImpact + vRadius * jitter * Math.sin(angle);
        }
    }

    static void calculateShardVertices(double[] seedVertices, int seedIndex, int kNeighbors, double uMin, double uMax, double vMin, double vMax) {
        Arrays.fill(a, 0);
        Arrays.fill(b, 0);
        System.arraycopy(boundary, 0, a, 0, sides*2);
        
        int count = sides;
        double sx = seedVertices[seedIndex * 2];
        double sy = seedVertices[seedIndex * 2 + 1];

        for (int i = 0; i < kNeighbors; i++) {
            Arrays.fill(b, 0);
            int nIndex = neighborStrides[i] * 2;
            double nx = seedVertices[nIndex];
            double ny = seedVertices[nIndex + 1];

            double mx = (sx + nx) * 0.5;
            double my = (sy + ny) * 0.5;
            double xnorm = nx - sx;
            double ynorm = ny - sy;
            double c = -(xnorm * mx + ynorm * my);

            int newCount = 0;
            for (int j = 0; j < count; j++) {
                int prevIndex = (j + count - 1) % count;

                double px = a[prevIndex * 2];
                double py = a[prevIndex * 2 + 1];
                double cx = a[j * 2];
                double cy = a[j * 2 + 1];

                double cres = (xnorm * cx + ynorm * cy) + c;
                double pres = (xnorm * px + ynorm * py) + c;

                boolean cInside = (cres <= 0);
                boolean pInside = (pres <= 0);

                if (pInside != cInside) {
                    double t = pres / (pres - cres);
                    b[newCount * 2]     = px + t * (cx - px);
                    b[newCount * 2 + 1] = py + t * (cy - py);
                    newCount++;
                }
                if (cInside) {
                    b[newCount * 2]     = cx;
                    b[newCount * 2 + 1] = cy;
                    newCount++;
                }
            }

            double[] temp = a;
            a = b;
            b = temp;
            count = newCount;
            if (count == 0) break;
        }
        verticeCount = count;
    }

    static Entity projectUVCordsToSphere(double[] seedVertices, int seedIndex, double radius, double xImpact, double yImpact) {
        if (verticeCount < 3) return null;

        int vertStride = Entity.INITIAL_MESH_STRIDE;
        double[] vertices = new double[verticeCount * 2 * vertStride];

        double cx = 0, cy = 0, cz = 0;
        double thickness = 0.001;

        for (int i = 0; i < verticeCount; i++) {
            double u = a[i * 2];
            double v = a[i * 2 + 1];

            double longitude = -(Math.PI / 2.0) + v * Math.PI;
            double latitude  = u * 2.0 * Math.PI;

            double nx = Math.cos(longitude) * Math.cos(latitude);
            double ny = Math.sin(longitude);
            double nz = Math.cos(longitude) * Math.sin(latitude);

            double px = radius * nx;
            double py = radius * ny;
            double pz = radius * nz;

            double frontX = px + nx * (thickness * 0.5);
            double frontY = py + ny * (thickness * 0.5);
            double frontZ = pz + nz * (thickness * 0.5);

            double backX = px - nx * (thickness * 0.5);
            double backY = py - ny * (thickness * 0.5);
            double backZ = pz - nz * (thickness * 0.5);

            cx += frontX + backX;
            cy += frontY + backY;
            cz += frontZ + backZ;

            int index = i * vertStride;
            int halfway = (i + verticeCount) * vertStride;

            vertices[index]     = frontX; vertices[index + 1] = frontY; vertices[index + 2] = frontZ;
            vertices[index + 3] = 1.0;   vertices[index + 4] = u;      vertices[index + 5] = v;

            vertices[halfway]     = backX; vertices[halfway + 1] = backY; vertices[halfway + 2] = backZ;
            vertices[halfway + 3] = 1.0;   vertices[halfway + 4] = u;      vertices[halfway + 5] = v;
        }

        int totalVerts = verticeCount * 2;
        cx /= totalVerts;
        cy /= totalVerts;
        cz /= totalVerts;

        for (int i = 0; i < totalVerts; i++) {
            int index = i * vertStride;
            vertices[index]     -= cx;
            vertices[index + 1] -= cy;
            vertices[index + 2] -= cz;
        }
        Entity shard = Entity.generateFlat3DShape(vertices, verticeCount);
        shard.setWorldPosition(cx, cy, cz);
        return shard;
    }
    static Entity projectUVCordsToSquare(double[] seedVertices, int seedIndex, double sideLength, double xImpact, double yImpact, int face, double uMin, double uMax, double vMin, double vMax) {
        if (verticeCount < 3) return null;

        int vertStride = Entity.INITIAL_MESH_STRIDE;
        double[] vertices = new double[verticeCount * 2 * vertStride];

        double cx = 0, cy = 0, cz = 0;
        double thickness = 0.001;

        for (int i = 0; i < verticeCount; i++) {
            double u = a[i * 2];
            double v = a[i * 2 + 1];
            double nx = 0, ny = 0, nz = 0, px = 0, py = 0, pz = 0;
            double uLocal = (u - 0.5) * sideLength;
            double vLocal = (v - 0.5) * sideLength;

            double frontX = px + nx * (thickness * 0.5);
            double frontY = py + ny * (thickness * 0.5);
            double frontZ = pz + nz * (thickness * 0.5);

            double backX = px - nx * (thickness * 0.5);
            double backY = py - ny * (thickness * 0.5);
            double backZ = pz - nz * (thickness * 0.5);

            cx += frontX + backX;
            cy += frontY + backY;
            cz += frontZ + backZ;

            int index = i * vertStride;
            int halfway = (i + verticeCount) * vertStride;

            vertices[index]     = uLocal; 
            vertices[index + 1] = vLocal; 
            vertices[index + 2] = 0;
            vertices[index + 3] = 1.0;   
            vertices[index + 4] = u;  
            vertices[index + 5] = v;

            vertices[halfway] = uLocal; 
            vertices[halfway + 1] = vLocal; 
            vertices[halfway + 2] = 0;
            vertices[halfway + 3] = 1.0;   
            vertices[halfway + 4] = u;      
            vertices[halfway + 5] = v;
        }

        int totalVerts = verticeCount * 2;
        cx /= totalVerts;
        cy /= totalVerts;
        cz /= totalVerts;

        for (int i = 0; i < totalVerts; i++) {
            int index = i * vertStride;
            vertices[index]     -= cx;
            vertices[index + 1] -= cy;
            vertices[index + 2] -= cz;
        }

        Entity shard = Entity.generateFlat3DShape(vertices, verticeCount);
        shard.setWorldPosition(cx, cy, cz);
        return shard;
    }
    static double[] getGapVertices(double uMin, double uMax, double vMin, double vMax){
        double uMid = (uMin + uMax) * 0.5;
        double vMid = (vMin + vMax) * 0.5;
        double[] corners = {
            uMin, vMin,
            uMax, vMin,
            uMax, vMax,
            uMin, vMax
        };
        double[] vertices = new double[sides * 36];
        int vertCount = 0;
        for(int i = 0; i < sides; i++){
            int next = (i+1) % sides;
            double x0 = boundary[i*2];
            double y0 = boundary[i*2+1];
            double x1 = boundary[next*2];
            double y1 = boundary[next*2+1];
            
            int c0 = getCornerIndex(x0, y0, uMid, vMid);
            int c1 = getCornerIndex(x1, y1, uMid, vMid);

            vertices[vertCount*2] = x0;
            vertices[vertCount*2+1] = y0;
            vertCount++;
            vertices[vertCount*2] = x1;
            vertices[vertCount*2+1] = y1;
            vertCount++;
            vertices[vertCount*2] = corners[c0*2];
            vertices[vertCount*2+1] = corners[c0*2+1];
            vertCount++;
            
            if(c0 != c1){
                int currCorner = c0;
                while(currCorner != c1){
                    int nextCorner = (currCorner + 1) % 4;
                    vertices[vertCount*2] = x1;
                    vertices[vertCount*2+1] = y1;
                    vertCount++;
                    vertices[vertCount*2] = corners[nextCorner*2];
                    vertices[vertCount*2+1] = corners[nextCorner*2+1];
                    vertCount++;
                    vertices[vertCount*2] = corners[currCorner*2];
                    vertices[vertCount*2+1] = corners[currCorner*2+1];
                    vertCount++;
                    currCorner = nextCorner;
                }
            } 

        }
        return Arrays.copyOf(vertices, vertCount * 2);
    }
    static double[] subdivideTriangles(double[] vertices, int levels){
        double[] currVertices = vertices;
        for(int i = 0; i < levels; i++){
            int vertCount = currVertices.length /2;
            int triangles = vertCount /3;
            double[] newVertices = new double[triangles*24];
            int newIndex = 0;
            for(int j = 0; j < triangles; j++){
                int index = j * 6;
                double ax = currVertices[index];
                double ay = currVertices[index+1];
                double bx = currVertices[index+2];
                double by = currVertices[index+3];
                double cx = currVertices[index+4];
                double cy = currVertices[index+5];

                double mabx = (ax + bx) *0.5;
                double maby = (ay + by) * 0.5;
                double mbcx = (bx + cx) * 0.5;
                double mbcy = (by + cy) * 0.5;
                double mcax = (cx + ax) * 0.5;
                double mcay = (cy + ay) * 0.5;
                newVertices[newIndex++] = ax;
                newVertices[newIndex++] = ay;
                newVertices[newIndex++] = mabx;
                newVertices[newIndex++] = maby;
                newVertices[newIndex++] = mcax;
                newVertices[newIndex++] = mcay;

                newVertices[newIndex++] = bx;
                newVertices[newIndex++] = by;
                newVertices[newIndex++] = mbcx;
                newVertices[newIndex++] = mbcy;
                newVertices[newIndex++] = mabx;
                newVertices[newIndex++] = maby;

                newVertices[newIndex++] = mbcx;
                newVertices[newIndex++] = mbcy;
                newVertices[newIndex++] = mcax;
                newVertices[newIndex++] = mcay;
                newVertices[newIndex++] = mabx;
                newVertices[newIndex++] = maby;

                newVertices[newIndex++] = cx;
                newVertices[newIndex++] = cy;
                newVertices[newIndex++] = mcax;
                newVertices[newIndex++] = mcay;
                newVertices[newIndex++] = mbcx;
                newVertices[newIndex++] = mbcy;
            }
            currVertices = newVertices;
        }
        return currVertices;
    }
    static int getCornerIndex(double u, double v, double uMid, double vMid){
        if(u < uMid && v < vMid) return 0;
        if(u >= uMid && v < vMid) return 1;
        if(u >= uMid && v >= vMid) return 2;
        return 3;
    }
    static Entity fillGapSquare(double sideLength, int face, double uMin, double uMax, double vMin, double vMax){
        double[] uvcords = getGapVertices(uMin, uMax, vMin, vMax);
        uvcords = subdivideTriangles(uvcords, 4);
        int vertCount = uvcords.length / 2;
        double[] vertices = new double[vertCount * Entity.LOCAL_MESH_STRIDE];
        int[] indices = new int[vertCount];
        for(int i = 0; i < vertCount; i++){
            double u = uvcords[i*2];
            double v = uvcords[i*2+1];
            double nx = 0, ny = 0, nz = -1, px = 0, py = 0, pz = 0;
            double uLocal = (u - 0.5) * sideLength;
            double vLocal = (v - 0.5) * sideLength;
            
            int vertIndex = i * Entity.LOCAL_MESH_STRIDE;
            vertices[vertIndex] = uLocal;
            vertices[vertIndex + 1] = vLocal;
            vertices[vertIndex + 2] = 0;
            vertices[vertIndex + 3] = 1;
            vertices[vertIndex + 4] = u;
            vertices[vertIndex + 5] = v;
            vertices[vertIndex + 6] = -nx;
            vertices[vertIndex + 7] = -ny;
            vertices[vertIndex + 8] = -nz;
        }
        for (int i = 0; i < vertCount; i += 3) {
            indices[i]     = i;
            indices[i + 1] = i + 2; 
            indices[i + 2] = i + 1;
        }
        Entity gap = new Entity();
        gap.initializeArrays(vertices,indices);
        gap.initializeVariables();
        gap.transformation = Matrix.identity();
        return gap;
    }
    static Entity fillGapSphere(double radius, double uMin, double uMax, double vMin, double vMax){
        double[] uvcords = getGapVertices(uMin, uMax, vMin, vMax);
        uvcords = subdivideTriangles(uvcords, 4);
        int vertCount = uvcords.length / 2;
        double[] vertices = new double[vertCount * Entity.LOCAL_MESH_STRIDE];
        int[] indices = new int[vertCount];
        for(int i = 0; i < vertCount; i++){
            double u = uvcords[i*2];
            double v = uvcords[i*2+1];

            double longitude = -(Math.PI /2) + v * Math.PI;
            double latitude = u * 2 * Math.PI;

            double nx = Math.cos(longitude) * Math.cos(latitude);
            double ny = Math.sin(longitude);
            double nz = Math.cos(longitude) * Math.sin(latitude);

            double x = radius * nx;
            double y = radius * ny;
            double z = radius * nz;
            int vertIndex = i * Entity.LOCAL_MESH_STRIDE;
            vertices[vertIndex] = x;
            vertices[vertIndex + 1] = y;
            vertices[vertIndex + 2] = z;
            vertices[vertIndex + 3] = 1;
            vertices[vertIndex + 4] = u;
            vertices[vertIndex + 5] = v;
            vertices[vertIndex + 6] = -nx;
            vertices[vertIndex + 7] = -ny;
            vertices[vertIndex + 8] = -nz;
        }
        for (int i = 0; i < vertCount; i += 3) {
            indices[i]     = i;
            indices[i + 1] = i + 2; 
            indices[i + 2] = i + 1;
        }
        Entity gap = new Entity();
        gap.initializeArrays(vertices,indices);
        gap.initializeVariables();
        gap.transformation = Matrix.identity();
        return gap;
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
    }

    static double getDistance(double x0, double y0, double x1, double y1) {
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
    static void assignVideo(Entity et, int row, int col, int totalRow, int totalCol) {
        int vc = et.worldVertices.length / Entity.LOCAL_MESH_STRIDE;
        double xMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double zMin = Double.POSITIVE_INFINITY;
        double zMax = Double.NEGATIVE_INFINITY;

        for(int i = 0; i < vc; i++){
            int index = i * Entity.LOCAL_MESH_STRIDE;
            double x = et.worldVertices[index];
            double y = et.worldVertices[index+1];
            double z = et.worldVertices[index+2];
            if(x < xMin) xMin = x;
            if(x > xMax) xMax = x;
            if(y < yMin) yMin = y;
            if(y > yMax) yMax = y;
            if(z < zMin) zMin = z;
            if(z > zMax) zMax = z;
        }
        double width = xMax - xMin;
        double length = yMax - yMin;
        double depth = zMax - zMin;

        for(int i = 0; i < vc; i++){
            int index = i * Entity.LOCAL_MESH_STRIDE;

            double u,v;

            if(depth > width && depth > length){
                u = (depth == 0) ? 0 : (et.worldVertices[index+2] - zMin) / depth;
                v = (depth == 0) ? 0 : (et.worldVertices[index+1] - yMin) / length;
            } else if(width >= length){
                u = (width == 0) ? 0 : (et.worldVertices[index] - xMin) / width;
                v = (width == 0) ? 0 : (et.worldVertices[index+1] - yMin) / length;
            } else {
                u = (length == 0) ? 0 : (et.worldVertices[index] - xMin) / width;
                v = (length == 0) ? 0 : (et.worldVertices[index+2] - zMin) / depth;
            }


            et.worldVertices[index+4] = (u + col ) / totalCol;
            et.worldVertices[index+5] = (v + row) / totalRow;
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
}