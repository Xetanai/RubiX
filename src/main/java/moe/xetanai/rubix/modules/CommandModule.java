package moe.xetanai.rubix.modules;

import moe.xetanai.rubix.Main;
import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.commands.About;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandModule extends ListenerAdapter {
	private static final Logger logger = LogManager.getLogger(CommandModule.class.getName());
	private static List<Command> COMMANDS = new ArrayList<>();

	private final Main bot;

	public CommandModule(Main bot) {
		this.bot = bot;
		registerCommands();
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		logger.traceEntry();
		// Non negotiable, this ignores bots
		if(event.getAuthor().isBot()) {return;}
		CommandContext ctx = null;

		try {
			// Get default settings and use that as a starting point
			GuildSettingsTable.GuildSettings gs = bot.getDatabase().guildSettings.getDefault();

			// If this is a text channel, the server it belongs to may have settings to honor
			if(event.getChannelType() == ChannelType.TEXT) {
				gs = this.bot.getDatabase().guildSettings.getSettings(event.getGuild().getIdLong());
			}

			String prefix = gs.getPrefix();

			// TODO: Mentions
			if(!event.getMessage().getContentRaw().startsWith(prefix)) {
				// Not a command, not our problem
				return;
			}

			// Create our command context
			ctx = new CommandContext(event, gs);
			logger.debug("Possible command: {}", ctx.getKeyword());
			Command command = getCommandByKeyword(ctx.getKeyword());

			if(command == null) {
				// TODO: Check config
				ctx.reply("No command called `"+ ctx.getKeyword() +"` exists.");
				logger.traceExit("No command");
				return;
			}
			if(!command.isAllowedInDms() && event.getChannelType() == ChannelType.PRIVATE) {
				ctx.reply("This command must be used in a server.");
				logger.traceExit("Not allowed in DM");
				return;
			}

			command.run(ctx);
		} catch (SQLException err) {
			logger.error("SQL Exception in command module.", err);
			logger.traceExit("SQL error");
			return;
		} catch (Exception err) {
			// TODO: Automatic reporting
			String msg = "Command threw an error. This has been reported to the dev automatically.\n";
			msg += "Error: `"+ err.getMessage() +"`";
			if(err.getCause() != null) {
				Throwable cause = err.getCause();
				msg += "\nCause: `"+ cause.getClass().getName() +": "+ cause.getMessage() +"`";
			}

			ctx.reply(msg);

			logger.error("Command threw an error.", err);
			logger.traceExit("Command error");
			return;
		}


		logger.traceExit();
	}

	/**
	 * Get the command a keyword is for. Only searches registered commands
	 * @param keyword keyword to check
	 * @return Command it matched, or null if none matched
	 */
	public static Command getCommandByKeyword(@Nonnull String keyword) {
		for(Command c : COMMANDS) {
			if(c.matchesKeyword(keyword)) {
				return c;
			}
		}

		return null;
	}

	/**
	 * Register all commands into the command list
	 */
	public static void registerCommands() {
		COMMANDS.add(new About());
	}
}
