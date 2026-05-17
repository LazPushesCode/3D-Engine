
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main{
    public static void main(String[] args){
        WindowManager wm = new WindowManager(1200, 600);
        CameraManager cm = new CameraManager(wm.width, wm.length,100);
        InputManager im = new InputManager();

        cm.setCameraPosition(0, 0, -5);
        
        wm.openWindow();
        wm.addInputListener(im);
       

        Scene world = new Scene();
        world.addCamera(cm);
        // for(int i = 0; i < 3; i++){
        //     for(int j = 0; j < 3; j++){
        //         for(int k = 0; k < 3; k++){
        //             Entity cube = new Entity();
        //             cube.cubeMesh();
        //             cube.setWorldPosition(i, j, k);
        //             world.addEntity(cube);
        //             cube.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        //         }
        //     }
        // }

        Entity cube = new Entity();
        cube.cubeMesh();
        cube.setWorldPosition(0, 0, 0);
        cube.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        world.addEntity(cube);

        Entity wall = new Entity();
        wall.cubeMesh();
        wall.translate(10.5, 0, 0).rotatex(90).scale(20, 20,-0.9);
        wall.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        world.addEntity(wall);

        Entity wall2 = new Entity();
        wall2.cubeMesh();
        wall2.translate(-10.5, 0, 0).rotatex(90).scale(20, 20,-0.9);
        wall2.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        world.addEntity(wall2);

        Entity wall3 = new Entity();
        wall3.cubeMesh();
        wall3.translate(0, 0, 10.5).rotatex(0).scale(20, 20,-0.9);
        wall3.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        world.addEntity(wall3);

        Entity wall4 = new Entity();
        wall4.cubeMesh();
        wall4.translate(0, 0, -10.5).rotatex(0).scale(20, 20,-0.9);
        wall4.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        world.addEntity(wall4);

        Entity floor = new Entity();
        floor.cubeMesh();
        floor.translate(0, -10.5, 0).rotatex(90).scale(20, -0.9,20);
        floor.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        world.addEntity(floor);

        Entity ceiling = new Entity();
        ceiling.cubeMesh();
        ceiling.translate(0, 10.5, 0).rotatex(90).scale(20, -0.9,20);
        ceiling.setTexture("C:\\Users\\lazar\\3D-Engine\\resources\\assets\\11635.png");
        world.addEntity(ceiling);
        



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
                        // tm.displayTileIndiceData();
                        //    world.printSceneData();
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
}