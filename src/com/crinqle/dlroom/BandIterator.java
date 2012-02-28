package com.crinqle.dlroom;


/**
 * This interface is intended to allow a user to iterate through a band in
 * the image data.  The concrete implementations Provide the actual
 * classes.
 */
public interface BandIterator
{
	boolean next();
	boolean prev();
	int get();
	int index();
	int x();
	int y();
	void set ( int value );
}
