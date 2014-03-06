package uk.ac.cam.pv273.fjava.tick4star;

import java.io.Serializable;

import uk.ac.cam.cl.fjava.messages.Message;

public class SwingClientCreator extends Message implements Serializable
{
	public static final long serialVersionUID = 1L;
	public String hostName = null;
	public int port;
	
	public SwingClientCreator(String hostName, int port)
	{
		this.hostName = hostName;
		this.port = port;
	}
	
	@uk.ac.cam.cl.fjava.messages.Execute
	public void Execute()
	{
		new SwingChatClient(hostName, port);
	}
}