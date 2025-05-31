package Main;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 12/29/2014.
 */

import GameState.GameStateManager;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Game {

    // Manager stuff
    private AssetManager assetManager;
    private GraphicsManager graphicsManager;
    private AudioManager audioManager;
    private Window window;
    private InputManager inputManager;

    private GameStateManager gsm;

    Game(AudioManager _audioManager, GraphicsManager _graphicsManager, AssetManager _assetManager) {

        assetManager = _assetManager;
        audioManager = _audioManager;
        graphicsManager = _graphicsManager;

        _audioManager.setVolume(OutputManager.getSetting("sfx_volume"), OutputManager.getSetting("music_volume"));

        window = new Window(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                gsm.quit();
            }
        });

        // Create a manager for handling key and mouse inputs
        inputManager = new InputManager(window.getInputTarget());

        // Create a manager for handling the different game states e.g. intro, play,
        // gameOver
        gsm = new GameStateManager(assetManager, audioManager, graphicsManager, window);

    }

    public void run() {

        double previous = System.currentTimeMillis();
        double lag = 0.0;
        final double MS_PER_UPDATE = 16;

        while (gsm.running) {
            double current = System.currentTimeMillis();
            double elapsed = current - previous;
            previous = current;
            lag += elapsed;

            // UPDATE
            if (lag >= MS_PER_UPDATE) {
                gsm.update(elapsed / MS_PER_UPDATE);

                gsm.inputHandle(inputManager.getEvents());

                lag -= MS_PER_UPDATE;
            }

            // RENDER
            window.render(gsm);

            // Thread.yield();
        }

        // int FPS = 60;
        // long fpsWait = (long) (1000 / FPS);
        // long renderTime = 0;
        // while (gsm.running) {
        //
        // long renderStart = System.nanoTime();
        //
        // // UPDATE
        // gsm.update(renderTime);
        //
        // // RENDER
        // window.render(gsm);
        //
        // // fps limiting
        // renderTime = (System.nanoTime() - renderStart) / 1000000;
        // try {
        // Thread.sleep(Math.max(0, fpsWait - renderTime));
        // } catch (InterruptedException e) {
        // Thread.interrupted();
        // break;
        // }
        // renderTime = (System.nanoTime() - renderStart) / 1000000;
        // }

        exit();

    }

    private void exit() {

        audioManager.stop();
        graphicsManager.stop();
        assetManager.stop();

        window.exit();

        System.out.println("Closing program...");
        OutputManager.saveAllSettings();
        System.out.println("--- LOG END at " + OutputManager.currentTime() + " ---");

        System.exit(0);

    }

    public static int currentTimeMillis() {
        long millisLong = System.currentTimeMillis();
        while (millisLong > Integer.MAX_VALUE) {
            millisLong -= Integer.MAX_VALUE;
        }
        return (int) millisLong;
    }

}