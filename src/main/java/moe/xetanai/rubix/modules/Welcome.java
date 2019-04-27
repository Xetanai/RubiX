package moe.xetanai.rubix.modules;

import moe.xetanai.rubix.utils.ImageUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Welcome extends ListenerAdapter {
	private static final Logger logger = LogManager.getLogger(Welcome.class.getName());
	private static Font FONT;

	// Formatting consts
	private static final String FONT_NAME = "SourceCodePro-Regular.ttf";
	private static final String HEADER_TEXT = "New member!";

	private static final int IMG_W = 400;
	private static final int IMG_H = 100;
	private static final int BACKGROUND_RADIUS = 20;
	private static final Color BACKGROUND_COLOR = new Color(0,147,255);

	private static final int PADDING = 15;

	// Shapes
	private static final RoundRectangle2D BACKGROUND = new RoundRectangle2D.Double(
			0,
			0,
			IMG_W,
			IMG_H,
			BACKGROUND_RADIUS,
			BACKGROUND_RADIUS
	);
	private static final Rectangle2D AVATAR = new Rectangle2D.Double(
			PADDING,
			PADDING,
			IMG_H-(PADDING *2),
			IMG_H-(PADDING *2)
	);
	private static final Rectangle2D HEADER = new Rectangle2D.Double(
			(PADDING *2)+AVATAR.getWidth(),
			0,
			IMG_W-(AVATAR.getWidth()+(PADDING *2))-PADDING,
			IMG_H/3
	);
	private static final Rectangle2D USERTAG = new Rectangle2D.Double(
			HEADER.getX(),
			HEADER.getY()+HEADER.getHeight(),
			HEADER.getWidth()-PADDING,
			IMG_H-PADDING-HEADER.getHeight()
	);

	public Welcome() {
		try {
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream(FONT_NAME);
			if(is == null) throw new IOException("Font was null.");
			FONT = Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (IOException | FontFormatException err) {
			logger.error("Failed to prepare font");
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// TODO: Change to member join event. onMessage is for debug purposes
		// TODO: Check if the guild has enabled welcoming
		// This module ignores bots.
		if(event.getAuthor().isBot()) {return;}

		logger.traceEntry();

		try {
			// Prepare assets
			String avatarURL = event.getAuthor().getEffectiveAvatarUrl();
			String userTag = event.getAuthor().getAsTag();
//			String userTag = event.getMessage().getContentDisplay();

			// Create the image
			BufferedImage img = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			ImageUtils.setDefaultHints(g);
			g.setFont(FONT);
			ImageUtils ge = new ImageUtils(g, "Welcome ("+ userTag +")");

			if(event.getMessage().getContentRaw().contains("debug")) {
				ge.setDebug(true);
			}

			// Background
			ge.fill(BACKGROUND, BACKGROUND_COLOR);
			// Avatar
			ge.drawCircleMaskedImage(AVATAR, ImageIO.read(new URL(avatarURL)));
			// Text
			ge.drawText(HEADER, HEADER_TEXT, Color.WHITE);
			ge.drawText(USERTAG, userTag, Color.WHITE);

			ge.dispose();
			g.dispose();

			event.getChannel().sendFile(ImageUtils.getInputStream(img), "Welcome.png").queue();
			logger.traceExit();
		} catch (IOException err) {
			err.printStackTrace();
		}
	}
}
