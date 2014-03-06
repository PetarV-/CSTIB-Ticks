package uk.ac.cam.pv273.fjava.tick5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer 
{
	static int port;
	static ServerSocket ss;
	static String db_path;
	static Database database;
	
	public static void main(String[] args) 
	{
		try
		{
			port = Integer.parseInt(args[0]);
			db_path = args[1];
			database = new Database(db_path);
		}
		catch (ArrayIndexOutOfBoundsException | SQLException | NumberFormatException ex)
		{
			System.out.println("Usage: java ChatServer <port> <database name>");
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
				new ClientHandler(s, mQueue, database);
			} 
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
		}
	}
}
