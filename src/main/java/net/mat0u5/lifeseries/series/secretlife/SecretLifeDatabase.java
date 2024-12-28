package net.mat0u5.lifeseries.series.secretlife;

import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.types.MinecraftTypes;
import net.mat0u5.lifeseries.config.DatabaseManager;

public class SecretLifeDatabase extends DatabaseManager {
    public static void loadLocations() {
        for (DataContainer container : secretlife.getContainers()) {
            TaskManager.successButtonPos = container.get(MinecraftTypes.BLOCKPOS, "successButtonPos").orElse(null);
            TaskManager.rerollButtonPos = container.get(MinecraftTypes.BLOCKPOS, "rerollButtonPos").orElse(null);
            TaskManager.failButtonPos = container.get(MinecraftTypes.BLOCKPOS, "failButtonPos").orElse(null);
            TaskManager.itemSpawnerPos = container.get(MinecraftTypes.BLOCKPOS, "itemSpawnerPos").orElse(null);
            return;
        }
    }

    public static void saveLocations() {
        for (DataContainer container : secretlife.getContainers()) {
            container.delete();
        }
        DataContainer container = secretlife.createContainer();
        if (TaskManager.successButtonPos != null) container.put(MinecraftTypes.BLOCKPOS, "successButtonPos", TaskManager.successButtonPos);
        if (TaskManager.rerollButtonPos != null) container.put(MinecraftTypes.BLOCKPOS, "rerollButtonPos", TaskManager.rerollButtonPos);
        if (TaskManager.failButtonPos != null) container.put(MinecraftTypes.BLOCKPOS, "failButtonPos", TaskManager.failButtonPos);
        if (TaskManager.itemSpawnerPos != null) container.put(MinecraftTypes.BLOCKPOS, "itemSpawnerPos", TaskManager.itemSpawnerPos);
    }

    public static void deleteLocations() {
        TaskManager.successButtonPos = null;
        TaskManager.rerollButtonPos = null;
        TaskManager.failButtonPos = null;
        TaskManager.itemSpawnerPos = null;
        for (DataContainer container : secretlife.getContainers()) {
            container.delete();
        }
    }
}
