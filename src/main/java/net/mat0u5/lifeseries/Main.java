package net.mat0u5.lifeseries;

import net.fabricmc.api.ModInitializer;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.Session;
import net.mat0u5.lifeseries.series.lastlife.LastLife;
import net.mat0u5.lifeseries.utils.ModRegistries;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main implements ModInitializer {
	public static final String MOD_ID = "lifeseries";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ConfigManager config;
	public static MinecraftServer server;
	public static Series currentSeries = new LastLife();
	public static Session currentSession = new Session();

	@Override
	public void onInitialize() {
		config = new ConfigManager("./config/"+MOD_ID+".properties");



		ModRegistries.registerModStuff();
		LOGGER.info("Initializing Life Series...");
	}
}