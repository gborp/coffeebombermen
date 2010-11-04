package com.braids.coffeebombermen.client.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.HashMap;

import com.braids.coffeebombermen.options.OptConsts.ImageScalingAlgorithms;
import com.braids.coffeebombermen.options.OptionsChangeListener;
import com.braids.coffeebombermen.options.model.ClientOptions;

/**
 * Handles an image.<br>
 * Since the space displaying images and the game scene is changable, different
 * sizes of pictures may be required from moment to moment. This class handles
 * image scaling assuring the minimal calculation if the same dimension of
 * picture is required between 2 requests. Also handles the minimal image loss
 * between the several scaling by storing the original image, and making scaled
 * version from it.
 */
public class ImageHandler {

	/** The original (non-scaled) image. */
	private final Image                                      originalImage;
	/** The lastly requested scaled image. */
	private Image                                            scaledImage;

	private HashMap<Color, Image>                            mapColoredScaledImage;
	private ColorFilter                                      colorFilter;

	/** Scale factor of the lastly produced image. */
	private float                                            scaleFactor;
	/** Used image scaling algorithm for the lastly produced image. */
	private ImageScalingAlgorithms                           usedimageScalingAlgorithm;

	/** The image scaling algoritm to use when scaling the image. */
	private static ImageScalingAlgorithms                    imageScalingAlgorithm;

	/**
	 * We create an options change listener to handle options change events, we
	 * need the image scaling algorithm setting.
	 */
	public static final OptionsChangeListener<ClientOptions> clientOptionsChangeListener = new OptionsChangeListener<ClientOptions>() {

		                                                                                     public void optionsChanged(final ClientOptions oldOptions,
		                                                                                             final ClientOptions newOptions) {
			                                                                                     if (newOptions.imageScalingAlgorithm != oldOptions.imageScalingAlgorithm) {
				                                                                                     imageScalingAlgorithm = newOptions.imageScalingAlgorithm;
			                                                                                     }
		                                                                                     }

	                                                                                     };

	/**
	 * Sets the image scaling algorithm. (used for initializing
	 * imageScalingAlgorithm)
	 * 
	 * @param imageScalingAlgorithm
	 *            image scaling algorithm to be set
	 */
	public static void setImageScalingAlgorithm(final ImageScalingAlgorithms imageScalingAlgorithm) {
		ImageHandler.imageScalingAlgorithm = imageScalingAlgorithm;
	}

	/**
	 * Creates a new ImageHandler.
	 * 
	 * @param image
	 *            the image to be handled
	 */
	public ImageHandler(final Image image) {
		originalImage = image;
		setScaledImage(originalImage, 1.0f, null);
	}

	/**
	 * Returns a scaled image of the original one. If the scale factor is equal
	 * to the one of the last query, the image will not be scaled again, the
	 * same image will be returned. Else a new scaled instance will be
	 * calculated and returned.
	 * 
	 * @param scaleFactor
	 *            scale factor or the required image
	 * @return a reference to a scaled instance of this image specified by the
	 *         scaleFactor attribute
	 */
	public Image getScaledImage(final float scaleFactor) {
		return getScaledImage(scaleFactor, true);
	}

	public Image getScaledImage(final float scaleFactor, boolean transparent) {
		if ((this.scaleFactor == scaleFactor) && (usedimageScalingAlgorithm == imageScalingAlgorithm)) {
			return scaledImage;
		}
		// Invoking Math.max() because with or height cannot be zero (and that
		// amount is calculated when the divider of JSplitPane is in outside)
		Image newScaledImage = originalImage.getScaledInstance(Math.max((int) (originalImage.getWidth(null) * scaleFactor), 1), Math.max((int) (originalImage
		        .getHeight(null) * scaleFactor), 1), imageScalingAlgorithm == ImageScalingAlgorithms.FAST ? Image.SCALE_FAST : Image.SCALE_SMOOTH);

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		BufferedImage newCompatibleScaledImage = gc.createCompatibleImage(newScaledImage.getWidth(null), newScaledImage.getHeight(null),
		        transparent ? Transparency.BITMASK : Transparency.OPAQUE);
		Graphics2D g = newCompatibleScaledImage.createGraphics();
		g.drawImage(newScaledImage, 0, 0, null);
		g.dispose();

		setScaledImage(newCompatibleScaledImage, scaleFactor, imageScalingAlgorithm);

		return scaledImage;
	}

	public Image getScaledImage(final float scaleFactor, boolean transparent, Color fromChange, Color toChange) {
		if (this.scaleFactor != scaleFactor) {
			mapColoredScaledImage = null;
		}

		if (mapColoredScaledImage == null) {
			mapColoredScaledImage = new HashMap<Color, Image>();
		}
		Image result = mapColoredScaledImage.get(toChange);
		if (result != null) {
			return result;
		}

		if (colorFilter == null) {
			colorFilter = new ColorFilter();
		}

		colorFilter.fromColor = fromChange.getRGB();
		colorFilter.toColor = toChange.getRGB();

		ImageProducer ip = new FilteredImageSource(originalImage.getSource(), colorFilter);
		Image coloredImage = Toolkit.getDefaultToolkit().createImage(ip);
		// Invoking Math.max() because with or height cannot be zero (and that
		// amount is calculated when the divider of JSplitPane is in outside)
		Image newScaledImage = coloredImage.getScaledInstance(Math.max((int) (originalImage.getWidth(null) * scaleFactor), 1), Math.max((int) (originalImage
		        .getHeight(null) * scaleFactor), 1), imageScalingAlgorithm == ImageScalingAlgorithms.FAST ? Image.SCALE_FAST : Image.SCALE_SMOOTH);

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		BufferedImage newCompatibleScaledImage = gc.createCompatibleImage(newScaledImage.getWidth(null), newScaledImage.getHeight(null),
		        transparent ? Transparency.BITMASK : Transparency.OPAQUE);
		Graphics2D g = newCompatibleScaledImage.createGraphics();
		g.drawImage(newScaledImage, 0, 0, null);
		g.dispose();

		mapColoredScaledImage.put(toChange, newCompatibleScaledImage);
		this.scaleFactor = scaleFactor;

		return newCompatibleScaledImage;
	}

	private static class ColorFilter extends RGBImageFilter {

		public int fromColor;
		public int toColor;

		public int filterRGB(int x, int y, int rgb) {
			if (rgb == fromColor) {
				return toColor;
			}
			return rgb;
		}

	}

	/**
	 * Sets the scaled image and its properties..
	 * 
	 * @param scaledImage
	 *            the new scaled image
	 * @param scaleFactor
	 *            the new scale factor
	 * @param usedImageScalingAlgorithms
	 *            image scaling algorithm used to produce the scaled version
	 */
	private void setScaledImage(final Image scaledImage, final float scaleFactor, final ImageScalingAlgorithms usedImageScalingAlgorithms) {
		this.scaledImage = scaledImage;
		this.scaleFactor = scaleFactor;
		this.usedimageScalingAlgorithm = imageScalingAlgorithm;
	}

	/**
	 * Returns the width of the original image.
	 * 
	 * @return the width of the original image
	 */
	public int getOriginalWidth() {
		return originalImage.getWidth(null);
	}

	/**
	 * Returns the height of the original image.
	 * 
	 * @return the height of the original image
	 */
	public int getOriginalHeight() {
		return originalImage.getHeight(null);
	}

}
