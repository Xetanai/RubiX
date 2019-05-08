package moe.xetanai.rubix.entities.commands;

import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.CommandException;
import moe.xetanai.rubix.utils.ImageUtils;
import net.dv8tion.jda.core.entities.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityPie extends Command {
	private static final String FONT_NAME = "SourceCodePro-Regular.ttf";
	private static Font BASE_FONT;
	private static double MINIMUM_PERCENTAGE = 0.01d;
	private static Color[] COLORS = {
			new Color(80,80,80,255),
			new Color(0,147,255, 255),
			new Color(253, 1, 0, 255),
			new Color(1, 253, 32, 255),
			new Color(253, 104, 1, 255),
			new Color(253, 214, 0, 255)
	};

	private static Font FONT_SMALL = null;
	private static Font FONT_REGULAR = null;
	private static Font FONT_LARGE = null;
	private static Font FONT_XL = null;
	private static StandardChartTheme CHART_THEME = (StandardChartTheme) StandardChartTheme.createJFreeTheme();

	public ActivityPie() {
		super(new String[]{"activitypie"});

		try {
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream(FONT_NAME);
			if(is == null) throw new IOException("Font was null.");
			BASE_FONT = Font.createFont(Font.TRUETYPE_FONT, is);

			FONT_SMALL = BASE_FONT.deriveFont(12f);
			FONT_REGULAR = BASE_FONT.deriveFont(18f);
			FONT_LARGE = BASE_FONT.deriveFont(25f);
			FONT_XL = BASE_FONT.deriveFont(32f);

			CHART_THEME.setSmallFont(FONT_SMALL);
			CHART_THEME.setRegularFont(FONT_REGULAR);
			CHART_THEME.setLargeFont(FONT_LARGE.deriveFont(Font.BOLD));
			CHART_THEME.setExtraLargeFont(FONT_XL.deriveFont(Font.BOLD));

			CHART_THEME.setChartBackgroundPaint(new Color(0,0,0,0));
			CHART_THEME.setPlotBackgroundPaint(new Color(0,0,0,0));
			CHART_THEME.setPlotOutlinePaint(new Color(0,0,0,0));
			CHART_THEME.setShadowVisible(false);
		} catch (IOException | FontFormatException err) {
			this.getLogger().error("Failed to prepare font");
		}
	}

	@Override
	public boolean isAllowedInDms() {
		return false;
	}

	@Override
	public void run(CommandContext ctx) throws CommandException{
		try {
			TextChannel channel = ctx.getEvent().getTextChannel();
			int amt = 100;

			if (ctx.getArgs().length != 1) {
				String amtArg = ctx.getArgs()[1];
				try {
					int tamt = Integer.parseInt(amtArg);

					if (tamt > 0 && tamt <= 1000) {
						amt = tamt;
					} else {
						ctx.reply("Amount must be between 1 and 1,000. You gave " + tamt);
						return;
					}
				} catch (NumberFormatException err) {
					ctx.reply(amtArg + " is not a number.");
					return;
				}
			}

			JFreeChart jfc = createChartForChannel(channel, amt);
			CHART_THEME.apply(jfc);
			InputStream is =ImageUtils.getInputStream(jfc.createBufferedImage(800, 600));

			channel.sendFile(is, "activeusers.png").queue();
		} catch (IOException err) {
			this.getLogger().error(err);
			throw new CommandException("Failed to generate the chart.", err);
		}
	}

	private Map<User,Integer> getMessageCounts(TextChannel tc, int count) {
		Map<User, Integer> res = new HashMap<>();

		// Null represents the total count.
		res.put(null, 0);

		MessageHistory history = tc.getHistory();

		// Fetch until 0 remain
		while(count > 0) {
			List<Message> messages;

			// Fetch 100 or remaining amount, whichever is smaller,
			// subtracting remaining count appropriately
			if(count < 100) {
				// Last fetch
				messages = history.retrievePast(count).complete();
				count = 0;
			} else {
				messages = history.retrievePast(100).complete();
				count -= 100;
			}

			// Iterate over every message in this batch
			for(Message m : messages) {
				res.put(null, res.get(null)+1);

				User author = m.getAuthor();

				// Create the key if they don't have one yet
				if(!res.containsKey(author)) {
					res.put(author, 1);
					continue;
				}

				res.replace(author, res.get(author)+1);
			}
		}

		return res;
	}

	private DefaultPieDataset generateDataset(Map<User, Integer> map) {
		DefaultPieDataset ds = new DefaultPieDataset();
		int total = map.get(null);
		ds.setValue("Other", 0);
		ds.sortByValues(SortOrder.ASCENDING);

		map.keySet().iterator().forEachRemaining(user -> {
			if(user != null) {
				double percentage = (double)(map.get(user)) / total;

				if(percentage > MINIMUM_PERCENTAGE) {
					ds.setValue(user.getAsTag(), map.get(user));
				} else {
					// Increment other count
					ds.setValue("Other", ds.getValue("Other").intValue() + map.get(user));
				}
			}
		});

		// Remove other if nobody was low enough
		if(ds.getValue("Other").intValue() == 0) {
			ds.remove("Other");
		}

		return ds;
	}

	private JFreeChart createChartForChannel(TextChannel tc, int amount) {
		DefaultPieDataset dataset = generateDataset(getMessageCounts(tc, amount));
		JFreeChart chart = ChartFactory.createPieChart(
				"Most active users in the last "+ amount +" messages.",
				dataset,
				true,
				true,
				false
		);

		// Customize colors
		// TODO: Figure out why the fizzity-uck this isn't working.
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setSectionPaint("Other", new Color(80,80,80,255));

		return chart;
	}
}
