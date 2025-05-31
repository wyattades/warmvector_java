package GameState;

import Main.AssetManager;
import Main.AudioManager;
import Main.GraphicsManager;
import Main.Window;
import UI.MouseCursor;
import Util.MyInputEvent;

import java.awt.*;
import java.util.ArrayList;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 1/25/2015.
 */
public class GameStateManager {

    // This is the only object visible in all gameStates
    // TODO put somewhere else?
    public MouseCursor cursor;

    public static final int
    // States
    INTRO = 0,
            MAINMENU = 1,
            PLAY = 2,
            NEXTLEVEL = 3,
            FADEIN = 4,
            PAUSE = 5,
            FADEOUT = 6,

            // Layers
            MAIN = 0,
            TOP = 1,
            LOADING = 2,

            LAYERS = 3;

    private GameState[] layers;

    public AssetManager assetManager;
    public AudioManager audioManager;
    public GraphicsManager graphicsManager;
    public Window window;

    public boolean running;

    public GameStateManager(AssetManager _assetManager, AudioManager _audioManager,
            GraphicsManager _graphicsManager, Window _window) {
        assetManager = _assetManager;
        audioManager = _audioManager;
        graphicsManager = _graphicsManager;
        window = _window;

        running = true;

        layers = new GameState[LAYERS];

        setState(INTRO, MAIN);

    }

    private void initState(int state, int layer) {

        switch (state) {
            case INTRO:
                layers[layer] = new IntroState(this);
                break;
            case MAINMENU:
                layers[layer] = new StartMenuState(this);
                break;
            case FADEIN:
                layers[layer] = new FadeInState(this);
                break;
            case PLAY:
                layers[layer] = new PlayState(this);
                break;
            case NEXTLEVEL:
                layers[layer] = new NextLevelState(this);
                break;
            case PAUSE:
                layers[layer] = new PauseState(this);
                break;
            case FADEOUT:
                layers[layer] = new FadeOutState(this);
                break;
        }

        layers[layer].load();

        if (layer == TOP) {
            while (!assetManager.isAvailable())
                ;
            layers[TOP].init();
        } else {
            if (assetManager.isAvailable()) {
                layers[layer].init();
            } else {
                layers[LOADING] = new LoadingState(this);
                layers[LOADING].init();
            }
        }

    }

    public void setState(int state, int layer) {
        if (layers[layer] != null)
            unloadState(layer);
        initState(state, layer);
    }

    public void unloadState(int layer) {
        layers[layer].unload();

        while (!assetManager.isAvailable())
            ;

        layers[layer] = null;
    }

    public void inputHandle(ArrayList<MyInputEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        for (MyInputEvent event : events) {
            if (layers[LOADING] == null) {
                if (layers[TOP] != null) {
                    layers[TOP].inputHandle(event);
                } else if (layers[MAIN] != null) {
                    layers[MAIN].inputHandle(event);
                } else {
                    System.out.println("Warning: All layers are null during inputHandle()");
                }
            }
        }
    }

    public void update(double deltaTime) {
        if (layers[LOADING] != null) {
            layers[LOADING].update(deltaTime);
            if (assetManager.isAvailable()) {
                layers[LOADING] = null;
                layers[MAIN].init();
            }
        } else if (layers[TOP] != null) {
            layers[TOP].update(deltaTime);
        } else if (layers[MAIN] != null) {
            layers[MAIN].update(deltaTime);
        } else {
            System.out.println("Warning: All layers are null during update()");
        }
    }

    public void draw(Graphics2D g) {
        if (layers[LOADING] != null) {
            layers[LOADING].draw(g);
        } else if (layers[MAIN] != null) {
            layers[MAIN].draw(g);
            if (layers[TOP] != null) {
                layers[TOP].draw(g);
            }
        } else {
            System.out.println("Warning: All layers are null during draw()");
        }
    }

    public void quit() {
        running = false;
    }

}
