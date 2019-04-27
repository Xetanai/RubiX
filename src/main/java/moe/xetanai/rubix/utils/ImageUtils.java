package moe.xetanai.rubix.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;

// Importing this statically to save prefixing every enum in the package
import static java.awt.RenderingHints.*;

public class ImageUtils {
	private static Color DEBUG_COLOR = new Color(255,0,255);
	private static final Logger logger = LogManager.getLogger(ImageUtils.class.getName());

	private Graphics2D g;
	private String name;
	private boolean debug;

	public ImageUtils(Graphics2D g) {
		this(g, "NAME NOT SET. HAVE FUN DEBUGGING, ASSHOLE.");
	}

	public ImageUtils(Graphics2D g, String name) {
		this.g = g;
		this.name = name;
	}

	// GETTERS

	public boolean isDebug() {
		return this.debug;
	}

	// SETTERS AND BASE METHODS

	public ImageUtils toggleDebug() {
		this.debug = !this.debug;
		return this;
	}

	public ImageUtils setDebug(boolean nv) {
		this.debug = nv;
		return this;
	}

	public void dispose() {
		g.dispose();
		logger.debug("ImageUtil {} disposed of", this.name);
	}

	// PUBLIC DRAWING METHODS


	// Quick shape drawing
	public ImageUtils draw(Shape shape, Color color, Stroke stroke) {
		this.g.setColor(color);
		this.g.setStroke(stroke);

		this.g.draw(shape);
		return debugDrawBounds(shape.getBounds2D());
	}

	public ImageUtils fill(Shape shape, Color color) {
		this.g.setColor(color);

		this.g.fill(shape);
		return debugDrawBounds(shape.getBounds2D());
	}

	// Advanced drawing methods
	public ImageUtils drawShapeMaskedImage(Rectangle2D bounds, BufferedImage cropImage, Shape cropShape) {
		BufferedImage img = shapeMaskImage(cropImage, cropShape);
		g.drawImage(
				img,
				(int) bounds.getX(),
				(int) bounds.getY(),
				(int) bounds.getWidth(),
				(int) bounds.getHeight(),
				null
		);
		return debugDrawBounds(bounds);
	}

	public ImageUtils drawCircleMaskedImage(Rectangle2D bounds, BufferedImage cropImage) {
		BufferedImage img = circleMaskImage(cropImage);
		g.drawImage(
				img,
				(int) bounds.getX(),
				(int) bounds.getY(),
				(int) bounds.getWidth(),
				(int) bounds.getHeight(),
				null
		);

		return debugDrawBounds(bounds);
	}

	public ImageUtils drawText(Rectangle2D bounds, String text, Color color) {
		scaleFontToBounds(text, bounds);
		int offset = this.g.getFontMetrics().getMaxDescent();

		this.g.setColor(color);
		this.g.drawString(
				text,
				(int) bounds.getX(),
				(int) (bounds.getY()+bounds.getHeight())-offset
		);
		return debugDrawBounds(bounds);
	}

	// PRIVATE HELPER METHODS

	private BufferedImage shapeMaskImage(BufferedImage cropImage, Shape cropShape) {
		BufferedImage masked = new BufferedImage(cropImage.getWidth(), cropImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D mg = setDefaultHints(masked.createGraphics());

		mg.setClip(cropShape);
		mg.drawImage(cropImage, 0, 0, null);
		mg.dispose();

		return masked;
	}

	private BufferedImage circleMaskImage(BufferedImage cropImage) {
		int w = cropImage.getWidth();
		int h = cropImage.getHeight();

		RoundRectangle2D circle = new RoundRectangle2D.Double(0,0,w,h,w*2,h*2);
		return shapeMaskImage(cropImage, circle);
	}

	private ImageUtils scaleFontToBounds(String text, Rectangle2D bounds) {
		// Prevent rounding errors. Ensure this hint is set.
		g.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);

		// Get the current font bounds
		Font cf = this.g.getFont();
		Rectangle2D cbounds = g.getFontMetrics(cf).getStringBounds(text, this.g);

		// Scale the font to fit horizontally
		Font nf = cf.deriveFont(
				(float) (cf.getSize2D() * bounds.getWidth()/cbounds.getWidth())
		);

		// Scale the font to fit vertically, if necessary
		cbounds = g.getFontMetrics(nf).getStringBounds(text, this.g);
		if(cbounds.getHeight() > bounds.getHeight()) {
			nf = nf.deriveFont(
					(float) (nf.getSize2D() * bounds.getHeight() / cbounds.getHeight())
			);
		}

		g.setFont(nf);
		return this;
	}

	private ImageUtils debugDrawBounds(Rectangle2D bounds) {
		if(!this.debug) {return this;}
		Color old = g.getColor();

		this.g.setColor(DEBUG_COLOR);
		this.g.draw(bounds);
		this.g.setColor(old);

		logger.trace("{} | Bounds: X={}, Y={}, W={}, H={}",
				this.name,
				bounds.getX(),
				bounds.getY(),
				bounds.getWidth(),
				bounds.getHeight()
		);
		return this;
	}

	// STATIC METHODS

	public static Graphics2D setDefaultHints(Graphics2D g) {
		RenderingHints hints = new RenderingHints(
				KEY_ANTIALIASING, VALUE_ANTIALIAS_ON
		);

		hints.put(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
		hints.put(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
		hints.put(KEY_RENDERING, VALUE_RENDER_QUALITY);
		hints.put(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
		hints.put(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
		hints.put(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);

		g.setRenderingHints(hints);
		return g;
	}

	public static InputStream getInputStream(BufferedImage img) throws IOException {
		ByteArrayOutputStream imgos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", imgos);
		return new ByteArrayInputStream(imgos.toByteArray());
	}
}
