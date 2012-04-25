package tmc.CrafterTracker;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.UnknownHostException;
import java.util.logging.Logger;

import static tmc.CrafterTracker.Configuration.*;

public class CrafterTrackerPlugin extends JavaPlugin {

    public static final String PLAYER_STATISTICS_COLLECTION = "PlayerStats";
    public static final String PLAYERS_COLLECTION = "Players";
    public static String MONGO_CONNECTION_ERROR = "Error connecting to MongoDB:\n\r%s";
    private Mongo mongoConnection;
    private DB betterProtectedDB;
    private Logger logger;
    private Server server;
    private Configuration configuration;

    @Override
    public void onEnable() {
        initializeServer();
        logger.info("Beginning CrafterTracker Initialization.");
        initializeMongoDB();
        initializeDatabase();
        logger.info("CrafterTracker initialization complete.");
    }

    @Override
    public void onDisable() {
        mongoConnection.close();
        logger.info("CrafterTracker shutdown complete.");
    }

    private void initializeServer() {
        server = this.getServer();
        logger = server.getLogger();
        logger.info("Configuring BetterProtected Plugin.");
        configuration = new Configuration(this.getConfig(), this);
    }

    private void initializeMongoDB() {
        try {
            String address = configuration.getDbConnectionInfo().get(ADDRESS_OPTION);
            mongoConnection = new Mongo(address);
            logger.info("Found MongoDB instance at " + address);
        } catch (UnknownHostException e) {
            logger.warning(String.format(MONGO_CONNECTION_ERROR, e.toString()));
        }
    }

    private void initializeDatabase() {
        String dbName = configuration.getDbConnectionInfo().get(DB_NAME_OPTION);
        logger.info("Connecting to database: " + dbName + ".");
        betterProtectedDB = mongoConnection.getDB(dbName);

        String user = configuration.getDbConnectionInfo().get(DB_USER_OPTION);
        if(user != null) {
            logger.info("Attempting authentication to database " + dbName + " with user " + user + ".");
            String password = configuration.getDbConnectionInfo().get(DB_PASSWORD_OPTION);
            boolean success = betterProtectedDB.authenticate(user, password.toCharArray());
            if (!success) {
                logger.warning("Incorrect Mongo Database Authentication Info: " +
                        "please double check the settings in your config.yml file.");
                onDisable();
            } else {
                logger.info("Authentication successful.");
            }
        }
    }
}
