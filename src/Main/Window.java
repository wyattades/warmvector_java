package Main;

import GameState.GameStateManager;
import Util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 12/15/2015.
 */

public class Window {

    private static String OS;

    // Dimensions
    public static final int WIDTH;
    public static final int HEIGHT;
    public static final double SCALE;

    static {
        OS = System.getProperty("os.name");
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // For maximized window, we can use maximum window bounds.
        Rectangle winSize = env.getMaximumWindowBounds();
        WIDTH = winSize.width;
        HEIGHT = winSize.height;

        System.out.println("Window Init: OS=" + OS + ", WIDTH=" + WIDTH + ", HEIGHT=" + HEIGHT);
        SCALE = HEIGHT / 1080.0; // TODO: why is this hardcoded?
    }

    private DrawingPanel drawingPanel; // Changed from Canvas
    private BufferedImage backBuffer;
    private Graphics2D backBufferGraphics; // Renamed for clarity from 'graphics'
    private JFrame frame;

    // Inner class for custom drawing using JPanel
    private class DrawingPanel extends JPanel {
        public DrawingPanel() {
            // Optimize JPanel for custom drawing
            setOpaque(true);
            // setIgnoreRepaint(true); // Temporarily commented out for debugging blank
            // screen
            setBackground(Color.BLACK); // Set a default background
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WIDTH, HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Important for Swing's painting integrity
            if (backBuffer != null) {
                g.drawImage(backBuffer, 0, 0, this); // 'this' refers to the DrawingPanel
            }
        }
    }

    public Component getInputTarget() {
        return drawingPanel;
    }

    public Window(WindowAdapter exitOperation) {
        System.out.println("Creating window with JPanel...");

        // JFrame
        frame = new JFrame("WarmVector");
        frame.addWindowListener(exitOperation);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setUndecorated(true); // For borderless window
        frame.setResizable(false);

        // DrawingPanel (custom JPanel)
        drawingPanel = new DrawingPanel();
        drawingPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT)); // Explicitly set preferred size
        drawingPanel.setFocusable(true); // Ensure the panel can receive focus
        frame.getContentPane().add(drawingPanel, BorderLayout.CENTER);

        // Set frame size explicitly, then maximize, then visible
        frame.setSize(WIDTH, HEIGHT);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        // Request focus for the drawing panel AFTER the frame is visible
        boolean focusRequested = drawingPanel.requestFocusInWindow();
        System.out.println("DrawingPanel focus requested in window: " + focusRequested);

        // Log panel size after frame is visible
        if (drawingPanel != null) {
            SwingUtilities.invokeLater(() -> { // Ensure we get size after EDT has processed layout
                System.out.println("DrawingPanel size: "
                        + drawingPanel.getWidth() + "x" + drawingPanel.getHeight());
            });
        } else {
            System.out.println("DrawingPanel is null.");
        }

        // Fullscreen logic simplified: Relies on undecorated + maximized_both
        // The previous device.setFullScreenWindow() logic has been removed.

        // Set default mouse cursor to transparent
        // Ensure this is done after frame is visible and has a peer
        Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                Toolkit.getDefaultToolkit().createImage(
                        new MemoryImageSource(16, 16, new int[16 * 16], 0, 16)),
                new Point(0, 0), "invisibleCursor");
        frame.setCursor(transparentCursor);

        // Create back buffer based on panel's actual size if possible, or fixed size
        // Using fixed WIDTH/HEIGHT as per original logic
        backBuffer = ImageUtils.getCompatableVersion(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB));
        backBufferGraphics = (Graphics2D) backBuffer.getGraphics();

        if (backBufferGraphics == null) {
            OutputManager.fatalAlert("Failed to create backBufferGraphics.");
            // Consider throwing an exception here to halt initialization
            return;
        }

        // Set rendering hints for the backBuffer's graphics context
        setQuality(
                OutputManager.getSetting("quality") == 1);

        System.out.println("Window and backBuffer graphics initialized.");
    }

    public void render(GameStateManager gsm) {
        if (backBufferGraphics == null)
            return; // Safety check

        // Clear backBuffer
        backBufferGraphics.setColor(Color.BLACK);
        backBufferGraphics.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw game state to backBuffer
        gsm.draw(backBufferGraphics);

        // Request the DrawingPanel to repaint itself (will call paintComponent)
        if (drawingPanel != null) {
            drawingPanel.repaint();
        }

        // Optional: Toolkit.getDefaultToolkit().sync();
        // This can sometimes help ensure drawing is flushed, especially on Linux.
        // Might be needed in CheerpJ if visual updates are laggy, but can also impact
        // performance.
        // Only use if necessary.
    }

    public void exit() {
        if (backBufferGraphics != null) {
            backBufferGraphics.dispose();
        }
        if (frame != null) {
            frame.dispose();
        }
    }

    public void setQuality(boolean betterQuality) {
        if (backBufferGraphics == null)
            return;
        backBufferGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                betterQuality ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                        : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        // Add other hints like KEY_ANTIALIASING, KEY_RENDERING if needed
        // backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        // RenderingHints.VALUE_ANTIALIAS_ON);
        // backBufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
        // RenderingHints.VALUE_RENDER_QUALITY);
    }
}
