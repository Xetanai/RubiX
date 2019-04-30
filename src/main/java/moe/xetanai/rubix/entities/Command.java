package moe.xetanai.rubix.entities;

import net.dv8tion.jda.core.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Command {
	private final Logger logger;

	private String[] keywords;
	private boolean allowDms;
	private Permission[] perms_bot;
	private Permission[] perms_user;

	public Command(String[] keywords) {
		this.keywords = keywords;
		this.logger = LogManager.getLogger(this.getClass().getName());

		this.allowDms = false;
		this.perms_bot = new Permission[0];
		this.perms_user = new Permission[0];
	}

	// Getters

	protected Logger getLogger() {
		return this.logger;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public boolean matchesKeyword(String keyword) {
		for(String s : this.keywords) {
			if(s.equals(keyword)) {
				return true;
			}
		}

		return false;
	}

	public boolean isAllowedInDms() {
		return allowDms;
	}

	public Permission[] getBotPermissions() {
		return perms_bot;
	}

	public Permission[] getUserPermissions() {
		return perms_user;
	}

	// Setters

	public Command setKeywords(String[] keywords) {
		this.keywords = keywords;
		return this;
	}

	public Command setAllowedDms(boolean allowed) {
		this.allowDms = allowed;
		return this;
	}

	public Command setBotPermissions(Permission[] perms) {
		this.perms_bot = perms;
		return this;
	}

	public Command setUserPermissions(Permission[] perms) {
		this.perms_user = perms;
		return this;
	}

	// General methods

	public void run(CommandContext ctx) throws CommandException {
		logger.warn("Command not implemented.");
		throw new CommandException("Command not implemented.");
	}
}
