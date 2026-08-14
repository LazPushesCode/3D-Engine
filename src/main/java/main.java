
import static java.lang.Math.random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main{
    public static void main(String[] args){

        int cores = Runtime.getRuntime().availableProcessors();

        WindowManager wm = new WindowManager(1920, 1080, cores);
        CameraManager cm = new CameraManager(wm.width, wm.length,100);
        InputManager im = new InputManager();

        Texture grassblock = new Texture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        Texture temp = new Texture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\L.png");

        cm.setCameraPosition(0, 0, -2);
        
        wm.openWindow();
        wm.addInputListener(im);

        VideoDecoder vd = new VideoDecoder("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\brazil.mp4");
        
        Scene world = new Scene();
        world.addCamera(cm);

        Entity test = Entity.generateFlat3DShape(new double[]{
            -0.2, -0.2, -0.5, 1, 0, 0,
            -0.5, 0.5, -0.5, 1, 0, 0,
            0.5, 0.5, -0.5, 1, 0, 0,
            0.5, -0.5, -0.5, 1, 0, 0,
            -0.5, -0.5, 0.5, 1, 0, 0,
            -0.5, 0.5, 0.5, 1, 0, 0,
            0.5, 0.5, 0.5, 1, 0, 0,
            0.5, -0.5, 0.5, 1, 0, 0,
        }, 4);
        test.applyTexture(temp);
        world.addEntity(test);
        test.rotatexWorld(180);
        
        Light testLight = new Light(0, 2, 0);
        testLight.setPoint();
         testLight.yawRotation(90);
        world.addLight(testLight);
        testLight.setIntensity(3);
        testLight.applyRotation();

        world.ambience = 0.5;

        Entity glassSphere = EffectManager.generateGlassSphere(1000, 100);
        
        glassSphere.scale(10,10,10);
        glassSphere.translate(10,0,0);
        world.addEntityChildren(glassSphere);
        double iterator = 0;
        for(Entity e : glassSphere.children.values()){
            e.applyTexture(temp);
            // e.translate(iterator, 0, iterator);
            // iterator += 0.001;
        }
        
        
        
        // Light testLight2 = new Light(0, -2, 0);
        // testLight2.setSpotlight();
        // testLight2.yawRotation(-90);
        // world.addLight(testLight2);
        // testLight2.setIntensity(2);
        // testLight2.applyRotation();
        
        
        
        Entity sphere = new Entity();
        sphere.sphereMesh(25, 25);
        world.addEntity(sphere);
        // sphere.applyMP4(vd);
        sphere.applyTexture(grassblock);
        sphere.scale(50,50,50);

        
        // Entity wall = new Entity();
        // wall.cubeMesh();
        // wall.translate(10.5, 0, 0).rotateyWorld(90).scale(20, 20,-0.9);
        // // wall.applyTexture(temp);
        // wall.applyMP4(vd);
        // world.addEntity(wall);
        
        // // wall.setGreenScreen(true);

        // Entity wall2 = new Entity();
        // wall2.cubeMesh();
        // wall2.translate(-10.5, 0, 0).rotateyWorld(90).scale(20, 20,-0.9);
        // wall2.applyTexture(grassblock);
        // world.addEntity(wall2);

        // Entity wall3 = new Entity();
        // wall3.cubeMesh();
        // wall3.translate(0, 0, 10.5).scale(20, 20,-0.9);
        // wall3.applyTexture(grassblock);
        // world.addEntity(wall3);

        // Entity wall4 = new Entity();
        // wall4.cubeMesh();
        // wall4.translate(0, 0, -10.5).scale(20, 20,-0.9);
        // wall4.applyTexture(grassblock);
        // world.addEntity(wall4);

        // Entity floor = new Entity();
        // floor.cubeMesh();
        // floor.translate(0, -10.5, 0).rotateyWorld(90).scale(20, -0.9,20);
        // floor.applyTexture(grassblock);
        // world.addEntity(floor);

        // Entity ceiling = new Entity();
        // ceiling.cubeMesh();
        // ceiling.translate(0, 10.5, 0).rotateyWorld(90).scale(20, -0.9,20);
        // ceiling.applyTexture(grassblock);
        // world.addEntity(ceiling);

        
        Entity testparent = new Entity();
        Entity testchild = new Entity();
        testchild.cubeMesh();
        testchild.translate(2, 2, 0);
        testparent.addChild(testchild);
        testchild.applyTexture(temp);
        world.addEntityChildren(testparent);


        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;

        double deltaTime = 0;
        world.bindVertices();
        world.initializeGlobalIndices();

        ExecutorService tilePool = Executors.newFixedThreadPool(cores);


        TileManager tm = new TileManager();
        tm.createTiles(cores, wm, world);
        
        int frames = 0;
        long lastFpsTime = System.currentTimeMillis();
        
        long timeInput = 0;
        long timeConv = 0;
        long timeCull = 0;
        long timeNdc = 0;
        long timeAssign = 0;
        long timeRender = 0;
        long timeScreen = 0;
        boolean flip = true;

        int numShards = 500;
        double[] speed = new double [numShards];
        for(int i = 0; i < numShards-1; i++){
            double random = random();
            speed[i] = random;
        }
while(true){
    try{
        deltaTime = (currentTime - previousTime)%1000;
        
        // vd.update();
        // sphere.applyNewMP4Frame(vd.getFrameBuffer());
        double num = 0;
        for(int i = numShards-1; i >= 0; i--){
            glassSphere.children.get(i).translate(-num, 0, 0);
            glassSphere.children.get(i).rotatexLocal(speed[i]).rotatey(speed[i]);

            num += 0.000001;
        }
        testchild.rotatexLocal(1);
        // testchild.translate(1,0,0);

        glassSphere.applyTransformationValues();
        
        long start = System.nanoTime();
        cm.pollInput(im, wm, deltaTime);
        cm.updateCameraMatrix();
        timeInput += (System.nanoTime() - start);

        start = System.nanoTime();
        world.assignEntitiesToThreads();
        world.entityConversions();
        world.fillLightBuffer();
        world.applyLightRotations();
        timeConv += (System.nanoTime() - start);

        start = System.nanoTime();
        TriangleManager.cullOperations(world);
        timeCull += (System.nanoTime() - start);

        start = System.nanoTime();
        world.convertToNDC(wm, tm);
        timeNdc += (System.nanoTime() - start);
        
        start = System.nanoTime();
        tm.assignTrianglesToTiles(world, wm);
        timeAssign += (System.nanoTime() - start);

        start = System.nanoTime();
        tm.tiles.parallelStream().forEach(tile -> {
            wm.renderTile(tile, world, cm);
        });
        timeRender += (System.nanoTime() - start);

        start = System.nanoTime();
        wm.updateScreen(tm);
        timeScreen += (System.nanoTime() - start);

        if(wm.errorOccured) break;
        Thread.sleep(1);
        frames++;
        

        long now = System.currentTimeMillis();
        if(now - lastFpsTime >= 1000){
            System.out.println("=========================================");
            System.out.println("FPS: " + frames);
            System.out.printf("Input & Cam:   %.2f ms\n", (timeInput / (double)frames) / 1_000_000.0);
            System.out.printf("Vertex Math:   %.2f ms\n", (timeConv / (double)frames) / 1_000_000.0);
            System.out.printf("Culling:       %.2f ms\n", (timeCull / (double)frames) / 1_000_000.0);
            System.out.printf("NDC Convert:   %.2f ms\n", (timeNdc / (double)frames) / 1_000_000.0);
            System.out.printf("Assign Tiles:  %.2f ms\n", (timeAssign / (double)frames) / 1_000_000.0);
            System.out.printf("Tile Render:   %.2f ms\n", (timeRender / (double)frames) / 1_000_000.0);
            System.out.printf("Update Screen:   %.2f ms\n", (timeScreen / (double)frames) / 1_000_000.0);
            System.out.println("=========================================");
            // world.printSceneData();
            // tm.displayTileIndiceData();
            // Reset trackers for the next second
            frames = 0;
            lastFpsTime = now;
            timeInput = timeConv = timeCull = timeNdc = timeAssign = timeRender = timeScreen = 0;
        }
        
        previousTime = currentTime;
        currentTime = (System.currentTimeMillis());
        world.resetSceneFrameData();
        tm.emptyTiles();
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }
}
    }
}