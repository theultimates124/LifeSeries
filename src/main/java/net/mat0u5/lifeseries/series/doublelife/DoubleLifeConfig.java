package net.mat0u5.lifeseries.series.doublelife;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.config.ConfigManager;

import java.util.List;

public class DoubleLifeConfig extends ConfigManager {
    public static final List<String> BLACKLISTED_ITEMS = List.of(
            "lectern",
            "bookshelf",
            "enchanting_table",
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
            "lectern",
            "bookshelf"
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

            "protection",
            "projectile_protection",
            "blast_protection",
            "fire_protection",
            "feather_falling",
            "thorns",

            "breach",
            "density",
            "wind_burst",

            "multishot",
            "piercing",
            "quick_charge"
    );

    public DoubleLifeConfig() {
        super("./config/"+ Main.MOD_ID,"doublelife.properties");
    }

    @Override
    public void defaultProperties() {
        defaultSessionProperties();
        properties.setProperty("max_player_health", "20");
        properties.setProperty("default_lives", "3");
        properties.setProperty("custom_enchanter_algorithm", "true");
        properties.setProperty("blacklist_items","["+String.join(", ", BLACKLISTED_ITEMS)+"]");
        properties.setProperty("blacklist_blocks","["+String.join(", ", BLACKLISTED_BLOCKS)+"]");
        properties.setProperty("blacklist_clamped_enchants","["+String.join(", ", CLAMPED_ENCHANTMENTS)+"]");
    }
}
