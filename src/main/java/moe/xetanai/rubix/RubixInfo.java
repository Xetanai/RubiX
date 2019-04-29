package moe.xetanai.rubix;

import net.dv8tion.jda.core.entities.User;

public class RubixInfo {
	private static final int VERSION_MAJOR = 0;
	private static final int VERSION_MINOR = 0;
	private static final int VERSION_PATCH = 0;
	private static final long BOT_OWNER_ID = 155490847494897664L;

	public static final String RELEASE_NAME = "InDev";
	public static final String VERSION = String.format("%d.%d.%d",
			VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, RELEASE_NAME);

	// Links
	public static final String LINK_INVITE = "https://discordapp.com/api/oauth2/authorize?client_id=254746730367680512&scope=bot";
	public static final String LINK_SERVER = "https://discord.gg/x4fPTjt";
	public static final String LINK_GIT = "https://git.xetanai.moe/Xetanai/RubiX";

	public static final String LINK_FIELD_TEXT = String.format(
			"[Invite RubiX to your server!](%s)\n" +
			"[Support server](%s)\n" +
			"[RubiX source code](%s)",
			LINK_INVITE, LINK_SERVER, LINK_GIT
	);

	// Below fields are populated on first fetch
	private static String BOT_AVATAR = null;
	private static User BOT_OWNER = null;

	public static String getAvatar() {
		if(BOT_AVATAR == null) {
			BOT_AVATAR = Main.getApi().getSelfUser().getEffectiveAvatarUrl();
		}
		return BOT_AVATAR;
	}

	public static User getOwner() {
		if(BOT_OWNER == null) {
			BOT_OWNER = Main.getApi().getUserById(BOT_OWNER_ID);
		}
		return BOT_OWNER;
	}
}
