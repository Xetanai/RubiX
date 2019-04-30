package moe.xetanai.rubix.database;

import com.zaxxer.hikari.HikariDataSource;
import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents the database as a whole
 */
public class Database {
	private static final Logger logger = LogManager.getLogger(Database.class.getName());
	private HikariDataSource ds;

	public final GuildSettingsTable guildSettings;

	/**
	 * Creates a database data source and connection pool
	 * @param config Database configuration
	 */
	public Database(JSONObject config) {
		logger.traceEntry();
		ds = new HikariDataSource();
		ds.setJdbcUrl(config.getString("url"));
		ds.setUsername(config.getString("username"));
		ds.setPassword(config.getString("password"));
		ds.setLeakDetectionThreshold(30000);

		guildSettings = new GuildSettingsTable(this);
	}

	/**
	 * Gets a connection from the pool
	 * @return A database connection
	 * @throws java.sql.SQLException If the database pool fails to give a connection
	 */
	public Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException err) {
			logger.fatal("Failed to get a connection from the Hikari pool.", err);
			return null;
		}
	}

	/**
	 * Close all connection resources, releasing the connection to be reused
	 * @param con Database connection
	 * @param ps Prepared Statement
	 * @param rs Result Set
	 */
	public static void closeAll(Connection con, PreparedStatement ps, ResultSet rs) {
		try {
			if(rs != null && !rs.isClosed()) {
				rs.close();
			}
			if(ps != null && !ps.isClosed()) {
				ps.close();
			}
			if(con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException err) {
			logger.trace("Exception was thrown and ignored while closing database objects.",err);
		}
	}
}
