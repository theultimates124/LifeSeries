package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.config.ConfigManager;

import java.util.List;

public class SecretLifeConfig extends ConfigManager {
    public static final List<String> BLACKLISTED_ITEMS = List.of(
            "lectern",
            "mace",
            "end_crystal",
            "leather_helmet",
            "chainmail_helmet",
            "golden_helmet",
            "iron_helmet",
            "diamond_helmet",
            "netherite_helmet",
            "turtle_helmet",
            "elytra"
    );

    public static final List<String> BLACKLISTED_BLOCKS = List.of(
            "lectern"
    );
    public static final List<String> CLAMPED_ENCHANTMENTS = List.of(
            "sharpness",
            "smite",
            "bane_of_arthropods",
            "fire_aspect",
            "knockback",
            "sweeping_edge",

            "power",
            "punch",

            "thorns",

            "breach",
            "density",
            "wind_burst",

            "multishot",
            "piercing",
            "quick_charge"
    );

    public SecretLifeConfig() {
        super("./config/"+ Main.MOD_ID,"secretlife.properties");
    }

    @Override
    public void defaultProperties() {
        defaultSessionProperties();
        properties.setProperty("task_health_easy_pass", "20");
        properties.setProperty("task_health_easy_fail", "0");
        properties.setProperty("task_health_hard_pass", "40");
        properties.setProperty("task_health_hard_fail", "-20");
        properties.setProperty("task_health_red_pass", "10");
        properties.setProperty("task_health_red_fail", "-5");


        properties.setProperty("max_player_health", "60");
        properties.setProperty("default_lives", "3");
        properties.setProperty("custom_enchanter_algorithm", "false");
        properties.setProperty("blacklist_items","["+String.join(", ", BLACKLISTED_ITEMS)+"]");
        properties.setProperty("blacklist_blocks","["+String.join(", ", BLACKLISTED_BLOCKS)+"]");
        properties.setProperty("blacklist_clamped_enchants","["+String.join(", ", CLAMPED_ENCHANTMENTS)+"]");
    }
}
