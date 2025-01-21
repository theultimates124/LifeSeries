package net.mat0u5.lifeseries.config;

import net.mat0u5.lifeseries.Main;
import net.minecraft.entity.ai.pathing.Path;

import javax.swing.text.DefaultEditorKit;
import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public abstract class ConfigManager {

    protected Properties properties = new Properties();
    protected String folderPath;
    protected String filePath;

    protected ConfigManager(String folderPath, String filePath) {
        this.folderPath = folderPath;
        this.filePath = folderPath + "/" + filePath;
        createFileIfNotExists();
        loadProperties();
        defaultProperties();
    }
    protected abstract void defaultProperties();

    protected void defaultSessionProperties() {
        getOrCreateDouble("spawn_egg_drop_chance", 0.05);
        getOrCreateBoolean("spawn_egg_allow_on_spawner", false);
        getOrCreateBoolean("creative_ignore_blacklist", true);
        getOrCreateBoolean("auto_set_worldborder", true);
        getOrCreateBoolean("auto_keep_inventory", true);
        getOrCreateBoolean("players_drop_items_on_last_death", false);
        getOrCreateBoolean("show_death_title_on_last_death", true);
    }

    public static void moveOldMainFileIfExists() {
        File newFolder = new File("./config/lifeseries/main/");
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        File oldFile = new File("./config/"+ Main.MOD_ID+".properties");
        if (!oldFile.exists()) return;
        File newFile = new File("./config/lifeseries/main/"+ Main.MOD_ID+".properties");
        if (newFile.exists()) {
            oldFile.delete();
            Main.LOGGER.info("Deleted old config file.");
            return;
        }
        else {
            try {
                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Main.LOGGER.info("Moved old config file.");
            } catch (IOException e) {
                Main.LOGGER.info("Failed to move old config file.");
            }
        }
    }

    private void createFileIfNotExists() {
        if (folderPath == null || filePath == null) return;
        File configDir = new File(folderPath);
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) return;
        }

        File configFile = new File(filePath);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (OutputStream output = new FileOutputStream(configFile)) {
                    defaultProperties();
                    properties.store(output, null);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadProperties() {
        if (folderPath == null || filePath == null) return;

        properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setProperty(String key, String value) {
        if (folderPath == null || filePath == null) return;
        properties.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
        Various getters
     */

    public String getOrCreateProperty(String key, String defaultValue) {
        if (folderPath == null || filePath == null) return "";
        if (properties == null) return "";

        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        setProperty(key, defaultValue);
        return defaultValue;
    }

    public boolean getOrCreateBoolean(String key, boolean defaultValue) {
        String value = getOrCreateProperty(key, String.valueOf(defaultValue));
        if (value == null) return defaultValue;
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        return defaultValue;
    }

    public double getOrCreateDouble(String key, double defaultValue) {
        String value = getOrCreateProperty(key, String.valueOf(defaultValue));
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {}
        return defaultValue;
    }

    public int getOrCreateInt(String key, int defaultValue) {
        String value = getOrCreateProperty(key, String.valueOf(defaultValue));
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {}
        return defaultValue;
    }
}
