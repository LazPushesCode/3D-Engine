
public class Animation {
    
    double[] entityIDs; // Contains entities currently with animation timeline being applied to them
    Camera cm; // Focused camera

    long prevTime; // Tracks number of seconds that have passed

    // Contain all transformation data in separate arrays
    double[] translate;
    double[] rotate;
    double[] scale;
    double[] duration;

    int currentKeyFrame; // Determines current frame to animate (frames have their own set of transformation data)
    int totalKeyFrames;

    final static int STRIDE = 3; // Number of values in each transformation type (ie. x, y, and z)

    boolean lockAnimation; // If true, prevents the animation from continuing

    Animation(){
        translate = new double[STRIDE*10];
        rotate = new double[STRIDE*10];
        scale = new double[STRIDE*10];
        duration = new double[10];
        entityIDs = new double[5];
        prevTime = 0;
        currentKeyFrame = 0;

        lockAnimation = false;
    }
    // Initializes all frame values
    void createKeyFrame(double tx, double ty, double tz, 
        double rx, double ry, double rz, 
        double sx, double sy, double sz,
        double keyFrameDuration){
            int index = totalKeyFrames * STRIDE;
            translate[index] = tx;
            translate[index+1] = ty;
            translate[index+2] = tz;
            rotate[index] = rx;
            rotate[index+1] = ry;
            rotate[index+2] = rz;
            scale[index] = sx;
            scale[index+1] = sy;
            scale[index+2] = sz;
            duration[totalKeyFrames] = keyFrameDuration;
            totalKeyFrames++;
            
    }
    
    void playAnimation(Entity e, long currTime){
         if(currTime > duration[currentKeyFrame]){
            currentKeyFrame++;
        }
        if(currentKeyFrame >= totalKeyFrames) {
            lockAnimation = true;
            return;
        }
        int index = currentKeyFrame * STRIDE;
        double elapsedTime = ((currTime - prevTime) / duration[currentKeyFrame]);
        e.translate(elapsedTime*translate[index], elapsedTime * translate[index+1],elapsedTime*translate[index+2]);
        e.rotatexLocal(elapsedTime*rotate[index]);
        e.rotateyWorld(elapsedTime*rotate[index+1]);
        e.rotatezWorld(elapsedTime*rotate[index+2]);
        prevTime = currTime;
    }
    void setCameraFocused(Camera givenCm){
        cm = givenCm;
    }
}
