package moe.xetanai.rubix.entities.commands;

import moe.xetanai.rubix.RubixInfo;
import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.utils.BotMetaUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.awt.*;
import java.util.Random;

public class About extends Command {

    private static Color[] RANDOM_COLORS = {
        new Color(0, 147, 255),
        new Color(253, 1, 0),
        new Color(1, 253, 32),
        new Color(253, 104, 1),
        new Color(253, 214, 0)
    };

    public About () {
        super(new String[]{"about", "info"});
    }

    @Override
    public boolean isAllowedInDms () {return true;}

    @Override
    public void run (CommandContext ctx) {
        Random rand = new Random();
        Color randColor = RANDOM_COLORS[rand.nextInt(RANDOM_COLORS.length - 1)];

        EmbedBuilder e = new EmbedBuilder().setTitle(String.format("RubiX (%s) v%s", RubixInfo.RELEASE_NAME, RubixInfo.VERSION)).setDescription("RubiX is an open source bot made in Java.\n" + "RubiX's goal is to make your server's data easier to visualize with a number of graphs and stats at your disposal.\n" + "If you have any suggestions, complaints, or compliments, please use the `feedback` command!").setThumbnail(BotMetaUtils.getBotUser().getEffectiveAvatarUrl()).setColor(randColor).addField("Developer", BotMetaUtils.getOwners()[0].getAsTag(), true) // TODO: Make this a list.
            .addField("Important links", RubixInfo.LINK_FIELD_TEXT, false);

        ctx.reply(new MessageBuilder(e).build(), true);
    }
}
