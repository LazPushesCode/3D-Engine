public class Light {

    static final int DIRECTIONAL = 0;
    static final int POINT = 1;
    static final int SPOTLIGHT = 2;

    int ID;

    double x, y, z;
    double yaw, pitch;
    double xDir, yDir, zDir;
    double intensity;
    int type;

    double r, g, b;
    

    Light(double tx, double ty, double tz){
        type = POINT;
        x = tx;
        y = ty;
        z = tz;
        yaw = 0;
        pitch = 0;
        intensity = 1;
        r = 1;
        g = 1;
        b = 1;
    }
    Light(int giventype, double tx, double ty, double tz){
        type = giventype;
        x = tx;
        y = ty;
        z = tz;
        yaw = 0;
        pitch = 0;
        intensity = 1;
    }
    void setDirectional(){
        type = DIRECTIONAL;
    }
    void setPoint(){
        type = POINT;
    }
    void setSpotlight(){
        type = SPOTLIGHT;
    }
    void setIntensity(double givenIntensity){
        intensity = givenIntensity;
    }
    void setColor(double tr, double tg, double tb){
        r = tr;
        g = tg;
        b = tb;
    }
    void yawRotation(double degree){
        yaw += degree;
    }
    void pitchRotation(double degree){
        pitch += degree;
    }
    void applyRotation(){
        double yRadians = Math.toRadians(yaw);
        double pRadians = Math.toRadians(pitch);

        xDir = Math.sin(yRadians) * Math.cos(pRadians);
        yDir = Math.sin(pRadians);
        zDir = Math.cos(yRadians) * Math.cos(pRadians);
    }
    void translate(double tx, double ty, double tz){
        x += tx; y += ty; z += tz;
    }
}
