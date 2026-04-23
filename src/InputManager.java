import java.awt.event.*;

public class InputManager implements KeyListener{
    boolean forward, backward, left, right, up, down, ru, rd, rl, rr, showTiles = false;


    @Override
    public void keyPressed(KeyEvent k){
        switch(k.getKeyCode()){
            case KeyEvent.VK_W:
                forward = true;
                break;
            case KeyEvent.VK_S:
                backward = true;
                break;
            case KeyEvent.VK_A:
                left = true;
                break;
            case KeyEvent.VK_D:
                right = true;
                break;
            case KeyEvent.VK_UP:
                ru = true;
                break;
            case KeyEvent.VK_DOWN:
                rd = true;
                break;
            case KeyEvent.VK_LEFT:
                rl = true;
                break;
            case KeyEvent.VK_RIGHT:
                rr = true;
                break;
            case KeyEvent.VK_SHIFT:
                down = true;
                break;
            case KeyEvent.VK_SPACE:
                up = true;
                break;
            case KeyEvent.VK_G:
                showTiles = (showTiles) ? false : true;
                break;
        }
    }
    @Override
    public void keyReleased(KeyEvent k){
        switch(k.getKeyCode()){
            case KeyEvent.VK_W:
                forward = false;
                break;
            case KeyEvent.VK_S:
                backward = false;
                break;
            case KeyEvent.VK_A:
                left = false;
                break;
            case KeyEvent.VK_D:
                right = false;
                break;
            case KeyEvent.VK_UP:
                ru = false;
                break;
            case KeyEvent.VK_DOWN:
                rd = false;
                break;
            case KeyEvent.VK_LEFT:
                rl = false;
                break;
            case KeyEvent.VK_RIGHT:
                rr = false;
                break;
            case KeyEvent.VK_SHIFT:
                down = false;
                break;
            case KeyEvent.VK_SPACE:
                up = false;
                break;
        }
    }
    @Override
    public void keyTyped(KeyEvent k){
        
    }
}
