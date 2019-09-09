package moe.xetanai.rubix.utils;

import moe.xetanai.rubix.RubixInfo;
import net.dv8tion.jda.core.entities.User;

public class MiscUtils {
	private MiscUtils(){};

	public static void sendMessageToDeveloper(String message) {
		// TODO: Things like this should be in a utils class, NOT an info class.
		User owner = RubixInfo.getOwner();

		owner.openPrivateChannel().complete().sendMessage(message).queue();
	}
}
