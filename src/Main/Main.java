package Main;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 12/29/2014.
 */
public class Main {
    private static boolean getBooleanProperty(String prop) {
        String value = System.getProperty(prop);
        return value != null && value.equals("true");
    }

    public static boolean CHEERPJ = getBooleanProperty("warmvector-cheerpj"); // TODO: test this
    public static boolean DEBUG = getBooleanProperty("warmvector-debug");

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
