package Main;

import UI.Sprite;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 8/31/2016.
 */
public class GraphicsManager implements Runnable {

    // TODO: Graphics2D should only reside in here??
    // TODO: make it a seperate thread??? (these will be VERY difficult to do)

    private boolean running;
    private BlockingQueue<Sprite> queue;
    private List<Sprite> sprites;

    GraphicsManager() {
        queue = new ArrayBlockingQueue<>(64);

        running = true;
        sprites = new ArrayList<>();
    }

    public void stop() {
        running = false;
    }

    public void put(Sprite sprite) {
        try {
            queue.put(sprite);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error: failed to queue sprite");
        }
    }

    public void draw(Graphics2D g) {
        for (Sprite s : sprites) {
            s.draw(g);
        }
    }

    private void addSprite(Sprite sprite) {

        int layer = sprite.layer;

        for (int i = 0; i < sprites.size(); i++) {
            if (sprites.get(i).layer > layer) {
                sprites.add(i, sprite);
                return;
            }
        }
        sprites.add(sprite);
    }

    public void update() {
        for (int i = sprites.size() - 1; i >= 0; i--) {
            if (!sprites.get(i).state)
                sprites.remove(i);
        }
    }

    @Override
    public void run() {
        while (running) {
            if (!queue.isEmpty()) {
                try {
                    addSprite(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.err.println("Error while taking item from queue.");
                }
            }

            // prevent CheerpJ from blocking the main thread
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
