public class CameraManager {
    double FOV;
    double aspect;
    double far;
    double near;

    double x, y, z;
    double rx, ry, rz;
    Matrix viewMatrix;
    Matrix projectionMatrix;

    double speed;

    CameraManager(int windowWidth, int windowLength, int fov){
        this.rx = 0;
        this.ry = 0;
        this.rz = 0;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.FOV = Math.toRadians(fov);
        this.aspect = (double)windowWidth/windowLength;
        this.near = 0.1;
        this.far = 1000;
        this.speed = 0.005;
        viewMatrix = Matrix.Identity();
        projectionMatrix = Matrix.Identity();
        updateCameraMatrix();
    }
    void updateViewMatrix(){
        // viewMatrix = Matrix.translate(-x, -y, -z).multiply(Matrix.rotatex(rx).multiply(Matrix.rotatey(ry)).multiply(Matrix.rotatez(rz)));
        viewMatrix = Matrix.rotatex(rx).multiply(Matrix.rotatey(ry).multiply(Matrix.rotatez(rz))).multiply(Matrix.translate(-x,-y,-z));
    }
    void updateProjectionMatrix(){
        double t = 1/Math.tan(FOV/2);
        double [][] temp = {
            {t/aspect, 0, 0, 0},
            {0, t, 0, 0},
            {0, 0, -(far+near)/(near-far), -2*far*near/(near-far)},
            {0, 0, 1, 0}
        };
        projectionMatrix.m = temp;
    }
    void convertToViewSpace(Entity m){
        for(int i = 0; i < m.worldSpaceVectors.length; i++){
            double view[] = (viewMatrix.vectorTransformation(m.worldSpaceVectors[i]));
            m.viewSpaceVectors.add(view);
        }
    }
        void convertToClipSpace(Entity m){
        for(int i = 0; i < m.viewSpaceVectors.size(); i++){
            double clip[] = (projectionMatrix.vectorTransformation(m.viewSpaceVectors.get(i)));
            m.finalVectors.add(clip);
        }
    }
    CameraManager setCameraPosition(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
        updateCameraMatrix();
        return this;
    }
    CameraManager translate(double x, double y, double z){
        this.x += x;
        this.y += y;
        this.z += z;
        updateCameraMatrix();
        return this;
    }
    void pollInput(InputManager im, double deltaTime){
        if(im.forward){
            this.z += speed * deltaTime;
        }
        if(im.backward){
            this.z -= speed * deltaTime;
        }
        if(im.left){
            this.x -= speed * deltaTime;
        }
        if(im.right){
            this.x += speed * deltaTime;
        }
         if(im.ru){
            this.rx += .3 * deltaTime;
        }
        if(im.rd){
            this.rx -= .3 * deltaTime;
        }
        if(im.rl){
            this.ry += .3 * deltaTime;
        }
        if(im.rr){
            this.ry -= .3 * deltaTime;
        }
    }
    void updateCameraMatrix(){
        updateViewMatrix();
        updateProjectionMatrix();
    }
}
