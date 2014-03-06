package uk.ac.cam.pv273.fjava.tick1star;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageChatClient extends JFrame 
{
	private ImageCanvas canvas;
	private Button upButton;
	private Socket s;
	private final JFileChooser fc = new JFileChooser();
	
	public ImageChatClient(String server, int port)
	{
		super("ImageChatClient - listening on " + server + ":" + port);
		try 
		{ 
			s = new Socket(server, port);
		}
		catch (Exception e)
		{
			System.err.println("Cannot connect to " + server + " on port " + port);
			return;
		}
		canvas = new ImageCanvas();
		upButton = new Button("Upload");
		upButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				if (arg0.getSource() == upButton)
				{
					int ret = fc.showOpenDialog(ImageChatClient.this);
					if (ret == JFileChooser.APPROVE_OPTION)
					{
						byte buff[] = new byte[1024];
						File file = fc.getSelectedFile();
						OutputStream outStream = null;
						try
						{
							outStream = s.getOutputStream();
						}
						catch (Exception e) { return; }
						
						try
						{
							BufferedImage buffImg = ImageIO.read(file);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(buffImg, "jpg", baos);
							baos.flush();
							outStream.write(baos.toByteArray());
						}
						catch (Exception e) { return; }
					}
				}
			}	
		});
		//contentPane.add(canvas, BorderLayout.CENTER);
		//buttonPane.add(upButton, BorderLayout.SOUTH);
		this.getContentPane().add(canvas, BorderLayout.CENTER);
		this.getContentPane().add(upButton, BorderLayout.SOUTH);
		this.setSize(500, 500);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		Thread output = new Thread() 
		{
			@Override
			public void run() 
			{
				byte buff[] = new byte[1024];
				InputStream sStream;
				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
				try 
				{
					sStream = s.getInputStream();
				} 
				catch (IOException e) 
				{
					return;
				}
				byte previousByte = 99;
				while (true)
				{
					try 
					{
						int bytesRead = sStream.read(buff);
						for (int i=0;i<bytesRead;i++)
						{
							bStream.write(buff[i]);
							if (previousByte == -1 && buff[i] == -39)
							{
								BufferedImage buffImg = ImageIO.read(new ByteArrayInputStream(bStream.toByteArray()));
								canvas.setImage(buffImg);
								ImageChatClient.this.setSize(buffImg.getWidth(), buffImg.getHeight());
								bStream.reset();
							}
							previousByte = buff[i];
						}
					} 
					catch (IOException e)
					{
						return;
					}
				}
			}
		};
		output.setDaemon(true);
		output.start();
	}
	
	public Socket getSocket() { return s; }

	public static void main(String[] args) 
	{
		if (args.length < 2)
		{
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		try
		{
			String serv = args[0];
			int por = Integer.parseInt(args[1]);
			ImageChatClient client = new ImageChatClient(serv, por);
	 	}
		catch (NumberFormatException e)
		{
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		
	}

}
