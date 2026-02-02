import java.util.HashMap;
public class Scene {
    HashMap<Integer, Entity> entities;
    HashMap<Integer, Entity> cameras;
    
    Scene(){
        entities = new HashMap<>();
        cameras = new HashMap<>();
    }
}
