package net.mat0u5.lifeseries.config;

import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.api.DataStore;
import me.mrnavastar.sqlib.api.database.Database;
import net.mat0u5.lifeseries.Main;

public class DatabaseManager {
    public static Database db = null;
    public static DataStore doublelife = null;
    public static DataStore secretlife = null;

    public static void initialize() {
        db = SQLib.getDatabase();
        doublelife = db.dataStore(Main.MOD_ID, "doublelife");
        secretlife = db.dataStore(Main.MOD_ID, "secretlife");
    }
}
