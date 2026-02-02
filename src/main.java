import java.util.ArrayList;

public class main{
    public static void main(String[] args){
        WindowManager wm = new WindowManager(920, 600);
        CameraManager cm = new CameraManager(wm.width, wm.length,100);
        InputManager im = new InputManager();

        cm.setCameraPosition(1, 0, 0);

        Entity cube = new Entity();
        cube.cubeMesh();
        cube.translate(0,0,3);
        
        Entity cube2 = new Entity();
        cube2.cubeMesh();
        cube2.translate(0, -1, 0);
        cube2.scale(1, 1, 1);

        Entity cube3 = new Entity();
        cube3.cubeMesh();
        cube3.translate(-1,0,3).scale(1, 1, 1);

        wm.openWindow();
        wm.addInputListener(im);
        ArrayList<Entity> entityList = new ArrayList<>();
        entityList.add(cube);
        entityList.add(cube2);
        entityList.add(cube3);
        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;
        double deltaTime = 0;
        double rotate = 0.01;
        while(true){
            try {
                deltaTime = (currentTime - previousTime)%1000;
                double move = 0.01 * deltaTime;
                cube3.translate(0, 0, 0).rotatex(-1).rotatey(1).rotatez(1).scale(1, 1, 1);
                rotate += 0.001;
                
                cube.scale(1,1,1);
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