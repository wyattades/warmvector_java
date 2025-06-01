package Main;

import Util.MyInputEvent;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 12/29/2014.
 */
class InputManager {

    private final BlockingQueue<MyInputEvent> eventQueue;
    private static final int QUEUE_SIZE = 256;
    private volatile boolean isActive = true;
    private final Component listenTarget;

    // Store references to listeners so we can remove them
    private final MouseAdapter mouseAdapter;
    private final MouseMotionAdapter motionAdapter;
    private final KeyAdapter keyAdapter;

    InputManager(Component listenTarget) {
        this.listenTarget = listenTarget;
        eventQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // System.out.println("mousePressed event: " + e);
                newEvent(new MyInputEvent(MyInputEvent.MOUSE_DOWN, e.getX(), e.getY(),
                        e.getButton()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // System.out.println("mouseReleased event: " + e);
                newEvent(new MyInputEvent(MyInputEvent.MOUSE_UP, e.getX(), e.getY(),
                        e.getButton()));
            }
        };

        motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // System.out.println("mouseMoved event: " + e);
                newEvent(new MyInputEvent(MyInputEvent.MOUSE_MOVE, e.getX(), e.getY()));
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // System.out.println("keyPressed event: " + e);
                newEvent(new MyInputEvent(MyInputEvent.KEY_DOWN, e.getKeyCode()));
            }
        };

        listenTarget.addMouseListener(mouseAdapter);
        listenTarget.addMouseMotionListener(motionAdapter);
        listenTarget.addKeyListener(keyAdapter);
    }

    ArrayList<MyInputEvent> getEvents() {
        ArrayList<MyInputEvent> events = new ArrayList<>();
        if (eventQueue.isEmpty()) {
            return events;
        }

        eventQueue.drainTo(events);

        // if (Main.DEBUG && !events.isEmpty()) {
        // System.out.println("Processing " + events.size() + " events");
        // }

        return events;
    }

    private void newEvent(MyInputEvent e) {
        if (!isActive)
            return;

        System.out.println("Adding event: " + e);

        try {
            eventQueue.add(e);
        } catch (Exception exception) {
            System.err.println("Error adding event to queue: " + exception);
            exception.printStackTrace();

        }
    }

    public void deactivate() {
        if (Main.DEBUG)
            System.out.println("Deactivating input manager...");
        isActive = false;

        // Remove all listeners
        listenTarget.removeMouseListener(mouseAdapter);
        listenTarget.removeMouseMotionListener(motionAdapter);
        listenTarget.removeKeyListener(keyAdapter);

    }
}
