package Main;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 12/13/2015.
 */
public class AudioManager implements Runnable {

    private static final int MAX_QUEUE_AMOUNT = 64;
    private boolean running;
    private volatile double musicVolume, SFXVolume;

    private BlockingQueue<Clip> clips;

    private AssetManager assetManager;

    // Placeholder for background music
    private String background;
    private ExecutorService backgroundPlayer;

    AudioManager(AssetManager assetManager) {
        this.assetManager = assetManager;

        clips = new ArrayBlockingQueue<>(MAX_QUEUE_AMOUNT);
        running = true;

        // Wait for JavaFX to initialize
        // final CountDownLatch latch = new CountDownLatch(1);
        // SwingUtilities.invokeLater(() -> {
        // new JFXPanel(); // initializes JavaFX environment
        // latch.countDown();
        // });
        // try {
        // latch.await();
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // System.exit(1);
        // }
    }

    public void stopBackground() {
        // Placeholder: implement background music stop logic with javax.sound.sampled
        // if needed
        if (backgroundPlayer != null) {
            // if (mediaPlayer != null) {
            // mediaPlayer.stop();
            // mediaPlayer.dispose();
            // mediaPlayer = null;
            // }
            backgroundPlayer.shutdownNow();
        }
    }

    // public void playSong(Media source, String name) {
    public void playSong(String name) {
        if (name.equals(background)) {
            return;
        }

        background = name;

        System.out.println("Playing song: " + name);

        if (!Main.CHEERPJ) {
            // stopBackground();
            // backgroundPlayer = Executors.newSingleThreadExecutor();
            // backgroundPlayer.execute(new Thread(() -> {

            // mediaPlayer = new MediaPlayer(source);

            // // Set song to loop
            // mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));

            // mediaPlayer.setVolume(musicVolume);

            // mediaPlayer.play();
            // }));
        } else {
            // TODO
        }
    }

    public void playSFX(String name) {
        if (!threadIsRunning) {
            return;
        }

        Clip source = assetManager.getSFX(name);

        try {
            clips.put(source);
        } catch (InterruptedException e) {
            System.err.println("playSFX queue error: " + e.toString());
            System.exit(1);
        }
    }

    private boolean threadIsRunning = false;

    @Override
    public void run() {
        threadIsRunning = true;

        // Continuously loop
        while (running) {

            // TODO: this is a bad way of updating mediaPlayer volume
            // if (mediaPlayer != null && musicVolume != mediaPlayer.getVolume()) {
            // mediaPlayer.setVolume(musicVolume);
            // }

            while (!clips.isEmpty()) {
                Clip source = clips.poll();
                if (source == null)
                    break;

                // javafx version:
                // AudioClip source = clips.poll();
                // source.play(SFXVolume);

                if (source.isRunning()) {
                    source.stop();
                }
                source.setFramePosition(0);
                FloatControl gainControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (20.0 * Math.log10(SFXVolume <= 0.0 ? 0.0001 : SFXVolume));
                gainControl.setValue(dB);
                source.start();

            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // Stop and dispose mediaPlayer when thread closes, idk if this is necessary
        // if (mediaPlayer != null) {
        // mediaPlayer.stop();
        // mediaPlayer.dispose();
        // }
    }

    public void setVolume(double SFXSetting, double musicSetting) {
        SFXVolume = SFXSetting * 0.25;
        musicVolume = musicSetting * 0.25;
    }

    public void stop() {
        running = false;
    }

}
