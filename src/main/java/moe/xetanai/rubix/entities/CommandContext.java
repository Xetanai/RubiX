package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.database.Database;
import moe.xetanai.rubix.database.tables.GuildSettingsTable;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.SQLException;

public class CommandContext {
	private MessageReceivedEvent event;
	private GuildSettingsTable.GuildSettings guildSettings;
	private String keyword;

	public CommandContext(MessageReceivedEvent event, GuildSettingsTable.GuildSettings guildSettings, String keyword) {
		this.event = event;
		this.guildSettings = guildSettings;
		this.keyword = keyword;
	}

	// Setters

	// Getters

	public MessageReceivedEvent getEvent() {
		return this.event;
	}

	public String getKeyword() {
		return this.keyword;
	}

	// Helper methods

	public void reply(String msg) {
		this.event.getChannel().sendMessage(msg).queue();
	}
}
