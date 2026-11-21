import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputManager implements KeyListener{
    boolean 
    forward, backward, left, right, up, down, // Transaltes camera's x, y, and z
    rotateUp, rotateDown, rotateLeft, rotateRight, // Rotates camera's yaw and pitch
    rollLeft, rollRight, // Rotates camera's roll
    sprint, increaseMovementSpeed, decreaseMovementSpeed, // Alters movement speed
    increaseRotationSpeed, decreaseRotationSpeed, // Alters rotation speed
    action, action2, action3, // Other keybinds
    showTiles = false; // Shows bounding boxes of screen tiles used in rendering

    // If a key listed is pressed, will set given boolean as true
    @Override
    public void keyPressed(KeyEvent k){
        switch(k.getKeyCode()){
            case KeyEvent.VK_W -> forward = true;
            case KeyEvent.VK_S -> backward = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_UP -> rotateUp = true;
            case KeyEvent.VK_DOWN -> rotateDown = true;
            case KeyEvent.VK_LEFT -> rotateLeft = true;
            case KeyEvent.VK_RIGHT -> rotateRight = true;
            case KeyEvent.VK_SHIFT -> down = true;
            case KeyEvent.VK_SPACE -> up = true;
            case KeyEvent.VK_G -> showTiles = (showTiles) ? false : true;
            case KeyEvent.VK_1 -> increaseMovementSpeed = true;
            case KeyEvent.VK_2 -> decreaseMovementSpeed = true;
            case KeyEvent.VK_Z -> increaseRotationSpeed = true;
            case KeyEvent.VK_C -> decreaseRotationSpeed = true;
            case KeyEvent.VK_F -> action = true;
            case KeyEvent.VK_R -> action2 = true;
            case KeyEvent.VK_T -> action3 = true;
            case KeyEvent.VK_Q -> rollLeft = true;
            case KeyEvent.VK_E -> rollRight = true;
        }
    }
    // If a key listed is released, will set given boolean as false
    @Override
    public void keyReleased(KeyEvent k){
        switch(k.getKeyCode()){
            case KeyEvent.VK_W -> forward = false;
            case KeyEvent.VK_S -> backward = false;
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
            case KeyEvent.VK_UP -> rotateUp = false;
            case KeyEvent.VK_DOWN -> rotateDown = false;
            case KeyEvent.VK_LEFT -> rotateLeft = false;
            case KeyEvent.VK_RIGHT -> rotateRight = false;
            case KeyEvent.VK_SHIFT -> down = false;
            case KeyEvent.VK_SPACE -> up = false;
            case KeyEvent.VK_1 -> increaseMovementSpeed = false;
            case KeyEvent.VK_2 -> decreaseMovementSpeed = false;
            case KeyEvent.VK_Z -> increaseRotationSpeed = false;
            case KeyEvent.VK_C -> decreaseRotationSpeed = false;
            case KeyEvent.VK_CAPS_LOCK -> sprint = !sprint;
            case KeyEvent.VK_F -> action = false;
            case KeyEvent.VK_R -> action2 = false;
            case KeyEvent.VK_T -> action3 = false;
            case KeyEvent.VK_Q -> rollLeft = false;
            case KeyEvent.VK_E -> rollRight = false;
            
        }
    }
    @Override
    public void keyTyped(KeyEvent k){
        
    }
}
