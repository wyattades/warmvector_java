package UI;

import GameState.GameStateManager;
import Main.Window;
import Main.Main;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 3/7/2015.
 */
public class MouseCursor {

    public static final int CURSOR = 1, CROSSHAIR = 2; //,NONE = 0;
    public int x, y;

    private int w, h, oldX, oldY;
    private BufferedImage sprite, cursor, crosshair;
    private Robot robot;

    public MouseCursor(GameStateManager gsm) {

        cursor = (BufferedImage)gsm.assetManager.getAsset("cursor.png");
        crosshair = (BufferedImage)gsm.assetManager.getAsset("crosshair.png");

        sprite = cursor;

        w = sprite.getWidth();
        h = sprite.getHeight();


        if (!Main.CHEERPJ) {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                System.out.println("Error: Failed to create mouse Robot: " + e.toString());
                System.exit(1);
            }
        }

    }

    public void setSprite(int type) {
        switch (type) {
            case CURSOR:
                sprite = cursor;
                break;
            case CROSSHAIR:
                sprite = crosshair;
                break;
        }
        w = sprite.getWidth();
        h = sprite.getHeight();
    }

    public void draw(Graphics2D g) {
        g.drawImage(sprite, x - (int)(w * 0.5), y - (int)(h * 0.5), null);
    }

    public void setPosition(int new_x, int new_y) {
        x = constrain(new_x, 0, Window.WIDTH);
        y = constrain(new_y, 0, Window.HEIGHT);
    }

    public void setMouse(int x, int y) {
        if (!Main.CHEERPJ) {
            robot.mouseMove(x, y);
        }
        setPosition(x, y);
    }

    private int constrain(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public void saveOldPos() {
        oldX = x;
        oldY = y;
    }

    public void loadOldPos() {
        if (!Main.CHEERPJ) {
            robot.mouseMove(oldX, oldY);
        }
        x = oldX;
        y = oldY;
    }
}
