package GameState;

import Main.OutputManager;
import Main.Window;
import Util.MyInputEvent;

import java.awt.*;

/**
 * Directory: WarmVector_Client_Singleplayer/GameState/
 * Created by Wyatt on 8/20/2015.
 */
public class FadeInState extends GameState {

    private static final double RATE = 5.0;

    private double opacity;

    private String backgroundMusic;

    public FadeInState(GameStateManager _gsm) {
        super(_gsm);
    }

    public void load() {
        int musicLevel = OutputManager.getSetting("level") % 3;
        backgroundMusic = "background" + musicLevel + ".mp3";

        gsm.assetManager.loadAssets(new String[] { backgroundMusic });
    }

    public void init() {
        gsm.audioManager.playSong(backgroundMusic);
        opacity = 255;
        gsm.cursor.setMouse((int) (Main.Window.WIDTH * 0.5 + 70), (int) (Window.HEIGHT * 0.5));
    }

    public void unload() {
        gsm.cursor.setMouse((int) (Window.WIDTH * 0.5) + 70, (int) (Window.HEIGHT * 0.5));
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, (int) Math.max(0.0, opacity)));
        g.fillRect(0, 0, Window.WIDTH, Window.HEIGHT);
    }

    public void update(double deltaTime) {
        opacity -= RATE;
        if (opacity < 0) {
            gsm.unloadState(GameStateManager.TOP);
        }

    }

    public void inputHandle(MyInputEvent event) {
    }

}
