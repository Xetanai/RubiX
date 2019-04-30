package moe.xetanai.rubix.entities;

import net.dv8tion.jda.core.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

/**
 * Represents a command
 */
public class Command {
	private final Logger logger;

	private String[] keywords;
	private Permission[] perms_bot;
	private Permission[] perms_user;

	/**
	 * Creates a command
	 * @param keywords keywords that can invoke this command
	 */
	public Command(@Nonnull String[] keywords) {
		this.keywords = keywords;
		this.logger = LogManager.getLogger(this.getClass().getName());

		this.perms_bot = new Permission[0];
		this.perms_user = new Permission[0];
	}

	// Getters

	/**
	 * @return logger
	 */
	@Nonnull
	protected Logger getLogger() {
		return this.logger;
	}

	/**
	 * @return array of keywords which invoke this command
	 */
	@Nonnull
	public String[] getKeywords() {
		return keywords;
	}

	/**
	 * Check if a keyword matches any for this command
	 * @param keyword Keyword to check
	 * @return True if this command matches, false otherwise
	 */
	public boolean matchesKeyword(String keyword) {
		for(String s : this.keywords) {
			if(s.equals(keyword)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return true if this command is allowed in DMs. False otherwise
	 */
	public boolean isAllowedInDms() {
		return false;
	}

	/**
	 * @return array of permissions the bot requires for this command
	 */
	@Nonnull
	public Permission[] getBotPermissions() {
		return perms_bot;
	}

	/**
	 * @return array of permissions the invoking user requires for this command
	 */
	@Nonnull
	public Permission[] getUserPermissions() {
		return perms_user;
	}

	// Setters

	/**
	 * Changes this command's keywords. Effective immediately
	 * @param keywords new keywords. Old array replaced
	 * @return chainable this
	 */
	@Nonnull
	public Command setKeywords(@Nonnull String[] keywords) {
		this.keywords = keywords;
		return this;
	}

	/**
	 * Changes this command's permission requirements. Effective immediately
	 * Does not have any effect on Discord-enforced permission requirements... Obviously.
	 * @param perms new permissions. Old array replaced
	 * @return chainable this
	 */
	@Nonnull
	public Command setBotPermissions(@Nonnull Permission[] perms) {
		this.perms_bot = perms;
		return this;
	}

	/**
	 * Changes this command's permission requirements. Effective immediately
	 *
	 * @param perms new permissions. Old array replaced
	 * @return chainable this
	 */
	@Nonnull
	public Command setUserPermissions(@Nonnull Permission[] perms) {
		this.perms_user = perms;
		return this;
	}

	// General methods

	/**
	 * Invoke the command
	 * @param ctx CommandContext containing context for the command
	 * @throws CommandException if the command threw an error
	 */
	public void run(@Nonnull CommandContext ctx) throws CommandException {
		logger.warn("Command not implemented.");
		throw new CommandException("Command not implemented.");
	}
}
