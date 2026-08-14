public class Quaternion {
    double[] data = new double[4];

    public Quaternion(){
        this.data[0] = 1;
        this.data[1] = 0;
        this.data[2] = 0;
        this.data[3] = 0;
    }
    public Quaternion(double w, double x, double y, double z){
        this.data[0] = w;
        this.data[1] = x;
        this.data[2] = y;
        this.data[3] = z;
    }
    public Quaternion normalize(){
        double length = Math.sqrt(
            data[0]*data[0] + 
            data[1]*data[1] +
            data[2]*data[2] +
            data[3]*data[3]
        ); 
        if (length == 0) return this;

        data[0] /= length;
        data[1] /= length;
        data[2] /= length;
        data[3] /= length;

        return this;
    }
    public Quaternion multiply(Quaternion q){
        double[] a = this.data;
        double[] b = q.data;
        double w = a[0]*b[0] - a[1]*b[1] - a[2]*b[2] - a[3]*b[3];
        double x = a[0]*b[1] + a[1]*b[0] + a[2]*b[3] - a[3]*b[2];
        double y = a[0]*b[2] - a[1]*b[3] + a[2]*b[0] + a[3]*b[1];
        double z = a[0]*b[3] + a[1]*b[2] - a[2]*b[1] + a[3]*b[0];
        return new Quaternion(w, x, y, z);
    }
    public static Quaternion nextOrientation(double ax, double ay, double az, double degrees){
        double radian = Math.toRadians(degrees) / 2.0;
        double sin = Math.sin(radian);
        double cos = Math.cos(radian);
        return new Quaternion(
            cos,
            ax * sin,
            ay * sin,
            az * sin
        );
    }
}
