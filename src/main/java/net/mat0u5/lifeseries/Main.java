package net.mat0u5.lifeseries;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.mat0u5.lifeseries.client.ClientHandler;
import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.config.MainConfig;
import net.mat0u5.lifeseries.config.UpdateChecker;
import net.mat0u5.lifeseries.events.Events;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.series.*;
import net.mat0u5.lifeseries.series.secretlife.SecretLife;
import net.mat0u5.lifeseries.series.secretlife.TaskManager;
import net.mat0u5.lifeseries.series.unassigned.UnassignedSeries;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.series.lastlife.LastLife;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLife;
import net.mat0u5.lifeseries.series.thirdlife.ThirdLife;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.registries.ModRegistries;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.morph.MorphComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main implements ModInitializer, EntityComponentInitializer {
	public static final ComponentKey<MorphComponent> MORPH_COMPONENT =
			ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("lifeseries","morph"), MorphComponent.class);
	public static final String MOD_VERSION = "dev-1.2.2.62";
	public static final String MOD_ID = "lifeseries";
	public static final String GITHUB_API_URL = "https://api.github.com/repos/Mat0u5/LifeSeries/releases/latest";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static ConfigManager config;

	@Nullable
	public static MinecraftServer server;
	public static Series currentSeries;
	public static Session currentSession;
	public static Blacklist blacklist;
	public static ConfigManager seriesConfig;
	public static final List<String> ALLOWED_SERIES_NAMES = SeriesList.getImplementedSeriesNames();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Life Series...");
		ConfigManager.createPolymerConfig();
		ConfigManager.moveOldMainFileIfExists();

		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		config = new MainConfig();
		String series = config.getOrCreateProperty("currentSeries", "unassigned");

		parseSeries(series);
		ConfigManager.createConfigs();

		ModRegistries.registerModStuff();
		UpdateChecker.checkForUpdates();

		NetworkHandlerServer.registerPackets();
		NetworkHandlerServer.registerServerReceiver();
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(MORPH_COMPONENT, MorphComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}


	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static boolean isLogicalSide() {
		if (!isClient()) return true;
		return ClientHandler.isRunningIntegratedServer();
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

	public static void reload() {
		if (Events.skipNextTickReload) return;
		if (!isLogicalSide()) return;
		if (currentSeries.getSeries() == SeriesList.WILD_LIFE) {
			WildcardManager.onSessionEnd();
		}
		if (currentSeries.getSeries() == SeriesList.SECRET_LIFE) {
			TaskManager.initialize();
		}
		if (currentSeries.getSeries() == SeriesList.DOUBLE_LIFE) {
			((DoubleLife) currentSeries).loadSoulmates();
		}
		seriesConfig.loadProperties();
		blacklist.reloadBlacklist();
		currentSeries.reload();
		NetworkHandlerServer.sendUpdatePackets();
	}

	public static void changeSeriesTo(String changeTo) {
		config.setProperty("currentSeries", changeTo);
		currentSeries.resetAllPlayerLives();
		Main.parseSeries(changeTo);
		currentSeries.initialize();
		reload();

		for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
			currentSeries.onPlayerJoin(player);
			currentSeries.onPlayerFinishJoining(player);
		}
		Stats.resetStats();
	}

	public static boolean isDevVersion() {
		return MOD_VERSION.contains("dev");
	}
}