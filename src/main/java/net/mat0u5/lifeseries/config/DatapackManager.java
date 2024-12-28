package net.mat0u5.lifeseries.config;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class DatapackManager {
    private void copyBundledDatapacks(MinecraftServer server) {
        Path targetFolder = server.getSavePath(WorldSavePath.DATAPACKS);
        try {
            // Ensure the target datapack folder exists
            Files.createDirectories(targetFolder);

            // Path within your mod's resources
            String resourcePath = "/assets/" + Main.MOD_ID + "/datapacks";

            // Access the resource folder as a URL
            URL resourceUrl = getClass().getResource(resourcePath);

            if (resourceUrl == null) {
                Main.LOGGER.error("Datapack folder not found: " + resourcePath);
                return;
            }

            // Check the protocol to determine if we are inside a JAR or in a file system
            if (resourceUrl.getProtocol().equals("file")) {
                handleNormal(targetFolder, resourceUrl);
            }
            else if (resourceUrl.getProtocol().equals("jar")) {
                handleJar(targetFolder, resourcePath);
            }
            else {
                Main.LOGGER.error("Unsupported resource protocol: " + resourceUrl.getProtocol());
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error copying bundled datapacks.", e);
        }
    }

    private void handleNormal(Path targetFolder, URL resourceUrl) {
        try {
            // Running in development or where resources are on the file system
            Path modDatapacksPath = Paths.get(resourceUrl.toURI());

            // Copy files directly from the file system
            Files.walk(modDatapacksPath).forEach(sourcePath -> {
                try {
                    if (!Files.isRegularFile(sourcePath)) return;

                    Path relativePath = modDatapacksPath.relativize(sourcePath);
                    Path targetPath = targetFolder.resolve(relativePath);

                    // Ensure parent directories exist
                    Files.createDirectories(targetPath.getParent());

                    // Copy the file to the world's datapack folder
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    Main.LOGGER.info("Copied datapack file: " + sourcePath + " -> " + targetPath);
                } catch (IOException e) {
                    Main.LOGGER.error("Failed to copy datapack file: " + sourcePath, e);
                }
            });
        } catch (Exception e) {
            Main.LOGGER.error("Error copying bundled datapacks.", e);
        }
    }

    private void handleJar(Path targetFolder, String resourcePath) {
        try {
            // Use the existing FileSystem for the JAR
            URI jarUri = getClass().getResource(resourcePath).toURI();

            if (!"jar".equals(jarUri.getScheme())) {
                Main.LOGGER.error("Unsupported URI scheme: " + jarUri.getScheme());
                return;
            }

            // Resolve the path inside the JAR using the current FileSystem
            try (FileSystem fs = FileSystems.getFileSystem(jarUri)) {
                Path jarDatapackPath = fs.getPath(resourcePath);

                // Walk through the entries in the JAR resource path
                Files.walk(jarDatapackPath).forEach(sourcePath -> {
                    try {
                        if (!Files.isRegularFile(sourcePath)) return;

                        Path relativePath = jarDatapackPath.relativize(sourcePath);
                        Path targetPath = targetFolder.resolve(relativePath.toString());

                        // Ensure parent directories exist
                        Files.createDirectories(targetPath.getParent());

                        // Copy the file to the world's datapack folder
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                        Main.LOGGER.info("Copied datapack file from JAR: " + sourcePath + " -> " + targetPath);
                    } catch (IOException e) {
                        Main.LOGGER.error("Failed to copy datapack file from JAR: " + sourcePath, e);
                    }
                });
            } catch (ProviderNotFoundException e) {
                Main.LOGGER.error("FileSystem for JAR not found: " + jarUri, e);
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error accessing datapack files in JAR.", e);
        }
    }

    public void onServerStarted(MinecraftServer server) {
        disableAllDatapacks();
        TaskScheduler.scheduleTask(20, () -> copyBundledDatapacks(server));
        TaskScheduler.scheduleTask(50, this::reloadServer);
        TaskScheduler.scheduleTask(100, this::enableUsedDatapacks);
    }

    private void disableAllDatapacks() {
        for (SeriesList series : SeriesList.getAllImplemented()) {
            String datapackName = SeriesList.getDatapackName(series);
            OtherUtils.executeCommand("datapack disable \"file/"+datapackName+"\"");
        }
    }

    private void reloadServer() {
        OtherUtils.executeCommand("reload");
    }

    private void enableUsedDatapacks() {
        for (SeriesList series : SeriesList.getAllImplemented()) {
            String datapackName = SeriesList.getDatapackName(series);
            if (currentSeries.getSeries() == series) {
                OtherUtils.executeCommand("datapack enable \"file/"+datapackName+"\"");
            }
            else {
                OtherUtils.executeCommand("datapack disable \"file/"+datapackName+"\"");
            }
        }
    }
}
