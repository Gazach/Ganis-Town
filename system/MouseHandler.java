package system;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Kode untuk ngehandle input mouse, termasuk posisi mouse dan klik kiri

public class MouseHandler implements MouseListener, MouseMotionListener {
    private GamePanel gp;

    public int mouseX = -1;
    public int mouseY = -1;
    public boolean leftPressed = false;
    private boolean leftClicked = false;

    public MouseHandler(GamePanel gp) {
        this.gp = gp;
    }

    private void updateMousePosition(MouseEvent e) {
        int panelX = e.getX();
        int panelY = e.getY();

        double scaleX = (double) gp.getWidth() / gp.besarLayar;
        double scaleY = (double) gp.getHeight() / gp.tinggiLayar;
        double scale = Math.min(scaleX, scaleY);

        int drawWidth = (int) (gp.besarLayar * scale);
        int drawHeight = (int) (gp.tinggiLayar * scale);
        int xOffset = (gp.getWidth() - drawWidth) / 2;
        int yOffset = (gp.getHeight() - drawHeight) / 2;

        if (panelX < xOffset || panelX > xOffset + drawWidth || panelY < yOffset || panelY > yOffset + drawHeight) {
            mouseX = -1;
            mouseY = -1;
            return;
        }

        mouseX = (int) ((panelX - xOffset) / scale);
        mouseY = (int) ((panelY - yOffset) / scale);
    }

    public boolean consumeLeftClick() {
        if (leftClicked) {
            leftClicked = false;
            return true;
        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        updateMousePosition(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftPressed = true;
            leftClicked = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        updateMousePosition(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftPressed = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        updateMousePosition(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseX = -1;
        mouseY = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateMousePosition(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMousePosition(e);
    }
}
