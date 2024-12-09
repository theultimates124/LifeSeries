package net.mat0u5.lifeseries;

import net.fabricmc.api.ModInitializer;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.Session;
import net.mat0u5.lifeseries.series.secretlife.SecretLife;
import net.mat0u5.lifeseries.series.unassigned.UnassignedSeries;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.series.lastlife.LastLife;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLife;
import net.mat0u5.lifeseries.series.thirdlife.ThirdLife;
import net.mat0u5.lifeseries.utils.ModRegistries;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;



public class Main implements ModInitializer {
	public static final String MOD_VERSION = "1.1.9.5";
	public static final String MOD_ID = "lifeseries";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ConfigManager config;
	public static MinecraftServer server;
	public static Series currentSeries;
	public static Session currentSession;
	public static Blacklist blacklist;
	public static final List<String> ALLOWED_SERIES_NAMES = SeriesList.getImplementedSeriesNames();

	@Override
	public void onInitialize() {
		config = new ConfigManager("./config/"+MOD_ID+".properties", null);

		String series = config.getProperty("currentSeries");
		if (series == null) parseSeries("");
		else parseSeries(series);

		ModRegistries.registerModStuff();
		LOGGER.info("Initializing Life Series...");
	}
	public void parseSeries(String series) {
		if (!ALLOWED_SERIES_NAMES.contains(series)) {
			currentSeries = new UnassignedSeries();
		}
		if (series.equalsIgnoreCase("thirdlife")) {
			currentSeries = new ThirdLife();
		}
		if (series.equalsIgnoreCase("lastlife")) {
			currentSeries = new LastLife();
		}
		if (series.equalsIgnoreCase("doublelife")) {
			currentSeries = new DoubleLife();
		}
		if (series.equalsIgnoreCase("limitedlife")) {
			currentSeries = new LimitedLife();
		}
		if (series.equalsIgnoreCase("secretlife")) {
			currentSeries = new SecretLife();
		}
		currentSession = currentSeries;
		blacklist = currentSeries.createBlacklist();
	}
}