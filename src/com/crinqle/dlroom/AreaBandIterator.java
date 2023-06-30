package com.crinqle.dlroom;


/**
 * This interface is intended to allow a user to iterate through a
 * band in the image data, but to return not just the pixel band in
 * question, but the entire neighborhood around the pixel, with the
 * center of the neighborhood being the next pixel in the iteration.
 */
public interface AreaBandIterator extends BandIterator
{
    int[] getArea ();
    void set ( int band, int val );
}
