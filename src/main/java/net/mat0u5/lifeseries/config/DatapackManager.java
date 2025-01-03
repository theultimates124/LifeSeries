package net.mat0u5.lifeseries.config;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.*;

public class DatapackManager {
    private void deleteAllDatapacks(MinecraftServer server) {
        Path datapackFolder = server.getSavePath(WorldSavePath.DATAPACKS);
        try {
            for (SeriesList series : SeriesList.getAllImplemented()) {
                String datapackName = SeriesList.getDatapackName(series);
                Path datapackPath = datapackFolder.resolve(datapackName);
                if (Files.exists(datapackPath) && Files.isRegularFile(datapackPath)) {
                    Files.delete(datapackPath);
                    Main.LOGGER.info("[LifeSeries] Deleted datapack: " + datapackName);
                }
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error deleting datapacks: " + e.getMessage(), e);
        }
    }

    private void disableAllDatapacks() {
        for (SeriesList series : SeriesList.getAllImplemented()) {
            String datapackName = SeriesList.getDatapackName(series);
            OtherUtils.executeCommand("datapack disable \"file/"+datapackName+"\"");
        }
    }

    public void onServerStarted(MinecraftServer server) {
        disableAllDatapacks();
        TaskScheduler.scheduleTask(50, this::reloadServer);
        TaskScheduler.scheduleTask(100, () -> deleteAllDatapacks(server));
    }

    private void reloadServer() {
        OtherUtils.executeCommand("reload");
    }
}
