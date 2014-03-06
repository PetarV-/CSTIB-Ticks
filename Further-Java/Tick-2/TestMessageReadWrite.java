package uk.ac.cam.pv273.fjava.tick2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class TestMessageReadWrite 
{
	
	static boolean writeMessage(String message, String filename) 
	{
		try
		{
			TestMessage tst = new TestMessage();
			tst.setMessage(message);
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(tst);
			out.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	static String readMessage(String location) 
	{
		if (location.startsWith("http://"))
		{
			try
			{
				URL addr = new URL(location);
				URLConnection conn = addr.openConnection();
				InputStream is = conn.getInputStream();
				ObjectInputStream in = new ObjectInputStream(is);
				TestMessage msg = (TestMessage)in.readObject();
				in.close();
				return msg.getMessage();
			}
			catch (Exception e)
			{
				return null;
			}
		}
		else
		{
			try
			{
				FileInputStream fis = new FileInputStream(location);
				ObjectInputStream in = new ObjectInputStream(fis);
				TestMessage msg = (TestMessage)in.readObject();
				in.close();
				return msg.getMessage();
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}

	public static void main(String args[]) 
	{
		System.out.println(readMessage("http://www.cl.cam.ac.uk/teaching/current/FJava/testmessage-pv273.jobj"));
	}

}
