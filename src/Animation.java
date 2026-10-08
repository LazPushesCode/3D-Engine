
public class Animation {
    
    
    double[] entityIDs;
    CameraManager cm;

    long prevTime;

    double[] translate;
    double[] rotate;
    double[] scale;
    double[] duration;

    int currentKeyFrame;
    int totalKeyFrames;

    final static int STRIDE = 3;

    boolean lockAnimation;

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
    void addEntity(Entity e){
    }
    void setCameraFocused(CameraManager givenCm){
        cm = givenCm;
    }
    //reallocate buffers
}
