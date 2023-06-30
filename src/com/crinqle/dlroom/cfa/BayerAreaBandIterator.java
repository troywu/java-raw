package com.crinqle.dlroom.cfa;


import com.crinqle.dlroom.*;



class BayerAreaBandIterator extends BayerBandIterator implements AreaBandIterator
{
    private final int   f_d;
    private       int[] f_array;


    BayerAreaBandIterator ( ImageData id, int x, int y, int radius )
    {
        super(id, x, y, radius);
        f_d = (f_r * 2) + 1;

        f_array = new int[f_d * f_d];
    }


    /**
     * Returns the neighborhood of a pixel, with the neighborhood
     * centered at the pixel.  The neighborhood is always square,
     * and accounts for the border (where the radius neighborhood
     * is not available).
     *
     * @return a row-major raster-like array of arrays
     */
    public int[] getArea ()
    {
        final int dx = f_x - f_r;
        final int dy = f_y - f_r;
        int       k  = 0;

        for ( int y = 0; y < f_d; ++y )
            for ( int x = 0; x < f_d; ++x )
                  f_array[k++] = f_id.getElem((x + dx) + ((y + dy) * f_w));

        return f_array;
    }


    public void set ( int band, int val ) { f_id.setElem(band * f_total + index(), val); }
}
