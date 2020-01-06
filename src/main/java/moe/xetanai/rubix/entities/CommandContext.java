package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.Main;
import moe.xetanai.rubix.database.tables.GuildSettingsTable;
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
import java.time.OffsetDateTime;

/**
 * Represents a specific command usage
 */
public class CommandContext {
    private static final Logger logger = LogManager.getLogger(CommandContext.class.getName());

    private MessageReceivedEvent event;
    @Nullable
    private RubixGuild guild;
    private RubixUser user;

    public CommandContext(MessageReceivedEvent event) {
        this.event = event;
        this.guild = event.getGuild() == null ? null : new RubixGuild(event.getGuild().getIdLong());
        this.user = new RubixUser(event.getAuthor().getIdLong());
    }

    /**
     * Gets the proxied guild this command was executed in.
     * Is null when the context is from a channel.
     * @return {@link moe.xetanai.rubix.entities.RubixGuild RubixGuild} or null
     */
    @Nullable
    public RubixGuild getGuild() {return this.guild;}

    /**
     * Gets the user responsible for this context
     * @return {@link moe.xetanai.rubix.entities.RubixUser RubixUser}
     */
    @Nonnull
    public RubixUser getUser() {return this.user;}

    /**
     * Gets the guild's settings, or the defaults if the settings either haven't been set, or
     * if there is no guild responsible. The results of this method will dictate how the bot
     * should effectively behave, as it will always return the most fitting settings to the
     * current context.
     * @return The effective guild settings of this context
     */
    @Nonnull
    public GuildSettingsTable.GuildSettings getEffectiveGuildSettings() {
        if(this.guild != null)
            return this.guild.getSettings();
        return Main.getDatabase().guildSettings.getDefault();
    }

    /**
     * Determines which kind of prefix was used to create this context.
     * CONFIGURED_BY_GUILD will be returned if the prefix matches the one set explicitly
     * by the guild, even if that prefix is identical to the default or a mention.
     * NONE will be returned if this context matches no possible prefix.
     * @return The prefix type used, or NONE.
     */
    @Nonnull
    public PrefixType getPrefixType() {
        String msg = this.event.getMessage().getContentRaw();
        GuildSettingsTable.GuildSettings gs = getEffectiveGuildSettings();

        // This is checked first so that mentions can't be effectively disabled by
        // an @ prefix. I doubt this should ever happen, but people often amaze.
        if(msg.startsWith(BotMetaUtils.getBotUser().getAsMention()))
            return PrefixType.MENTION;

        if(!gs.isDefault() && msg.startsWith(this.getEffectiveGuildSettings().getEffectivePrefix()))
            return PrefixType.CONFIGURED_BY_GUILD;
        if(msg.startsWith(BotMetaUtils.getDefaultPrefix()))
            return PrefixType.DEFAULT;


        return PrefixType.NONE;
    }

    /**
     * Determines wether or not a message should be (publicly) acknowledged as a
     * potential command. Specifically, guilds which have a custom prefix set should
     * not receive responses to messages with the default prefix. Mentions should
     * always work. Irrelevant messages should always be false, ideally.
     * @return true if this message can be publicly acknowledged, false otherwise
     */
    public boolean shouldAcknowledge() {
        PrefixType pt = getPrefixType();

        if(pt == PrefixType.DEFAULT && !getEffectiveGuildSettings().isDefault()) {
            // There is a custom prefix set. Defaults are ignored.
            return false;
        }

        // Barring the above condition, this suffices.
        return getPrefixType() != PrefixType.NONE;
    }

    public enum PrefixType {
        DEFAULT, CONFIGURED_BY_GUILD, MENTION, NONE
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
        // TODO: Create a MessageProxy object and use its filtered methods.
        String[] args = this.event.getMessage().getContentRaw().split(" ");

        if (args.length == 0) {
            // Messages with no text will have no arguments.
            return new String[0];
        }

        // Strip the prefix from the first argument
        int prefixLength = getPrefixType() == PrefixType.MENTION ?
            BotMetaUtils.getBotUser().getAsMention().length() :
            this.getEffectiveGuildSettings().getEffectivePrefix().length();
        args[0] = args[0].substring(prefixLength);

        return args;
    }

    /**
     * Syntactical sugar. Simply calls <code>CommandContext#getArgs()[0];</code>
     *
     * @return The command keyword for this context
     */
    public String getKeyword () {
        return this.getArgs()[0];
    }
    /* SETTERS */

    /**
     * Puts the command in use on cooldown for the current user for a given number of seconds.
     *
     * @param seconds Seconds to ratelimit for
     */
    public void setRateLimitTime (int seconds) {
        RatelimitUtils.setResetTime(
            this.event.getAuthor(),
            CommandModule.getCommandByKeyword(getKeyword()),
            OffsetDateTime.now().plusSeconds(seconds)
        );
    }

    /* HELPERS */

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

        // TODO: post-process. Useful stuff. Also, provide toggleable tips

        target.sendMessage(message).queue();

        if (this.event.getChannel().getType() != ChannelType.PRIVATE && privateMessage) {
            this.event.getChannel().sendMessage("I sent you a PM! Make sure you're allowing messages from this server!").queue();
        }
    }

    /**
     * Replies to the user with post-processing in the invoked channel.
     * Convenience method for simple text-only replies.
     *
     * @param message
     */
    public void reply (@Nonnull String message) {
        reply(new MessageBuilder().append(message).build(), false);
    }
}