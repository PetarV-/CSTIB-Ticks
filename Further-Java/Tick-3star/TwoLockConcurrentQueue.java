package uk.ac.cam.pv273.fjava.tick3star;

public class TwoLockConcurrentQueue<T> implements ConcurrentQueue<T> 
{
	private static class Link<L> 
	{
		L val;
		Link<L> next;
		Link() { this.next = null; }
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;
	
	private Object HeadLock = new Object(), TailLock = new Object();

	public TwoLockConcurrentQueue()
	{
		first = last = new Link<T>();
	}
	
	public void offer(T val) 
	{
		Link<T> nextLink = new Link<T>(val);
		synchronized (TailLock)
		{
			last.next = nextLink;
			last = nextLink;
		}
	}

	public T poll() 
	{
		synchronized (HeadLock)
		{
			Link<T> newFirst = first.next;
			if (newFirst == null) return null;
			T ret = newFirst.val;
			first = newFirst;
			return ret;
		}
	}
}