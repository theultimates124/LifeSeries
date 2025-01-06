package net.mat0u5.lifeseries.config;

import net.mat0u5.lifeseries.Main;

public class MainConfig extends ConfigManager {
    public MainConfig() {
        super("./config/lifeseries/main", Main.MOD_ID+".properties");
    }
    @Override
    public void defaultProperties() {
        getOrCreateProperty("currentSeries","unassigned");
    }
}
