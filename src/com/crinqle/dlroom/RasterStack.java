package com.crinqle.dlroom;

import java.util.*;


public class RasterStack
{
	private Stack<RawRaster> f_s = new Stack<RawRaster>();


	public boolean empty() { return f_s.empty(); }
	public void removeAllElements() { f_s.removeAllElements(); }


	public RawRaster peek()
	{
		try
		{
			return (RawRaster)f_s.peek();
		}
		catch ( EmptyStackException e ) { System.err.println("Peeking...Nothing on top; returning null."); return null; }
	}


	public RawRaster pop()
	{
		return f_s.pop();
	}


	public void push ( RawRaster rr )
	{
		f_s.add(rr);
	}


	public RawRaster first()
	{
		return f_s.firstElement();
	}


	public RawRaster get ( int n )
	{
		return f_s.get(n);
	}
}
