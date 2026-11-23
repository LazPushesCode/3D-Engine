
public class main{
    public static void main(String[] args){

        Camera cm = new Camera(100);

        Scene world = new Scene();
        world.addCamera(cm);

        Entity cube = new Entity();
        cube.cubeMesh(false);
        world.addEntity(cube);
        cube.scale(1,1,1);
        world.addEntity(cm);
        world.activeCamera = cm;

        Entity testcube = new Entity();
        testcube.cubeMesh(false);
        testcube.scale(10,10,10);
        world.addEntity(testcube);
        

        Light testLight = new Light(12, 12, 12);
        testLight.setPoint();
        testLight.setIntensity(5);
        world.addLight(testLight);
        testLight.yaw = 75;
        testLight.pitch = 0;
        cube.rotateyWorld(75);
        cube.rotatexWorld(0);
        cube.x = testLight.x; cube.y = testLight.y; cube.z = testLight.z;
        
        world.ambience = 0.2;

        ParticleSystem stars = new ParticleSystem();
        stars.initializeSystem(0,0,0,10000, 1, 100000, 100000,100000);
        world.addParticleSystem(stars);
        stars.setColor(255, 255, 255);


        Engine eg = new Engine();
        eg.enableBilinearTextureFiltering();
        eg.run(world);
    }
}