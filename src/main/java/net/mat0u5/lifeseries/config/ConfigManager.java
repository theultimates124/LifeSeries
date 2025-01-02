package net.mat0u5.lifeseries.config;

import java.io.*;
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
    }
    protected abstract void defaultProperties();

    private void createFileIfNotExists() {
        if (filePath == null) return;
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
        if (filePath == null) return;

        properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        if (filePath == null) return "";

        return properties.getProperty(key);
    }

    public String getOrCreateProperty(String key, String defaultValue) {
        if (filePath == null) return "";

        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        setProperty(key, defaultValue);
        return defaultValue;
    }

    public void setProperty(String key, String value) {
        if (filePath == null) return;
        properties.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
