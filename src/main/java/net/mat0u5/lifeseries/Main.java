package net.mat0u5.lifeseries;

import net.fabricmc.api.ModInitializer;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.config.MainConfig;
import net.mat0u5.lifeseries.config.UpdateChecker;
import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.Session;
import net.mat0u5.lifeseries.series.doublelife.DoubleLifeConfig;
import net.mat0u5.lifeseries.series.lastlife.LastLifeConfig;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLifeConfig;
import net.mat0u5.lifeseries.series.secretlife.SecretLife;
import net.mat0u5.lifeseries.series.secretlife.SecretLifeConfig;
import net.mat0u5.lifeseries.series.secretlife.TaskManager;
import net.mat0u5.lifeseries.series.thirdlife.ThirdLifeConfig;
import net.mat0u5.lifeseries.series.unassigned.UnassignedSeries;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.series.lastlife.LastLife;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLife;
import net.mat0u5.lifeseries.series.thirdlife.ThirdLife;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.WildLifeConfig;
import net.mat0u5.lifeseries.utils.ModRegistries;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;



public class Main implements ModInitializer {
	public static final String MOD_VERSION = "dev-1.2.2.7";
	public static final String MOD_ID = "lifeseries";
	public static final String GITHUB_API_URL = "https://api.github.com/repos/Mat0u5/LifeSeries/releases/latest";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ConfigManager config;
	public static boolean isClient = false;

	@Nullable
	public static MinecraftServer server;
	public static Series currentSeries;
	public static Session currentSession;
	public static Blacklist blacklist;
	public static ConfigManager seriesConfig;
	public static final List<String> ALLOWED_SERIES_NAMES = SeriesList.getImplementedSeriesNames();

	@Override
	public void onInitialize() {
		ConfigManager.moveOldMainFileIfExists();
		config = new MainConfig();

		String series = config.getOrCreateProperty("currentSeries", "unassigned");
		parseSeries(series);
		createConfigs();

		ModRegistries.registerModStuff();
		UpdateChecker.checkForUpdates();
		LOGGER.info("Initializing Life Series...");
	}

	public static void parseSeries(String series) {
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
		if (series.equalsIgnoreCase("wildlife")) {
			currentSeries = new WildLife();
		}
		currentSession = currentSeries;
		seriesConfig = currentSeries.getConfig();
		blacklist = currentSeries.createBlacklist();
	}

	public static void createConfigs() {
		new ThirdLifeConfig();
		new LastLifeConfig();
		new DoubleLifeConfig();
		new LimitedLifeConfig();
		new SecretLifeConfig();
		new WildLifeConfig();
	}

	public static void reload() {
		if (currentSeries.getSeries() == SeriesList.SECRET_LIFE) {
			TaskManager.initialize();
		}
		if (currentSeries.getSeries() == SeriesList.DOUBLE_LIFE) {
			((DoubleLife) currentSeries).loadSoulmates();
		}
		seriesConfig.loadProperties();
		blacklist.reloadBlacklist();
		currentSeries.reload();
	}

	public static void changeSeriesTo(String changeTo) {
		config.setProperty("currentSeries", changeTo);
		currentSeries.resetAllPlayerLives();
		Main.parseSeries(changeTo);
		currentSeries.initialize();
		reload();

		for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
			currentSeries.onPlayerJoin(player);
		}
	}
}