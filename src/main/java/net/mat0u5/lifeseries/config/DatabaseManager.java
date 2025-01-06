package net.mat0u5.lifeseries.config;

import me.mrnavastar.sqlib.api.DataStore;
import me.mrnavastar.sqlib.api.database.SQLite;
import net.mat0u5.lifeseries.Main;

public class DatabaseManager {
    public static SQLite db = null;
    public static DataStore doublelife = null;
    public static DataStore secretlife = null;
    public static DataStore secretlifeUsedTasks = null;

    public static void initialize() {
        db = new SQLite("lifeseries", "./config/lifeseries/main");
        doublelife = db.dataStore(Main.MOD_ID, "doublelife");
        secretlife = db.dataStore(Main.MOD_ID, "secretlife");
        secretlifeUsedTasks = db.dataStore(Main.MOD_ID, "secretlifeUsedTasks");
    }
}
