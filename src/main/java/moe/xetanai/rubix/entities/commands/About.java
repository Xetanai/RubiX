package moe.xetanai.rubix.entities.commands;

import moe.xetanai.rubix.RubixInfo;
import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Random;

public class About extends Command {
	private static Color[] RANDOM_COLORS = {
			new Color(0,147,255),
			new Color(253, 1, 0),
			new Color(1, 253, 32),
			new Color(253, 104, 1),
			new Color(253, 214, 0)
	};
	private static EmbedBuilder EMBED_TEMPLATE = new EmbedBuilder()
			.setTitle(String.format(
					"RubiX (%s) v%s",
					RubixInfo.RELEASE_NAME,
					RubixInfo.VERSION
			)).setDescription("RubiX is an open source bot made in Java, provided free of charge.\n" +
						"It's made to be as customizeable as possible, valuing user and admin control above a lengthy command list.\n" +
						"If you have any suggestions, complaints, or compliments, please use the `feedback` command!");

	public About() {
		super(new String[]{"about", "info"});
	}

	@Override
	public void run(CommandContext ctx) {
		Random rand = new Random();
		Color randColor = RANDOM_COLORS[rand.nextInt(RANDOM_COLORS.length-1)];

		EmbedBuilder e = new EmbedBuilder(EMBED_TEMPLATE)
				.setThumbnail(RubixInfo.getAvatar())
				.setColor(randColor)
				.addField("Developer", RubixInfo.getOwner().getAsTag(), true)
				.addField("Prefix here", ctx.getGuildSettings().getPrefix(), true)
				.addField("Important links", RubixInfo.LINK_FIELD_TEXT, false);

		ctx.getEvent().getChannel().sendMessage(e.build()).queue();
	}
}
