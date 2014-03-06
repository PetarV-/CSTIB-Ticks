package uk.ac.cam.pv273.fjava.tick3;

public class UnsafeMessageQueue<T> implements MessageQueue<T> 
{
	private static class Link<L> 
	{
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;

	public void put(T val) 
	{
		Link<T> nextLink = new Link<T>(val);
		if (first == null) first = nextLink;
		else last.next = nextLink;
		last = nextLink;
	}

	public T take() 
	{
		while (first == null) //use a loop to block thread until data is available
		{
			try {Thread.sleep(100);} catch(InterruptedException ie) {}
		}
		
		T ret = first.val;
		first = first.next;	
		
		return ret;
	}
}