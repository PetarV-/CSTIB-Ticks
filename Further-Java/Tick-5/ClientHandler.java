package uk.ac.cam.pv273.fjava.tick5;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ClientHandler 
{
	private Socket socket;
	private MultiQueue<Message> multiQueue;
	private String nickname;
	private MessageQueue<Message> clientMessages;
	private Random r = new Random();
	private InetAddress address;
	private ObjectInputStream iStream;
	private ObjectOutputStream oStream;
	private boolean dead = false;
	private Database database;
	
	public ClientHandler(Socket s, MultiQueue<Message> q, Database db) 
	{
		socket = s;
		multiQueue = q;
		clientMessages = new SafeMessageQueue<Message>();
		multiQueue.register(clientMessages);
		nickname = "Anonymous" + (10000 + r.nextInt(89999));
		address = s.getInetAddress();
		multiQueue.put(new StatusMessage(nickname + " connected from " + address.getHostName() + "."));
		database = db;
		
		try 
		{
			database.incrementLogins();
		} 
		catch (SQLException e2) 
		{
			e2.printStackTrace();
		}
		
		try
		{
			iStream = new ObjectInputStream(socket.getInputStream());
			oStream = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try 
		{
			ArrayList<RelayMessage> recentMsgs = (ArrayList<RelayMessage>)db.getRecent();
			for (RelayMessage rmsg : recentMsgs)
			{
				oStream.writeObject(rmsg);
			}
		} 
		catch (SQLException | IOException e1)
		{
			e1.printStackTrace();
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
		
		Thread tOut = new Thread()
		{
			@Override
			public void run()
			{
				while (true)
				{
					if (dead) break;
					SendMessage();
				}
			}
		};
		
		tIn.start();
		tOut.setDaemon(true);
		tOut.start();
	}
	
	private void ReceiveMessage()
	{
		try 
		{
			Message msg = (Message)iStream.readObject();
			if (msg instanceof ChangeNickMessage)
			{
				String oldNickname = nickname;
				nickname = ((ChangeNickMessage)msg).name;
				multiQueue.put(new StatusMessage(oldNickname + " is now known as " + nickname + "."));
			}
			else if (msg instanceof ChatMessage)
			{
				database.addMessage(new RelayMessage(nickname, (ChatMessage)msg));
				multiQueue.put(new RelayMessage(nickname, (ChatMessage)msg));
			}
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			multiQueue.deregister(clientMessages);
			multiQueue.put(new StatusMessage(nickname + " has disconnected."));
			dead = true;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	private void SendMessage()
	{
		Message msg = clientMessages.take();
		try 
		{
			oStream.writeObject(msg);
		} 
		catch (IOException e) 
		{
			multiQueue.deregister(clientMessages);
			multiQueue.put(new StatusMessage(nickname + " has disconnected."));
			dead = true;
		}
	}
}