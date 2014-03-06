package uk.ac.cam.pv273.fjava.tick1;

import java.io.InputStream;
import java.net.Socket;

public class StringReceive 
{
	
	public static void main(String[] args) 
	{
		String serverName;
		int port;
		
		byte buff[] = new byte[1024];
		
		if (args.length < 2)
		{
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		try
		{
			serverName = args[0];
			port = Integer.parseInt(args[1]);
 		}
		catch (NumberFormatException e)
		{
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		try 
		{
			Socket s = new Socket(serverName, port);
			InputStream sStream = s.getInputStream();
			while (true)
			{
				int bytesRead = sStream.read(buff);
				if (bytesRead != -1)
				{
					String currResponse = new String(buff, 0, bytesRead);
					System.out.println(currResponse);
				}
			}
		} 
		catch (Exception e) 
		{
			System.err.println("Cannot connect to " + serverName + " on port " + port);
			return;
		} 
	}
}
