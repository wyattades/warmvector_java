package UI;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 2/26/2015.
 */
public class Sprite {

    private double orient;
    private int x, y, w, h, cornerX, cornerY, frame, step, currentStep;
    private BufferedImage[] sprites;

    private final boolean loop;
    private final int length, frameRate;

    public boolean state;
    public int layer;

    public Sprite(double _x, double _y, double _orient, int _frameRate, boolean _loop, BufferedImage[] _sprites) {
        w = _sprites[0].getWidth();
        h = _sprites[0].getHeight();
        updatePos(_x, _y, _orient);
        sprites = _sprites;
        length = _sprites.length;
        frameRate = _frameRate;
        orient = _orient;
        loop = _loop;

        frame = 0;
        state = true;
    }

    public void updatePos(double _x, double _y, double _orient) {
        x = (int) _x;
        y = (int) _y;
        cornerX = -(int) (w * 0.5);
        cornerY = -(int) (h * 0.5);
        orient = _orient;
    }

    public void draw(Graphics2D g) {
        if (orient != 0.0) {
            AffineTransform oldTForm = g.getTransform();
            g.translate(x, y);
            g.rotate(orient);
            g.drawImage(sprites[frame], cornerX, cornerY, w, h, null);
            g.setTransform(oldTForm);
        } else {
            g.drawImage(sprites[frame], x + cornerX, y + cornerY, w, h, null);
        }
    }

    public void step() {
        currentStep++;
        if (currentStep - step > frameRate) {
            step = currentStep;
            frame++;
            if (frame + 1 > length) {
                if (loop) {
                    frame = 0;
                } else {
                    state = false;
                }
            }
        }
    }

    public void setDimensions(int width, int height) {
        w = width;
        h = height;
        updatePos(x, y, orient);
    }
}
