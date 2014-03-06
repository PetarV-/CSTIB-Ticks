package uk.ac.cam.pv273.fjava.tick5star;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer 
{
	
	public static synchronized void sendFederated(ArrayList<FederatedHandler> fedLst, Message msg)
	{
		for (FederatedHandler fedHandler : fedLst)
		{
			fedHandler.SendMessage(msg);
		}
	}
	
	public static void main(String[] args) 
	{
		int port;
		int fedPort;
		ServerSocket ss;
		final ServerSocket ssFederated;
		String db_path;
		final Database database;
		final ArrayList<FederatedHandler> fedServers = new ArrayList<FederatedHandler>();
		
		try
		{
			db_path = args[0];
			port = Integer.parseInt(args[1]);
			fedPort = Integer.parseInt(args[2]);

			database = new Database(db_path);
		}
		catch (ArrayIndexOutOfBoundsException | SQLException | NumberFormatException ex)
		{
			System.out.println("Usage: java ChatServer <dbpath> <client> <fed> [fedsrv1:port fedsrv2:port ...]");
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
		
		try
		{
			ssFederated = new ServerSocket(fedPort);
		}
		catch (Exception e) 
		{
			System.out.println("Cannot use port number " + fedPort);
			return;
		}
		
		final MultiQueue<Message> mQueue = new MultiQueue<Message>();	
	
		for (int i=3;i<args.length;i++)
		{
			try
			{
				String[] tokens = args[i].split(":", 2);
				String fedHostName = tokens[0];
				int currFedPort = Integer.parseInt(tokens[1]);
				Socket s = new Socket(fedHostName, currFedPort);
				fedServers.add(new FederatedHandler(s, mQueue, database));
			}
			catch (IOException ex)
			{
				System.out.println("Warning: Cannot connect to '" + args[i] + "'. Ignoring.");
			}
			catch (ArrayIndexOutOfBoundsException | NumberFormatException ex)
			{
				System.out.println("Warning: cannot interpret '" + args[i] + "' as 'fedsrv:port'. Ignoring.");
			}
		}
		
		Thread tFed = new Thread()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try 
					{
						Socket sFed = ssFederated.accept();
						fedServers.add(new FederatedHandler(sFed, mQueue, database));
					} 
					catch (IOException e) 
					{				
						e.printStackTrace();
					}
				}
			}
		};
		
		tFed.setDaemon(true);
		tFed.start();
		
		while (true)
		{
			try 
			{
				Socket s = ss.accept();
				new ClientHandler(s, mQueue, database, fedServers);
			} 
			catch (IOException e) 
			{				
				e.printStackTrace();
			}
		}
	}
}
