package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.Main;
import moe.xetanai.rubix.database.tables.GuildSettingsTable.GuildSettings;
import moe.xetanai.rubix.modules.CommandModule;
import moe.xetanai.rubix.utils.BotMetaUtils;
import moe.xetanai.rubix.utils.RatelimitUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.OffsetDateTime;

/**
 * Represents a specific command usage
 */
public class CommandContext {

    private static final Logger logger = LogManager.getLogger(CommandContext.class.getName());

    private MessageReceivedEvent event;
    private GuildSettings guildSettings;

    private CommandContext (MessageReceivedEvent event, GuildSettings gs) {
        this.event = event;
        this.guildSettings = gs;
    }

    @Nullable
    public static CommandContext generateContext (MessageReceivedEvent event) {
        GuildSettings gs = null;

        // Populate guild settings
        try {
            if (event.getChannel().getType().equals(ChannelType.TEXT))
                gs = Main.getDatabase().guildSettings.getSettings(event.getGuild().getIdLong());
            else
                gs = Main.getDatabase().guildSettings.getDefault();
        } catch (SQLException err) {
            logger.error("Failed to create a command context.", err);
            return null;
        }

        return new CommandContext(event, gs);
    }

    /* GETTERS */

    /**
     * @return Guild's configured settings, or the default
     */
    @Nonnull
    public GuildSettings getGuildSettings () {return this.guildSettings;}

    /**
     * It's not advised to use methods contained here for anything besides reading, as they will bypass internal
     * post-processing methods. This method exposes the JDA API for message events.
     *
     * @return JDA MessageReceivedEvent that this context was created with.
     */
    public MessageReceivedEvent getEvent () {return this.event;}

    /**
     * Gets the used prefix for this command context.
     *
     * @return enum denoting which prefix was used
     */
    public PrefixType getPrefixType () {
        String message = this.event.getMessage().getContentRaw();
        if (message.startsWith(BotMetaUtils.getDefaultPrefix()))
            return PrefixType.DEFAULT;
        if (message.startsWith(this.guildSettings.getEffectivePrefix()))
            return PrefixType.CONFIGURED_BY_GUILD;
        if (message.startsWith(BotMetaUtils.getBotUser().getAsMention()))
            return PrefixType.MENTION;
        return null;
    }

    /**
     * Determines wether or not the bot should acknowledge a command was given.
     * Should currently only be false if the guild has overridden the default prefix, or if the message did not match
     * any valid prefixes.
     *
     * @return boolean representing wether or not the command context is valid
     */
    public boolean isValid () {
        if (getPrefixType() == PrefixType.DEFAULT && this.guildSettings.getPrefix() != null) {
            // The default prefix is to be ignored if the guild has set one
            return false;
        }
        // False if the message matched an accepted prefix. True otherwise
        return getPrefixType() != null;
    }

    /**
     * Gets an array of the arguments supplied to the command.
     * The index 0 will be the keyword used for determining which command is to be run.
     * The prefix, regardless of type, will be stripped.
     *
     * @return An array of Strings supplied by the user
     */
    @Nonnull
    public String[] getArgs () {
        String[] args = this.event.getMessage().getContentRaw().split(" ");

        if (args.length == 0) {
            // Messages with no text will have no arguments.
            return new String[0];
        }

        // Strip the prefix from the first argument
        int prefixLength = getPrefixType() == PrefixType.MENTION ?
            BotMetaUtils.getBotUser().getAsMention().length() :
            this.guildSettings.getEffectivePrefix().length();
        args[0] = args[0].substring(prefixLength);

        return args;
    }

    /**
     * Syntactical sugar. Simply calls <code>CommandContext#getArgs()[0];</code>
     *
     * @return The command keyword for this context
     */
    public String getKeyword () {
        return getArgs()[0];
    }

    /* SETTERS */

    /**
     * Puts the command in use on cooldown for the current user for a given number of seconds.
     *
     * @param seconds Seconds to ratelimit for
     */
    public void setRateLimitTime (int seconds) {
        RatelimitUtils.setResetTime(this.event.getAuthor(), CommandModule.getCommandByKeyword(getKeyword()), OffsetDateTime.now().plusSeconds(seconds));
    }

    /* HELPERS */

    /**
     * Replies to the user with post-processing in the invoked channel.
     * Convenience method for simple text-only replies.
     *
     * @param message
     */
    public void reply (@Nonnull String message) {
        reply(new MessageBuilder().append(message).build(), false);
    }

    /**
     * Replies to the user with post-processing.
     * If privateMessage is true, the user will be PMed and a message will be sent in the channel specifying this.
     *
     * @param message        Message to send
     * @param privateMessage Wether or not to PM the user or to reply in the channel the command was given.
     */
    public void reply (@Nonnull Message message, boolean privateMessage) {
        MessageChannel target = privateMessage ?
            this.event.getAuthor().openPrivateChannel().complete() :
            this.event.getChannel();

        // TODO: post-process. Filter tokens, slurs, other useful stuff. Also, provide toggleable tips

        target.sendMessage(message).queue();

        if (this.event.getChannel().getType() != ChannelType.PRIVATE && privateMessage) {
            this.event.getChannel().sendMessage("I sent you a PM! Make sure you're allowing messages from this server.").queue();
        }
    }

    public enum PrefixType {
        DEFAULT, CONFIGURED_BY_GUILD, MENTION
    }
}
