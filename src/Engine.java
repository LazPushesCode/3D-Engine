
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Engine {
    WindowManager window; // Manages window panel operations and values
    InputManager inputManager; // Manages user input and values
    AnimationManager animationManager; // Manages keyframe animation systems
    TileManager tileManager; // Manages screen tiles rendering

    int cores; // Number of cores available for use in rendering

    boolean displayPipelineInfo; // Displays info used for debugging in terminal

    int defaultWindowColor;

    // Default engine constructor if no parameters provided
    // Initializes all classes engine uses in main rendering loop
    Engine(){ 
        cores = Runtime.getRuntime().availableProcessors();
        window = new WindowManager(1920, 1080, cores);
        inputManager = new InputManager();
        animationManager = new AnimationManager(5,1);
        window.addInputListener(inputManager);
        tileManager = new TileManager();
        displayPipelineInfo = false;
        defaultWindowColor = 0xFF000000;
    }
    // Reassigns window dimensions before runtime
    void setWindowDimensions(int width, int length){
        window.width = width;
        window.length = length;
    }
    // Assigns default color to window pixels
    void setWindowDefaultColor(int color){
        defaultWindowColor = color;
    }
    // Sets pipeline's texture filtering type to closest neighbor
    // Lower quality, but fastest rendering
    void enableClosestNeighborTextureFiltering(){
        window.textureFilteringType = window.CLOSEST_NEIGHBOR; 
    }
    // Sets pipeline's texture filtering type to bilinear
    // Slight edge in quality over closest neighbor at cost of performance
    void enableBilinearTextureFiltering(){
        window.textureFilteringType = window.BILINEAR;
    }
    // If on, displays time analytics of each pipeline segment
    void enableDisplayPipelineInformation(){
        displayPipelineInfo = true;
    }
    // Starts the main render loop of pipeline.
    // Takes a scene as a parameter. Given scene will have all its objects rendered on screen
    void run(Scene sceneToRender, Entity logo){
        window.openWindow(); // Opens window panel using external libraries
        
        // Scene manages vertice and indice memory internally
        sceneToRender.bindVertices(); // Scene initialize vertice buffers
        sceneToRender.initializeGlobalIndices(); // Scene initializes indice buffers

        Camera activeCamera = sceneToRender.activeCamera; // Snatch primary camera reference from scene

        activeCamera.aspect = (double) window.width / window.length; // Set primary camera's aspect using window's dimnesions

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

        ExecutorService tilePool = Executors.newFixedThreadPool(cores); // Pool of threads each rendering one tile section
        tileManager.createTiles(cores, window, defaultWindowColor, sceneToRender); // Initialize screen tiles for rendering

        // Main rendering loop
        boolean runLoop = true;
        while(runLoop){ 
            try {
                deltaTime = (currentTime - previousTime)%1000;
                totalTime += deltaTime;
                long start = System.nanoTime();
                activeCamera.pollInput(inputManager, window, deltaTime);
                activeCamera.updateCameraMatrix();
                timeInput += (System.nanoTime() - start);
                start = System.nanoTime();

                animationManager.playAnimations(sceneToRender, deltaTime); // Plays all initialized animations in scene

                if(inputManager.action){
                    double speed = 0.00001;
                    for(int i = 0; i < 994; i++){
                        logo.children.get(i).translate(0, 0, speed*deltaTime);
                        speed += 0.00000001;
                    }
                }

                sceneToRender.assignEntitiesToThreads(); // Each thread gets even amount of entities to render
                sceneToRender.updateEntityTransformations(); // Applies world transformations on all entities
                sceneToRender.entityConversions(); // Converts entities to clip space
                sceneToRender.fillLightBuffer(); // Resets light buffers, updating values
                sceneToRender.applyLightRotations(); // Applies rotation transfromation on all lights
                timeConv += (System.nanoTime() - start);

                start = System.nanoTime();
                TriangleManager.cullOperations(sceneToRender); // Discards triangles facing away from camera and outside frustum view
                timeCull += (System.nanoTime() - start); 

                start = System.nanoTime();
                sceneToRender.convertToNDC(window, tileManager); // Convert remaining vertices to normalized space coordinates [-1, 1]
                timeNdc += (System.nanoTime() - start);
                
                start = System.nanoTime();
                tileManager.assignTrianglesToTiles(sceneToRender, window); // Each tile receives batch of indices to render on screen
                timeAssign += (System.nanoTime() - start);

                start = System.nanoTime();
                tileManager.tiles.parallelStream().forEach(tile -> { // Allow each thread to render assigned tile
                    window.renderTile(tile, sceneToRender, activeCamera, inputManager.wireframe); // Render assigned indices on assigned tile
                });
                sceneToRender.convertParticlesToClipSpace(activeCamera, window.width, window.length); // Converts particles to clip space
                tileManager.tiles.parallelStream().forEach(tile -> { // Allow each thread to render assigned tile
                    window.renderParticlesOnTile(tile, sceneToRender); // Render particles on tile
                });
                timeRender += (System.nanoTime() - start);

                start = System.nanoTime();
                window.updateScreen(tileManager); // Fill screen with pixel colors computed by each tile
                timeScreen += (System.nanoTime() - start);

                if(window.errorOccured) break;
                Thread.sleep(1);
                frames++;

                long now = System.currentTimeMillis();
                if(now - lastFpsTime >= 1000){
                    if(displayPipelineInfo){ // If true, display pipeline debugging information in terminal
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
                    frames = 0;
                    lastFpsTime = now;
                    timeInput = timeConv = timeCull = timeNdc = timeAssign = timeRender = timeScreen = 0;
                }
                previousTime = currentTime;
                currentTime = (System.currentTimeMillis());
                sceneToRender.resetSceneFrameData(); // Reset temporary scene data each frame
                tileManager.emptyTiles(); // Remove all values from tile buffers. Fills back up each frame
                if(inputManager.closeWindow){
                    window.closeWindow(); // Disposes of window frame and closes
                    tilePool.shutdownNow(); // Stops all threads inside tilePool
                    runLoop = false; // Breaks while loop on next iteration
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        System.exit(0); // Shuts down program after while loop
    }
}
