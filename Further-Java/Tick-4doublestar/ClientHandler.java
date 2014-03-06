package uk.ac.cam.pv273.fjava.tick4star;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
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
	
	private void sendGUI()
	{	
		try
		{
			byte[] buff = new byte[1024];			
			
			Class<SerializableActionListener> SALClass = SerializableActionListener.class;
			String name = SALClass.getName();
			InputStream is = SALClass.getClassLoader().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (true)
			{
				int bytesRead = is.read(buff);
				if (bytesRead <= 0) break;
				baos.write(buff, 0, bytesRead);
			}
			NewMessageType nmsg = new NewMessageType(name, baos.toByteArray());
			oStream.writeObject(nmsg);
			
			Class<SerializableThread> STClass = SerializableThread.class;
			name = STClass.getName();
			is = STClass.getClassLoader().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
			baos = new ByteArrayOutputStream();
			while (true)
			{
				int bytesRead = is.read(buff);
				if (bytesRead <= 0) break;
				baos.write(buff, 0, bytesRead);
			}
			nmsg = new NewMessageType(name, baos.toByteArray());
			oStream.writeObject(nmsg);		
				
			ArrayList<Class<?>> anonClasses = new SwingChatClient().getAnonymousClasses();
			
			for (Class<?> anon : anonClasses)
			{
				name = anon.getName();
				is = STClass.getClassLoader().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
				baos = new ByteArrayOutputStream();
				while (true)
				{
					int bytesRead = is.read(buff);
					if (bytesRead <= 0) break;
					baos.write(buff, 0, bytesRead);
				}
				nmsg = new NewMessageType(name, baos.toByteArray());
				oStream.writeObject(nmsg);
			}
			
			Class<SwingChatClient> GUIClass = SwingChatClient.class;
			name = GUIClass.getName();
			is = GUIClass.getClassLoader().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
			baos = new ByteArrayOutputStream();
			while (true)
			{
				int bytesRead = is.read(buff);
				if (bytesRead <= 0) break;
				baos.write(buff, 0, bytesRead);
			}
			nmsg = new NewMessageType(name, baos.toByteArray());
			oStream.writeObject(nmsg);
			
			Class<SwingClientCreator> CreatorClass = SwingClientCreator.class;
			name = CreatorClass.getName();
			is = CreatorClass.getClassLoader().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
			baos = new ByteArrayOutputStream();
			while (true)
			{
				int bytesRead = is.read(buff);
				if (bytesRead <= 0) break;
				baos.write(buff, 0, bytesRead);
			}
			nmsg = new NewMessageType(name, baos.toByteArray());
			oStream.writeObject(nmsg);
			
			oStream.writeObject(new SwingClientCreator(ChatServer.hostName, ChatServer.port));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (SecurityException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} 
	}
	
	public ClientHandler(Socket s, MultiQueue<Message> q) 
	{
		socket = s;
		multiQueue = q;
		clientMessages = new SafeMessageQueue<Message>();
		multiQueue.register(clientMessages);
		nickname = "Anonymous" + (10000 + r.nextInt(89999));
		address = s.getInetAddress();
		multiQueue.put(new StatusMessage(nickname + " connected from " + address.getHostName() + "."));
		
		try
		{
			iStream = new ObjectInputStream(socket.getInputStream());
			oStream = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		sendGUI();
		
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