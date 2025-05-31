package Main;

import Util.MyInputEvent;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 12/29/2014.
 */
class InputManager {

    private BlockingQueue<MyInputEvent> eventQueue;
    private static final int QUEUE_SIZE = 256; // Increased queue size
    private volatile boolean isActive = true;

    InputManager(Component listenTarget) {
        eventQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

        listenTarget.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isActive) {
                    newEvent(new MyInputEvent(MyInputEvent.MOUSE_DOWN, e.getX(), e.getY(), e.getButton()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isActive) {
                    newEvent(new MyInputEvent(MyInputEvent.MOUSE_UP, e.getX(), e.getY(), e.getButton()));
                }
            }
        });

        listenTarget.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isActive) {
                    newEvent(new MyInputEvent(MyInputEvent.MOUSE_MOVE, e.getX(), e.getY()));
                }
            }
        });

        listenTarget.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isActive) {
                    newEvent(new MyInputEvent(MyInputEvent.KEY_DOWN, e.getKeyCode()));
                }
            }
        });
    }

    ArrayList<MyInputEvent> getEvents() {

        // System.out.println("debug getEvents: " + eventQueue.size());
        ArrayList<MyInputEvent> events = new ArrayList<>();
        if (eventQueue.isEmpty()) {
            return events;
        }
        eventQueue.drainTo(events);
        return events;
    }

    private void newEvent(MyInputEvent e) {
        if (!isActive)
            return;

        try {
            boolean added = eventQueue.offer(e, 100, TimeUnit.MILLISECONDS); // Use offer with timeout instead of add
            if (!added) {
                System.err.println("Failed to add event to queue: Queue full");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while adding event to queue");
        } catch (Exception exception) {
            System.err.println("Error adding event to queue: " + exception.toString());
            exception.printStackTrace();
        }
    }

    public void deactivate() {
        isActive = false;
        // listenTarget.removeMouseListener(TODO);
        // listenTarget.removeMouseMotionListener(TODO);
        // listenTarget.removeKeyListener(TODO);
    }
}
