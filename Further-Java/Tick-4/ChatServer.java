package uk.ac.cam.pv273.fjava.tick4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer 
{
	static int port;
	static ServerSocket ss;
	
	public static void main(String[] args) 
	{
		try
		{
			port = Integer.parseInt(args[0]);
		}
		catch (ArrayIndexOutOfBoundsException | NumberFormatException ex)
		{
			System.out.println("Usage: java ChatServer <port>");
			return;
		}
		
		try 
		{
			ss = new ServerSocket(port);
		} 
		catch (Exception e) 
		{
			System.out.println("Cannot use port number " + port);
			return;
		}
		
		MultiQueue<Message> mQueue = new MultiQueue<Message>();
		
		while (true)
		{
			try 
			{
				Socket s = ss.accept();
				new ClientHandler(s, mQueue);
			} 
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
		}
	}
}
