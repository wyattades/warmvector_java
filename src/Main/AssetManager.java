package Main;

import Util.ImageUtils;
// import javafx.scene.media.AudioClip;
// import javafx.scene.media.Media;
import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 9/8/2016.
 * Last edited by Wyatt on 9/8/2016.
 */
public class AssetManager {
    // public enum AssetType {
    // SHORT_AUDIO,
    // LONG_AUDIO,
    // IMAGE,
    // ANIMATION
    // }

    // public class Asset {

    // public String fileName;
    // public AssetType type;
    // public int animationCount = 0;

    // public Asset(AssetType type, String fileName) {
    // this.type = type;
    // this.fileName = fileName;
    // }

    // public Asset(AssetType type, String fileName, int animationCount) {
    // this.type = type;
    // this.fileName = fileName;
    // this.animationCount = animationCount;
    // }

    // }

    private ConcurrentHashMap<String, Object> assets;

    private ExecutorService executor;

    AssetManager() {
        assets = new ConcurrentHashMap<>();
        System.out.println("AssetManager initialized");
    }

    private Object loadAsset(String fileName) {
        Object asset = assets.get(fileName);
        if (asset != null) {
            return asset;
        }

        if (fileName.endsWith(".mp3")) {
            asset = loadLongAudio(fileName);
        } else if (fileName.endsWith(".wav")) {
            asset = loadShortAudio(fileName);
        } else if (fileName.endsWith(".png")) {
            asset = loadImage("resources/Images/" + fileName);
        } else if (fileName.endsWith("_")) {
            asset = loadAnimation(fileName);
        } else {
            System.err.println("Error: invalid file type requested: " + fileName);
            System.exit(1);
        }

        if (asset == null) {
            System.err.println("Failed to load asset: " + fileName);
            System.exit(1);
        }

        assets.put(fileName, asset);
        System.out.println("Loaded asset: " + fileName);
        return asset;
    }

    public void loadAssets(String[] fileNames) {
        if (!isAvailable()) {
            System.err.println(
                    "Error: cannot add to loading queue while loader is running." + Arrays.toString(fileNames));
            System.exit(1);
        }

        if (Main.CHEERPJ) {
            // Load synchronously in CheerpJ environment
            System.out.println("Loading assets synchronously for CheerpJ...");
            for (String fileName : fileNames) {

                // skip .mp3 files for now
                if (fileName.endsWith(".mp3")) {
                    continue;
                }

                loadAsset(fileName);
            }
        } else {
            // Use executor for async loading in desktop environment
            executor = Executors.newSingleThreadExecutor();
            for (String fileName : fileNames) {
                executor.execute(() -> loadAsset(fileName));
            }
            executor.shutdown();
        }
    }

    public boolean isAvailable() {
        return Main.CHEERPJ || executor == null || executor.isTerminated();
    }

    public Clip getSFX(String name) {
        return (Clip) loadAsset(name);
    }

    public Object getAsset(String name) {
        Object asset = null;
        if (isAvailable()) {
            asset = assets.get(name);
        } else {
            System.err.println("Error: cannot access asset " + name + " while loader is running.");
            System.exit(1);
        }
        if (asset == null) {
            OutputManager.fatalAlert("Error: asset " + name + " has not been loaded.");
        }
        return asset;
    }

    public void stop() {
        executor.shutdownNow();
        unload();
    }

    public void unload() {
        if (isAvailable()) {
            assets.clear();
        } else {
            System.err.println("Error: cannot unload assets while loader is running.");
            System.exit(1);
        }
    }

    public void unload(String[] fileNames) {
        if (isAvailable()) {
            for (String fileName : fileNames) {
                assets.remove(fileName);
            }
        } else {
            System.err.println("Error: cannot unload assets while loader is running.");
            System.exit(1);
        }
    }

    private static String ABS_PATH = null;
    static {

        try {
            if (Main.CHEERPJ) {
                ABS_PATH = "/app/jar";
            } else if (Main.DEBUG) {
                ABS_PATH = Paths.get("").toAbsolutePath().toString();
            } else {
                ABS_PATH = AssetManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().resolve(".")
                        .toString();
            }
        } catch (URISyntaxException e) {
            OutputManager.fatalAlert("Error: Failed to locate class location");
        }
    }

    private static File resolveFile(String relativePath) {
        String resolvedPath = ABS_PATH + "/" + relativePath;

        File file = new File(resolvedPath);

        return file;
    }

    public static Clip loadShortAudio(String name) {
        File file = resolveFile("resources/Audio/" + name);

        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            // this fails in CheerpJ v4.1 for Java 11
            clip.open(audioIn);
            return clip;
        } catch (Exception e) {
            System.err.println("Error: Failed to load Clip " + name + ": " + e.toString());
            System.out.println("AudioSystem.getMixerInfo(): " + Arrays.toString(AudioSystem.getMixerInfo()));
            System.exit(1);
            return null;
        }
    }

    // TODO
    // private Media loadLongAudio(String name) {
    private Object loadLongAudio(String name) {
        File file = resolveFile("resources/Audio/" + name);
        if (!Main.CHEERPJ) {

            // try {
            // return new Media(file.toURI().toString());
            // } catch (Exception e) {
            // e.printStackTrace();
            // OutputManager.fatalAlert("Error: Failed to load Media: " + name);
            // }
        }
        return null;
    }

    private BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(resolveFile(path));
        } catch (IOException e) {
            e.printStackTrace();
            OutputManager.fatalAlert("Error: failed to read image file: " + path);
        }
        if (image == null) {
            OutputManager.fatalAlert("Error: image path " + path + " is null");
        }
        return ImageUtils.getCompatableVersion(image);
    }

    private BufferedImage[] loadAnimation(String fileName) {
        List<BufferedImage> frames = new ArrayList<>();

        // NOTE: we used to do listFiles() here, but that doesn't work in the CheerpJ
        // `/app` directory i think
        int animCount = -1;
        if (fileName.equals("intro_")) {
            animCount = 7;
        } else if (fileName.equals("hit_")) {
            animCount = 4;
        } else {
            OutputManager.fatalAlert("Error: unknown animation name: " + fileName);
        }

        for (int i = 0; i < animCount; i++) {
            frames.add(loadImage("resources/Animations/" + fileName + "/" + fileName + i + ".png"));
        }

        return frames.toArray(new BufferedImage[frames.size()]);
    }

}
