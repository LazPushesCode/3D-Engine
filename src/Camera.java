public class Camera extends Entity{
    double FOV; // Field of view
    double aspect; // Width / length
    double far; // Far plane distance
    double near; // Near plane distance

    Matrix viewMatrix; // Position and orientation of the camera (transforms world space to view space)
    Matrix projectionMatrix; // Transforms view space to clip space

    Matrix combinedMatrix; //  Vieww * Projection

    double movementSpeed; // Speed of camera's movement
    double rotationSpeed; // Speed of camera's rotation
    double sprintSpeed; // Speed of camera's sprint

    // Initializes Camera object and values. 
    // Takes parameters representing the window's width, window's length, and camera's field of view (FOV)
    Camera(int fov){
        this.pitch = 0;
        this.yaw = 0;
        this.roll = 0;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.FOV = Math.toRadians(fov);
        this.near = 0.1;
        this.far = 1000;
        this.movementSpeed = 0.005;
        this.rotationSpeed = 0.3;
        sprintSpeed = 0.1;
        viewMatrix = Matrix.Identity();
        projectionMatrix = Matrix.Identity();
        combinedMatrix = Matrix.Identity();
        updateCameraMatrix();
    }
    // Applies current transformation on viewMatrix
    void updateViewMatrix(){
        viewMatrix = Matrix.Rotatez(roll)
        .multiply(Matrix.Rotatex(pitch))
        .multiply(Matrix.Rotatey(-yaw))
        .multiply(Matrix.Translate(-x,-y,-z));
    } 
    // Reinitializes projection matrix every frame
    // View matrix creates the view volume and perspective transformation
    void updateProjectionMatrix(){
        double t = 1/Math.tan(FOV/2);
        double [] temp = {
            t/aspect, 0, 0, 0,
            0, t, 0, 0,
            0, 0, -(far+near)/(near-far), -2*far*near/(near-far),
            0, 0, 1, 0
        };
        projectionMatrix.data = temp;
    }
    // Set x, y, and z position for camera
    Camera setCameraPosition(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
        updateCameraMatrix();
        return this;
    }
    // Takes user input to process how camera moves
    // Takes InputerManager, WindowManager, and deltaTime as parameters
    void pollInput(InputManager im, WindowManager wm, double deltaTime){
        wm.displayTilesOnScreen = im.showTiles; // If true, displays tile box boundaries
        
        // If any are true, rotate camera accordingly
        if(im.rotateUp)this.pitch += rotationSpeed * deltaTime; 
        if(im.rotateDown)this.pitch -= rotationSpeed * deltaTime;
        if(im.rotateLeft)this.yaw -= rotationSpeed * deltaTime;
        if(im.rotateRight)this.yaw += rotationSpeed * deltaTime;
        clampPitch();
        double[] forward = normalize(computeForward()); // Normalizes forward vector to length of 1
        double[] worldUp = {0,1,0}; 
        
        // Find right vector by computing cross product of up and forward vectors (producing a perpendicular vector)
        double[] right = normalize(crossProduct(worldUp,forward)); // Find cross product and normalize it right after. This is the right vector
        double speed = (im.sprint) ? sprintSpeed : movementSpeed; // If sprint is true, then set speed as the sprint speed. Else, use basic movement speed.
        if(im.forward){ // If forward boolean is true
            x += forward[0] * speed * deltaTime; // Multiply forward direction x by speed and deltaTime. Add to x
            z += forward[2] * speed * deltaTime; // Multiply forward direction z by speed and deltaTime. Add to z
        }
        if(im.backward){ // If backward boolean is true
            x -= forward[0] * speed * deltaTime; // Multiply forward direction x by speed and deltaTime. Subtract from x
            z -= forward[2] * speed * deltaTime; // Multiply forward direction z by speed and deltaTime. Subtract from z
        }
        if(im.left){ // If left boolean is true
            x -= right[0] * speed * deltaTime; // Multiply right direction x by speed and deltaTime. Subtract from x.
            z -= right[2] * speed * deltaTime; // Multiply right direction z by speed and deltaTime. Subtract from z.
        }
        if(im.right){ // If right boolean is true
            x += right[0] * speed * deltaTime; // Multiply right direction x by speed and deltaTime. Add to x.
            z += right[2] * speed * deltaTime; // Multiply right direction z by speed and deltaTime. Add to z;
        }
        if(im.up){ // If up boolean is true
            y += speed * deltaTime; // Multiply speed by deltaTime. Add to y.
        }
        if(im.down){ // If down boolean is true
            y -= speed * deltaTime; // Multiply speed by deltaTime. Subtract from y.
        }
        if(im.rollLeft){ // If rollLeft boolean is true
            roll -= 0.02*deltaTime; // Multiply 0.02 * deltaTime. Subtract from roll.
        }
         if(im.rollRight){ // If rollRight boolean is true
            roll += 0.02*deltaTime; // Multiply 0.02 * deltaTime. Add to roll.
        }
        if(im.increaseMovementSpeed){ // If increaseMovementSpeed boolean is true
            // If movementSpeed is less than 0.05 (max), then add 0.000005. else set as 0.05 (max)
            movementSpeed = (movementSpeed < 0.05) ? movementSpeed + 0.000005 : 0.05; 
        }
        if(im.decreaseMovementSpeed){ // If decreaseMovementSpeed boolean is true
            // If movementSpeed is greater than 0 (min), then subtract 0.000005. else set as 0 (min)
            movementSpeed = (movementSpeed > 0) ? movementSpeed - 0.000005 : 0;
        }
        if(im.increaseRotationSpeed){ // If increaseRotationSpeed boolean is true
            // If rotationSpeed is less than 0.3 (max), then add 0.005. else set as 0.3 (max)
            rotationSpeed = (rotationSpeed < 0.3) ? rotationSpeed + 0.005 : 0.3;
        }
        if(im.decreaseRotationSpeed){ // If decreaseRotationSpeed boolean is true
            // If rotationSpeed is greater than 0 (min), then subtract 0.005. else set as 0 (min)
            rotationSpeed = (rotationSpeed > 0) ? rotationSpeed - 0.005 : 0;
        }
    }
    // Computes forward direction, which is direction camera is facing
    double[] computeForward(){ 
        double pitchRadian = Math.toRadians(pitch); // Convert pitch degree to radians
        double yawRadian = Math.toRadians(yaw); // Convert yaw degree to radians
        // Calculate and return forward direction vector (x, y, and z)
        return new double[] {
            Math.cos(pitchRadian) * Math.sin(yawRadian),
            0,
            Math.cos(yawRadian) * Math.cos(pitchRadian)
        };
    }
    // Normalises direction's length to 1 and returns new direction
    double[] normalize(double[] direction){
        double length = 0;
        for(int i = 0; i < direction.length; i++){ // Square each value and add to sum
            length += (direction[i] * direction[i]);
        }
        length = 1/Math.sqrt(length); // Calculate square root of sum and invert (avoids unnecessary division to each value)
        for(int i = 0; i < direction.length; i++){ // Multiply each direction value by invLength to get new direction vector
            direction[i] *= length;
        }
        return direction;
    }
    // Returns cross product of two vectors
    // Used to calculate right vector using the up and forward vectors
    double[] crossProduct(double[] a, double[] b){
        return new double[]{
            a[1]*b[2] - a[2]*b[1],
            a[2]*b[0] - a[0]*b[2],
            a[0]*b[1] - a[1]*b[0]            
        };
    }
    // Clamps pitch degree to range [-90, 90]
    // Prevents camera from inverting (as if camera is upside down)
    void clampPitch(){
        if(pitch > 90) pitch = 90;
        if(pitch < -90) pitch = -90;
    }
    // Updates view and projection matrices and multiplies them to form one matrix.
    // The combined matrix will be applied to world vertices in scene class
    void updateCameraMatrix(){
        updateViewMatrix();
        updateProjectionMatrix();
        combinedMatrix = projectionMatrix.multiply(viewMatrix); // Multiplies projection matrix by view matrix (order matters!)
    }
    // Prints world x, y, and z of camera to terminal
    void printCameraInfo(){
        System.out.println("x: " + x + " y: " + y + " z: " + z);
    }
}
