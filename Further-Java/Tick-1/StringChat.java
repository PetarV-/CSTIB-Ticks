package uk.ac.cam.pv273.fjava.tick1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class StringChat 
{

	public static void main(String[] args)
	{

		final String server;
		final int port;
			
		if (args.length < 2)
		{
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		  
		try
		{
			server = args[0];
			port = Integer.parseInt(args[1]);
	 	}
		catch (NumberFormatException e)
		{
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}

		/*
			The Socket is declared final because it is used within the anonymous class
			used to instantiate a new thread. If the main method were to exit before this
			thread, non-final variables could get cleaned up while still being used
			by the thread.
		*/
		final Socket s;
		try 
		{ 
			s = new Socket(server, port);
		}
		catch (Exception e)
		{
			System.err.println("Cannot connect to " + server + " on port " + port);
			return;
		}
		Thread output = new Thread() 
		{
			@Override
			public void run() 
			{
				InputStream sStream;
				try 
				{
					sStream = s.getInputStream();
				} 
				catch (IOException e) 
				{
					System.err.println("Cannot connect to " + server + " on port " + port);
					return;
				}
				byte[] buff = new byte[1024];
				while (true)
				{
					int bytesRead = 0;
					try 
					{
						bytesRead = sStream.read(buff);
					} 
					catch (IOException e)
					{
						System.err.println("Cannot connect to " + server + " on port " + port);
						return;
					}
					if (bytesRead > 0)
					{
						String currResponse = new String(buff, 0, bytesRead);
						System.out.println(currResponse);
					}
				}
			}
		};
		output.setDaemon(true); // JVM running until only daemon threads remain
	    output.start();
	    
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		OutputStream oStream;
		try 
		{
			oStream = s.getOutputStream();
		} 
		catch (IOException e1) 
		{
			System.err.println("Cannot connect to " + server + " on port " + port);
			return;
		}
		while(true) 
		{
			 try 
			 {
				 byte buff[] = r.readLine().getBytes();
				 oStream.write(buff);
				 oStream.flush();
			 } 
			 catch (IOException e) 
			 {
				 System.err.println("Cannot connect to " + server + " on port " + port);
				 return;
			 }
		}
	}
}