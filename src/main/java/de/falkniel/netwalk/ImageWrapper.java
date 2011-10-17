package de.falkniel.netwalk;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;


public class ImageWrapper {
	
	private static double IMG_H_CENTER = ((double)Netwalk.IMG_H)/2;
	private static double IMG_W_CENTER = ((double)Netwalk.IMG_W)/2;
	/** The image to be drawn for this sprite */
	private Image image;
	
	/** 
	 * Create a new sprite based on an image
	 * 
	 * @param image The image that is this sprite
	 */
	public ImageWrapper(Image image) {
		this.image = image;
	}
	
	/**
	 * Get the width of the drawn sprite
	 * 
	 * @return The width in pixels of this sprite
	 */
	public int getWidth() {
		return image.getWidth(null);
	}

	/**
	 * Get the height of the drawn sprite
	 * 
	 * @return The height in pixels of this sprite
	 */
	public int getHeight() {
		return image.getHeight(null);
	}
	
	/**
	 * Draw the sprite onto the graphics context provided
	 * 
	 * @param g The graphics context on which to draw the sprite
	 * @param x The x location at which to draw the sprite
	 * @param y The y location at which to draw the sprite
	 */
	public void draw(Graphics2D g,int x,int y, boolean isLocked){
		draw(g, x, y, isLocked,null);
	}
	public void draw(Graphics2D g,int x,int y, boolean isLocked,Double rotation) {
		if (isLocked){
			g.setColor(Color.YELLOW);
			g.fillRect(x,y,Netwalk.IMG_W,Netwalk.IMG_H);
			
		}
		if (rotation != null){
			double rotateRadians = Math.toRadians(rotation);
			double rotateX = IMG_W_CENTER +x;
			double rotateY = IMG_H_CENTER +y;
			g.rotate((double)rotateRadians,rotateX,rotateY);
			g.drawImage(image,x,y,null);
			g.rotate((double)-rotateRadians,rotateX,rotateY);
		}else{
			g.drawImage(image,x,y,null);
		}
	}
}
