


public class main{
    public static void main(String[] args){

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
}