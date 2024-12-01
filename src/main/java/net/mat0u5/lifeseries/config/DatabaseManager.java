package net.mat0u5.lifeseries.config;

import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.DataStore;
import me.mrnavastar.sqlib.api.database.Database;
import me.mrnavastar.sqlib.api.types.JavaTypes;
import net.mat0u5.lifeseries.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DatabaseManager {
    public static Database db = null;
    public static DataStore doublelife = null;

    public static void initialize() {
        db = SQLib.getDatabase();
        doublelife = db.dataStore(Main.MOD_ID, "doublelife");
    }
    public static void deleteDoubleLifeSoulmates() {
        for (DataContainer container : doublelife.getContainers()) {
            container.delete();
        }
    }
    public static void setAllSoulmates(Map<UUID, UUID> soulmates) {
        for (Map.Entry<UUID, UUID> entry : soulmates.entrySet()) {
            DataContainer container = doublelife.createContainer();
            container.put(JavaTypes.UUID, "player1", entry.getKey());
            container.put(JavaTypes.UUID, "player2", entry.getValue());
        }
    }
    public static Map<UUID, UUID> getAllSoulmates() {
        Map<UUID, UUID> soulmates = new HashMap<>();
        for (DataContainer container : doublelife.getContainers()) {
            Optional<UUID> player1 = container.get(JavaTypes.UUID, "player1");
            Optional<UUID> player2 = container.get(JavaTypes.UUID, "player2");
            if (player1.isEmpty() || player2.isEmpty()) continue;
            soulmates.put(player1.get(), player2.get());
            soulmates.put(player2.get(), player1.get());
        }
        return soulmates;
    }
}
