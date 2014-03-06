package uk.ac.cam.pv273.fjava.tick2star;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

@FurtherJavaPreamble(
author = "Petar Veličković",
date = "7th November 2013",
crsid = "pv273",
summary = "Further Java Tick 2star, Safe Chat Client",
ticker = FurtherJavaPreamble.Ticker.A)
public class SafeChatClient 
{
	
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private static String getDate()
	{
		return dateFormat.format(new Date());
	}
	
	public static void main(String[] args)
	{
		System.setProperty("java.security.policy", "http://www.cl.cam.ac.uk/teaching/1011/FJava/all.policy");
		System.setSecurityManager(new SecurityManager());
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
		System.out.println(getDate() + " [Client] Connected to " + server + " on port " + port + ".");
		Thread output = new Thread() 
		{
			@Override
			public void run() 
			{
				InputStream sStream;
				SafeObjectInputStream in;
				try 
				{
					sStream = s.getInputStream();
					in = new SafeObjectInputStream(sStream);
				} 
				catch (IOException e) 
				{
					System.err.println("Cannot connect to " + server + " on port " + port);
					return;
				}
				while (true)
				{
					try 
					{
						Message msg = (Message)in.readObject();
						if (msg instanceof RelayMessage)
						{
							RelayMessage rmsg = (RelayMessage)msg;
							System.out.println(getDate() + " [" + rmsg.getFrom() + "] " + rmsg.getMessage());
						}
						else if (msg instanceof StatusMessage)
						{
							StatusMessage smsg = (StatusMessage)msg;
							System.out.println(getDate() + " [Server] " + smsg.getMessage());
						}
						else if (msg instanceof NewMessageType)
						{
							NewMessageType nmsgt = (NewMessageType)msg;
							in.addClass(nmsgt.getName(), nmsgt.getClassData());
							System.out.println(getDate() + " [Client] New class " + nmsgt.getName() + " loaded.");
						}
						else
						{
							Class<?> inClass = msg.getClass();
							Field[] fields = inClass.getDeclaredFields();
							Method[] methods = inClass.getDeclaredMethods();
							System.out.print(getDate() + " [Client] " + inClass.getSimpleName() + ":");
							for (int i=0;i<fields.length;i++)
							{
								try
								{
									fields[i].setAccessible(true);
									Object value = fields[i].get(msg);								
									System.out.print(" " + fields[i].getName() + "(" + value.toString() + ")");
									if (i != fields.length - 1) System.out.print(","); 
								}
								catch (Exception e) {}
							}
							System.out.println();
							for (int i=0;i<methods.length;i++)
							{
								
								if (methods[i].getGenericParameterTypes().length == 0 && methods[i].isAnnotationPresent(Execute.class))
								{
									methods[i].invoke(msg, null);
								}
							}
						}
					} 
					catch (IOException | ClassNotFoundException e)
					{
						System.err.println("Cannot connect to " + server + " on port " + port);
						return;
					} 
					catch (IllegalAccessException e) 
					{
						e.printStackTrace();
					} 
					catch (IllegalArgumentException e) 
					{
						e.printStackTrace();
					} 
					catch (InvocationTargetException e) 
					{
						e.printStackTrace();
					}
				}
			}
		};
		output.setDaemon(true);
		output.start();
		    
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		OutputStream oStream;
		ObjectOutputStream out;
		try 
		{
			oStream = s.getOutputStream();
			out = new ObjectOutputStream(oStream);
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
				 String msg = r.readLine();
				 if (msg.startsWith("\\"))
				 {
					 String[] tokens = msg.split(" ");
					 String command = tokens[0].substring(1);
					 if (command.equals("nick"))
					 {
						 out.writeObject(new ChangeNickMessage(tokens[1]));
					 }
					 else if (command.equals("quit"))
					 {
						 System.out.println(getDate() + " [Client] Connection terminated.");
						 return;
					 }
					 else
					 {
						 System.out.println(getDate() + " [Client] Unknown command \"" + command + "\"");
					 }
				 }
				 else
				 {
					 out.writeObject(new ChatMessage(msg));
				 }
			 } 
			 catch (IOException e) 
			 {
				 System.err.println("Cannot connect to " + server + " on port " + port);
				 return;
			 }
		}
	}
	
}
