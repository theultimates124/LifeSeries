package net.mat0u5.lifeseries.series.secretlife;

import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.types.JavaTypes;
import net.mat0u5.lifeseries.config.DatabaseManager;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SecretLifeDatabase extends DatabaseManager {
    public static void loadLocations() {
        for (DataContainer container : secretlife.getContainers()) {
            String name = container.get(JavaTypes.STRING, "name").orElse(null);
            if (name == null) continue;
            Integer x = container.get(JavaTypes.INT, "x").orElse(null);
            Integer y = container.get(JavaTypes.INT, "y").orElse(null);
            Integer z = container.get(JavaTypes.INT, "z").orElse(null);

            if (x == null || y == null || z == null) continue;
            BlockPos blockPos = new BlockPos(x, y, z);
            if (name.equalsIgnoreCase("successButtonPos")) TaskManager.successButtonPos = blockPos;
            else if (name.equalsIgnoreCase("rerollButtonPos")) TaskManager.rerollButtonPos = blockPos;
            else if (name.equalsIgnoreCase("failButtonPos")) TaskManager.failButtonPos = blockPos;
            else if (name.equalsIgnoreCase("itemSpawnerPos")) TaskManager.itemSpawnerPos = blockPos;
        }
    }

    public static void saveLocations() {
        for (DataContainer container : secretlife.getContainers()) {
            container.delete();
        }
        if (TaskManager.successButtonPos != null) {
            DataContainer container = secretlife.createContainer();
            container.put(JavaTypes.STRING, "name", "successButtonPos");
            container.put(JavaTypes.INT, "x", TaskManager.successButtonPos.getX());
            container.put(JavaTypes.INT, "y", TaskManager.successButtonPos.getY());
            container.put(JavaTypes.INT, "z", TaskManager.successButtonPos.getZ());
        }
        if (TaskManager.rerollButtonPos != null){
            DataContainer container = secretlife.createContainer();
            container.put(JavaTypes.STRING, "name", "rerollButtonPos");
            container.put(JavaTypes.INT, "x", TaskManager.rerollButtonPos.getX());
            container.put(JavaTypes.INT, "y", TaskManager.rerollButtonPos.getY());
            container.put(JavaTypes.INT, "z", TaskManager.rerollButtonPos.getZ());
        }
        if (TaskManager.failButtonPos != null){
            DataContainer container = secretlife.createContainer();
            container.put(JavaTypes.STRING, "name", "failButtonPos");
            container.put(JavaTypes.INT, "x", TaskManager.failButtonPos.getX());
            container.put(JavaTypes.INT, "y", TaskManager.failButtonPos.getY());
            container.put(JavaTypes.INT, "z", TaskManager.failButtonPos.getZ());
        }
        if (TaskManager.itemSpawnerPos != null){
            DataContainer container = secretlife.createContainer();
            container.put(JavaTypes.STRING, "name", "itemSpawnerPos");
            container.put(JavaTypes.INT, "x", TaskManager.itemSpawnerPos.getX());
            container.put(JavaTypes.INT, "y", TaskManager.itemSpawnerPos.getY());
            container.put(JavaTypes.INT, "z", TaskManager.itemSpawnerPos.getZ());
        }
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

    public static void deleteUsedTasks() {
        for (DataContainer container : secretlifeUsedTasks.getContainers()) {
            container.delete();
        }
    }

    public static void deleteAllTasks(List<String> tasks) {
        if (tasks.isEmpty()) return;
        for (DataContainer container : secretlifeUsedTasks.getContainers()) {
            Optional<String> task = container.get(JavaTypes.STRING, "task");
            if (task.isEmpty()) continue;
            if (tasks.contains(task.get())) {
                container.delete();
            }
        }
    }

    public static void addUsedTask(String task) {
        DataContainer container = secretlifeUsedTasks.createContainer();
        container.put(JavaTypes.STRING, "task", task);
    }

    public static List<String> getUsedTasks() {
        List<String> result = new ArrayList<>();
        for (DataContainer container : secretlifeUsedTasks.getContainers()) {
            Optional<String> task = container.get(JavaTypes.STRING, "task");
            if (task.isEmpty()) continue;
            result.add(task.get());
        }
        return result;
    }
}
