package moe.xetanai.rubix.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;

// Importing this statically to save prefixing every enum in the package
import static java.awt.RenderingHints.*;

/**
 * Represents an extended Graphics2D instance
 */
public class ImageUtils {
	private static Color DEBUG_COLOR = new Color(255,0,255);
	private static final Logger logger = LogManager.getLogger(ImageUtils.class.getName());
	private static final String DEFAULT_FONT_NAME = "SourceCodePro-Regular.ttf";
	private static final Font FONT = ImageUtils.initFont();

	private Graphics2D g;
	private String name;
	private boolean debug;

	/**
	 * Extends an G2d object
	 * @param g Graphics2D instance
	 */
	public ImageUtils(@Nonnull Graphics2D g) {
		this(g, "NAME NOT SET. HAVE FUN DEBUGGING, ASSHOLE.");
	}

	/**
	 * Extends and names a G2d object
	 * @param g Graphics2D instance
	 * @param name Name for logging
	 */
	public ImageUtils(@Nonnull Graphics2D g, @Nonnull String name) {
		this.g = g;
		this.name = name;
	}

	// GETTERS

	/**
	 * @return true if debug is enabled. False otherwise
	 */
	public boolean isDebug() {
		return this.debug;
	}

	/**
	 * @return the font initialized at startup
	 */
	public static Font getFont() {
		return FONT;
	}

	// SETTERS AND BASE METHODS

	/**
	 * Toggles debug drawing mode
	 * @return chainable this
	 */
	@Nonnull
	public ImageUtils toggleDebug() {
		this.debug = !this.debug;
		return this;
	}

	/**
	 * Sets debug drawing mode
	 * @param nv new value
	 * @return chainable this
	 */
	@Nonnull
	public ImageUtils setDebug(boolean nv) {
		this.debug = nv;
		return this;
	}

	/**
	 * Disposes of the underlying g2d object
	 */
	public void dispose() {
		g.dispose();
		logger.debug("ImageUtil {} disposed of", this.name);
	}

	// PUBLIC DRAWING METHODS

	/**
	 * Draws the outline of a given shape.
	 * @param shape Shape to draw the outline of
	 * @param color Color to draw it in. Null keeps current g2d color
	 * @param stroke Stroke width to draw with. Null keeps current g2d stroke
	 * @return chainable this
	 */
	@Nonnull
	public ImageUtils outline(@Nonnull Shape shape, @Nullable Color color,@Nullable Stroke stroke) {
		this.g.setColor(color);
		this.g.setStroke(stroke);

		this.g.draw(shape);
		return debugDrawBounds(shape.getBounds2D());
	}

	/**
	 * Draws a filled shape
	 * @param shape Shape to draw
	 * @param color Color to draw it in. Null keeps current g2d color
	 * @return chainable this
	 */
	@Nonnull
	public ImageUtils fill(@Nonnull Shape shape,@Nullable Color color) {
		this.g.setColor(color);

		this.g.fill(shape);
		return debugDrawBounds(shape.getBounds2D());
	}

	// Advanced drawing methods

	/**
	 * Draws an image using a shape as a mask
	 * @param bounds bounds to draw it in
	 * @param cropImage Image to mask and draw
	 * @param cropShape Shape to use as the mask
	 * @return chainable this
	 */
	@Nonnull
	public ImageUtils drawShapeMaskedImage(@Nonnull Rectangle2D bounds,@Nonnull BufferedImage cropImage,@Nonnull Shape cropShape) {
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

	/**
	 * Draws an image cropped into a circular shape
	 * @param bounds bounds to draw it in
	 * @param cropImage Image to mask and draw
	 * @return chainable this
	 */
	@Nonnull
	public ImageUtils drawCircleMaskedImage(@Nonnull Rectangle2D bounds,@Nonnull BufferedImage cropImage) {
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

	/**
	 * Draws text onto the image
	 * @param bounds bounds to draw it in
	 * @param text text to draw
	 * @param color color to draw it in
	 * @return chainable this
	 */
	@Nonnull
	@Deprecated
	public ImageUtils drawText(@Nonnull Rectangle2D bounds,@Nonnull String text,@Nullable Color color) {
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

	/**
	 * Masks an image to a given shape
	 * @param cropImage image to crop
	 * @param cropShape shape to mask
	 * @return result of the mask
	 */
	@Nonnull
	private BufferedImage shapeMaskImage(@Nonnull BufferedImage cropImage,@Nonnull Shape cropShape) {
		BufferedImage masked = new BufferedImage(cropImage.getWidth(), cropImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D mg = setDefaultHints(masked.createGraphics());

		mg.setClip(cropShape);
		mg.drawImage(cropImage, 0, 0, null);
		mg.dispose();

		return masked;
	}

	/**
	 * Masks an image to a circle
	 * @param cropImage image to crop
	 * @return the result of the mask
	 */
	@Nonnull
	private BufferedImage circleMaskImage(@Nonnull BufferedImage cropImage) {
		int w = cropImage.getWidth();
		int h = cropImage.getHeight();

		RoundRectangle2D circle = new RoundRectangle2D.Double(0,0,w,h,w*2,h*2);
		return shapeMaskImage(cropImage, circle);
	}

	/**
	 * Scales the current g2d font to fit given bounds as much as possible
	 * @param text text to fit
	 * @param bounds bounds to fit in
	 * @return chainable this
	 */
	@Nonnull
	private ImageUtils scaleFontToBounds(@Nonnull String text,@Nonnull Rectangle2D bounds) {
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

	/**
	 * Draws bounds colored in debug color onto the image
	 * @param bounds bounds to outline
	 * @return chainable this
	 */
	@Nonnull
	private ImageUtils debugDrawBounds(@Nonnull Rectangle2D bounds) {
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

	private static Font initFont() {
		try {
			InputStream is = ImageUtils.class.getClassLoader()
					.getResourceAsStream(DEFAULT_FONT_NAME);
			if(is == null) throw new IOException("Font was null.");
			return Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (IOException | FontFormatException err) {
			logger.error("Failed to prepare font", err);
		}
		return null;
	}

	/**
	 * Sets default rendering hints on the image for optimal quality
	 * @param g Graphics2d object
	 * @return chainable parameter
	 */
	@Nonnull
	public static Graphics2D setDefaultHints(@Nonnull Graphics2D g) {
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

	/**
	 * Get buffered image as an input stream for sending over Discord without saving
	 * @param img image to convert
	 * @return image as an input stream
	 * @throws IOException if ImageIO fucks up. Unlikely.
	 */
	@Nonnull
	public static InputStream getInputStream(@Nonnull BufferedImage img) throws IOException {
		ByteArrayOutputStream imgos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", imgos);
		return new ByteArrayInputStream(imgos.toByteArray());
	}
}
