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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityPie extends Command {
	private static double DEFAULT_OTHER_PERCENTAGE = 0.05d;
	private static Color[] COLORS = {
		new Color(253, 1, 0, 255),
		new Color(1, 253, 32, 255),
		new Color(253, 104, 1, 255),
		new Color(253, 214, 0, 255),
		new Color(250, 250, 250, 255)
	};
	private static StandardChartTheme CHART_THEME = ActivityPie.initChartTheme();

	// Bounds
	private static Rectangle2D IMAGE_SIZE = new Rectangle2D.Double(
			0,
			0,
			1280,
			720
	);

	public ActivityPie() {
		super(new String[]{"activitypie"});
	}

	@Override
	public boolean isAllowedInDms() {
		return false;
	}

	@Override
	public void run(CommandContext ctx) throws CommandException {
		String arg = ctx.getArgs().length == 1 ? null : ctx.getArgs()[1];
		int countarg = getMessageCountArg(arg);

		if(countarg < 10 || countarg > 1000) {
			ctx.reply("Amount must be between 10 and 1,000.");
			return;
		}

		TextChannel channel = ctx.getEvent().getTextChannel();
		JFreeChart chart = createChartForChannel(channel, countarg);
		try {
			channel.sendFile(ImageUtils.getInputStream(generateImage(chart)), "activitypie.png").queue();
		} catch (IOException err) {
			getLogger().error("Failed to generate chart.", err);
		}
	}

	private int getMessageCountArg(String arg) {
		int amt = 100; // Default is 100

		if(arg != null) {
			try {
				amt = Integer.parseInt(arg);
			} catch (NumberFormatException err) {
				return -1;
			}
		}

		return amt;
	}

	private JFreeChart createChartForChannel(TextChannel tc, int amt) {
		DefaultPieDataset ds = generateDataset(getMessageCounts(tc, amt));
		JFreeChart chart = ChartFactory.createPieChart(
				null,
				ds,
				true,
				false,
				false);
		CHART_THEME.apply(chart);
		chart.getLegend().setItemFont(ImageUtils.getFont().deriveFont(30f));

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(null);

		// Set colors
		int i = 0;
		String othersKey = null;
		for(Object key : plot.getDataset().getKeys()) {
			// Get exact name of our Others section, if applicable.
			if(((String) key).endsWith("Others")) {
				othersKey = (String) key;
			}

			plot.setSectionPaint((String) key, COLORS[i%COLORS.length]);
			i++;
		}

		if(othersKey != null) {
			plot.setSectionPaint(othersKey, Color.GRAY);
		}
		return chart;
	}

	private DefaultPieDataset generateDataset(Map<String, Integer> map) {
		DefaultPieDataset ds = new DefaultPieDataset();
		int total = map.values().stream().mapToInt(Integer::intValue).sum();
		int others = 0;
		int othersMessages = 0;
		String lastOther = null;

		for(String user : map.keySet()) {
			double percentage = (double)(map.get(user)) / total;

			if(percentage > DEFAULT_OTHER_PERCENTAGE) {
				ds.setValue(user, map.get(user));
			} else {
				others++;
				lastOther = user;
				othersMessages += map.get(user);
			}
		}

		// if Others contains only one, throw them back in
		ds.sortByValues(SortOrder.DESCENDING);
		if(others == 1) {
			ds.setValue(lastOther, othersMessages);
		} else {
			ds.setValue(others +" Others", othersMessages);
		}

		return ds;
	}

	private Map<String, Integer> getMessageCounts(TextChannel tc, int amt) {
		Map<String, Integer> results = new HashMap<>();
		MessageHistory history = tc.getHistory();

		// While more than 0 remain
		while(amt > 0) {
			List<Message> msgs;

			// Fetch 100 or remaining, whichever is smaller
			if(amt < 100) {
				// Last fetch.
				msgs = history.retrievePast(amt).complete();
			} else {
				msgs = history.retrievePast(100).complete();
			}
			amt -= 100;

			for(Message m : msgs) {
				User author = m.getAuthor();
				results.compute(author.getAsTag(), (k,v) -> v==null ? 1 : v+1);
			}
		}

		return results;
	}

	private BufferedImage generateImage(JFreeChart jfc) {
		BufferedImage finalImage = new BufferedImage(
				(int) IMAGE_SIZE.getWidth(),
				(int) IMAGE_SIZE.getHeight(),
				BufferedImage.TYPE_INT_ARGB
		);
		Graphics2D g2d = finalImage.createGraphics();
		ImageUtils utils = new ImageUtils(g2d, "activitypie");
		ImageUtils.setDefaultHints(g2d);

		// Background
		utils.fill(new RoundRectangle2D.Double(
				0,
				0,
				IMAGE_SIZE.getWidth(),
				IMAGE_SIZE.getHeight(),
				20,
				20
		), new Color(0,147,255));
		g2d.drawImage(jfc.createBufferedImage(
					(int) IMAGE_SIZE.getWidth() - 50,
					(int) IMAGE_SIZE.getHeight() - 50
				),
				25,
				25,
				null
		);

		utils.dispose();

		return finalImage;
	}

	private static StandardChartTheme initChartTheme() {
		StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();

		// Set fonts
		Font f = ImageUtils.getFont();
		theme.setSmallFont(f.deriveFont(18f));
		theme.setRegularFont(f.deriveFont(Font.BOLD, 20f));
		theme.setLargeFont(f.deriveFont(Font.BOLD, 25f));
		theme.setExtraLargeFont(f.deriveFont(Font.BOLD, 32f));

		// Paints
		Color invisible = new Color(0,0,0,0);
		theme.setChartBackgroundPaint(invisible);
		theme.setPlotBackgroundPaint(invisible);
		theme.setPlotOutlinePaint(invisible);
		theme.setShadowPaint(invisible);

		return theme;
	}
}
//
//			JFreeChart jfc = createChartForChannel(channel, amt);
//			BufferedImage finalImage = new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);
//			Graphics2D g2d = finalImage.createGraphics();
//			ImageUtils utils = new ImageUtils(g2d, "activitypie");
//
//			ImageUtils.setDefaultHints(g2d);
//			utils.fill(new RoundRectangle2D.Double(
//					0,
//					0,
//					1280,
//					720,
//					20,
//					20
//			), new Color(0,147,255));
//			g2d.drawImage(jfc.createBufferedImage(1230, 670), 25, 25, null);
//
//			InputStream is = ImageUtils.getInputStream(finalImage);
//			utils.dispose();
//
//			channel.sendFile(is, "activeusers.png").queue();
//		} catch (IOException err) {
//			this.getLogger().error(err);
//			throw new CommandException("Failed to generate the chart.", err);
//		}
//	}
//
//	private Map<String, Integer> getMessageCounts(TextChannel tc, int count) {
//		Map<String, Integer> res = new HashMap<>();
//		MessageHistory history = tc.getHistory();
//
//		// While more than 0 remain
//		while(count > 0) {
//			List<Message> msgs;
//
//			// Fetch 100 or remaining, whichever is smaller
//			if(count < 100) {
//				// This is the last fetch. 100 may be too many.
//				msgs = history.retrievePast(count).complete();
//			} else {
//				msgs = history.retrievePast(100).complete();
//			}
//			count -= 100;
//
//			// Iterate over every message in this batch
//			for(Message m : msgs) {
//				User author = m.getAuthor();
//
//				// Increment their count, creating it as necessary
//				res.compute(author.getAsTag(), (k,v) -> v==null ? 1 : v+1);
//			}
//
//			// Add this batch to our total count
//			int size = msgs.size();
//			res.compute("TOTAL", (k,v) -> v == null ? size : v + size);
//		}
//
//		return res;
//	}
//
//	private DefaultPieDataset generateDataset(Map<String, Integer> map) {
//		DefaultPieDataset ds = new DefaultPieDataset();
//		int total = map.get("TOTAL");
//		int misc = 0;
//		//ds.setValue("Other", 0);
//
//		for (String user : map.keySet()) {
//			double percentage = (double)(map.get(user)) / total;
//
//			if(percentage > MINIMUM_PERCENTAGE) {
//				ds.setValue(user, map.get(user));
//			} else {
//				misc += map.get(user);
//              miscCount += 1;
//				//ds.setValue("Other", ds.getValue("Other").intValue() + map.get(user));
//			}
//		}
//
//		// Remove meta TOTAL value
//		ds.remove("TOTAL");
//
//		ds.sortByValues(SortOrder.DESCENDING);
//		ds.setValue("Other", misc);
//
//		// Remove Other if unused
//		if(ds.getValue("Other").intValue() == 0) {
//			ds.remove("Other");
//		}
//
//		return ds;
//	}
//
//	private JFreeChart createChartForChannel(TextChannel tc, int amount) {
//		DefaultPieDataset dataset = generateDataset(getMessageCounts(tc, amount));
//		JFreeChart chart = ChartFactory.createPieChart(
//				"Most active users in the last " + amount + " messages.",
//				dataset,
//				true,
//				false,
//				false
//		);
//		CHART_THEME.apply(chart);
//		chart.getLegend().setItemFont(BASE_FONT.deriveFont(30f));
//
//		PiePlot plot = (PiePlot) chart.getPlot();
//		plot.setLabelGenerator(null);
//
//		// Customize colors
//		int i = 0;
//		for(Object key : plot.getDataset().getKeys()) {
////			if(key.equals("Other")) {
////				continue;
////			}
//
//			plot.setSectionPaint((String) key, COLORS[i%COLORS.length]);
//			i++;
//		}
//
//		plot.setSectionPaint("Other", Color.GRAY);
//		return chart;
//	}
//}
