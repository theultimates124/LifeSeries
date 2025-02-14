package net.mat0u5.lifeseries.config;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.doublelife.DoubleLifeConfig;
import net.mat0u5.lifeseries.series.lastlife.LastLifeConfig;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLifeConfig;
import net.mat0u5.lifeseries.series.secretlife.SecretLifeConfig;
import net.mat0u5.lifeseries.series.thirdlife.ThirdLifeConfig;
import net.mat0u5.lifeseries.series.wildlife.WildLifeConfig;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.ai.pathing.Path;

import javax.swing.text.DefaultEditorKit;
import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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
        getOrCreateBoolean("spawn_egg_drop_only_natural", true);
        getOrCreateBoolean("creative_ignore_blacklist", true);
        getOrCreateBoolean("auto_set_worldborder", true);
        getOrCreateBoolean("auto_keep_inventory", true);
        getOrCreateBoolean("players_drop_items_on_last_death", false);
        getOrCreateBoolean("show_death_title_on_last_death", true);
        getOrCreateProperty("blacklist_banned_enchants","[]");
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

    public static void createConfigs() {
        new ThirdLifeConfig();
        new LastLifeConfig();
        new DoubleLifeConfig();
        new LimitedLifeConfig();
        new SecretLifeConfig();
        new WildLifeConfig();
    }


    public static void createPolymerConfig() {
        File newFolder = new File("./config/polymer/");
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        String resourcePack = "{\n  \"_c0\": \"UUID of default/main resource pack.\",\n  \"main_uuid\": \"e18b8296-585a-4be0-aee2-5125a3bebca6\",\n  \"_c1\": \"Marks resource pack as required, only effects clients and mods using api to check it\",\n  \"markResourcePackAsRequiredByDefault\": false,\n  \"_c2\": \"Force-enables offset of CustomModelData\",\n  \"forcePackOffset\": false,\n  \"_c3\": \"Value of CustomModelData offset when enabled\",\n  \"offsetValue\": 100000,\n  \"_c4\": \"Enables usage of alternative armor rendering for increased mod compatibility. (Always on with Iris or Canvas present)\",\n  \"use_alternative_armor_rendering\": false,\n  \"_c5\": \"Included resource packs from mods!\",\n  \"include_mod_assets\": [],\n  \"_c6\": \"Included resource packs from zips!\",\n  \"include_zips\": [\n    \"world/resources.zip\"\n  ],\n  \"_c7\": \"Path used for creation of default resourcepack!\",\n  \"resource_pack_location\": \"config/lifeseries/resource_pack.zip\",\n  \"_c8\": \"Prevents selected paths from being added to resource pack, if they start with provided text.\",\n  \"prevent_path_with\": []\n}";
        String autoHost = "{\n  \"_c1\": \"Enables Polymer's ResourcePack Auto Hosting\",\n  \"enabled\": false,\n  \"_c2\": \"Marks resource pack as required\",\n  \"required\": false,\n  \"_c3\": \"Type of resource pack provider. Default: 'polymer:automatic'\",\n  \"type\": \"polymer:automatic\",\n  \"_c4\": \"Configuration of type, see provider's source for more details\",\n  \"settings\": {},\n  \"_c5\": \"Message sent to clients before pack is loaded\",\n  \"message\": \"The Life Series uses a resource pack to enhance gameplay with custom textures, models and sounds. Some of these features are necessary.\",\n  \"_c6\": \"Disconnect message in case of failure\",\n  \"disconnect_message\": \"Couldn't apply server resourcepack!\",\n  \"external_resource_packs\": [],\n  \"setup_early\": false\n}";
        createOrModifyFile(new File("./config/polymer/resource-pack.json"), resourcePack);
        createOrModifyFile(new File("./config/polymer/auto-host.json"), autoHost);
    }

    private static void createOrModifyFile(File file, String defaultContents) {
        try {
            if (file.exists()) {
                Main.LOGGER.info("[Life Series] Modifying existing configuration file: " + file.getAbsolutePath());
                Files.write(file.toPath(), defaultContents.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(defaultContents);
                    Main.LOGGER.info("[Life Series] Polymer configuration file created at: " + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            Main.LOGGER.error("[Life Series] Failed to create or modify the polymer configuration file: " + e.getMessage());
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

    public void setPropertyCommented(String key, String value, String comment) {
        if (folderPath == null || filePath == null) return;
        properties.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, comment);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void resetProperties(String comment) {
        properties.clear();
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, comment);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
        Various getters
     */

    public String getProperty(String key) {
        if (folderPath == null || filePath == null) return null;
        if (properties == null) return null;

        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        return null;
    }

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
