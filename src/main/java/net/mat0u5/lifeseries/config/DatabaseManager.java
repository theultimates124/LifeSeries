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
}
