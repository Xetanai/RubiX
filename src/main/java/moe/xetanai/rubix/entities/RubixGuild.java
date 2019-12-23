package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.Main;
import moe.xetanai.rubix.database.tables.GuildSettingsTable.GuildSettings;
import moe.xetanai.rubix.utils.FilterUtils;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class RubixGuild {
    private final Logger logger = LogManager.getLogger("RubiX Guild-"+this.id);

    private long id;

    public RubixGuild(long id) {
        this.id = id;
    }

    public RubixGuild(Guild guild) {
        this(guild.getIdLong());
    }

    private Guild getGuild() {
        return Main.getApi().getGuildById(this.id);
    }

    /* FILTER METHODS */

    public String getName() {
        Guild g = getGuild();
        return FilterUtils.filter(g.getName());
    }

    /* PROXY METHODS */

    /* CONVENIENCE METHODS */
    public GuildSettings getSettings() {
        try {
            return Main.getDatabase().guildSettings.getSettings(this.id);
        } catch (SQLException err) {
            logger.error("Failed to get guild config. Using defaults.", err);
            return Main.getDatabase().guildSettings.getDefault();
        }
    }
}