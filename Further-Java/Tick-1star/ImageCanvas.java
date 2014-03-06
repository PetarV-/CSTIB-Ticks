package uk.ac.cam.pv273.fjava.tick1star;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageCanvas extends Canvas 
{
	private BufferedImage buffImg = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
	
	public ImageCanvas()
	{
		this.setBackground(Color.BLACK);
	}
	
	@Override
	public void paint(Graphics g)
	{
		g.drawImage(buffImg, 0, 0, buffImg.getWidth(), buffImg.getHeight(), null);
		g.dispose();
	}
	
	public void setImage(BufferedImage buffImg)
	{
		this.buffImg = buffImg;
		this.repaint();
	}
}
