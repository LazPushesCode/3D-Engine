
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main{
    public static void main(String[] args){
        WindowManager wm = new WindowManager(1200, 600);
        CameraManager cm = new CameraManager(wm.width, wm.length,100);
        InputManager im = new InputManager();


        cm.setCameraPosition(0, 2, 0);
        
        wm.openWindow();
        wm.addInputListener(im);
       

        Scene world = new Scene();
        world.addCamera(cm);

        for(int i = -5; i < 5; i++){
            for(int j = -5; j < 5; j++){
                Entity cube = new Entity();
                cube.cubeMesh();
                cube.setWorldPosition(i, 0, j);
                world.addEntity(cube);
                cube.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
            }
        }

        // Entity cube = new Entity();
        // cube.cubeMesh();
        // cube.setWorldPosition(-2, 2, 2);
        // world.addEntity(cube);

        // Entity cube2 = new Entity();
        // cube2.cubeMesh();
        // cube2.setWorldPosition(-1, 2, 2);
        // world.addEntity(cube2);
        // cube2.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");

        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;

        double deltaTime = 0;
        world.bindVertices();
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService tilePool = Executors.newFixedThreadPool(cores);
        TileManager tm = new TileManager();
        tm.allocateTiles(2, 4, wm, world);
        
        int frames = 0;
        long lastFpsTime = System.currentTimeMillis();

        boolean newSystem = true;
        
        if(newSystem){
            while(true){
                try{
                    deltaTime = (currentTime - previousTime)%1000;
                    wm.clearScreen();
                    cm.pollInput(im, wm, deltaTime);
                    cm.updateCameraMatrix();
                    world.transformEntities();
                    world.convertVerticesToWorldSpace();
                    world.convertVerticesToViewSpace();
                    TriangleManager.cullOperations(world);
                    world.convertToNDC(wm, tm);
                    tm.assignTrianglesToTiles(world, wm);
                        CountDownLatch latch = new CountDownLatch(tm.tiles.size());
                        for(int i = 0; i < tm.tiles.size(); i++){
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
                        wm.updateScreen(tm);
                        if(wm.errorOccured) break;
                        Thread.sleep(1);
                        frames++;
                        long now = System.currentTimeMillis();
                        if(now - lastFpsTime >= 1000){
                            System.out.println("FPS: " + frames);
                            frames = 0;
                            lastFpsTime = now;
                        }
                        previousTime = currentTime;
                        currentTime = (System.currentTimeMillis());
                        world.resetSceneFrameData();
                        tm.emptyTiles();
                        // break;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        if(!newSystem){
        //     while(true){
        //         try {
        //             deltaTime = (currentTime - previousTime)%1000;
        //             wm.clearScreen();
        //             cm.pollInput(im, wm, deltaTime);
        //             cm.updateCameraMatrix();
        //             for(Entity et : world.entities.values()){
        //                 renderEntity(world.cameras.get(0), wm, tm, et);
        //             }
        //             CountDownLatch latch = new CountDownLatch(tm.tiles.size());
        //             for(int i = 0; i < tm.tiles.size(); i++){
        //                 final int index = i;
        //                 tilePool.execute(() -> {
        //                     try {
        //                         wm.oldRenderTile(cm, tm.tiles.get(index), world);
        //                     } finally {
        //                         latch.countDown();
        //                     }
        //                 });
        //             }
        //             latch.await();
        //             wm.updateScreen(tm);
        //             Thread.sleep(8);

        //             frames++;
        //             long now = System.currentTimeMillis();
        //             if(now - lastFpsTime >= 1000){
        //                 System.out.println("FPS: " + frames);
        //                 frames = 0;
        //                 lastFpsTime = now;
        //             }
        //             previousTime = currentTime;
        //             currentTime = (System.currentTimeMillis());
        //             tm.emptyTiles();
        //             // break;
        //         } catch (Exception e) {
        //             e.printStackTrace();
        //         }
        //     }
        }
    }
    static void renderEntity(CameraManager cm, WindowManager wm, TileManager tm, Entity et){
      et.applyTransformationValues();
      et.convertToWorldSpace();
      cm.convertToViewSpace(et);
      TriangleManager.cullTriangles(et, cm);
      wm.convertToNDC(et, tm);
    //   wm.renderObject(cm, et);
      et.resetFrameData();
   }
}