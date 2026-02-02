public class Matrix {
    double[][] m;
    public Matrix(double[][] a){
        this.m = a;
    }
    public static Matrix Identity(){
        return new Matrix(new double[][]{
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        });
    }
    public static Matrix translate(double x, double y, double z){
        return new Matrix(new double[][]{
            {1, 0, 0, x},
            {0, 1, 0, y},
            {0, 0, 1, z},
            {0, 0, 0, 1}
        });
    } 
    public static Matrix scale(double x, double y, double z){
        return new Matrix(new double[][] {
            {x, 0, 0, 0},
            {0, y, 0, 0},
            {0, 0, z, 0},
            {0, 0, 0, 1}
        });
    } 
    public static Matrix rotatex(double degree){
        double radian = Math.toRadians(degree);
        double cosRes = Math.cos(radian);
        double sinRes = Math.sin(radian);
         return new Matrix(new double[][] {
            {1, 0, 0, 0},
            {0, cosRes, -(sinRes), 0},
            {0, sinRes, cosRes, 0},
            {0, 0, 0, 1}
        });
    }
    public static Matrix rotatey(double degree){
        double radian = Math.toRadians(degree);
        double cosRes = Math.cos(radian);
        double sinRes = Math.sin(radian);
         return new Matrix(new double[][] {
            {cosRes, 0, sinRes, 0},
            {0, 1, 0, 0},
            {-(sinRes), 0, cosRes, 0},
            {0, 0, 0, 1}
        });
    }
    public static Matrix rotatez(double degree){
        double radian = Math.toRadians(degree);
        double cosRes = Math.cos(radian);
        double sinRes = Math.sin(radian);
         return new Matrix(new double[][] {
            {cosRes, -(sinRes), 0, 0},
            {sinRes, cosRes, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        });
    }
    public Matrix multiply(Matrix a){
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
    public double[] vectorTransformation(double [] vector){
        double[] res = new double[vector.length];
        for(int i = 0; i < m.length; i++){
            res[i] = 0;
            for(int j = 0; j < m[i].length; j++){
                res[i] += m[i][j] * vector[j];
            }
        }
        return res;
    }
    void printMatrix(){
        for(int i = 0; i < m.length; i++){
            for(int j = 0; j < m[i].length; j++){
                System.out.print(m[i][j] + " ");
            }
            System.out.println("\n");
        }
    }
}
