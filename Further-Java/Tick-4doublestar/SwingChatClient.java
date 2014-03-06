package uk.ac.cam.pv273.fjava.tick4star;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class SwingChatClient extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private JTextField chatBox;
	private JTextArea chatData;
	private JScrollPane scroll;
	private JButton nickButton;
	private transient Socket s;
	private String server;
	private int port;
	private transient ObjectInputStream in;
	private transient ObjectOutputStream out;
	private ArrayList<Class<?>> anonClasses = new ArrayList<Class<?>>();
	
	public ArrayList<Class<?>> getAnonymousClasses()
	{
		return anonClasses;
	}
	
	private static String getDate()
	{
		return dateFormat.format(new Date());
	}
	
	public ActionListener chatActionListener = new SerializableActionListener()
	{		
		public static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if (arg0.getSource() == chatBox)
			{					
				try
				{
					String msg = chatBox.getText();
					if (!msg.isEmpty())
					{
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
								chatData.append(getDate() + " [Client] Connection terminated.\n");
								System.exit(0);
								return;
							}
							else
							{
								chatData.append(getDate() + " [Client] Unknown command \"" + command + "\"\n");
							}
						}
						else
						{
							out.writeObject(new ChatMessage(msg));
						}
					}
				}
				catch (IOException e) 
				{ 
					System.err.println("Cannot connect to " + server + " on port " + port);
					return;
				}					
				chatBox.setText("");
			}
		}
	};
	
	public ActionListener buttonActionListener = new SerializableActionListener()
	{		
		public static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if (arg0.getSource() == nickButton)
			{					
				String newNickname = JOptionPane.showInputDialog(SwingChatClient.this, "Enter new nickname");
				if (newNickname != null)
				{
					if (!newNickname.isEmpty())
					{
						try 
						{
							out.writeObject(new ChangeNickMessage(newNickname));
						} 
						catch (IOException e) 
						{
							System.err.println("Cannot connect to " + server + " on port " + port);
							return;
						}	
					}
				}
			}
		}
	};
	
	public SerializableThread output = new SerializableThread()
	{
		public static final long serialVersionUID = 1L;

		@Override
		public void run() 
		{
			try 
			{
				in = new ObjectInputStream(s.getInputStream());
			} 
			catch (IOException e1) 
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
						chatData.append(getDate() + " [" + rmsg.getFrom() + "] " + rmsg.getMessage() + "\n");
					}
					else if (msg instanceof StatusMessage)
					{
						StatusMessage smsg = (StatusMessage)msg;
						chatData.append(getDate() + " [Server] " + smsg.getMessage() + "\n");
					}
				}
				catch (Exception e)
				{
					System.err.println("Cannot connect to " + server + " on port " + port);
					return;
				}
			}
		}
	};
	
	public SwingChatClient()
	{
		//Just fill the anon class array, so they can be serialised
		anonClasses.add(chatActionListener.getClass());
		anonClasses.add(buttonActionListener.getClass());
		anonClasses.add(output.getClass());
	}
	
	public SwingChatClient(String srv, int prt)
	{		
		super("Swing Chat Client - Further Java Tick 4*");
		
		server = srv;
		port = prt;
		
		try 
		{ 
			s = new Socket(server, port);
			out = new ObjectOutputStream(s.getOutputStream());
		}
		catch (Exception e)
		{
			System.err.println("Cannot connect to " + server + " on port " + port);
			return;
		}
		
		chatBox = new JTextField(20);
		chatBox.addActionListener(chatActionListener);
		anonClasses.add(chatActionListener.getClass());
		chatData = new JTextArea();
		chatData.setEditable(false);
		scroll = new JScrollPane(chatData, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		nickButton = new JButton("Change nickname");
		nickButton.addActionListener(buttonActionListener);
		anonClasses.add(buttonActionListener.getClass());
		
		this.add(scroll);
		this.getContentPane().add(chatBox, BorderLayout.SOUTH);
		this.getContentPane().add(nickButton, BorderLayout.NORTH);
		this.setSize(500, 500);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		
		chatData.append(getDate() + " [Client] Connected to " + server + " on port " + port + ".\n");
		
		output.setDaemon(true);
		output.start();	
		anonClasses.add(output.getClass());
	}
}
