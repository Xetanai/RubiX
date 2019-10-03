package moe.xetanai.rubix.modules;

import humanize.Humanize;
import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.commands.About;
import moe.xetanai.rubix.entities.commands.ActivityPie;
import moe.xetanai.rubix.entities.commands.ThrowError;
import moe.xetanai.rubix.utils.MiscUtils;
import moe.xetanai.rubix.utils.RatelimitUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommandModule extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(CommandModule.class.getName());
    private static List<Command> COMMANDS = new ArrayList<>();

    public CommandModule () {
        registerCommands();
    }

    @Override
    public void onMessageReceived (@Nonnull MessageReceivedEvent event) {
        logger.traceEntry();
        // Non negotiable, this ignores bots
        if (event.getAuthor().isBot()) {return;}

        CommandContext ctx = CommandContext.generateContext(event);
        GuildSettingsTable.GuildSettings gs = ctx.getGuildSettings();

        try {
            if (!ctx.isValid()) {
                // Likely not a command
                return;
            }

            logger.debug("Possible command: {}", ctx.getKeyword());
            Command command = getCommandByKeyword(ctx.getKeyword());

            if (command == null) {
                // TODO: Check config
                //                ctx.reply("No command called `" + ctx.getKeyword() + "` exists.");
                logger.traceExit("No command");
                return;
            }
            if (!command.isAllowedInDms() && event.getChannelType() == ChannelType.PRIVATE) {
                //                ctx.reply("This command must be used in a server.");
                logger.traceExit("Not allowed in DM");
                return;
            }

            // Check ratelimits
            OffsetDateTime resetTime = RatelimitUtils.getResetTime(event.getAuthor(), command);
            if (resetTime != null && resetTime.isAfter(OffsetDateTime.now())) {
                String humanized = Humanize.naturalTime(Date.from(resetTime.toInstant())); // Why is Java time so stupid?
                //                ctx.reply("This command is on cooldown! You can use it again " + humanized + ".");
                logger.traceExit("Ratelimit");
                return;
            }

            command.run(ctx);
        } catch (Exception err) {
            String msg = "Command threw an error. This has been reported to the dev automatically.\n";
            msg += "Error: `" + err.getMessage() + "`";
            if (err.getCause() != null) {
                Throwable cause = err.getCause();
                msg += "\nCause: `" + cause.getClass().getName() + ": " + cause.getMessage() + "`";
            }

            ctx.reply(msg);
            MiscUtils.sendMessageToDeveloper(msg);

            logger.error("Command threw an error.", err);
            logger.traceExit("Command error");
            return;
        }

        logger.traceExit();
    }

    /**
     * Get the command a keyword is for. Only searches registered commands
     *
     * @param keyword keyword to check
     *
     * @return Command it matched, or null if none matched
     */
    public static Command getCommandByKeyword (@Nonnull String keyword) {
        for (Command c : COMMANDS) {
            if (c.matchesKeyword(keyword)) {
                return c;
            }
        }

        return null;
    }

    /**
     * Register all commands into the command list
     */
    public static void registerCommands () {
        COMMANDS.add(new About());
        COMMANDS.add(new ActivityPie());
        COMMANDS.add(new ThrowError());
    }
}
