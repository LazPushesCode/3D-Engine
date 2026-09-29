public class Matrix {
    double[] data;
    double[][] m;
    public Matrix(double[][] a){
        this.m = a;
    }
    public Matrix(double[] d){
        this.data = d;
    }
    public static Matrix identity(){
        return new Matrix(new double[][]{
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        });
    }
    public static Matrix Identity(){
        return new Matrix(new double[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        });
    }
    public static Matrix Translate(double x, double y, double z){
        return new Matrix(new double[]{
            1, 0, 0, x,
            0, 1, 0, y,
            0, 0, 1, z,
            0, 0, 0, 1
        });
    }
    public static Matrix Scale(double x, double y, double z){
        return new Matrix(new double[] {
            x, 0, 0, 0,
            0, y, 0, 0,
            0, 0, z, 0,
            0, 0, 0, 1
        });
    }
    public static Matrix Rotatex(double degree){
        double radian = Math.toRadians(degree);
        double cosRes = Math.cos(radian);
        double sinRes = Math.sin(radian);
         return new Matrix(new double[] {
            1, 0, 0, 0,
            0, cosRes, -(sinRes), 0,
            0, sinRes, cosRes, 0,
            0, 0, 0, 1
        });
    }
    public static Matrix Rotatey(double degree){
        double radian = Math.toRadians(degree);
        double cosRes = Math.cos(radian);
        double sinRes = Math.sin(radian);
         return new Matrix(new double[] {
            cosRes, 0, sinRes, 0,
            0, 1, 0, 0,
            -(sinRes), 0, cosRes, 0,
            0, 0, 0, 1
        });
    }
    public static Matrix Rotatez(double degree){
        double radian = Math.toRadians(degree);
        double cosRes = Math.cos(radian);
        double sinRes = Math.sin(radian);
         return new Matrix(new double[] {
            cosRes, -(sinRes), 0, 0,
            sinRes, cosRes, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        });
    }
    public Matrix multiply(Matrix givenMatrix){
        double [] res = new double [16];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                res[(i*4) + j] = 0;
                for(int k = 0; k < 4; k++){
                    res[(i*4) + j] += this.data[(i*4) + k] * givenMatrix.data[(k * 4) + j];
                }
            }
        }
        return new Matrix(res);
    }
    public double[] vectorTransformation(double[] vector){
        double [] res = new double[4];
        for(int i = 0; i < 4; i++){
            res[i] = 0;
            for(int j = 0; j < 4; j++){
                res[(i)] += this.data[(i*4) + j] * vector[j];
            }
        }
        return res;
    }
    public Matrix transposeMatrix(){
        double[] res = new double[16];
        //(i*4) + j
        //(j*4) + i
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                res[(j*4)+i] = data[(i*4)+j];
            }
        }
        return new Matrix(res);
    }
    public Matrix shortcutInverseMatrix(){
        double tx = this.data[3];
        double ty = this.data[7];
        double tz = this.data[11];
        
        double invTx = -(data[0]*tx + data[4] * ty + data[8] * tz);
        double invTy = -(data[1]*tx + data[5] * ty + data[9] * tz);
        double invTz = -(data[2]*tx + data[6] * ty + data[10] * tz);

        double[] temp = {
            data[0], data[4], data[8], invTx,
            data[1], data[5], data[9], invTy,
            data[2], data[6], data[10], invTz,
            0, 0, 0, 1
        };

        return new Matrix(temp);
    }
    public Matrix multiply2dMatrix(Matrix a){
        double [][] res = new double[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                res[i][j] = 0;
                for(int k = 0; k < 4; k++){
                    res[i][j] += m[i][k] * a.m[k][j];
                }
            }
        }
        return new Matrix(res);
    }
    public static Matrix Rotate(Quaternion q){
        double w = q.data[0];
        double x = q.data[1];
        double y = q.data[2];
        double z = q.data[3];
    
        return new Matrix(new double[] {
            1 - 2*(y*y + z*z),  2*(x*y - z*w),      2*(x*z + y*w),      0,
            2*(x*y + z*w),      1 - 2*(x*x + z*z),  2*(y*z - x*w),      0,
            2*(x*z - y*w),      2*(y*z + x*w),      1 - 2*(x*x + y*y),  0,
            0,                  0,                  0,                  1
        });
    }
    void printMatrix(){
        System.out.println("=========start=========");
        for(int i = 0; i < m.length; i++){
            for(int j = 0; j < m[i].length; j++){
                System.out.print(m[i][j] + " ");
            }
            System.out.println("\n");
        }
        System.out.println("=======================");
    }
}
