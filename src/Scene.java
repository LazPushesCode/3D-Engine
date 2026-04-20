import java.util.HashMap;
public class Scene {
    HashMap<Integer, Entity> entities;
    HashMap<Integer, CameraManager> cameras;

    int entityCount;
    int cameraCount;
    Scene(){
        entities = new HashMap<>();
        cameras = new HashMap<>();
        entityCount = 0;
        cameraCount = 0;
    }
    void addEntity(Entity et){
        entities.put(entityCount, et);
        entityCount++;
    }
    void addCamera(CameraManager cm){
        cameras.put(cameraCount, cm);
        cameraCount++;
    }
}
