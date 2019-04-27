package moe.xetanai.rubix.entities;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandContext {
	private MessageReceivedEvent event;
	private boolean matchedPrefix = false;
	private String keyword;

	public CommandContext(MessageReceivedEvent event) {
		this.event = event;

		String message = event.getMessage().getContentRaw();
		// TODO: Custom prefixes
		if(!message.startsWith("!")) {return;}
		this.matchedPrefix = true;

		// TODO: Substring by prefix length
		this.keyword = message.split(" ")[0].substring(1);
	}

	// Setters

	// Getters

	public MessageReceivedEvent getEvent() {
		return this.event;
	}

	public boolean matchedPrefix() {
		return this.matchedPrefix;
	}

	public String getKeyword() {
		return this.keyword;
	}

	// Helper methods

	public void reply(String msg) {
		this.event.getChannel().sendMessage(msg).queue();
	}
}
