package moe.xetanai.rubix.database;

import com.zaxxer.hikari.HikariDataSource;
import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.json.JSONObject;

import java.awt.desktop.SystemEventListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: Check this on startup, and do something more noticeable than 15 console errors when DB connection fails.

/**
 * Represents the database as a whole
 */
public class Database {

    private static final Logger logger = LogManager.getLogger(Database.class.getName());
    private HikariDataSource ds;
    private Flyway flyway;

    public final GuildSettingsTable guildSettings;

    /**
     * Creates a database data source and connection pool
     *
     * @param config Database configuration
     */
    public Database (JSONObject config) {
        logger.traceEntry();
	    this.ds = new HikariDataSource();
	    this.ds.setJdbcUrl(config.getString("url"));
	    this.ds.setUsername(config.getString("username"));
	    this.ds.setPassword(config.getString("password"));
	    this.ds.setLeakDetectionThreshold(30000);

	    this.guildSettings = new GuildSettingsTable(this);

	    this.flyway = this.configureFlyway(config);
    }

    /**
     * Configures Flyway, performs any pending migrations, and then validates the database.
     * @param config Config passed to the constructor of the Database object.
     * @return Flyway object, in case any further calls to it are needed.
     */
    private Flyway configureFlyway(JSONObject config) {
        Flyway flyway = null;
        try {
            flyway = Flyway.configure().dataSource(
                config.getString("url"),
                config.getString("username"),
                config.getString("password")
            ).load();

            int migrations = flyway.migrate();

            flyway.validate();
        } catch (FlywayException err) {
            logger.fatal("Migrations and validation failed. This could cause extreme problems if not addressed.", err);
            if(flyway != null) {
                logger.warn("Flyway repair beginning. It's still advised you check the database's health manually.");
                flyway.repair();
            }
            System.exit(1);
        }

        return flyway;
    }

    /**
     * Gets a connection from the pool
     *
     * @return A database connection
     *
     * @throws java.sql.SQLException If the database pool fails to give a connection
     */
    public Connection getConnection () {
        try {
            return this.ds.getConnection();
        } catch (SQLException err) {
            logger.fatal("Failed to get a connection from the Hikari pool.", err);
            return null;
        }
    }

    /**
     * Close all connection resources, releasing the connection to be reused
     *
     * @param con Database connection
     * @param ps  Prepared Statement
     * @param rs  Result Set
     */
    public static void closeAll (Connection con, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            if (ps != null && !ps.isClosed()) {
                ps.close();
            }
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException err) {
            logger.trace("Exception was thrown and ignored while closing database objects.", err);
        }
    }
}
