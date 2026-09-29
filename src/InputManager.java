import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputManager implements KeyListener{
    boolean forward, backward, left, right, up, down, ru, rd, rl, rr, rollLeft, rollRight, sprint,
     increaseMovementSpeed, decreaseMovementSpeed, increaseRotationSpeed, decreaseRotationSpeed, action, action2, action3,
      showTiles = false;


    @Override
    public void keyPressed(KeyEvent k){
        switch(k.getKeyCode()){
            case KeyEvent.VK_W -> forward = true;
            case KeyEvent.VK_S -> backward = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_UP -> ru = true;
            case KeyEvent.VK_DOWN -> rd = true;
            case KeyEvent.VK_LEFT -> rl = true;
            case KeyEvent.VK_RIGHT -> rr = true;
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
    @Override
    public void keyReleased(KeyEvent k){
        switch(k.getKeyCode()){
            case KeyEvent.VK_W -> forward = false;
            case KeyEvent.VK_S -> backward = false;
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
            case KeyEvent.VK_UP -> ru = false;
            case KeyEvent.VK_DOWN -> rd = false;
            case KeyEvent.VK_LEFT -> rl = false;
            case KeyEvent.VK_RIGHT -> rr = false;
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
