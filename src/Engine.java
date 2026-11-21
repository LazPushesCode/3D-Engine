
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Engine {
    WindowManager window;
    InputManager inputManager;
    AnimationManager animationManager;
    TileManager tileManager;

    int cores;

    boolean displayPipelineInfo;

    Engine(){
        cores = Runtime.getRuntime().availableProcessors();
        window = new WindowManager(1920, 1080, cores);
        inputManager = new InputManager();
        animationManager = new AnimationManager(5,1);
        window.addInputListener(inputManager);
        tileManager = new TileManager();
        displayPipelineInfo = false;
    }
    void setWindowDimensions(int width, int length){
        window.width = width;
        window.length = length;
    }
    void enableBilinearTextureFiltering(){
        window.textureFilteringType = window.BILINEAR;
    }

    void run(Scene sceneToRender){

        window.openWindow();

        sceneToRender.bindVertices();
        sceneToRender.initializeGlobalIndices();

        Camera activeCamera = sceneToRender.activeCamera;

        activeCamera.aspect = (double) window.width / window.length;

        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;
        long deltaTime = 0;
        long totalTime = 0;

        long timeInput = 0;
        long timeConv = 0;
        long timeCull = 0;
        long timeNdc = 0;
        long timeAssign = 0;
        long timeRender = 0;
        long timeScreen = 0;

        int frames = 0;
        long lastFpsTime = System.currentTimeMillis();

        ExecutorService tilePool = Executors.newFixedThreadPool(cores);
        tileManager.createTiles(cores, window, sceneToRender);

        while(true){ 
            try {
                deltaTime = (currentTime - previousTime)%1000;
                totalTime += deltaTime;
                long start = System.nanoTime();
                activeCamera.pollInput(inputManager, window, deltaTime);
                activeCamera.updateCameraMatrix();
                timeInput += (System.nanoTime() - start);
                start = System.nanoTime();

                animationManager.playAnimations(sceneToRender, deltaTime);

                sceneToRender.assignEntitiesToThreads();
                sceneToRender.updateEntityTransformations();
                sceneToRender.entityConversions();
                sceneToRender.fillLightBuffer();
                sceneToRender.applyLightRotations();
                timeConv += (System.nanoTime() - start);

                start = System.nanoTime();
                TriangleManager.cullOperations(sceneToRender);
                timeCull += (System.nanoTime() - start);

                start = System.nanoTime();
                sceneToRender.convertToNDC(window, tileManager);
                timeNdc += (System.nanoTime() - start);
                
                start = System.nanoTime();
                tileManager.assignTrianglesToTiles(sceneToRender, window);
                timeAssign += (System.nanoTime() - start);

                sceneToRender.convertParticlesToClipSpace(activeCamera, window.width, window.length);
                tileManager.tiles.parallelStream().forEach(tile -> {
                    window.renderParticlesOnTile(tile, sceneToRender);
                });

                start = System.nanoTime();
                tileManager.tiles.parallelStream().forEach(tile -> {
                    window.renderTile(tile, sceneToRender, activeCamera);
                });
                timeRender += (System.nanoTime() - start);

                start = System.nanoTime();
                window.updateScreen(tileManager);
                timeScreen += (System.nanoTime() - start);

                if(window.errorOccured) break;
                Thread.sleep(1);
                frames++;

                long now = System.currentTimeMillis();
                if(now - lastFpsTime >= 1000){
                    if(displayPipelineInfo){
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
                    }
                    // world.printSceneData();
                    // tm.displayTileIndiceData();
                    // Reset trackers for the next second
                    frames = 0;
                    lastFpsTime = now;
                    timeInput = timeConv = timeCull = timeNdc = timeAssign = timeRender = timeScreen = 0;
                }

                previousTime = currentTime;
                currentTime = (System.currentTimeMillis());
                sceneToRender.resetSceneFrameData();
                tileManager.emptyTiles();

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
