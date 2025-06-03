package Main;

import GameState.GameStateManager;
import Util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 12/15/2015.
 */

public class Window {

    private static GraphicsDevice device;
    private static String OS;

    // Dimensions
    public static final int WIDTH;
    public static final int HEIGHT;
    public static final double SCALE;
    static {
        OS = System.getProperty("os.name");

        // Dimensions
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        device = env.getDefaultScreenDevice();
        if (!device.isFullScreenSupported() || OS.equals("Linux")) {
            Rectangle winSize = env.getMaximumWindowBounds();
            WIDTH = winSize.width;
            HEIGHT = winSize.height;
        } else {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            WIDTH = (int) screenSize.getWidth();
            HEIGHT = (int) screenSize.getHeight();
        }

        System.out.println("Loading window: OS=" + OS + " WIDTH=" + WIDTH + " HEIGHT=" + HEIGHT);

        SCALE = HEIGHT / 1080.0;
    }

    private Canvas canvas;
    private BufferedImage backBuffer;
    private Graphics2D graphics;
    private JFrame frame;

    public Component getInputTarget() {
        return canvas;
    }

    public Window(WindowAdapter exitOperation) {
        System.out.println("Creating window...");

        // JFrame
        frame = new JFrame("WarmVector");
        frame.addWindowListener(exitOperation);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);
        frame.setUndecorated(true);

        // Canvas
        canvas = new Canvas(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration());
        canvas.setSize(WIDTH, HEIGHT);

        // NOTE: in a previous version of this code, idk why we did:
        // `canvas.createBufferStrategy(2)` and then `canvas.getBufferStrategy()`,

        // Add canvas before making frame visible
        frame.add(canvas, 0);
        frame.setVisible(true);

        // Fullscreen handling
        if (device.isFullScreenSupported() && !OS.equals("Linux")) {
            try {
                device.setFullScreenWindow(frame);
            } finally {
                device.setFullScreenWindow(null);
            }
        }

        // Set default mouse cursor to transparent
        Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                Toolkit.getDefaultToolkit().createImage(
                        new MemoryImageSource(16, 16, new int[16 * 16], 0, 16)),
                new Point(0, 0), "invisibleCursor");
        frame.setCursor(transparentCursor);

        canvas.setVisible(true);

        // Create back buffer
        backBuffer = ImageUtils.getCompatableVersion(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB));

        graphics = (Graphics2D) backBuffer.getGraphics();
        if (graphics == null) {
            OutputManager.fatalAlert("Failed to create graphics context.");
        }

        // Set rendering hints
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                OutputManager.getSetting("quality") == 1 ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                        : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        System.out.println("Graphics initialized.");
    }

    // private boolean updateScreen() {
    // graphics.dispose();
    // graphics = null;
    // try {
    // strategy.show();
    // Toolkit.getDefaultToolkit().sync();
    // return (!strategy.contentsLost());

    // } catch (NullPointerException | IllegalStateException e) {
    // return true;

    // }

    public void render(GameStateManager gsm) {

        // Clear background
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        // System.out.println("Cleared background");

        gsm.draw(graphics);

        // Draw to screen
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        if (g == null) {
            System.out.println("WARNING: Could not get canvas graphics for rendering");
            return;
        }

        // System.out.println("Got canvas graphics, drawing to screen...");
        g.drawImage(backBuffer, 0, 0, null);
        g.dispose();
        canvas.repaint();
        frame.repaint();
        // System.out.println("Frame rendered successfully");

    }

    public void exit() {
        graphics.dispose();
        frame.dispose();
    }

    public void setQuality(boolean better) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                better ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }
}
