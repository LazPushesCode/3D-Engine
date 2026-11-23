

public class TriangleManager {

    // TriangleManager is responsible for filtering out invalid triangles from the pipeline. 
    // Applies backface culling and frustum culling to find valid triangles and adds to processedIndices buffer witin scene class.

    static final int INDICE_STRIDE = 4; // Number of triangle values (x, y, z, entityID);

    static void cullOperations(Scene s){ 
        int validIndices[] = new int[s.currentIndicesSize]; // Create buffer big enough to hold all possible validIndices
        int currentIndex = 0;
        
        for(int i = 0; i < s.currentIndicesSize; i+=INDICE_STRIDE){ // Iterate through all recorded indices
            // Pull references to indices
            int p0 = s.globalIndices[i];   // Indice 0
            int p1 = s.globalIndices[i+1]; // Indice 1
            int p2 = s.globalIndices[i+2]; // Indice 2
            int entityID = s.globalIndices[i+3]; // Entity ID
            if(s.entities.get(entityID).backFaceCulling){ // Check if entity is allowed to test backface culling
                // We use winding order test to check if triangle is facing the camera. If positive result, it is facing the camera and should be processed.
                if(determineDirection(s.globalVertices, p0, p1, p2) <= 0){ // If winding order result is negative, discard triangle
                    continue;
                }
            }
            // Add triangle indices to temporary buffer to pass as parameter to processedValidIndices
            validIndices[currentIndex] = p0; 
            validIndices[currentIndex+1] = p1;
            validIndices[currentIndex+2] = p2;
            validIndices[currentIndex+3] = entityID;
            currentIndex+=4;
        }
        // Process indices once we have collected all valid ones
        processValidIndices(s, validIndices, currentIndex); // Applies frustum culling to as a final check to see if indices are in view, need to be clipped, or discarded.
    }
    // Checks to see if given indices reside in the frustum view by testing if indices are inside frustum box. 
    // If all are inside, send all indices to processedIndices buffer
    // If partially inside (indices or edges), clip and send to processedIndices buffer
    // If none are inside (indices or edges), discard 
    static void processValidIndices(Scene s, int[] validIndices, int validIndicesSize){
        int x_offset = s.X_OFFSET;
        int y_offset = s.Y_OFFSET;
        int z_offset = s.Z_OFFSET;
        int w_offset = s.W_OFFSET;

        // Iterate through validIndices
        for(int i = 0; i < validIndicesSize; i+=INDICE_STRIDE){
            boolean discard = false; // If true, discard triangle and continue
            boolean clip = false; // If true, clip triangle until all indices are within frustum bounds
            for(int p = 0; p < 6; p++){ // Iterate through 6 frustum planes (near, far, left, right, top, bottom). Test one plane at a time
                int pointsOutsidePlane = 0; // Keep track of number of indices outside frustum 
                for(int j = i; j < (i+3); j++){
                    int index = validIndices[j]*Scene.STRIDE; // Obtains indice position in scene's globalVertices array
                    double xm = s.globalVertices[index]; // x clip space position
                    double ym = s.globalVertices[index+y_offset]; // y clip space position
                    double zm = s.globalVertices[index+z_offset]; // z clip space position
                    double wm = s.globalVertices[index+w_offset]; // w clip space 
                    switch(p){ // p = plane -> (near, far, left, right, top, bottom)
                        case 0: // Test left plane
                            if(xm < -wm) { // If x is outside left plane
                                pointsOutsidePlane++;
                            }
                            break;
                        case 1: // Test right plane
                            if(xm > wm) { // If x is outside right plane
                                pointsOutsidePlane++;
                            }
                            break;
                        case 2: // Test bottom plane
                            if(ym < -wm) { // If y is outside bottom plane
                                pointsOutsidePlane++;
                            }
                            break;
                        case 3: // Test top plane
                            if(ym > wm) { // If y is outside top plane
                                pointsOutsidePlane++;
                            }
                            break;
                        case 4: // Test near plane
                            if(zm < -wm) { // If z is outside near plane
                                pointsOutsidePlane++;
                            }
                            break;
                        case 5: // Test far plane
                            if(zm < wm) { // if z is outside far plane
                                pointsOutsidePlane++;
                            }
                            break;
                    }
                }
                // If all 3 indices are outside the tested plane, then discard
                if(pointsOutsidePlane == 3){
                    discard = true;
                    break;
                }
                // If some indices are outside the tested plane, triangle needs to be clipped
                if(pointsOutsidePlane > 0){
                    clip = true;
                }
            }
            if(discard){ // If true, skip triangle
                continue;
            }
            if(clip){ // If true, clip triangle
                // Clip triangle until indices are within frustum plane. Send resulting indices to scene's processedIndices array
                clipTriangle(s, validIndices[i], validIndices[i+1], validIndices[i+2], validIndices[i+3]); 
                continue;
            }
            s.addProcessedTriangle(validIndices[i], validIndices[i+1], validIndices[i+2], validIndices[i+3]); // If neither discard or clip, we can just add the 3 indices to scene's processedIndices array
        }
        s.perspectiveDivideVectors(); // Divides vectors by its respective w to create depth
    }
    static double determineDirection(double[] vectors, int p0, int p1, int p2){
        int y_offset = Scene.Y_OFFSET;
        int z_offset = Scene.Z_OFFSET;
        int stride = Scene.STRIDE;

        /// Obtain true indice positions within vectors array
        p0 *= stride;
        p1 *= stride;
        p2 *= stride;

        // Calculate edge differences (subtract vectors on triangle)

        // Edge 1 xyz
        double e1x = vectors[p1]-vectors[p0];
        double e1y = vectors[p1+y_offset] - vectors[p0+y_offset];
        double e1z = vectors[p1+z_offset] - vectors[p0+z_offset];

        // Edge 2 xyz
        double e2x = vectors[p2] - vectors[p0];
        double e2y = vectors[p2+y_offset] - vectors[p0+y_offset];
        double e2z = vectors[p2+z_offset] - vectors[p0+z_offset];

        double nx = e1y * e2z - e1z * e2y;
        double ny = e1z * e2x - e1x * e2z;
        double nz = e1x * e2y - e1y * e2x; 

        // Invert p0 vector
        double vx = -vectors[p0];
        double vy = -vectors[p0 + y_offset];
        double vz = -vectors[p0 + z_offset];

        return (nx * vx) + (ny * vy) + (nz * vz); // Returns winding order double (If less than 0 then discard triangle)
    }
    // 
    static void clipTriangle(Scene s, int v0, int v1, int v2, int entityID){
        
        // not sure why this works
        int[] input = new int[13];
        int[] output = new int[13];
        // int[] input = new int[12];
        // int[] output = new int[12];

        // Store indices into input buffer
        input[0] = v0; // Indice 0
        input[1] = v1; // Indice 1
        input[2] = v2; // Indice 2
        int inputSize = 3; 

        for (int p = 0; p < 5; p++) { // 5 planes
            int outputSize = 0;
            if(inputSize == 0) return;
            for(int i = 0; i < inputSize; i++){ // Iterate through input buffer
                int t1 = input[i]; // Set t1 as the current ith point inside input
                int t2 = input[(i+1) % inputSize]; // Set t2 as the follow up point (i+1). Use % inputSize to wrap back to start if in last ith position.

                // Test to see if each indice is in given p plane
                boolean e1 = isInPlane(p, s.globalVertices, t1); 
                boolean e2 = isInPlane(p, s.globalVertices, t2);

                // Three cases: both points inside plane, one point inside plane, no points inside plane
                if(e1 && e2){ // Both points are inside plane. 
                    output[outputSize++] = t2; // Add second indice to output
                } else if(e1){ // First point inside plane
                    output[outputSize++] = intersectAndStore(s, t1, t2, p); // Compute line between t1 and t2 and find point where it intersects with plane. Return that point
                } else if(e2){ // Second point inside plane
                    output[outputSize++] = intersectAndStore(s, t1, t2, p); // Compute line between t1 and t2 and find point where it intersects with plane. Return that point
                    output[outputSize++] = t2; // Add second point to output
                }
            }
            // Swap input and output.
            // Output is now the next plane test's input
            int[] temp = input; 
            input = output;
            output = temp;
            inputSize = outputSize;
        }
        // We now have all indices needed to stitch shape together. Send to distributeIndices function to group indices into triangles.
        distributeIndices(s, input, inputSize, entityID); 
    }
    // Connects indices to one another to form triangles, which are sent to scene class's processedIndices array.
    static void distributeIndices(Scene s, int[] input, int inputSize, int entityID){
        if(inputSize < 3) return; // If less than 3 indices, cannot form proper triangle
        int v0 = input[0];
        for(int i = 1; i < inputSize-1; i++){ // Iterate through input
            // Stitch 3 indices together at a time. v0 Acts as anchor point and is stitched to input[i] and input[i+1], creating enough triangles to cover all indices inside input 
            s.addProcessedTriangle(v0, input[i], input[i+1], entityID); // Pass 3 indices and entityID to be added to scene's processedIndices array
        }
    }
    // Tests to see if given indice is in frustum plane
    static boolean isInPlane(int plane, double[] vertices, int indice){
        indice *= Scene.STRIDE; // Obtain indice's position in scene's globalVertice array
        double x = vertices[indice]; // x clip space
        double y = vertices[indice+1]; // y clip space
        double z = vertices[indice+2]; // z clip space
        double w = vertices[indice+3]; // w clip space

        // For any case, return true if indice is within plane's boundaries
         switch (plane) { // Test specific plane
            case 0:
                return ((x + w) >= 0); // Check left plane
            case 1:
                return ((w - x) >= 0); // Check right plane
            case 2:
                return ((y + w) >= 0); // Check bottom plane
            case 3:
                return ((w - y) >= 0); // Check top plane
            case 4:
                return ((w) >= 0); // Check near plane
            case 5: 
                return ((w - z) >= 0); // Check far plane
            default:
                throw new AssertionError();
        }
    }
    static int intersectAndStore(Scene s, int p1, int p2, int plane){
        int y_offset = Scene.Y_OFFSET;
        int z_offset = Scene.Z_OFFSET;
        int w_offset = Scene.W_OFFSET;
        int u_offset = Scene.U_OFFSET;
        int v_offset = Scene.V_OFFSET;

        int stride = Scene.STRIDE;
        p1 *= stride; // Obtain indice's position in scene's globalVertice array
        p2 *= stride; // obtain indice's position in scene's globalVertice array
        double fp1= 0; 
        double fp2 = 0;
        switch (plane) { // Calculate fp1 and fp2 based on given plane
            // For each, add w_offset to obtain true camera depth 
            case 0: // Left plane
                fp1 = s.globalVertices[p1+w_offset] + s.globalVertices[p1]; 
                fp2 = s.globalVertices[p2+w_offset] + s.globalVertices[p2]; 
                break;
            case 1: // Right plane
                fp1 = s.globalVertices[p1 + w_offset] - s.globalVertices[p1];
                fp2 = s.globalVertices[p2 + w_offset] - s.globalVertices[p2];
                break;
            case 2: // Bottom plane
                fp1 = s.globalVertices[p1 + w_offset] + s.globalVertices[p1 + y_offset];
                fp2 = s.globalVertices[p2 + w_offset] + s.globalVertices[p2 + y_offset];
                break;
            case 3: // Top plane
                fp1 = s.globalVertices[p1 + w_offset] - s.globalVertices[p1 + y_offset]; 
                fp2 = s.globalVertices[p2 + w_offset] - s.globalVertices[p2 + y_offset];
                break;
            case 4: // Near plane
                fp1 = s.globalVertices[p1 + w_offset] + s.globalVertices[p1 + z_offset]; 
                fp2 = s.globalVertices[p2 + w_offset] + s.globalVertices[p2 + z_offset];
                break;
            case 5: // Far plane
                fp1 = s.globalVertices[p1 + w_offset] - s.globalVertices[p1 + z_offset]; 
                fp2 = s.globalVertices[p2 + w_offset] - s.globalVertices[p2 + z_offset];
                break;
        }
        // Interpolation factor that calculates how far along the line the plane is between p1 and p2.
        // If 0, then plane is on p1. If 0.5 then plane is halfway between points. If 1, then plane is on p2.
        double t = fp1 / (fp1 - fp2); // Interpolation factor

        int newIndice = s.currentVerticesSize; // Populate starting from the last slot used inside globalVertices
        for(int i = 0; i < INDICE_STRIDE; i++){ // Use lerp to linearly interpolate each value (A + t(B-A)) from 0 to INDICE_STRIDE
            if(newIndice+i >= s.initialVerticesSize){ // If max number of vertices has been reached, reallocate to add space to the memory
                s.reallocateGlobalVerticeMemory();
            }
            s.globalVertices[newIndice + i] = s.globalVertices[p1+i] + t*(s.globalVertices[p2+i] - s.globalVertices[p1+i]);
        }
        // Pull UV textures from each indice's original vertice data.
        double u1 = s.globalVertices[p1+u_offset];
        double v1 = s.globalVertices[p1+v_offset];

        double u2 = s.globalVertices[p2+u_offset];
        double v2 = s.globalVertices[p2+v_offset];

        // Interpolate UV values using lerp
        s.globalVertices[newIndice+u_offset] = u1 + t*(u2 - u1); 
        s.globalVertices[newIndice+v_offset] = v1 + t*(v2 - v1);
        for(int i = 6; i < stride; i++){ // Use lerp once more to linearly interpolate each value starting from 6 offset to stride offset
            s.globalVertices[newIndice + i] = s.globalVertices[p1+i] + t*(s.globalVertices[p2+i] - s.globalVertices[p1+i]);
        }
        s.currentVerticesSize += Scene.STRIDE; // Increment by STRIDE to set position for new vectors to be placed at
        return (newIndice/stride); // Return indice int
    }
}
