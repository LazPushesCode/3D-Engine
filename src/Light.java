public class Light extends Entity{

    static final int DIRECTIONAL = 0; // Light that has no real position, simply casts light from a direction (ie. the sun)
    static final int POINT = 1; // Light with real position, emitting light in all directions (ie. lightbulb)
    static final int SPOTLIGHT = 2; // Light with real position, emitting light in a focused cone beam

    int ID; // Light ID given by scene class

    double xDir, yDir, zDir; // Light direction in 3D space
    double intensity; // How bright the emission of light is
    int type; // Directional, point, or spotlight

    double r, g, b; // Color of light. RGB = (Red, Green, Blue)
    
    // Initializes Light and values.
    // Takes tx, ty, and tz as parameters determining light's 3D world positioning
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
    // Secondary Light constructor to list light type alongside x, y, and z world coordinates
    Light(int giventype, double tx, double ty, double tz){
        type = giventype;
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
    // Sets type as Directional
    void setDirectional(){
        type = DIRECTIONAL;
    }
    // Sets type as Point
    void setPoint(){
        type = POINT;
    }
    // Sets type as Spotlight
    void setSpotlight(){
        type = SPOTLIGHT;
    }
    // Sets given intensity as light's intensity
    void setIntensity(double givenIntensity){
        intensity = givenIntensity;
    }
    // Sets given tr, tg, and tb as light's rgb value\
    // Parameters are in range of 0 to 255
    void setColor(double tr, double tg, double tb){
        // rgb values are converted to range of 0 to 1
        r = tr/255;
        g = tg/255;
        b = tb/255;
    }
    // Rotates light's yaw
    void yawRotation(double degree){
        yaw += degree;
    }
    // Rotates light's pitch
    void pitchRotation(double degree){
        pitch += degree;
    }
    // Applies rotation values on light's direction vector (xDir, yDir, zDir)
    void applyRotation(){
        double yRadians = Math.toRadians(yaw);
        double pRadians = Math.toRadians(pitch);

        xDir = Math.sin(yRadians) * Math.cos(pRadians);
        yDir = Math.sin(pRadians);
        zDir = Math.cos(yRadians) * Math.cos(pRadians);
    }
}
