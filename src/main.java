import java.util.ArrayList;

public class main{
    public static void main(String[] args){
        WindowManager wm = new WindowManager(920, 600);
        CameraManager cm = new CameraManager(wm.width, wm.length,100);
        InputManager im = new InputManager();

        cm.setCameraPosition(0, 1, 0);

        // Entity cube = new Entity();
        // Entity cube2 = new Entity();
    
        // cube.setTexture("C:\\Users\\yalfo\\3D-Engine\\resources\\assets\\11635.png");
        // cube2.setTexture("C:\\Users\\yalfo\\3D-Engine\\resources\\assets\\11635.png");
        
        wm.openWindow();
        wm.addInputListener(im);
        ArrayList<Entity> entityList = new ArrayList<>();

        Entity wall1 = new Entity();wall1.cubeMesh();
        wall1.translate(0,-1,2);

       
        wall1.setTexture("C:\\Users\\yalfo\\3D-Engine\\resources\\assets\\11635.png");

        entityList.add(wall1);

        long currentTime = (System.currentTimeMillis());
        long previousTime = 0;
        double deltaTime = 0;
        while(true){
            try {
                deltaTime = (currentTime - previousTime)%1000;
                wm.clearScreen();
                cm.pollInput(im, deltaTime);
                cm.updateCameraMatrix();
                for(Entity et : entityList){
                    // et.rotatex(1).rotatey(1).translate(0,0.001,0);
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
      et.applyTransformationValues();
      et.convertToWorldSpace();
      cm.convertToViewSpace(et);
      TriangleManager.cullTriangles(et, cm);
      wm.convertToNDC(et);
      wm.renderOnScreen(cm, et);
      et.resetFrameData();
   }
}