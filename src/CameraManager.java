public class CameraManager {
    double FOV;
    double aspect;
    double far;
    double near;

    double x, y, z;
    double pitch, yaw, roll;

    double xMoved, yMoved, zMoved;

    Matrix viewMatrix;
    Matrix projectionMatrix;

    double speed;

    CameraManager(int windowWidth, int windowLength, int fov){
        this.pitch = 0;
        this.yaw = 0;
        this.roll = 0;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.FOV = Math.toRadians(fov);
        this.aspect = (double)windowWidth/windowLength;
        this.near = 0.1;
        this.far = 1000;
        this.speed = 0.001;
        viewMatrix = Matrix.Identity();
        projectionMatrix = Matrix.Identity();
        updateCameraMatrix();
    }
    void updateViewMatrix(){
        viewMatrix = Matrix.rotatex(pitch).multiply(Matrix.rotatey(yaw)).multiply(Matrix.translate(-x,-y,-z));
        // System.out.print("pos:       (" + x + ", " + y + ", " + z + ")");
        // System.out.println(" (" + pitch + ", " + yaw + ", " + roll + ")");
        // viewMatrix = Matrix.translate(-x,-y,-z);
        // viewMatrix = viewMatrix.multiply(Matrix.rotatey(yaw));
        // viewMatrix.printMatrix();
    }
    CameraManager translate(double givenx, double giveny, double givenz){
        this.x += givenx;
        this.y += giveny;
        this.z += givenz;
        viewMatrix = viewMatrix.multiply(Matrix.translate(-x, -y, -z));
        return this;
    }
    CameraManager rotatey(double degree){
        yaw += degree;
        viewMatrix = viewMatrix.multiply(Matrix.rotatey(yaw));
        return this;
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
            m.finalTextureMappings.add(m.textureMappings[i]);
            
        }
    }
    CameraManager setCameraPosition(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
        updateCameraMatrix();
        return this;
    }
    void pollInput(InputManager im, double deltaTime){
        double[] forward = computeForward();
        // System.out.println();
        // for(int i = 0; i < forward.length; i++){
        //     System.out.print(forward[i] + " ");
        // }
        forward = normalize(forward);
        // System.out.println(" " + pitch + " " + yaw + " x: " + x + " y: " + y + " z: " + z);
        double[] right = computeRight();
        right = normalize(right);
        if(im.ru){
            this.pitch += .3 * deltaTime;
        }
        if(im.rd){
            this.pitch -= .3 * deltaTime;
        }
        if(im.rl){
            this.yaw += .3 * deltaTime;
        }
        if(im.rr){
            this.yaw -= .3 * deltaTime;
        }
        if(im.forward){
            z += forward[0] * speed * deltaTime;
            x += forward[2] * speed * deltaTime;
        }
        if(im.backward){
            z -= forward[0] * speed * deltaTime;
            x -= forward[2] * speed * deltaTime;
        }
        if(im.left){
            z -= right[0] * speed * deltaTime;
            x += right[2] * speed * deltaTime;
        }
        if(im.right){
            z += right[0] * speed * deltaTime;
            x -= right[2] * speed * deltaTime;
        }
        clampPitch();
    }
    double[] computeForward(){
        double pitchRadian = Math.toRadians(pitch);
        double yawRadian = Math.toRadians(yaw);
        return new double[] {
            Math.cos(pitchRadian) * Math.cos(yawRadian),
            0,
            Math.cos(pitchRadian) * Math.sin(yawRadian)
        };
    }
    double[] computeRight(){
        double pitchRadian = Math.toRadians(pitch);
        double yawRadian = Math.toRadians(yaw);
        return new double[] {
            Math.cos(yawRadian - Math.PI/2),
            0,
            Math.sin(yawRadian - Math.PI/2)
        };
    }
    double[] normalize(double[] direction){
        double length = 0;
        for(int i = 0; i < direction.length; i++){
            length += (direction[i] * direction[i]);
        }
        length = Math.sqrt(length);
        for(int i = 0; i < direction.length; i++){
            direction[i] /= length;
        }
        return direction;
    }
    double[] crossProduct(double[] a, double[] b){
        return new double[]{
            a[1]*b[2] - a[2]*b[1],
            a[2]*b[0] - a[0]*b[2],
            a[0]*b[1] - a[1]*b[0]            
        };
    }
    void clampPitch(){
        if(pitch > 90) pitch = 90;
        if(pitch < -90) pitch = -90;
    }
    void updateCameraMatrix(){
        updateViewMatrix();
        updateProjectionMatrix();
    }
    void resetFrameData(){
        viewMatrix = Matrix.Identity();
    }
    void printCameraInfo(){
        System.out.println("x: " + x + " y: " + y + " z: " + z);
    }
}
