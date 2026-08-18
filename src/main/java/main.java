
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

        cm.yaw-=90;
        cm.setCameraPosition(-10, 1, -2);

        wm.openWindow();
        wm.addInputListener(im);

        VideoDecoder vd = new VideoDecoder("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\0815.mp4");
        VideoDecoder moonvid = new VideoDecoder("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\moonv2.mp4");
        VideoDecoder staticwhite = new VideoDecoder("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\staticwhite.mp4");

        Scene world = new Scene();
        world.addCamera(cm);
        


        world.ambience = 0.1;
        int scale = 15;

        // Entity moonBackground = new Entity();
        // moonBackground.cubeMesh(true);
        // world.addEntity(moonBackground);
        // moonBackground.applyMP4(moonvid);
        // moonBackground.scale(50, 50, 50);
        // moonBackground.translate(0, 5, 0);
        // moonBackground.rotateyWorld(-90);

        // Entity glassSphere = EffectManager.generateGlassSphere(100, 100, 2500, 500, 1, 1, 20, 80, 0, 80);
        Entity glassSphere = EffectManager.generateGlassSphere(100, 100, 2500, 500, 1, 1, 24, 45, 55, 35, 45);
        world.addEntityChildren(glassSphere);
        double iterator = 0;
        for(Entity e : glassSphere.children.values()){
            e.applyMP4(staticwhite);
        }
        glassSphere.scale(scale, scale, scale);
        glassSphere.rotatexWorld(180);

        
        int numShards = 1500;
        double[] speed = new double [numShards];
        for(int i = 0; i < numShards-1; i++){
            double random = 0.0001 - (random()*0.0002);
            speed[i] = random;
            // glassSphere.children.get(i).applyTexture(grassblock);
        }
        

        Light wl = new Light(0 , 0, 0);
        wl.setPoint();
        wl.setIntensity(6);
        world.addLight(wl);

        // Entity normalSphere = new Entity();
        // normalSphere.sphereMesh(100, 100, 1, 1, 0, 0, 0, 0);
        // normalSphere.scale(scale, scale, scale);
        // normalSphere.applyMP4(staticwhite);
        // world.addEntity(normalSphere);

        

        
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

while(true){
    try{
        deltaTime = (currentTime - previousTime)%1000;
        
        // staticwhite.update();

        // normalSphere.applyMP4(staticwhite);

        long start = System.nanoTime();
        cm.pollInput(im, wm, deltaTime);
        cm.updateCameraMatrix();
        timeInput += (System.nanoTime() - start);

        glassSphere.applyTransformationValues();
        double num = 0;
           for(int i = numShards-1; i >= 0; i--){
               glassSphere.children.get(i).translate(-num*deltaTime, 0, 0);
               glassSphere.children.get(i).rotatexLocal(speed[i]*deltaTime).rotateyLocal(speed[i]*deltaTime);
               num -= 0.0000001;
        }

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