package moe.xetanai.rubix.database.tables;

import moe.xetanai.rubix.database.Column;
import moe.xetanai.rubix.database.Database;
import moe.xetanai.rubix.database.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildSettingsTable extends Table {
	private Column<Long> DISCORD_ID = new Column<>("discordid", -1L);
	private Column<String> PREFIX = new Column<>("prefix","!");

	public GuildSettingsTable(Database db) {
		super(db);
	}

	public GuildSettings getSettings(long id) throws SQLException {
		Connection con = this.getDb().getConnection();

		if(con == null) {
			throw new SQLException("Failed to get a connection from pool");
		}

		PreparedStatement ps = con.prepareStatement("SELECT * FROM \"Guilds\" WHERE discordid=?");
		ps.setLong(1, id);

		ResultSet rs = ps.executeQuery();

		GuildSettings gs;

		if(!rs.next()) {
			gs = new GuildSettings();
		} else {
			gs = new GuildSettings(rs);
		}
		Database.closeAll(con, ps, rs);
		return gs;
	}

	public GuildSettings getDefault() {
		return new GuildSettings();
	}

	public class GuildSettings {
		private final long id;
		private final String prefix;

		GuildSettings() {
			this.id = DISCORD_ID.getDefaultValue();
			this.prefix = PREFIX.getDefaultValue();
		}

		private GuildSettings(ResultSet rs) {
			this.id = DISCORD_ID.getLong(rs);
			this.prefix = PREFIX.getString(rs);
		}

		public long getId() {
			return id;
		}

		public String getPrefix() {
			return prefix;
		}
	}
}
