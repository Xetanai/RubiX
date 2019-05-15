package moe.xetanai.rubix.entities.commands;

import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.CommandException;
import moe.xetanai.rubix.utils.ImageUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
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
	private static String FONT_FILENAME = "SourceCodePro-Regular.ttf";
	private static Font BASE_FONT = null;
	private static double MINIMUM_PERCENTAGE = 0.05d;
	private static Color[] COLORS = {
			new Color(0,147,255, 255),
			new Color(253, 1, 0, 255),
			new Color(1, 253, 32, 255),
			new Color(253, 104, 1, 255),
			new Color(253, 214, 0, 255)
	};
	private static StandardChartTheme CHART_THEME = null;

	public ActivityPie() {
		super(new String[]{"activitypie"});

		loadFont();
		createChartTheme();
	}

	@Override
	public boolean isAllowedInDms() {
		// Dummy, dms would just have 2 colors.
		return false;
	}

	@Override
	public void run(CommandContext ctx) throws CommandException {
		try {
			TextChannel channel = ctx.getEvent().getTextChannel();
			int amt = 100;

			if(ctx.getArgs().length != 1) {
				String amtArg = ctx.getArgs()[1];
				try {
					amt = Integer.parseInt(amtArg);
				} catch (NumberFormatException err) {
					ctx.reply(amtArg +" is not an integer number.");
					return;
				}
			}

			if(amt < 10 || amt > 1000) {
				ctx.reply("Amount must be between 10 and 1,000.");
				return;
			}

			JFreeChart jfc = createChartForChannel(channel, amt);
			InputStream is = ImageUtils.getInputStream(jfc.createBufferedImage(800,600));

			channel.sendFile(is, "activeusers.png").queue();
		} catch (IOException err) {
			this.getLogger().error(err);
			throw new CommandException("Failed to generate the chart.", err);
		}
	}

	private void loadFont() {
		try {
			// Load our font from resources
			InputStream is = getClass().getClassLoader().getResourceAsStream(FONT_FILENAME);
			if(is == null) throw  new IOException("Font was null");
			BASE_FONT = Font.createFont(Font.TRUETYPE_FONT, is);
			is.close();
		} catch (IOException | FontFormatException err) {
			this.getLogger().error("Failed to load font.", err);
		}
	}

	private void createChartTheme() {
		// Set the starting point
		CHART_THEME = (StandardChartTheme) StandardChartTheme.createJFreeTheme();

		// Change fonts
		CHART_THEME.setSmallFont(BASE_FONT.deriveFont(18f));
		CHART_THEME.setRegularFont(BASE_FONT.deriveFont(Font.BOLD,20f));
		CHART_THEME.setLargeFont(BASE_FONT.deriveFont(Font.BOLD, 25f));
		CHART_THEME.setExtraLargeFont(BASE_FONT.deriveFont(Font.BOLD, 32f));

		// Paints
		Color invisible = new Color(0,0,0,0);
		CHART_THEME.setChartBackgroundPaint(invisible);
		CHART_THEME.setPlotBackgroundPaint(invisible);
		CHART_THEME.setPlotOutlinePaint(invisible);
		CHART_THEME.setShadowPaint(invisible);
	}

	private Map<String, Integer> getMessageCounts(TextChannel tc, int count) {
		Map<String, Integer> res = new HashMap<>();
		MessageHistory history = tc.getHistory();

		// While more than 0 remain
		while(count > 0) {
			List<Message> msgs;

			// Fetch 100 or remaining, whichever is smaller
			if(count < 100) {
				// This is the last fetch. 100 may be too many.
				msgs = history.retrievePast(count).complete();
			} else {
				msgs = history.retrievePast(100).complete();
			}
			count -= 100;

			// Iterate over every message in this batch
			for(Message m : msgs) {
				User author = m.getAuthor();

				// Increment their count, creating it as necessary
				res.compute(author.getAsTag(), (k,v) -> v==null ? 1 : v+1);
			}

			// Add this batch to our total count
			int size = msgs.size();
			res.compute("TOTAL", (k,v) -> v == null ? size : v + size);
		}

		return res;
	}

	private DefaultPieDataset generateDataset(Map<String, Integer> map) {
		DefaultPieDataset ds = new DefaultPieDataset();
		int total = map.get("TOTAL");
		ds.setValue("Other", 0);

		map.keySet().iterator().forEachRemaining(user -> {
			double percentage = (double)(map.get(user)) / total;

			if(percentage > MINIMUM_PERCENTAGE) {
				ds.setValue(user, map.get(user));
			} else {
				ds.setValue("Other", ds.getValue("Other").intValue() + map.get(user));
			}
		});

		// Remove Other if unused
		if(ds.getValue("Other").intValue() == 0) {
			ds.remove("Other");
		}
		// Remove meta TOTAL value
		ds.remove("TOTAL");


		ds.sortByValues(SortOrder.DESCENDING);

		return ds;
	}

	private JFreeChart createChartForChannel(TextChannel tc, int amount) {
		DefaultPieDataset dataset = generateDataset(getMessageCounts(tc, amount));
		JFreeChart chart = ChartFactory.createPieChart(
				"Most active users in the last " + amount + " messages.",
				dataset,
				true,
				false,
				false
		);
		CHART_THEME.apply(chart);
		chart.getLegend().setItemFont(BASE_FONT.deriveFont(30f));

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(null);

		// Customize colors
		int i = 0;
		for(Object key : plot.getDataset().getKeys()) {
			if(key.equals("Other")) {
				continue;
			}

			plot.setSectionPaint((String) key, COLORS[i%COLORS.length]);
			i++;
		}

		plot.setSectionPaint("Other", Color.GRAY);
		return chart;
	}
}
