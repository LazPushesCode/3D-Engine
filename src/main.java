import java.util.ArrayList;

public class main{
    public static void main(String[] args){
        WindowManager wm = new WindowManager(920, 600);
        CameraManager cm = new CameraManager(wm.width, wm.length,100);
        InputManager im = new InputManager();

        cm.setCameraPosition(1, 0, 0);

        Entity cube = new Entity();
        cube.cubeMesh();
        cube.translate(0,0,4);
        wm.openWindow();
        wm.addInputListener(im);
        ArrayList<Entity> entityList = new ArrayList<>();
        entityList.add(cube);
        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;
        double deltaTime = 0;
        System.out.println("cubes size: " + cube.xSize);
        while(true){
            try {
                deltaTime = (currentTime - previousTime)%1000;
                cube.translate(0, 0.01, 0);
                wm.clearScreen();
                cm.pollInput(im, deltaTime);
                cm.updateCameraMatrix();
                for(Entity et : entityList){
                    renderEntity(cm, wm, et);
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
      et.convertToWorldSpace();
      cm.convertToViewSpace(et);
      TriangleManager.cullTriangles(et, cm);
      wm.convertToNDC(et);
      wm.renderOnScreen(cm, et);
      et.resetFrameData();
   }
}