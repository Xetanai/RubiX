package moe.xetanai.rubix.database.tables;

import moe.xetanai.rubix.database.Column;
import moe.xetanai.rubix.database.Database;
import moe.xetanai.rubix.database.Table;
import moe.xetanai.rubix.utils.BotMetaUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents the basic guild settings SQL table
 */
public class GuildSettingsTable extends Table {

    private Column<Long> DISCORD_ID = new Column<>("discordid", -1L);
    private Column<String> PREFIX = new Column<>("prefix", null);

    /**
     * Creates a GuildSettingsTable, responsible for managing and parsing guild settings
     *
     * @param db Database to use
     */
    public GuildSettingsTable (@Nonnull Database db) {
        super(db);
    }

    /**
     * Get the guild settings for a server
     *
     * @param id ID of the server to get settings for
     *
     * @return Guild's basic settings deserialized
     *
     * @throws SQLException if the underlying database errors
     */
    @Nonnull
    public GuildSettings getSettings (long id) throws SQLException {
        Connection con = this.getDb().getConnection();

        if (con == null) {
            throw new SQLException("Failed to get a connection from pool");
        }

        PreparedStatement ps = con.prepareStatement("SELECT * FROM \"guilds\" WHERE discordid=?");
        ps.setString(1, ""+ id); // No unsigned bigint support in postgres. Sigh.

        ResultSet rs = ps.executeQuery();

        GuildSettings gs;

        if (!rs.next()) {
            gs = new GuildSettings();
        } else {
            gs = new GuildSettings(rs);
        }
        Database.closeAll(con, ps, rs);
        return gs;
    }

    @Nonnull
    public GuildSettings getDefault () {
        return new GuildSettings();
    }

    /**
     * Represents a particular guild's basic settings
     */
    public class GuildSettings {

        private final long id;
        private final String prefix;

        GuildSettings () {
            this.id = GuildSettingsTable.this.DISCORD_ID.getDefaultValue();
            this.prefix = GuildSettingsTable.this.PREFIX.getDefaultValue();
        }

        private GuildSettings (@Nonnull ResultSet rs) {
            this.id = GuildSettingsTable.this.DISCORD_ID.getLong(rs);
            this.prefix = GuildSettingsTable.this.PREFIX.getString(rs);
        }

        /**
         * Gets the ID of the guild these settings are for
         *
         * @return Long ID of the guild, or -1L if using defaults
         */
        public long getId () {
            return this.id;
        }

        /**
         * Gets the configured prefix of the guild
         * This will be the one the guild has set, or null if the default is to be used
         *
         * @return The configured command prefix of the server, or null
         */
        @Nullable
        public String getPrefix () {
            return this.prefix;
        }

        /**
         * Gets the effective prefix of the guild
         * This will be the one the guild has set if available, otherwise the bot's default.
         *
         * @return The configured command prefix, or the default
         */
        @Nonnull
        public String getEffectivePrefix () {
            if (getPrefix() == null) {
                return BotMetaUtils.getDefaultPrefix();
            }
            return getPrefix();
        }
    }
}
