package Main;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by wyatt on 8/17/2015.
 */
public class OutputManager {

    private static final DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    public static String currentTime() {
        return df.format(new Date());
    }
    
    private static String userPath;

    public static HashMap<String, Integer> settings;
    public static HashMap<String, Integer> saves;
    
    
    static {
        // Default settings
        settings = new HashMap<>();
        settings.put("fix_bugs", 0);
        settings.put("quality", 0);
        settings.put("cave_mode", 0);
        settings.put("music_volume", 2);
        settings.put("sfx_volume", 2);
        settings.put("x_sensitivity", 1);
        settings.put("y_sensitivity", 1);
        settings.put("level", 1);
        
        // Default save parameters
        saves = new HashMap<>();
        saves.put("level", 1);


        if (!Main.CHEERPJ) {
            // Get user's default directory
            JFileChooser fr = new JFileChooser();
            FileSystemView fw = fr.getFileSystemView();
            userPath = fw.getDefaultDirectory().getPath() + "/My Games/WarmVector/";

            File userDirectory = new File(userPath);
            userDirectory.mkdirs();
        } else {
            userPath = "/files/";
        }

        // Set up logging to file log.txt
        if (!Main.CHEERPJ) {
            // Make sure log file exists
            File logFile = new File(userPath + "log.txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    fatalAlert("Failed to create log file: " + userPath + "log.txt\n" + e.toString());
                }
            }

            // Set program output to print to log
            try {
                PrintStream out = new PrintStream(new FileOutputStream(userPath + "log.txt", true));
                System.setOut(out);
                System.setErr(out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        System.out.println("\n--- LOG START at " + currentTime() + " ---");


        // Make sure save file exists
        File saveFile = new File(userPath + "save");
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to create save file.");
            }
        }

        // Make sure config file exists
        File configFile = new File(userPath + "config");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to create config file.");
            }
        }
        
        System.out.println("Loading Settings...");

        if (configFile.exists()) {
            Scanner sc = null;
            try {
                sc = new Scanner(configFile);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error: Could not locate config file");
                System.exit(1);
            }
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(" ");
                if (line.length == 2) {
                    String name = line[0];
                    String value = line[1];
                    if (settings.containsKey(name) && isInteger(value)) {
                        settings.replace(name, Integer.parseInt(value));
                    }
                }
            }

        }

        System.out.println("Successfully loaded settings.");

    }

    private static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static int getSetting(String name) {
        try {
            return settings.get(name);
        } catch (Exception e) {
            System.err.println(e);
            System.out.println("Error: Could not locate setting to get: " + name);
            System.exit(1);
        }
        return 0;
    }

    public static void setSetting(String name, int value) {
        try {
            settings.replace(name, value);
        } catch (Exception e) {
            System.err.println("Could not locate setting to replace: " + name);
        }
    }

    public static void saveAllSettings() {

        System.out.println("Saving data...");

        List<String> settingsList = new ArrayList<>();
        for (Object o : settings.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            settingsList.add(pair.getKey() + " " + pair.getValue());
        }

        File saveFile = new File(userPath + "config");
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error: failed to create new save file.");
            }
        }

        try {
            Files.write(saveFile.toPath(), settingsList);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: failed to write to save file.");
        }

        System.out.println("Successfully saved all data.");

    }

    public static void fatalAlert(String msg) {
        if (Main.CHEERPJ) {
            System.err.println("WARMVECTOR FATAL ALERT: " + msg);
        } else {
            JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }

        System.exit(1);
    }
}
