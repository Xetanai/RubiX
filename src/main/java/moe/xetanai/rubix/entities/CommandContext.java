package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import moe.xetanai.rubix.modules.CommandModule;
import moe.xetanai.rubix.utils.RatelimitUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * Represents a particular use of a command
 */
public class CommandContext {
	private MessageReceivedEvent event;
	private GuildSettingsTable.GuildSettings guildSettings;
	private String[] args;

	/**
	 * Create context from the event and guildsettings
	 * @param event the message event that invoked the command
	 * @param guildSettings the settings of the guild it was in
	 */
	public CommandContext(MessageReceivedEvent event, GuildSettingsTable.GuildSettings guildSettings) {
		this.event = event;
		this.guildSettings = guildSettings;
		this.args = event.getMessage().getContentRaw().split(" ");

		// Strip prefix from the keyword argument
		this.args[0] = this.args[0].substring(guildSettings.getPrefix().length());
	}

	// Getters

	/**
	 * @return The message received event that caused this
	 */
	@Nonnull
	public MessageReceivedEvent getEvent() {
		return this.event;
	}

	/**
	 * @return The keyword used
	 */
	@Nonnull
	public String getKeyword() {
		return this.args[0];
	}

	/**
	 * Returns arguments given, split by spaces. Index 0 is the keyword
	 * @return Arguments given
	 */
	@Nonnull
	public String[] getArgs() {
		return this.args;
	}

	/**
	 * @return Get this guild's settings
	 */
	@Nonnull
	public GuildSettingsTable.GuildSettings getGuildSettings() {
		return guildSettings;
	}

	// Setter methods

	/**
	 * Ratelimits the current command for a given amount of time for the current user
	 * @param seconds Seconds to limit for
	 */
	public void setRatelimitTime(int seconds) {
		RatelimitUtils.setResetTime(this.event.getAuthor(), CommandModule.getCommandByKeyword(this.getKeyword()), OffsetDateTime.now().plusSeconds(seconds));
	}

	// Helper methods

	/**
	 * Quickly send a basic reply in the channel
	 * @param msg message to send
	 */
	public void reply(@Nonnull String msg) {
		this.event.getChannel().sendMessage(msg).queue();
	}
}
