package moe.xetanai.rubix.modules;

import moe.xetanai.rubix.Main;
import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.CommandException;
import moe.xetanai.rubix.entities.commands.About;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandModule extends ListenerAdapter {
	private static final Logger logger = LogManager.getLogger(CommandModule.class.getName());
	private static List<Command> COMMANDS = new ArrayList<>();

	private final Main bot;

	public CommandModule(Main bot) {
		this.bot = bot;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		logger.traceEntry();
		// Non negotiable, this ignores bots
		if(event.getAuthor().isBot()) {return;}

		GuildSettingsTable.GuildSettings gs = bot.getDatabase().guildSettings.getDefault();
		try {
			if(event.getChannelType() == ChannelType.TEXT) {
				gs = this.bot.getDatabase().guildSettings.getSettings(event.getGuild().getIdLong());
			}
		} catch (SQLException err) {
			logger.error("SQL Exception in command module.", err);
			return;
		}

		String prefix = gs.getPrefix();
		if(!event.getMessage().getContentRaw().startsWith(prefix)) {
			return;
		}

		logger.debug("Potential command in {}.", event.getGuild().getName());
	}

	public static void registerCommands() {
		COMMANDS.add(new About());
	}
}
