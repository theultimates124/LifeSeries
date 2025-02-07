package net.mat0u5.lifeseries.series.wildlife;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.config.ConfigManager;

import java.util.List;

import static net.mat0u5.lifeseries.Main.seriesConfig;

public class WildLifeConfig extends ConfigManager {
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

    public WildLifeConfig() {
        super("./config/"+ Main.MOD_ID,"wildlife.properties");
    }

    @Override
    public void defaultProperties() {
        defaultSessionProperties();
        getOrCreateBoolean("spawner_recipe", true);
        getOrCreateBoolean("spawn_egg_allow_on_spawner", true);
        getOrCreateInt("wildcard_hunger_randomize_interval", 36000);

        getOrCreateDouble("wildcard_sizeshifting_min_size", 0.25);
        getOrCreateDouble("wildcard_sizeshifting_max_size", 3);
        getOrCreateDouble("wildcard_sizeshifting_size_change_multiplier", 1);

        getOrCreateDouble("wildcard_snails_speed_multiplier", 1);
        getOrCreateBoolean("wildcard_snails_drown_players", true);

        getOrCreateInt("wildcard_mobswap_start_spawn_delay", 7200);
        getOrCreateInt("wildcard_mobswap_end_spawn_delay", 2400);
        getOrCreateInt("wildcard_mobswap_spawn_mobs", 250);
        getOrCreateDouble("wildcard_mobswap_boss_chance_multiplier", 1);

        getOrCreateInt("max_player_health", 20);
        getOrCreateInt("default_lives", 6);
        getOrCreateBoolean("custom_enchanter_algorithm", true);
        getOrCreateProperty("blacklist_items","["+String.join(", ", BLACKLISTED_ITEMS)+"]");
        getOrCreateProperty("blacklist_blocks","["+String.join(", ", BLACKLISTED_BLOCKS)+"]");
        getOrCreateProperty("blacklist_clamped_enchants","["+String.join(", ", CLAMPED_ENCHANTMENTS)+"]");
    }
}
