package net.mat0u5.lifeseries;

import net.fabricmc.api.ModInitializer;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.Session;
import net.mat0u5.lifeseries.series.lastlife.LastLife;
import net.mat0u5.lifeseries.utils.ModRegistries;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class Main implements ModInitializer {
	public static final String MOD_ID = "lifeseries";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ConfigManager config;
	public static MinecraftServer server;
	public static Series currentSeries;
	public static Session currentSession;
	public static Blacklist blacklist;

	public static final List<String> ALLOWED_SERIES_NAMES = List.of("lastlife");

	@Override
	public void onInitialize() {
		config = new ConfigManager("./config/"+MOD_ID+".properties");
		String series = config.getProperty("currentSeries");
		parseSeries(series);

		ModRegistries.registerModStuff();
		LOGGER.info("Initializing Life Series...");
	}
	public void parseSeries(String series) {
		if (!ALLOWED_SERIES_NAMES.contains(series)) {
			seriesUnassigned(series);
			return;
		}
		if (series.equalsIgnoreCase("lastlife")) {
			currentSeries = new LastLife();
		}
		currentSession = currentSeries;
		blacklist = currentSeries.createBlacklist();
	}
	public void seriesUnassigned(String series) {
		LOGGER.error("Life Series is not chosen, shutting down server!");
		LOGGER.error("You must replace '"+series+"' in the 'currentSeries' field with the desired series name in the config file, located at " + config.filePath);
		LOGGER.error("Valid values for 'currentSeries' are "+String.join(", ",ALLOWED_SERIES_NAMES));
		throw new RuntimeException("Server initialization aborted by Life Series.");
	}
}