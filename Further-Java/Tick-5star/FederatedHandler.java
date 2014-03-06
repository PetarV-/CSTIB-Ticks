package uk.ac.cam.pv273.fjava.tick5star;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.FederatedRelayMessage;
import uk.ac.cam.cl.fjava.messages.FederatedStatusMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class FederatedHandler
{
	private Socket socket;
	private MultiQueue<Message> multiQueue;
	private ObjectInputStream iStream;
	private ObjectOutputStream oStream;
	private boolean dead = false;
	private Database database;
	
	public FederatedHandler(Socket s, MultiQueue<Message> q, Database db)
	{
		socket = s;
		multiQueue = q;
		database = db;
		
		try
		{
			oStream = new ObjectOutputStream(socket.getOutputStream());
			iStream = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Thread tIn = new Thread()
		{
			@Override
			public void run()
			{
				while (true)
				{
					if (dead) break;
					ReceiveMessage();
				}
			}
		};

		tIn.start();
	}
	
	private void ReceiveMessage()
	{
		try 
		{
			Message msg = (Message)iStream.readObject();
			if (msg instanceof FederatedRelayMessage)
			{
				String nickname = ((FederatedRelayMessage) msg).getFrom();
				String message = ((FederatedRelayMessage) msg).getMessage();
				Date date = msg.getCreationTime();
				RelayMessage relay = new RelayMessage(nickname, message, date);
				database.addMessage(relay);
				multiQueue.put(relay);
			}
			else if (msg instanceof FederatedStatusMessage)
			{
				String message = ((FederatedStatusMessage) msg).getMessage();
				StatusMessage status = new StatusMessage(message);
				multiQueue.put(status);
			}
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			dead = true;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void SendMessage(Message msg)
	{
		if (!dead)
		{
			try
			{
				if (msg instanceof RelayMessage)
				{
					RelayMessage rl = (RelayMessage)msg;
					FederatedRelayMessage fedRelayMsg = new FederatedRelayMessage(rl.getFrom(), rl.getMessage(), rl.getCreationTime());
					oStream.writeObject(fedRelayMsg);
				}
				else if (msg instanceof StatusMessage)
				{
					StatusMessage sm = (StatusMessage)msg;
					FederatedStatusMessage fedStatMsg = new FederatedStatusMessage(sm.getMessage());
					oStream.writeObject(fedStatMsg);
				}
			}
			catch (IOException ex)
			{
				dead = true;
			}
		}
	}
}
