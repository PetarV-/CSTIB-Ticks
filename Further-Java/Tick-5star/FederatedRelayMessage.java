package uk.ac.cam.cl.fjava.messages;

import java.util.Date;

public class FederatedRelayMessage extends RelayMessage 
{
	private static final long serialVersionUID = 1L;
	
	public FederatedRelayMessage(String from, ChatMessage original) 
	{
		super(from, original);
	}
	
	public FederatedRelayMessage(String from, String message, Date time) 
	{
		super(from, message, time);
	}
}
