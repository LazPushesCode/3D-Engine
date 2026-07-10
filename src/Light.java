public class Light {

    static final int DIRECTIONAL = 0;
    static final int POINT = 1;
    static final int SPOTLIGHT = 2;

    double x;
    double y;
    double z;
    int type;
    Light(int t, double tx, double ty, double tz){
        type = t;
        x = tx;
        y = ty;
        z = tz;
    }
}
