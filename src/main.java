
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main{
    public static void main(String[] args){
        WindowManager wm = new WindowManager(1200, 592);
        CameraManager cm = new CameraManager(wm.width, wm.length,100);
        InputManager im = new InputManager();

        cm.setCameraPosition(0, 0, -2);
        
        wm.openWindow();
        wm.addInputListener(im);
       

        Scene world = new Scene();
        world.addCamera(cm);
        world.setLight(-1, 2, 0, 0, 1, 1, 0.3);
        
        Texture grassblock = new Texture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        
        // for(int i = -30; i < 30; i++){
        //     for(int j = -30; j < 30; j++){
        //         // for(int k = 0; k < 3; k++){
        //             Entity cube = new Entity();
        //             cube.cubeMesh();
        //             cube.setWorldPosition(i, j, 0);
        //             cube.applyTexture(grassblock);
        //             world.addEntity(cube);
        //         // }
        //     }
        // }

        // Entity triangle = new Entity();
        // triangle.triangleMesh();
        // triangle.setWorldPosition(0, 0, 0);
        // world.addEntity(triangle);

        // Entity cube = new Entity();
        // cube.cubeMesh();
        // cube.setWorldPosition(0, 0, 0);
        // cube.applyTexture(grassblock);
        // cube.scale(10, 0, 10);
        // world.addEntity(cube);
        Entity sphere = new Entity();
        sphere.sphereMesh(25, 25);
        sphere.applyTexture(grassblock);
        world.addEntity(sphere);


        // Entity wall = new Entity();
        // wall.cubeMesh();
        // wall.translate(10.5, 0, 0).rotatex(90).scale(20, 20,-0.9);
        // wall.applyTexture(grassblock);
        // world.addEntity(wall);

        // Entity wall2 = new Entity();
        // wall2.cubeMesh();
        // wall2.translate(-10.5, 0, 0).rotatex(90).scale(20, 20,-0.9);
        // wall2.applyTexture(grassblock);
        // world.addEntity(wall2);

        // Entity wall3 = new Entity();
        // wall3.cubeMesh();
        // wall3.translate(0, 0, 10.5).rotatex(0).scale(20, 20,-0.9);
        // wall3.applyTexture(grassblock);
        // world.addEntity(wall3);

        // Entity wall4 = new Entity();
        // wall4.cubeMesh();
        // wall4.translate(0, 0, -10.5).rotatex(0).scale(20, 20,-0.9);
        // wall4.applyTexture(grassblock);
        // world.addEntity(wall4);

        // Entity floor = new Entity();
        // floor.cubeMesh();
        // floor.translate(0, -10.5, 0).rotatex(90).scale(20, -0.9,20);
        // floor.applyTexture(grassblock);
        // world.addEntity(floor);

        // Entity ceiling = new Entity();
        // ceiling.cubeMesh();
        // ceiling.translate(0, 10.5, 0).rotatex(90).scale(20, -0.9,20);
        // ceiling.applyTexture(grassblock);
        // world.addEntity(ceiling);
        



        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;

        double deltaTime = 0;
        world.bindVertices();
        world.initializeGlobalIndices();
        int cores = Runtime.getRuntime().availableProcessors();

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

while(true){
    try{
        deltaTime = (currentTime - previousTime)%1000;
        
        long start = System.nanoTime();
        cm.pollInput(im, wm, deltaTime);
        cm.updateCameraMatrix();
        timeInput += (System.nanoTime() - start);

        start = System.nanoTime();
        world.assignEntitiesToThreads();
        world.entityConversions();
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
        CountDownLatch latch = new CountDownLatch(tm.tileCount);
        for(int i = 0; i < tm.tileCount; i++){
            final int index = i;
            tilePool.execute(() -> {
                try {
                    wm.renderTile(tm.tiles.get(index), world, cm);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
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