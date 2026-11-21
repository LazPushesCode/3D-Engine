
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

        Entity rubiksCube = new Entity();
        int key = 0;
        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                for(int k = -1; k <= 1; k++){
                    if(i == 0 && j == 0 && k == 0) continue;
                    Entity e = new Entity();
                    rubiksCube.addChild(e);
                    e.cubeMesh(false);
                    e.setWorldPosition(i, j, k);
                    e.setMaterialValues(2, 2, 1);
                    key++;
                }
            }
        }
        world.addEntity(rubiksCube);
        MeshGenerator.initializeRubikCubeTextures(rubiksCube);
        rubiksCube.scale(12,12,12);
        rubiksCube.translate(0, 2, 100);
        rubiksCube.rotateyLocal(45);

        Light testLight = new Light(50, 50, 90);
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