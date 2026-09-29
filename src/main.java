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
        Texture gridTexture = new Texture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\grid0.png");
       
        cm.setCameraPosition(0, 0, 0);

        wm.openWindow();
        wm.addInputListener(im);

        Scene world = new Scene();
        world.addCamera(cm);

        Entity cube = new Entity();
        cube.cubeMesh(false);
        world.addEntity(cube);
        cube.scale(1,1,1);

        Animation anitest = new Animation();
        anitest.createKeyFrame(10, 0, 0 , 180, 180, 180, 0, 0, 0, 5000);
        anitest.createKeyFrame(0, 10, 0 , 0, 0, 0, 0, 0, 0, 10000);
        
        world.ambience = 1;

        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;

        double deltaTime = 0;
        world.bindVertices();
        world.initializeGlobalIndices();

        ParticleSystem stars = new ParticleSystem();
        stars.initializeSystem(0,0,0,10000, 1, 10000, 10000,10000);
        world.addParticleSystem(stars);
        stars.setColor(255, 255, 255);

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

        long totalTime = 0;

    while(true){
        try{
            deltaTime = (currentTime - previousTime)%1000;
            totalTime += deltaTime;
            long start = System.nanoTime();
            cm.pollInput(im, wm, deltaTime);
            cm.updateCameraMatrix();
            timeInput += (System.nanoTime() - start);

        
        start = System.nanoTime();

        if(!anitest.lockAnimation)anitest.playAnimation(cube, totalTime);
        // System.out.println("x: " + cube.x);
        world.assignEntitiesToThreads();
        world.updateEntityTransformations();
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

        world.convertParticlesToClipSpace(cm, wm.width, wm.length);
        tm.tiles.parallelStream().forEach(tile -> {
            wm.renderParticlesOnTile(tile, world);
        });

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