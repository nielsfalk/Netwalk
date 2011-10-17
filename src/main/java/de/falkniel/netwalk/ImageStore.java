package de.falkniel.netwalk;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;



public class ImageStore { 

	private static ImageStore single = new ImageStore();

	private HashMap<String, ImageWrapper> images = new HashMap<String, ImageWrapper>();

	public static ImageStore get() {
		return single;
	}

	public ImageWrapper getImage(String ref) {
		ImageWrapper retVal = images.get(ref);
		if (retVal != null) {
			return retVal;
		}
		
		String refFielname = "img/"+ref+ ".png";

		// otherwise, go away and grab the sprite from the resource
		// loader
		BufferedImage sourceImage = null;

		try {
			// The ClassLoader.getResource() ensures we get the sprite
			// from the appropriate place, this helps with deploying the game
			// with things like webstart. You could equally do a file look
			// up here.
			URL url = this.getClass().getClassLoader().getResource(refFielname);

			if (url == null) {
				fail("Can't find ref: " + refFielname);
			}

			// use ImageIO to read the image in
			sourceImage = ImageIO.read(url);
		}
		catch (IOException e) {
			fail("Failed to load: " + refFielname);
		}

		// create an accelerated image of the right size to store our sprite in
		GraphicsConfiguration gc =
		    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Image image = gc.createCompatibleImage(sourceImage.getWidth(), sourceImage.getHeight(), Transparency.BITMASK);

		// draw our source image into the accelerated image
		image.getGraphics().drawImage(sourceImage, 0, 0, null);

		// create a sprite, add it the cache then return it
		ImageWrapper imageWrapper = new ImageWrapper(image);
		images.put(ref, imageWrapper);

		return imageWrapper;
	}

	/**
	 * Utility method to handle resource loading failure
	 * 
	 * @param message The message to display on failure
	 */
	private void fail(String message) {
		// we're pretty dramatic here, if a resource isn't available
		// we dump the message and exit the game
		System.err.println(message);
		System.exit(0);
	}

}
