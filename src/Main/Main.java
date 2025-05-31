package Main;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 12/29/2014.
 */
public class Main {
    public static boolean CHEERPJ = true; // TODO: test this

    public static boolean DEBUG = false;
    static {
        try {
            String prop = System.getProperty("warmvector-debug");
            if (prop != null && prop.equals("true"))
                DEBUG = true;
        } catch (Exception e) {
        }

        try {
            String prop = System.getProperty("cheerpj");
            if (prop != null && prop.equals("true"))
                CHEERPJ = true;
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        // System.setProperty("sun.java2d.opengl","True");
        // -Dsun.java2d.accthreshold=0

        AssetManager assetManager = new AssetManager();
        AudioManager audioManager = new AudioManager(assetManager);
        GraphicsManager graphicsManager = new GraphicsManager();

        Game game = new Game(audioManager, graphicsManager, assetManager);

        // TODO
        if (!Main.CHEERPJ) {
            new Thread(audioManager, "audio").start();
        }

        // not implemented yet
        // new Thread(graphicsManager, "graphics").start();

        game.run();
    }

}
