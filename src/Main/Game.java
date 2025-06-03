package Main;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 12/29/2014.
 */

import GameState.GameStateManager;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import Util.MyInputEvent;

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

    private final int MS_PER_UPDATE = 16;

    public void run() {
        long previous = System.currentTimeMillis();
        long lag = 0;
        int frameCount = 0;
        long lastCheck = System.currentTimeMillis();

        while (gsm.running) {
            long current = System.currentTimeMillis();
            long elapsed = current - previous;
            previous = current;
            lag += elapsed;

            // UPDATE
            while (lag >= MS_PER_UPDATE) {
                // Process input events
                gsm.inputHandle(inputManager.getEvents());

                // gsm.update(elapsed / MS_PER_UPDATE);
                gsm.update(0.8); // since this is a fixed update, the deltaTime should be constant
                lag -= MS_PER_UPDATE;
            }

            // RENDER
            window.render(gsm);

            frameCount++;

            // Print FPS every second
            if (Main.DEBUG) {
                if (current - lastCheck >= 1000) {
                    System.out.println("FPS: " + frameCount);
                    frameCount = 0;
                    lastCheck = current;
                }
            }

            // Small yield to prevent excessive CPU usage
            if (Main.CHEERPJ) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Interrupted while sleeping");
                }
            }
        }

        exit();
    }

    private void exit() {
        inputManager.deactivate();
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