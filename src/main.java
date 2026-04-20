
import java.util.ArrayList;
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
        Entity cube = new Entity();
        cube.cubeMesh();
        cube.setWorldPosition(0, 1, 0);
        world.addEntity(cube);

        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;

        double deltaTime = 0;

        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService tilePool = Executors.newFixedThreadPool(cores);
        ArrayList<Tile> tiles = new ArrayList<>();
        int xAddend = wm.width/4;
        int yAddend = wm.length/2;
        int xOffset = xAddend;
        int yOffset = yAddend;
        for(int i = 0; i < 8; i++){
            tiles.add(new Tile(xOffset, yOffset));
            System.out.println("Tile created with offsets: " 
            + tiles.get(i).xOffset + " " + tiles.get(i).yOffset);
            xOffset += xAddend;
            if(xOffset > wm.width){
                xOffset = xAddend;
                yOffset += yAddend;
            }
        }
        
        cube.setTexture("C:\\Users\\yalfo\\3D-Engine\\resources\\assets\\11635.png");
        
        while(true){
            try {
                deltaTime = (currentTime - previousTime)%1000;
                wm.clearScreen();
                cm.pollInput(im, deltaTime);
                cm.updateCameraMatrix();
                for(Entity et : world.entities.values()){
                    renderEntity(world.cameras.get(0), wm, et);
                }
                wm.updateScreen();
                Thread.sleep(16);
                previousTime = currentTime;
                currentTime = (System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static void renderEntity(CameraManager cm, WindowManager wm, Entity et){
      et.applyTransformationValues();
      et.convertToWorldSpace();
      cm.convertToViewSpace(et);
      TriangleManager.cullTriangles(et, cm);
      wm.convertToNDC(et);
      wm.renderOnScreen(cm, et);
      et.resetFrameData();
   }
}