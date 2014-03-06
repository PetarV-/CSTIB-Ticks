package uk.ac.cam.pv273.fjava.tick3star;

public class OneLockConcurrentQueue<T> implements ConcurrentQueue<T> 
{
	private static class Link<L> 
	{
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;

	public synchronized void offer(T val) 
	{
		Link<T> nextLink = new Link<T>(val);
		if (first == null) first = nextLink;
		else last.next = nextLink;
		last = nextLink;
		this.notify();
	}

	public synchronized T poll() 
	{
		if (first == null) return null;
		
		T ret = first.val;
		first = first.next;
		
		return ret;
	}
}