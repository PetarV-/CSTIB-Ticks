package uk.ac.cam.pv273.fjava.tick3star;

import java.util.concurrent.atomic.AtomicReference;

public class NoLockConcurrentQueue<T> implements ConcurrentQueue<T> 
{
	private static class Link<L> 
	{
		L val;
		AtomicReference<Link<L>> next;
		Link() { this.next = new AtomicReference<Link<L>>(null); }
		Link(L val) { this.val = val; this.next = new AtomicReference<Link<L>>(null); }
	}
	
	AtomicReference<Link<T>> first = new AtomicReference<Link<T>>(null);
	AtomicReference<Link<T>> last = new AtomicReference<Link<T>>(null);

	public NoLockConcurrentQueue()
	{
		Link<T> newNode = new Link<T>();
		first.set(newNode);
		last.set(newNode);
	}
	
	public void offer(T val) 
	{
		Link<T> nextLink = new Link<T>(val);
		Link<T> tail;
		while (true)
		{
			tail = last.get();
			Link<T> next = tail.next.get();
			if (tail == last.get())
			{
				if (next == null)
				{
					if (tail.next.compareAndSet(next, nextLink)) break;
				}
				else 
				{
					last.compareAndSet(tail, next);
				}
			}
		}
		last.compareAndSet(tail, nextLink);
	}

	public T poll() 
	{
		while (true)
		{
			Link<T> head = first.get();
			Link<T> tail = last.get();
			Link<T> next = head.next.get();
			if (head == first.get())
			{
				if (head == tail) 
				{
					if (next == null) return null;
					last.compareAndSet(tail, next);
				}
				else
				{
					T ret = next.val;
					if (first.compareAndSet(head, next)) return ret;
				}
			}
		}
	}
}