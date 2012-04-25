package tmc.CrafterTracker;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
    public static final String DB_CONNECTION_SECTION = "MongoDbConnectionInfo";
    public static final String ADDRESS_OPTION = "address";
    public static final String DB_NAME_OPTION = "dbName";
    public static final String DB_USER_OPTION = "dbUser";
    public static final String DB_PASSWORD_OPTION = "dbPassword";

    private FileConfiguration configuration;
    private CrafterTrackerPlugin plugin;
    private Map<String, String> dbConnectionInfo;

    public Configuration(FileConfiguration configuration, CrafterTrackerPlugin plugin) {
        this.configuration = configuration;
        this.plugin = plugin;
        if (configuration.getKeys(false).isEmpty()) {
            registerDefaultValues();
        } else {
            ConfigurationSection section = configuration.getConfigurationSection("MongoDbConnectionInfo");
            dbConnectionInfo = new HashMap<String, String>();
            dbConnectionInfo.put(ADDRESS_OPTION, section.getString(ADDRESS_OPTION));
            dbConnectionInfo.put(DB_NAME_OPTION, section.getString(DB_NAME_OPTION));
            dbConnectionInfo.put(DB_USER_OPTION, section.getString(DB_USER_OPTION));
            dbConnectionInfo.put(DB_PASSWORD_OPTION, section.getString(DB_PASSWORD_OPTION));
        }
    }

    private void registerDefaultValues() {
        dbConnectionInfo = new HashMap<String, String>();
        dbConnectionInfo.put(ADDRESS_OPTION, "localhost");
        dbConnectionInfo.put(DB_NAME_OPTION, "CrafterTracker");
        dbConnectionInfo.put(DB_USER_OPTION, "root");
        dbConnectionInfo.put(DB_PASSWORD_OPTION, "password");
        configuration.createSection(DB_CONNECTION_SECTION, dbConnectionInfo);

        plugin.saveConfig();
    }

    public Map<String, String> getDbConnectionInfo() {
        return dbConnectionInfo;
    }
}
