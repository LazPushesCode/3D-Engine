
import static java.lang.Math.random;
import java.util.HashMap;
import java.util.Random;


public class main{
    public static void main(String[] args){

        Texture valdaTexture = new Texture("..\\resources\\assets\\valda4.png");

        Camera cm = new Camera(70);

        Scene world = new Scene();
        world.addCamera(cm);

        Entity cube = new Entity();
        cube.cubeMesh(false);
        cube.scale(7,7,7);
        cube.translate(0,0,15);
        // cube.applyTexture(valdaTexture);
        
        Entity sphere = new Entity();
        sphere.sphereMesh(10, 10, 1, 1, 0, 0, 0, 0);
        sphere.scale(4, 4,4);
        sphere.translate(0,0,15);

        int pink = 0xFFfa05f6;
        int green = 0x05f205;
        int red = 0xfc0000;
        int cyan = 0x00ebfc;
        int black = 0x121212;
        

        HashMap<Integer, Entity> entityList = new HashMap<>();
        for(int i = 0; i < 5000; i++){
            Entity a = new Entity();
            if(i%2 == 0){
                a.copyEntityValues(cube);   
            } 
            if(i%9 == 0){
                a.copyEntityValues(sphere);
            }
            entityList.put(i, a);
            world.addEntity(a);
            // a.translate(col, row, 0);
            if(i%5==0)a.setDefaultColor(pink);
            if(i%6==0)a.setDefaultColor(green);
        }
        
        int scalevalue = 10000;

        world.addEntity(cm);
        world.activeCamera = cm;

        Light testLight = new Light(0, 0, 0);
        testLight.setDirectional();
        testLight.setIntensity(1);
        world.addLight(testLight);
        // testLight.yaw = 50;
        testLight.pitch = 25;

        // Entity lightCube = new Entity();
        // lightCube.cubeMesh(false);
        // world.addEntity(lightCube);
        // lightCube.rotateyWorld(75);
        // lightCube.rotatexWorld(0);
        // lightCube.setAffectedByLight(false);
        // lightCube.x = testLight.x; lightCube.y = testLight.y; lightCube.z = testLight.z;
        
        world.ambience = 0.1;

        moveObjects(entityList);

        Entity logo = new Entity();
        logo = EffectManager.generateGlassSquare(1000, 100, 24, 0.5, 0.5, 1, 0, 0, 100, 0, 100);
        logo.translate(0, 0, 50);
        world.addEntity(logo);
        logo.scale(40,25,25);
        logo.rotatexLocal(180);
        logo.applyTexture(valdaTexture);
        logo.applyTexturesToChildren(valdaTexture);
        logo.setChildrenMaterialValues(1, 0.5, 1);
        logo.setGreenScreen(true);
        logo.setChildrenGreenScreen(true);
        

        int color = red;
        for(int i = 0; i < logo.children.size(); i++){
            Entity c = logo.children.get(i);
            c.setAffectedByLight(false);
            // c.setDefaultColor(red);
            // logo.children.get(i).useDefaultColorWithTexture = true;
        }

        ParticleSystem stars = new ParticleSystem();
        stars.initializeSystem(0,0,0,1000, 1, 100000000, 100000000,100000000);
        world.addParticleSystem(stars);
        stars.setColor(255, 255, 255);

        Engine eg = new Engine();
        eg.setWindowDefaultColor(0xFF757474);
        eg.enableBilinearTextureFiltering();
        eg.run(world, logo);
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