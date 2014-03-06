package uk.ac.cam.pv273.fjava.tick3;

public class SafeMessageQueue<T> implements MessageQueue<T> 
{
	private static class Link<L> 
	{
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;

	public synchronized void put(T val) 
	{
		Link<T> nextLink = new Link<T>(val);
		if (first == null) first = nextLink;
		else last.next = nextLink;
		last = nextLink;
		this.notify();
	}

	public synchronized T take() 
	{
		while (first == null) //use a loop to block thread until data is available
		{
			try 
			{
				this.wait();
			} 
			catch(InterruptedException ie) {}
		}
		
		T ret = first.val;
		first = first.next;
		
		return ret;
	}
}