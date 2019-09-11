package moe.xetanai.rubix.utils;

import net.dv8tion.jda.core.entities.User;

public class MiscUtils {

    private MiscUtils () {}

    public static void sendMessageToDeveloper (String message) {
        // TODO: Create a proper support system.
        User owner = BotMetaUtils.getOwners()[0];

        owner.openPrivateChannel().complete().sendMessage(message).queue();
    }
}
