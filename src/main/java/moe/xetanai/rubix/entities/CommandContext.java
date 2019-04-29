package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;


public class CommandContext {
	private MessageReceivedEvent event;
	private GuildSettingsTable.GuildSettings guildSettings;
	private String[] args;

	public CommandContext(MessageReceivedEvent event, GuildSettingsTable.GuildSettings guildSettings) {
		this.event = event;
		this.guildSettings = guildSettings;
		this.args = event.getMessage().getContentRaw().split(" ");

		// Strip prefix from the keyword argument
		this.args[0] = this.args[0].substring(guildSettings.getPrefix().length());
	}

	// Setters

	// Getters

	public MessageReceivedEvent getEvent() {
		return this.event;
	}

	public String getKeyword() {
		return this.args[0];
	}

	public String[] getArgs() {
		return this.args;
	}

	public GuildSettingsTable.GuildSettings getGuildSettings() {
		return guildSettings;
	}

	// Helper methods

	public void reply(String msg) {
		this.event.getChannel().sendMessage(msg).queue();
	}
}
