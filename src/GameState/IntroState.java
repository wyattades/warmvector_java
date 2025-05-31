package GameState;

import Main.Window;
import UI.MouseCursor;
import UI.Sprite;
import Util.MyInputEvent;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 1/25/2015.
 */
public class IntroState extends GameState {

    private Sprite intro;

    public IntroState(GameStateManager _gsm) {
        super(_gsm);
    }

    public void load() {
        gsm.assetManager.loadAssets(new String[] { "start_menu.mp3", "intro_", "cursor.png", "crosshair.png" });
    }

    public void init() {
        gsm.cursor = new MouseCursor(gsm);

        intro = new Sprite(Window.WIDTH * 0.5, Window.HEIGHT * 0.5, 0, 40, false,
                (BufferedImage[]) gsm.assetManager.getAsset("intro_"));
        intro.setDimensions(Window.WIDTH, Window.HEIGHT);

        gsm.audioManager.playSong("start_menu.mp3");
    }

    public void unload() {
    }

    public void draw(Graphics2D g) {
        intro.draw(g);
    }

    public void update(double deltaTime) {
        intro.step();

        if (!intro.state) {
            gsm.setState(GameStateManager.MAINMENU, GameStateManager.MAIN);
        }

    }

    public void inputHandle(MyInputEvent event) {
        if ((event.type == MyInputEvent.KEY_DOWN && event.code == KeyEvent.VK_SPACE) ||
                (event.type == MyInputEvent.MOUSE_DOWN && event.code == MouseEvent.BUTTON1)) {
            gsm.setState(GameStateManager.MAINMENU, GameStateManager.MAIN);
        }
    }

}
