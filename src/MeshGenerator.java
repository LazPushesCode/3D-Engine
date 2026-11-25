public class MeshGenerator {

    // Returns double array of cube oriented vertices
    static double[] generateCubeVertices(){
        return new double[] {
            //top
            0.5,0.5,0.5,1, 1, 0,//0
            -0.5,0.5,0.5,1, 0, 0,//1
            0.5,0.5,-0.5,1, 1, 1,//2
            -0.5,0.5,-0.5,1, 0, 1,//3

            //front
            0.5,0.5,-0.5,1, 1, 0,//4
            -0.5,0.5,-0.5,1, 0, 0,//5
            0.5, -0.5, -0.5,1, 1, 1,//6
            -0.5,-0.5,-0.5,1, 0, 1,//7

            //right
            0.5,0.5,0.5,1, 1, 0,//8
            0.5,0.5,-0.5,1, 0, 0,//9
            0.5,-0.5,0.5,1, 1, 1,//10
            0.5,-0.5,-0.5,1, 0, 1,//11

            //left
            -0.5,0.5,0.5,1, 0, 0,//12
            -0.5,0.5,-0.5,1, 1, 0,//13
            -0.5,-0.5,0.5,1, 0, 1,//14
            -0.5,-0.5,-0.5,1, 1, 1,//15

            //back
            0.5,0.5,0.5,1, 1, 0,//16
            -0.5,0.5,0.5,1, 0, 0,//17
            0.5,-0.5,0.5,1, 1, 1,//18
            -0.5,-0.5,0.5,1, 0, 1,//19

            //bottom
            0.5,-0.5,0.5,1, 1, 0,//20
            0.5, -0.5, -0.5,1, 1, 1,//21
            -0.5,-0.5, 0.5,1, 0, 0,//22
            -0.5,-0.5,-0.5,1, 0, 1,//23
            
        };
    }
    // Returns int array made up of cube indices
    static int[] generateCubeIndices(boolean flag){
        if(!flag){
            return new int[] {
            //top
                0, 2, 1,
                2, 3, 1,

                //front
                5, 4, 6,
                5, 6, 7,

                //right
                9, 8, 10,
                9, 10, 11,

                //left
                12, 13, 15,
                12, 15, 14,

                //back
                16, 17, 19,
                16, 19, 18,

                //bottom
                20, 22, 23,
                20, 23, 21
            };
        } else {
            return new int[] {
            //top
                0, 1, 2,
                2, 1, 3,

                //front
                5, 6, 4,
                5, 7, 6,

                //right
                9, 10, 8,
                9, 11, 10,

                //left
                12, 15, 13,
                12, 14, 15,

                //back
                16, 19, 17,
                16, 18, 19,

                //bottom
                20, 23, 22,
                20, 21, 23
            };
        }
    }
    static double[] generatePyramidVertices(){
        return new double[] {
            //top
            0,0.5,0,1, 1, 0,//0
            -0,0.5,0,1, 0, 0,//1
            0,0.5,-0,1, 1, 1,//2
            -0,0.5,-0,1, 0, 1,//3

            //front
            0.0,0.5,0.0,1, 1, 0,//4
            0.0,0.5,0.0,1, 0, 0,//5
            0.5, -0.5, -0.5,1, 1, 1,//6
            -0.5,-0.5,-0.5,1, 0, 1,//7

            //right
            0.0,0.5,0.0,1, 1, 0,//8
            0.0,0.5,0.0,1, 0, 0,//9
            0.5,-0.5,0.5,1, 1, 1,//10
            0.5,-0.5,-0.5,1, 0, 1,//11

            //left
            0.0,0.5,0.0,1, 0, 0,//12
            0.0,0.5,0.0,1, 1, 0,//13
            -0.5,-0.5,0.5,1, 0, 1,//14
            -0.5,-0.5,-0.5,1, 1, 1,//15

            //back
            0.0,0.5,0.0,1, 1, 0,//16
            0.0,0.5,0.0,1, 0, 0,//17
            0.5,-0.5,0.5,1, 1, 1,//18
            -0.5,-0.5,0.5,1, 0, 1,//19

            //bottom
            0.5,-0.5,0.5,1, 1, 0,//20
            0.5, -0.5, -0.5,1, 1, 1,//21
            -0.5,-0.5, 0.5,1, 0, 0,//22
            -0.5,-0.5,-0.5,1, 0, 1,//23
            
        };
    }
    // Returns double array of triangle vertices
    static double[] generateTriangleVertices(){
        return new double[] {
            0.5,0.5,-0.5,1, 1, 0,//0
            -0.5,0.5,-0.5,1, 0, 0,//1
            0.5, -0.5, -0.5,1, 1, 1,//2
        };
    }
    // Returns int array of triangle indices
    static int[] generateTriangleIndices(){
        return new int[] {
            0, 2, 1
        };
    }
    // Returns double array of square vertices
    static double[] generateSquareVertices(){
        return new double[] {
            -0.5, -0.5, 0, 1, 1, 0,
            -0.5, 0.5, 0, 1, 0, 0,
            0.5, 0.5, 0, 1, 1, 0,
            0.5, -0.5, 0, 1, 0, 1
        };
    }
    // Returns int array of square indices
    static int[] generateSquareIndices(){
        return new int[] {
            0, 1, 2,
            0, 2, 3
        };
    }

    // Returns double array of new vertices
    // Appends normal and tangent vectors to vertice array
    static double[] bakeNormalVectors(double [] vertices, int[] indices){
        int indicesLength = indices.length;
        int verticesLength = vertices.length;

        // Size is length of vertice array with normal vectors (nx, ny, nz) and
        // tangend vectors (tx, ty, tz) added
        int size = verticesLength + (verticesLength/6)*6;
        // int size = verticesLength + (verticesLength/6)*3;

        double[] newVertices = new double[size];

        int currentPosition = 0;

        // For each triangle calculate normal and tangent vector, then add to new array
        for(int i = 0; i < indicesLength; i+=3){
            // Extract 3 indices (making up triangle)
            int p0 = indices[i]*6;
            int p1 = indices[i+1]*6;
            int p2 = indices[i+2]*6;

            // create indice strides
            // reduce from initial mesh size and expand to new total mesh size
            int np0 = (p0/Entity.INITIAL_MESH_STRIDE)*Entity.LOCAL_MESH_STRIDE;
            int np1 = (p1/Entity.INITIAL_MESH_STRIDE)*Entity.LOCAL_MESH_STRIDE;
            int np2 = (p2/Entity.INITIAL_MESH_STRIDE)*Entity.LOCAL_MESH_STRIDE;

            double x0 = vertices[p0];
            double y0 = vertices[p0+1];
            double z0 = vertices[p0+2];
            double w0 = vertices[p0+3];
            double u0 = vertices[p0+4];
            double v0 = vertices[p0+5];

            double x1 = vertices[p1];
            double y1 = vertices[p1+1];
            double z1 = vertices[p1+2];
            double w1 = vertices[p1+3];
            double u1 = vertices[p1+4];
            double v1 = vertices[p1+5];

            double x2 = vertices[p2];
            double y2 = vertices[p2+1];
            double z2 = vertices[p2+2];
            double w2 = vertices[p2+3];
            double u2 = vertices[p2+4];
            double v2 = vertices[p2+5];

            //Normal vector calculations
            double ax = x1-x0;
            double ay = y1-y0;
            double az = z1-z0;

            double bx = x2-x0;
            double by = y2-y0;
            double bz = z2-z0;

            double nx = (ay * bz) - (az * by);
            double ny = (az * bx) - (ax * bz);
            double nz = (ax * by) - (ay * bx);

            double normLength = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (normLength > 1e-8) {
                nx /= normLength;
                ny /= normLength;
                nz /= normLength;
            } else {
                nx = 0; ny = 0; nz = 1;
            }

            // Tangent vector calculations
            double p1x = x1 - x0;
            double p2x = x2 - x0;

            double p1y = y1 - y0;
            double p2y = y2 - y0;

            double p1z = z1 - z0;
            double p2z = z2 - z0;

            double deltaU1 = u1 - u0;
            double deltaV1 = v1 - v0;

            double deltaU2 = u2 - u0;
            double deltaV2 = v2 - v0;

            double d = 1 / (deltaU1 * deltaV2 - deltaU2 * deltaV1);

            double tx = d * (deltaV2*p1x - deltaV1 *p2x);
            double ty = d * (deltaV2*p1y - deltaV1 *p2y);
            double tz = d * (deltaV2*p1z - deltaV1 *p2z);

            double tanLength = Math.sqrt(tx*tx + ty*ty + tz*tz);
            if(tanLength > 1e-8){
                tx /= tanLength;
                ty /= tanLength;
                tz /= tanLength;
            } else {
                tx = 1; ty = 0; tz = 0;
            }

            // adds already existing vector data, normal vector, and tangent vector to new array
            newVertices[np0] = x0;
            newVertices[np0+1] = y0;
            newVertices[np0+2] = z0;
            newVertices[np0+3] = w0;
            newVertices[np0+4] = u0;
            newVertices[np0+5] = v0;
            newVertices[np0+6] = nx;
            newVertices[np0+7] = ny;
            newVertices[np0+8] = nz;
            newVertices[np0+9] = tx;
            newVertices[np0+10] = ty;
            newVertices[np0+11] = tz;

            newVertices[np1] = x1;
            newVertices[np1+1] = y1;
            newVertices[np1+2] = z1;
            newVertices[np1+3] = w1;
            newVertices[np1+4] = u1;
            newVertices[np1+5] = v1;
            newVertices[np1+6] = nx;
            newVertices[np1+7] = ny;
            newVertices[np1+8] = nz;
            newVertices[np1+9] = tx;
            newVertices[np1+10] = ty;
            newVertices[np1+11] = tz;

            newVertices[np2] = x2;
            newVertices[np2+1] = y2;
            newVertices[np2+2] = z2;
            newVertices[np2+3] = w2;
            newVertices[np2+4] = u2;
            newVertices[np2+5] = v2;
            newVertices[np2+6] = nx;
            newVertices[np2+7] = ny;
            newVertices[np2+8] = nz;
            newVertices[np2+9] = tx;
            newVertices[np2+10] = ty;
            newVertices[np2+11] = tz;
        }
        return newVertices;
    }

    static void initializeRubikCubeTextures(Entity cube){
        Texture tex = new Texture("..\\resources\\assets\\cube-map.jpg");
        // double[] blue = new double[]{0.114, 0.008, 0.21, 0.104};
        // double[] green = new double[]{0.114, 0.346, 0.21, 0.442};
        // double[] red = new double[]{0.114, 0.68, 0.21, 0.776};
        // double[] orange = new double[]{0.45, 0.346, 0.546, 0.442};
        // double[] white = new double[]{0.45, 0.68, 0.546, 0.776};
        // double[] yellow = new double[]{0.788, 0.68, 0.884, 0.776};
         double[] blue = new double[]{0.112, 0.112, 0.215, 0.215};
        double[] green = new double[]{0.112, 0.449, 0.215, 0.553};
        double[] red = new double[]{0.112, 0.784, 0.215, 0.886};
        double[] orange = new double[]{0.448, 0.449, 0.55, 0.553};
        double[] white = new double[]{0.448, 0.784, 0.55, 0.886};
        double[] yellow = new double[]{0.786, 0.784, 0.889, 0.886};
        
        int LEFT_LAYER= 0;
        int MID_LAYER= 1;
        int RIGHT_LAYER = 2;
        applyTextureToChild(cube.children.get(0), tex, 3, yellow, blue, red, LEFT_LAYER);
        applyTextureToChild(cube.children.get(1), tex, 2, yellow, red, null, LEFT_LAYER);
        applyTextureToChild(cube.children.get(2), tex, 3, yellow, red, green, LEFT_LAYER);
        applyTextureToChild(cube.children.get(3), tex, 2, red, blue, null, LEFT_LAYER);
        applyTextureToChild(cube.children.get(4), tex, 1, red, null, null, LEFT_LAYER);
        applyTextureToChild(cube.children.get(5), tex, 2, red, green, null, LEFT_LAYER);
        applyTextureToChild(cube.children.get(6), tex, 3, blue, white, red, LEFT_LAYER);
        applyTextureToChild(cube.children.get(7), tex, 2, red, white, null, LEFT_LAYER);
        applyTextureToChild(cube.children.get(8), tex, 3, red, white, green, LEFT_LAYER);
    
        applyTextureToChild(cube.children.get(9), tex, 2, yellow, blue, null, MID_LAYER);
        applyTextureToChild(cube.children.get(10), tex, 1, yellow, null, null, MID_LAYER);
        applyTextureToChild(cube.children.get(11), tex, 2, yellow, green, null, MID_LAYER);
        applyTextureToChild(cube.children.get(12), tex, 1, blue, null, null, MID_LAYER);
        applyTextureToChild(cube.children.get(13), tex, 1, green, null, null, MID_LAYER);
        applyTextureToChild(cube.children.get(14), tex, 2, blue, white, null, MID_LAYER);
        applyTextureToChild(cube.children.get(15), tex, 1, white, null, null, MID_LAYER);
        applyTextureToChild(cube.children.get(16), tex, 2, green, white, null, MID_LAYER);
        
        applyTextureToChild(cube.children.get(17), tex, 3, yellow, orange, blue, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(18), tex, 2, yellow, orange, null, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(19), tex, 3, yellow, green, orange, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(20), tex, 2, orange, blue, null, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(21), tex, 1, orange, null, null, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(22), tex, 2, orange, green, null, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(23), tex, 3, orange, white, blue, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(24), tex, 2, orange, white, null, RIGHT_LAYER);
        applyTextureToChild(cube.children.get(25), tex, 3, green, white, orange, RIGHT_LAYER);
    }
    static void applyTextureToChild(Entity e, Texture t, int cubeFaces, double [] face1, double[] face2, double[] face3, int layer){
        e.applyTexture(t);
        int frontStride = 4;
        int leftStride = 12;
        int backStride = 16;

        int LEFT_LAYER= 0;
        int MID_LAYER= 1;
        int RIGHT_LAYER = 2;

        int LOCAL_MESH_STRIDE = Entity.LOCAL_MESH_STRIDE;

        if(face1 == null) return;
            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+4] = face1[2];//4
            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+5] = face1[1];

            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+(LOCAL_MESH_STRIDE+4)] = face1[0];//5
            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+(LOCAL_MESH_STRIDE+5)] = face1[1];

            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+(LOCAL_MESH_STRIDE*2+4)] = face1[2];//6
            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+(LOCAL_MESH_STRIDE*2+5)] = face1[3];

            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+(LOCAL_MESH_STRIDE*3+4)] = face1[0];//7
            e.worldVertices[LOCAL_MESH_STRIDE*leftStride+(LOCAL_MESH_STRIDE*3+5)] = face1[3];
        if(cubeFaces >= 2){
            if(face2 == null) return;
            e.worldVertices[4] = face2[2];//4
            e.worldVertices[5] = face2[1];

            e.worldVertices[(LOCAL_MESH_STRIDE+4)] = face2[0];//5
            e.worldVertices[(LOCAL_MESH_STRIDE+5)] = face2[1];

            e.worldVertices[(LOCAL_MESH_STRIDE*2+4)] = face2[2];//6
            e.worldVertices[(LOCAL_MESH_STRIDE*2+5)] = face2[3];

            e.worldVertices[(LOCAL_MESH_STRIDE*3+4)] = face2[0];//7
            e.worldVertices[(LOCAL_MESH_STRIDE*3+5)] = face2[3];
        }
        if(cubeFaces == 3){
            if(face3 == null) return;
            e.worldVertices[LOCAL_MESH_STRIDE*backStride+4] = face3[2];//4
            e.worldVertices[LOCAL_MESH_STRIDE*backStride+5] = face3[1];

            e.worldVertices[LOCAL_MESH_STRIDE*backStride+(LOCAL_MESH_STRIDE+4)] = face3[0];//5
            e.worldVertices[LOCAL_MESH_STRIDE*backStride+(LOCAL_MESH_STRIDE+5)] = face3[1];

            e.worldVertices[LOCAL_MESH_STRIDE*backStride+(LOCAL_MESH_STRIDE*2+4)] = face3[2];//6
            e.worldVertices[LOCAL_MESH_STRIDE*backStride+(LOCAL_MESH_STRIDE*2+5)] = face3[3];

            e.worldVertices[LOCAL_MESH_STRIDE*backStride+(LOCAL_MESH_STRIDE*3+4)] = face3[0];//7
            e.worldVertices[LOCAL_MESH_STRIDE*backStride+(LOCAL_MESH_STRIDE*3+5)] = face3[3];
        }
        if(layer == RIGHT_LAYER) {
            e.rotateyLocal(180);
            if(e.z == 1) e.rotateyLocal(-90);
            if(e.y == -1) e.rotatezLocal(90);
            if(e.y == 0) {
                if(e.z == -1) {
                    e.rotatexLocal(90);
                } else if(e.z == 1){
                    e.rotateyLocal(90);
                    e.rotatexLocal(-90);
                }
            } 
        }
        if(layer == MID_LAYER){
            if(e.z == -1) e.rotateyLocal(-90);
            if(e.z == 1) e.rotateyLocal(90);
            if(e.y == 1 && e.z == 0) e.rotatezLocal(-90);
            if(e.y == -1) e.rotatezLocal(90);
        }
        if(layer == LEFT_LAYER){
            if(e.z == -1) e.rotateyLocal(-90);
            if(e.y == -1) e.rotatezLocal(90);
            if(e.y == 0) {
                if(e.z == 1) {
                    e.rotatexLocal(90);
                } else if(e.z == -1){
                    // rotatexLocal(90).rotatey(-90);
                    e.rotateyLocal(90);
                    e.rotatexLocal(-90);
                }
            } 
        }
    } 
}
