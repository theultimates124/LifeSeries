package net.mat0u5.lifeseries.config;

import net.mat0u5.lifeseries.series.SeriesList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ConfigManager {

    private Properties properties = new Properties();
    public String filePath;
    public SeriesList series;

    public ConfigManager(String filePath, SeriesList series) {
        this.filePath = filePath;
        this.series = series;
        createFileIfNotExists();
        loadProperties();
    }

    private void createFileIfNotExists() {
        File configDir = new File("./config");
        if (!configDir.exists()) {
            if (!configDir.mkdir()) return;
        }

        File configFile = new File(filePath);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (OutputStream output = new FileOutputStream(configFile)) {
                    if (series == null) {
                        //Main config
                        properties.setProperty("currentSeries","unassigned");
                    }

                    properties.store(output, null);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadProperties() {
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
