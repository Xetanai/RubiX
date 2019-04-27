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

public class Database {
	private static final Logger logger = LogManager.getLogger(Database.class.getName());
	private HikariDataSource ds;

	public final GuildSettingsTable guildSettings;

	public Database(JSONObject config) {
		logger.traceEntry();
		ds = new HikariDataSource();
		ds.setJdbcUrl(config.getString("url"));
		ds.setUsername(config.getString("username"));
		ds.setPassword(config.getString("password"));
		ds.setLeakDetectionThreshold(30000);

		guildSettings = new GuildSettingsTable(this);
	}

	public Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException err) {
			logger.fatal("Failed to get a connection from the Hikari pool.", err);
			return null;
		}
	}

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
