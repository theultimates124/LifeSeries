package net.mat0u5.lifeseries.series.limitedlife;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.config.ConfigManager;

import java.util.List;

import static net.mat0u5.lifeseries.Main.seriesConfig;

public class LimitedLifeConfig extends ConfigManager {
    public static final List<String> BLACKLISTED_ITEMS = List.of(
            "lectern",
            "bookshelf",
            "mace",
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

    public LimitedLifeConfig() {
        super("./config/"+ Main.MOD_ID,"limitedlife.properties");
    }

    @Override
    public void defaultProperties() {
        defaultSessionProperties();
        getOrCreateProperty("max_player_health", "20");
        getOrCreateProperty("time_default", "86400");
        getOrCreateProperty("time_yellow", "57600");
        getOrCreateProperty("time_red", "28800");
        getOrCreateProperty("custom_enchanter_algorithm", "true");
        getOrCreateProperty("blacklist_items","["+String.join(", ", BLACKLISTED_ITEMS)+"]");
        getOrCreateProperty("blacklist_blocks","["+String.join(", ", BLACKLISTED_BLOCKS)+"]");
        getOrCreateProperty("blacklist_clamped_enchants","["+String.join(", ", CLAMPED_ENCHANTMENTS)+"]");
        getOrCreateInt("time_death",-3600);
        getOrCreateInt("time_death_boogeyman",-7200);
        getOrCreateInt("time_kill",1800);
        getOrCreateInt("time_kill_boogeyman",3600);
    }
}
