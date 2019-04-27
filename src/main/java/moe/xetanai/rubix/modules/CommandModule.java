package moe.xetanai.rubix.modules;

import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.CommandException;
import moe.xetanai.rubix.entities.commands.About;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CommandModule extends ListenerAdapter {
	private static final Logger logger = LogManager.getLogger(CommandModule.class.getName());
	private static List<Command> COMMANDS = new ArrayList<>();

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		logger.traceEntry();
		// Non negotiable, this ignores bots
		if(event.getAuthor().isBot()) {return;}

		CommandContext ctx = new CommandContext(event);

		for(Command cmd : COMMANDS) {
			try {
				if(cmd.matchesKeyword(ctx.getKeyword())) {
					cmd.run(ctx);
					logger.traceExit("Command ran successfully");
					return;
				}
			} catch (CommandException err) {
				logger.error("Command threw an error.", err);
				ctx.reply("Command had an error. This has been automatically reported and no further action is needed.\n`"+err.getMessage()+"`");
				logger.traceExit("Command errored.");
				return;
			}
		}

		//TODO: Check if this is enabled
		ctx.reply("No command found with that name.");
		logger.traceExit("No command found");
	}

	public static void registerCommands() {
		COMMANDS.add(new About());
	}
}
