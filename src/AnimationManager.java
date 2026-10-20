













public class AnimationManager {
    
    double[] transformKeyframeData;
    double[] transformBlueprints;

    int[] activeEntities;
    int[] activeEntityMeshes;

    double [] vectorKeyFrameData;
    double [] vectorBlueprints;


    final static int TRANSFORM_KEYFRAME_STRIDE = 12;
    final static int BLUEPRINT_STRIDE = 2;
    final static int MESH_STRIDE = 2;
    final static int VECTOR_KEYFRAME_STRIDE = 6;

    int transformKeyFrameCount;
    int transformMaxKeyFrameCount;

    int vectorKeyFrameCount;
    int vectorMaxKeyFrameCount;

    int transformAnimationCount;
    int maxTransformAnimationCount;

    int vectorAnimationCount;
    int maxVectorAnimationCount;

    int entityCount;
    int maxEntityCount;

    int meshCount;
    int maxMeshCount;

    AnimationManager(int initialKeyFrameCount, int initialtransformAnimationCount){
        initializeArrays(initialKeyFrameCount, initialtransformAnimationCount);
    }
    void initializeArrays(int initialKeyFrameCount, int initialtransformAnimationCount){
        transformKeyframeData = new double[initialKeyFrameCount*TRANSFORM_KEYFRAME_STRIDE];
        transformBlueprints = new double[initialtransformAnimationCount * BLUEPRINT_STRIDE];
        vectorKeyFrameData = new double[initialKeyFrameCount*VECTOR_KEYFRAME_STRIDE];
        vectorBlueprints = new double[initialtransformAnimationCount*BLUEPRINT_STRIDE];
        maxEntityCount = 10;
        maxMeshCount = 5;
        activeEntities = new int[maxEntityCount];
        activeEntityMeshes = new int[maxMeshCount*MESH_STRIDE];
        transformKeyFrameCount = 0;
        vectorKeyFrameCount = 0;
        entityCount = 0;
        meshCount = 0;
        transformMaxKeyFrameCount = initialKeyFrameCount;
        vectorMaxKeyFrameCount = initialKeyFrameCount;
        maxTransformAnimationCount = initialtransformAnimationCount;
        maxVectorAnimationCount = initialtransformAnimationCount;
    }
    void addTransformAnimation(double[] givenKeyFrames){
        int length = givenKeyFrames.length;
        if((length % TRANSFORM_KEYFRAME_STRIDE) != 0 || (length < TRANSFORM_KEYFRAME_STRIDE)){
            System.out.println("Did not meet array size requirements");
            return;
        }
        int keyFramesAdded = length / TRANSFORM_KEYFRAME_STRIDE;
        if((transformKeyFrameCount + keyFramesAdded) > transformMaxKeyFrameCount){
            reallocateTransformKeyFrameMemory();
        }
        if((transformAnimationCount + 1) > maxTransformAnimationCount){
            reallocateTransformBlueprintMemory();
        }
        // int givenKeyFrameCount = givenKeyFrames.length / TRANSFORM_KEYFRAME_STRIDE;
        int startIndex = transformKeyFrameCount * TRANSFORM_KEYFRAME_STRIDE;
        for(int i = startIndex; i < length+ startIndex; i++){
            transformKeyframeData[i] = givenKeyFrames[i - startIndex];
        }
        int blueprintIndex = transformAnimationCount * BLUEPRINT_STRIDE;
        transformBlueprints[blueprintIndex] = startIndex;
        transformBlueprints[blueprintIndex+1] = keyFramesAdded; 
        transformKeyFrameCount += keyFramesAdded;  
        transformAnimationCount++;      
    }
    void addVectorAnimation(double[] givenKeyFrames){
        int length = givenKeyFrames.length;
        if((length % VECTOR_KEYFRAME_STRIDE) != 0 || (length < VECTOR_KEYFRAME_STRIDE)){
            return;
        }
        int keyFramesAdded = length / VECTOR_KEYFRAME_STRIDE;
        while((vectorKeyFrameCount + keyFramesAdded) > vectorMaxKeyFrameCount){
            reallocateVectorKeyFrameMemory();
        }
        while((vectorAnimationCount+1) > maxVectorAnimationCount){
            reallocateVectorBlueprintMemory();
        }
        int startIndex = vectorKeyFrameCount * VECTOR_KEYFRAME_STRIDE;
        for(int i = startIndex; i < length+startIndex; i++){
            vectorKeyFrameData[i] = givenKeyFrames[i-startIndex];
        }
        int blueprintIndex = vectorAnimationCount * BLUEPRINT_STRIDE;
        vectorBlueprints[blueprintIndex] = startIndex;
        vectorBlueprints[blueprintIndex + 1] = keyFramesAdded; 
        vectorKeyFrameCount += keyFramesAdded;
        vectorAnimationCount++;
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
    void applyAnimationOnMesh(Entity e, int trackIndex, int animationID, double startTime){
        while(meshCount >= maxMeshCount){
            reallocateMeshMemory();
        }
        activeEntityMeshes[meshCount*MESH_STRIDE] = e.ID;
        activeEntityMeshes[meshCount*MESH_STRIDE+1] = trackIndex;
        meshCount++;
        int index = trackIndex * Entity.TRACK_STRIDE;
        e.trackData[index] = animationID;
        
        e.trackData[index+1] = 0;

        int durationIndex = (int)vectorBlueprints[animationID*BLUEPRINT_STRIDE];
        double duration = vectorKeyFrameData[durationIndex+3];

        double remainingTime = startTime;
        int startFrame = 0;
        while(remainingTime >= duration){
            startFrame++;
            remainingTime -= duration;
        }
        e.trackData[index+1] = startFrame;
        e.trackPrevTimes[index]= remainingTime;
        setMeshStartPosition(e, trackIndex, animationID, remainingTime, e.trackData[index+1], e.trackData[index+2], e.trackData[index+3]);
    }
    void setMeshStartPosition(Entity e, int trackIndex, int animationID, double startTime, int frameStart, int offset, int vertCount){
        int index = (int)vectorBlueprints[animationID*BLUEPRINT_STRIDE];
        double duration = vectorKeyFrameData[index+3];
        double elapsedTime = (startTime / duration);
        double xInterpolated = 0, yInterpolated = 0, zInterpolated = 0;
        for(int i = 0; i < frameStart;i++){
            int dataOffset = index+i*VECTOR_KEYFRAME_STRIDE;
            xInterpolated += vectorKeyFrameData[dataOffset];
            yInterpolated += vectorKeyFrameData[dataOffset+1];
            zInterpolated += vectorKeyFrameData[dataOffset+2];
        }

        // System.out.println("elapsed time: " + startTime  + " / " + duration);
        
        xInterpolated += elapsedTime * vectorKeyFrameData[index+frameStart *VECTOR_KEYFRAME_STRIDE];
        yInterpolated += elapsedTime * vectorKeyFrameData[index+frameStart *VECTOR_KEYFRAME_STRIDE+1];
        zInterpolated += elapsedTime * vectorKeyFrameData[index+frameStart *VECTOR_KEYFRAME_STRIDE+2];
        for(int i = 0; i < vertCount; i++){
            int indiceOffset = (e.trackIndices[i+offset] * Entity.LOCAL_MESH_STRIDE);
            double xBase = e.staticVertices[indiceOffset];
            double yBase = e.staticVertices[indiceOffset+1];
            double zBase = e.staticVertices[indiceOffset+2];
            e.worldVertices[indiceOffset] = xBase+ xInterpolated;
            e.worldVertices[indiceOffset+1] = yBase+ yInterpolated;
            e.worldVertices[indiceOffset+2] = zBase+ zInterpolated;
        }
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
    void removeVertice(Scene s, Entity e,int meshToRemove){
        if(meshCount == 0) return;
        int index = meshToRemove *MESH_STRIDE;
        if(meshToRemove == meshCount-1){
            activeEntityMeshes[index] = 0;
            activeEntityMeshes[index+1] = 0;
            meshCount--;
            return;
        }
        int maxMeshIndex = (meshCount-1) * MESH_STRIDE;
        activeEntityMeshes[index] = activeEntityMeshes[maxMeshIndex];
        activeEntityMeshes[index+1] = activeEntityMeshes[maxMeshIndex+1];
        activeEntityMeshes[maxMeshIndex] = 0;
        activeEntityMeshes[maxMeshIndex+1] = 0;
        meshCount--;
    }
    void reallocateTransformKeyFrameMemory(){
        transformMaxKeyFrameCount *=4;
        double[] newtransformKeyframeData = new double[transformMaxKeyFrameCount*TRANSFORM_KEYFRAME_STRIDE];
        System.arraycopy(transformKeyframeData, 0, newtransformKeyframeData, 0, transformKeyframeData.length);
        transformKeyframeData = newtransformKeyframeData;
    }
     void reallocateTransformBlueprintMemory(){
        maxTransformAnimationCount *= 4;
        double[] newtransformBlueprints = new double[maxTransformAnimationCount*BLUEPRINT_STRIDE];
        System.arraycopy(transformBlueprints, 0, newtransformBlueprints, 0, transformBlueprints.length);
        transformBlueprints = newtransformBlueprints;
     }
     void reallocateVectorKeyFrameMemory(){
        vectorMaxKeyFrameCount *= 4;
        double [] newvectorKeyFrameData = new double[vectorMaxKeyFrameCount*VECTOR_KEYFRAME_STRIDE];
        System.arraycopy(vectorKeyFrameData, 0, newvectorKeyFrameData, 0, vectorKeyFrameData.length);
        vectorKeyFrameData = newvectorKeyFrameData;
     }
     void reallocateVectorBlueprintMemory(){
        maxVectorAnimationCount *= 4;
        double[] newvectorBlueprints = new double[maxVectorAnimationCount*BLUEPRINT_STRIDE];
        System.arraycopy(transformBlueprints, 0, newvectorBlueprints, 0, vectorBlueprints.length);
        transformBlueprints = newvectorBlueprints;
     }

     void reallocateEntityMemory(){
        maxEntityCount *= 10;
        int[] newActiveEntities = new int[maxEntityCount*MESH_STRIDE];
        System.arraycopy(activeEntities, 0, newActiveEntities, 0, activeEntities.length);
        activeEntities = newActiveEntities;
    }
    void reallocateMeshMemory(){
        maxMeshCount *= 4;
        int[] newActiveMeshes = new int[maxMeshCount*MESH_STRIDE];
        System.arraycopy(activeEntityMeshes, 0,
             newActiveMeshes, 0, activeEntityMeshes.length);
        activeEntityMeshes = newActiveMeshes;
    }
    void addKeyFrame(int tx, double ty, double tz,
        double rx, double ry, double rz,
        double sx, double sy, double sz,
        double duration, double type, double tension
    ){
        if(transformKeyframeData == null) return;
        int index = transformKeyFrameCount * TRANSFORM_KEYFRAME_STRIDE;
        transformKeyframeData[index] = tx;
        transformKeyframeData[index + 1] = ty;
        transformKeyframeData[index + 2] = tz;
        transformKeyframeData[index+3] = rx;
        transformKeyframeData[index+4] = ry;
        transformKeyframeData[index+5] = rz;
        transformKeyframeData[index+6] = sx;
        transformKeyframeData[index+7] = sy;
        transformKeyframeData[index+8] = sz; 
        transformKeyframeData[index+9] = duration;
        transformKeyframeData[index+10] = type;
        transformKeyframeData[index+11] = tension;
    }
    
    void playAnimations(Scene s, long currTime){
        for(int i = 0; i < entityCount; i++){
            Entity e = s.entities.get(activeEntities[i]);
            boolean isFinishedAnimating = evaluateTransformAnimation(e, currTime);
            if(isFinishedAnimating){
                removeEntity(s, e);
            }
        }
        for(int i = 0; i < meshCount; i++){
            int index = i * MESH_STRIDE;
            Entity e = s.entities.get(activeEntityMeshes[index]);
            boolean isFinishedAnimating = evaluateVectorAnimation(s, e, activeEntityMeshes[index+1]*e.TRACK_STRIDE, currTime);
            if(isFinishedAnimating){
                removeVertice(s, e, i);
                //do some
            }
        }
    }
    boolean evaluateVectorAnimation(Scene s, Entity e, int trackIndex, double deltaTime){
        int animationID = e.trackData[trackIndex];
        int currentFrame = e.trackData[trackIndex+1];
        int offset = e.trackData[trackIndex+2];
        int vertCount = e.trackData[trackIndex+3];
        // System.out.println(Arrays.toString(e.trackIndices));
        // System.out.println(Arrays.toString(e.trackData));
        
        double startIndex = vectorBlueprints[animationID*BLUEPRINT_STRIDE];
        double totalKeyFrames = vectorBlueprints[animationID*BLUEPRINT_STRIDE+1];

        if(currentFrame >= totalKeyFrames){
            return true;
        }
        int index = (int)startIndex + (currentFrame*VECTOR_KEYFRAME_STRIDE);
        double duration = vectorKeyFrameData[index+3];
        double prevTime = e.trackPrevTimes[trackIndex];
        prevTime += deltaTime;
        if(prevTime >= duration){
            prevTime -= duration;
            currentFrame++;
            if(currentFrame >= totalKeyFrames){
                for(int i = 0 ;i < vertCount; i++){
                    int indiceOffset = e.trackIndices[i+offset]*Entity.LOCAL_MESH_STRIDE;
                    // System.out.println((duration * vectorKeyFrameData[index+1]) + " " + e.staticVertices[indiceOffset+1]);
                    e.worldVertices[indiceOffset] = e.staticVertices[indiceOffset];
                    e.worldVertices[indiceOffset+1] = e.staticVertices[indiceOffset+1];
                    e.worldVertices[indiceOffset+2] = e.staticVertices[indiceOffset+2];
                }
                return true;
            }
            index = (int)startIndex + (currentFrame*VECTOR_KEYFRAME_STRIDE);
            duration = vectorKeyFrameData[index+3];
        }
        double xInterpolated = 0;
        double yInterpolated = 0;
        double zInterpolated = 0;
        double elapsedTime = ((deltaTime) / duration);
        // System.out.println("prevTime: " + prevTime + " deltaTime: " + deltaTime +  " elapsedTime: " + elapsedTime + " alt: " + (prevTime / duration) +" duration: " + duration);
        switch((int)vectorKeyFrameData[index+4]){
            case 0 -> {
                xInterpolated = elapsedTime * vectorKeyFrameData[index];
                yInterpolated = elapsedTime * vectorKeyFrameData[index+1];
                zInterpolated = elapsedTime * vectorKeyFrameData[index+2];
                for(int i = 0 ;i < vertCount; i++){
                    int indiceOffset = e.trackIndices[i+offset]*Entity.LOCAL_MESH_STRIDE;
                    double xBase = e.worldVertices[indiceOffset];
                    double yBase = e.worldVertices[indiceOffset+1];
                    double zBase = e.worldVertices[indiceOffset+2];
                    
                    e.worldVertices[indiceOffset] = xBase + xInterpolated;
                    e.worldVertices[indiceOffset+1] = yBase + yInterpolated;
                    e.worldVertices[indiceOffset+2] = zBase + zInterpolated;
                }
            }
            case 1 -> {
                double u = (prevTime / duration);
                double r = vectorKeyFrameData[index+5];
                double u2 = u * u;
                double u3 = u2 * u;
                double b0 = -(r)*u + 2* r * u2 - (r)*u3;
                double b1 = 1 + (r-3)*u2 + (2-r)*u3;
                double b2 = r*u + (3 - 2*r)*u2 + (r - 2)*u3;
                double b3 = -r*u2 + r*u3;

                int i1 = currentFrame;
                int i2 = Math.min((int)totalKeyFrames - 1, i1+1);
                int i0 = Math.max(0, i1-1);
                int i3 = Math.min((int)totalKeyFrames -1, i2+1);

                i0 = (int)startIndex + (i0*VECTOR_KEYFRAME_STRIDE);
                i1 = (int)startIndex + (i1*VECTOR_KEYFRAME_STRIDE);
                i2 = (int)startIndex + (i2*VECTOR_KEYFRAME_STRIDE);
                i3 = (int)startIndex + (i3*VECTOR_KEYFRAME_STRIDE);
                
                
                xInterpolated = (b0 * vectorKeyFrameData[i0] + b1 * vectorKeyFrameData[i1] + b2 * vectorKeyFrameData[i2] + b3*vectorKeyFrameData[i3]);
                yInterpolated = ( b0 * vectorKeyFrameData[i0+1] + b1 * vectorKeyFrameData[i1+1] + b2 * vectorKeyFrameData[i2+1] + b3*vectorKeyFrameData[i3+1]);
                zInterpolated = (b0 * vectorKeyFrameData[i0+2] + b1 * vectorKeyFrameData[i1+2] + b2 * vectorKeyFrameData[i2+2] + b3*vectorKeyFrameData[i3+2]);
                for(int i = 0; i < vertCount; i++){
                    int indiceOffset = e.trackIndices[i]*Entity.LOCAL_MESH_STRIDE;
                    e.worldVertices[indiceOffset] =   xInterpolated;
                    e.worldVertices[indiceOffset+1] =   yInterpolated;
                    e.worldVertices[indiceOffset+2] =   zInterpolated;
                }
            }
            default -> {
            }
        }
        e.trackData[trackIndex+1] = currentFrame;
        e.trackPrevTimes[trackIndex] = prevTime;

        return false;
    }
    boolean evaluateTransformAnimation(Entity e, double deltaTime){
        double startIndex = transformBlueprints[e.animationID*BLUEPRINT_STRIDE];
        double totalKeyFrames = transformBlueprints[e.animationID*BLUEPRINT_STRIDE+1];
        if(e.currentKeyFrame >= totalKeyFrames){
            return true;
        }

        int index = (int)startIndex + (e.currentKeyFrame*TRANSFORM_KEYFRAME_STRIDE);
        double duration = transformKeyframeData[index+9];
        e.prevTime += deltaTime;
        if(e.prevTime >= duration){
            e.prevTime -= duration;
            e.currentKeyFrame++;
            if(e.currentKeyFrame >= totalKeyFrames-1){
                return true;
            }
            index = (int)startIndex + (e.currentKeyFrame*TRANSFORM_KEYFRAME_STRIDE);
            duration = transformKeyframeData[index+9];
        }
        double xInterpolated = 0;
        double yInterpolated = 0;
        double zInterpolated = 0;
        double elapsedTime = ((deltaTime) / duration);
        switch((int)transformKeyframeData[index+10]){
            case 0 -> {
                xInterpolated = elapsedTime * transformKeyframeData[index];
                yInterpolated = elapsedTime * transformKeyframeData[index+1];
                zInterpolated = elapsedTime * transformKeyframeData[index+2];
                e.translate(xInterpolated, yInterpolated, zInterpolated);
                e.rotatexWorld(elapsedTime * transformKeyframeData[index+3]);
                e.rotateyWorld(elapsedTime * transformKeyframeData[index+4]);
                e.rotatezWorld(elapsedTime * transformKeyframeData[index+5]);
            }
            case 1 -> {
                double u = (e.prevTime / duration);
                double r = transformKeyframeData[index+11];
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

                i0 = (int)startIndex + (i0*TRANSFORM_KEYFRAME_STRIDE);
                i1 = (int)startIndex + (i1*TRANSFORM_KEYFRAME_STRIDE);
                i2 = (int)startIndex + (i2*TRANSFORM_KEYFRAME_STRIDE);
                i3 = (int)startIndex + (i3*TRANSFORM_KEYFRAME_STRIDE);
                
                xInterpolated = (b0 * transformKeyframeData[i0] + b1 * transformKeyframeData[i1] + b2 * transformKeyframeData[i2] + b3*transformKeyframeData[i3]);
                yInterpolated = ( b0 * transformKeyframeData[i0+1] + b1 * transformKeyframeData[i1+1] + b2 * transformKeyframeData[i2+1] + b3*transformKeyframeData[i3+1]);
                zInterpolated = (b0 * transformKeyframeData[i0+2] + b1 * transformKeyframeData[i1+2] + b2 * transformKeyframeData[i2+2] + b3*transformKeyframeData[i3+2]);
                e.setWorldPosition(xInterpolated, yInterpolated,zInterpolated);
                e.rotatexWorld(elapsedTime * transformKeyframeData[index+3]);
                e.rotateyWorld(elapsedTime * transformKeyframeData[index+4]);
                e.rotatezWorld(elapsedTime * transformKeyframeData[index+5]);
            }
            default -> {
            }
        }
        // e.translate(xInterpolated-e.x, yInterpolated - e.y, zInterpolated - e.z);
        // e.rotatexWorld(elapsedTime * transformKeyframeData[index+3]);
        // e.rotateyWorld(elapsedTime * transformKeyframeData[index+4]);
        // e.rotatezWorld(elapsedTime * transformKeyframeData[index+5]);
        return false;
    }
}
