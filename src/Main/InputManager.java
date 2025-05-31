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

    private BlockingQueue<MyInputEvent> eventQueue;
    private static final int QUEUE_SIZE = 16;

    InputManager(Component listenTarget) {
        listenTarget.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                newEvent(new MyInputEvent(MyInputEvent.MOUSE_DOWN, e.getX(), e.getY(), e.getButton()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                newEvent(new MyInputEvent(MyInputEvent.MOUSE_UP, e.getX(), e.getY(), e.getButton()));
            }
        });
        listenTarget.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                newEvent(new MyInputEvent(MyInputEvent.KEY_DOWN, e.getKeyCode()));
            }
        });
        listenTarget.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                newEvent(new MyInputEvent(MyInputEvent.MOUSE_MOVE, e.getX(), e.getY()));
            }
        });

        eventQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    }

    ArrayList<MyInputEvent> getEvents() {
        // System.out.println("debug getEvents: " + eventQueue.size());
        ArrayList<MyInputEvent> events = new ArrayList<>();
        eventQueue.drainTo(events);
        return events;
    }

    private void newEvent(MyInputEvent e) {
        // System.out.println("debug newEvent: " + e.type + " - " + e.x + ", " + e.y + "
        // - " + e.code);
        try {
            eventQueue.add(e);
        } catch (Exception exception) {
            System.err.println("BlockingQueue Exception: queue has overflowed: " + exception.toString());
        }
    }
}
