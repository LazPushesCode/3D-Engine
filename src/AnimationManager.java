

public class AnimationManager {
    
    double[] keyFrameData;
    double[] blueprints;
    int[] activeEntities;

    final static int KEYFRAME_STRIDE = 12;
    final static int BLUEPRINT_STRIDE = 2;

    int keyFrameCount;
    int maxKeyFrameCount;

    int animationCount;
    int maxAnimationCount;

    int entityCount;
    int maxEntityCount;

    AnimationManager(int initialKeyFrameCount, int initialAnimationCount){
        initializeArrays(initialKeyFrameCount, initialAnimationCount);
    }
    void initializeArrays(int initialKeyFrameCount, int initialAnimationCount){
        keyFrameData = new double[initialKeyFrameCount*KEYFRAME_STRIDE];
        blueprints = new double[initialAnimationCount * BLUEPRINT_STRIDE];
        maxEntityCount = 10;
        activeEntities = new int[maxEntityCount];
        keyFrameCount = 0;
        entityCount = 0;
        maxKeyFrameCount = initialKeyFrameCount;
        maxAnimationCount = initialAnimationCount;
    }
    void addAnimation(double[] givenKeyFrames){
        int length = givenKeyFrames.length;
        if((length % KEYFRAME_STRIDE) != 0 || (length < KEYFRAME_STRIDE)){
            System.out.println("Did not meet array size requirements");
        }
        int keyFramesAdded = length / KEYFRAME_STRIDE;
        if((keyFrameCount + keyFramesAdded) > maxKeyFrameCount){
            reallocateKeyFrameMemory();
        }
        if((animationCount + 1) > maxAnimationCount){
            reallocateBlueprintMemory();
        }
        // int givenKeyFrameCount = givenKeyFrames.length / KEYFRAME_STRIDE;
        int startIndex = keyFrameCount * KEYFRAME_STRIDE;
        for(int i = startIndex; i < length+ startIndex; i++){
            keyFrameData[i] = givenKeyFrames[i - startIndex];
        }
        int blueprintIndex = animationCount * BLUEPRINT_STRIDE;
        blueprints[blueprintIndex] = startIndex;
        blueprints[blueprintIndex+1] = keyFramesAdded; 
        keyFrameCount += keyFramesAdded;  
        animationCount++;      
    }
    void applyAnimationOnEntity(Entity e, int animationID){
        if(entityCount >= maxEntityCount){
            reallocateEntityMemory();
        }
        activeEntities[entityCount] = e.ID;
        entityCount++;
        e.indexID = entityCount-1;
        e.animationID = animationID;
        e.prevTime = 0;
        e.currentKeyFrame = 0;
        
    }
    void addEntity(Entity e){
        if(entityCount >= maxEntityCount){
            reallocateEntityMemory();
        }
        activeEntities[entityCount] = e.ID;
        entityCount++;
        e.indexID = entityCount-1;
    }
    void removeEntity(Scene s, Entity e){
        int index = e.indexID;
        Entity e2 = s.entities.get(activeEntities[entityCount-1]);
        if(entityCount == 0)return;
        if(index == entityCount-1){
            activeEntities[index] = 0;
            entityCount--;
            return;
        }
        activeEntities[index] = activeEntities[entityCount-1];
        e2.indexID = index;
        activeEntities[entityCount-1] = 0;
        entityCount--;
    }
    void reallocateKeyFrameMemory(){
        maxKeyFrameCount *=4;
        double[] newKeyFrameData = new double[maxKeyFrameCount*KEYFRAME_STRIDE];
        System.arraycopy(keyFrameData, 0, newKeyFrameData, 0, keyFrameData.length);
        keyFrameData = newKeyFrameData;
    }
     void reallocateBlueprintMemory(){
        maxAnimationCount *= 4;
        double[] newBlueprints = new double[maxAnimationCount*BLUEPRINT_STRIDE];
        System.arraycopy(blueprints, 0, newBlueprints, 0, blueprints.length);
        blueprints = newBlueprints;
     }
     void reallocateEntityMemory(){
        maxEntityCount *= 10;
        int[] newActiveEntities = new int[maxEntityCount];
        System.arraycopy(activeEntities, 0, newActiveEntities, 0, activeEntities.length);
        activeEntities = newActiveEntities;
    }
    void addKeyFrame(int tx, double ty, double tz,
        double rx, double ry, double rz,
        double sx, double sy, double sz,
        double duration, double type, double tension
    ){
        if(keyFrameData == null) return;
        int index = keyFrameCount * KEYFRAME_STRIDE;
        keyFrameData[index] = tx;
        keyFrameData[index + 1] = ty;
        keyFrameData[index + 2] = tz;
        keyFrameData[index+3] = rx;
        keyFrameData[index+4] = ry;
        keyFrameData[index+5] = rz;
        keyFrameData[index+6] = sx;
        keyFrameData[index+7] = sy;
        keyFrameData[index+8] = sz; 
        keyFrameData[index+9] = duration;
        keyFrameData[index+10] = type;
        keyFrameData[index+11] = tension;
    }
    
    void playAnimations(Scene s, long currTime){
        for(int i = 0; i < entityCount; i++){
            Entity e = s.entities.get(activeEntities[i]);
            boolean isFinishedAnimating = evaluateAnimation(e, currTime);
            if(isFinishedAnimating){
                removeEntity(s, e);
            }
        }
    }
    boolean evaluateAnimation(Entity e, double deltaTime){
        double startIndex = blueprints[e.animationID*BLUEPRINT_STRIDE];
        double totalKeyFrames = blueprints[e.animationID*BLUEPRINT_STRIDE+1];
        if(e.currentKeyFrame >= totalKeyFrames){
            System.out.println("done");
            return true;
        }

        int index = (int)startIndex + (e.currentKeyFrame*KEYFRAME_STRIDE);
        double duration = keyFrameData[index+9];
        e.prevTime += deltaTime;
        if(e.prevTime >= duration){
            e.prevTime -= duration;
            e.currentKeyFrame++;
            if(e.currentKeyFrame >= totalKeyFrames-1){
                return true;
            }
            index = (int)startIndex + (e.currentKeyFrame*KEYFRAME_STRIDE);
            duration = keyFrameData[index+9];
        }
        double xInterpolated = 0;
        double yInterpolated = 0;
        double zInterpolated = 0;
        double elapsedTime = ((deltaTime) / duration);
        switch((int)keyFrameData[index+10]){
            case 0 -> {
                xInterpolated = elapsedTime * keyFrameData[index];
                yInterpolated = elapsedTime * keyFrameData[index+1];
                zInterpolated = elapsedTime * keyFrameData[index+2];
                e.translate(xInterpolated-e.x, yInterpolated - e.y, zInterpolated - e.z);
                e.rotatexWorld(elapsedTime * keyFrameData[index+3]);
                e.rotateyWorld(elapsedTime * keyFrameData[index+4]);
                e.rotatezWorld(elapsedTime * keyFrameData[index+5]);
            }
            case 1 -> {
                double u = (e.prevTime / duration);
                double r = keyFrameData[index+11];
                double u2 = u * u;
                double u3 = u2 * u;
                double b0 = -(r)*u + 2* r * u2 - (r)*u3;
                double b1 = 1 + (r-3)*u2 + (2-r)*u3;
                double b2 = r*u + (3 - 2*r)*u2 + (r - 2)*u3;
                double b3 = -r*u2 + r*u3;

                int i1 = e.currentKeyFrame;
                int i2 = Math.min((int)totalKeyFrames - 1, i1+1);
                int i0 = Math.max(0, i1-1);
                int i3 = Math.min((int)totalKeyFrames -1, i2+1);

                i0 = (int)startIndex + (i0*KEYFRAME_STRIDE);
                i1 = (int)startIndex + (i1*KEYFRAME_STRIDE);
                i2 = (int)startIndex + (i2*KEYFRAME_STRIDE);
                i3 = (int)startIndex + (i3*KEYFRAME_STRIDE);
                
                
                xInterpolated = (b0 * keyFrameData[i0] + b1 * keyFrameData[i1] + b2 * keyFrameData[i2] + b3*keyFrameData[i3]);
                yInterpolated = ( b0 * keyFrameData[i0+1] + b1 * keyFrameData[i1+1] + b2 * keyFrameData[i2+1] + b3*keyFrameData[i3+1]);
                zInterpolated = (b0 * keyFrameData[i0+2] + b1 * keyFrameData[i1+2] + b2 * keyFrameData[i2+2] + b3*keyFrameData[i3+2]);
                e.setWorldPosition(xInterpolated, yInterpolated,zInterpolated);
                e.rotatexWorld(elapsedTime * keyFrameData[index+3]);
                e.rotateyWorld(elapsedTime * keyFrameData[index+4]);
                e.rotatezWorld(elapsedTime * keyFrameData[index+5]);
            }
            default -> {
            }
        }
        // e.translate(xInterpolated-e.x, yInterpolated - e.y, zInterpolated - e.z);
        // e.rotatexWorld(elapsedTime * keyFrameData[index+3]);
        // e.rotateyWorld(elapsedTime * keyFrameData[index+4]);
        // e.rotatezWorld(elapsedTime * keyFrameData[index+5]);
        return false;
    }
}
