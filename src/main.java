
import static java.lang.Math.random;
import java.util.HashMap;
import java.util.Random;


public class main{
    public static void main(String[] args){

        Texture valdaTexture = new Texture("..\\resources\\assets\\valda4.png");

        Camera cm = new Camera(70);
        cm.translate(-9,2,-9);
        cm.yaw+=45;
        cm.pitch-=15;

        Scene world = new Scene();
        world.addCamera(cm);

        Entity cube = new Entity();
        cube.cubeMesh(false);
        cube.scale(3,3,3);
        cube.translate(4,-4,4);
        world.addEntity(cube);
        cube.setMaterialValues(1, 1, 1);

        Entity sphere = new Entity();
        sphere.sphereMesh(25, 25, 1, 1, 0, 0, 0, 0);
        sphere.translate(-4, -4, 4);
        world.addEntity(sphere);
        sphere.scale(1,1,1);
        sphere.rotatezLocal(-90);

        Entity pyramid = new Entity();
        pyramid.pyramidMesh(false);
        pyramid.translate(4, -4, -4);
        world.addEntity(pyramid);
        pyramid.scale(3,3,3);


        Entity room = new Entity ();
        room.cubeMesh(true);
        room.scale(20, 10, 20);
        world.addEntity(room);

        Light roomLight = new Light(0, 5, 0 );
        roomLight.setPoint();
        roomLight.pitch = -90;
        world.addLight(roomLight);
        roomLight.setIntensity(1);

        Entity lightCube = new Entity();
        lightCube.cubeMesh(false);
        world.addEntity(lightCube);
        lightCube.setAffectedByLight(false);
        lightCube.x = roomLight.x; lightCube.y = roomLight.y; lightCube.z = roomLight.z;
        

        world.addEntity(cm);
        world.activeCamera = cm;
        
        world.ambience = 0.1;

        Engine eg = new Engine();
        eg.setWindowDefaultColor(0xFF757474);
        eg.enableBilinearTextureFiltering();
        eg.run(world);
    }
    static void moveObjects(HashMap<Integer, Entity> entityList){
        Random rand = new Random();
        // int num = rand.nextInt(-100, 101);
        for(int i = 0; i < entityList.size(); i++){
            double theta = random() * 2 * Math.PI;
            double r = Math.max(0.3, random());
            double radius = Math.pow(r, 2);
            double x = radius * Math.cos(theta)*2000;
            double y = radius * Math.sin(theta)*1000;
            double z = random() *500;

            double rx = random()*180;
            double ry = random()*180;
            applyTransformationsOnEntity(entityList, i, x, y, z, rx, ry, 0);
        }

    }
    static void applyTransformationsOnEntity(HashMap<Integer, Entity> list, int i, double tx, double ty, double tz, double rx, double ry, double rz){
        Entity e = list.get(i);
        e.translate(tx, ty, tz);
        e.rotatexLocal(rx);
        e.rotateyLocal(ry);
        e.rotatezLocal(rz);
    }
}