
public class main{
    public static void main(String[] args){

        Camera cm = new Camera(100);

        Scene world = new Scene();
        world.addCamera(cm);

        Entity testCube = new Entity();
        testCube.cubeMesh(false);
        testCube.scale(5,5,5);
        testCube.translate(0,0,15);
        world.addEntity(testCube);
        
        int scalevalue = 100;

        Entity floor = new Entity();
        floor.cubeMesh(false);
        floor.scale(scalevalue, 0, scalevalue);
        floor.translate(0, -15, 15);
        world.addEntity(floor);

        world.addEntity(cm);
        world.activeCamera = cm;

        Light testLight = new Light(4, 6, 9);
        testLight.setPoint();
        testLight.setIntensity(2);
        world.addLight(testLight);
        testLight.yaw = 75;
        testLight.pitch = 0;

        Entity lightCube = new Entity();
        lightCube.cubeMesh(false);
        world.addEntity(lightCube);
        lightCube.rotateyWorld(75);
        lightCube.rotatexWorld(0);
        lightCube.setAffectedByLight(false);
        lightCube.x = testLight.x; lightCube.y = testLight.y; lightCube.z = testLight.z;
        
        world.ambience = 0.1;

        ParticleSystem stars = new ParticleSystem();
        stars.initializeSystem(0,0,0,1000, 1, 100000000, 100000000,100000000);
        world.addParticleSystem(stars);
        stars.setColor(255, 255, 255);

        
        // testCube.prioritize = true;

        Engine eg = new Engine();
        eg.setWindowDefaultColor(0xFF757474);
        eg.enableBilinearTextureFiltering();
        eg.run(world);
    }
}