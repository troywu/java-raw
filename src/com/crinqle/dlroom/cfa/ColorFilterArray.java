package com.crinqle.dlroom.cfa;


import static com.crinqle.dlroom.Const.*;

import com.crinqle.dlroom.*;



/**
 * This class provides concrete BandIterator objects which are
 * created for a given Color Filter Array specification.
 */
public abstract class ColorFilterArray
{
    protected final int                f_fs;
    protected       int                f_bc;
    protected       BandIterator[]     f_bis  = new BandIterator[BAND_COUNT_MAX];
    protected       AreaBandIterator[] f_abis = new AreaBandIterator[BAND_COUNT_MAX];


    public static ColorFilterArray getInstance ( int filterSpec )
    {
        switch ( filterSpec )
        {
            case FILTER_BGGR:
            case FILTER_GRBG:
            case FILTER_GBRG:
            case FILTER_RGGB:
                return new BayerCFA(filterSpec);

            case FILTER_GMCYMGCY:
            case FILTER_CYGMCYMG:
            case FILTER_CYMGYCGM:
            case FILTER_YCMGCYGM:
            default:
        }

        throw new RuntimeException("Excuse?  What kind of CFA?");
    }


    protected ColorFilterArray ( int filterSpec ) { f_fs = filterSpec; }


    public int getBandCount () { return f_bc; }
    public abstract ColorFilterArray arrayAtOffset ( int x, int y );


    public abstract Interpolation interpolation ( RawRaster rr );
    public abstract BandIterator[] bandIterators ( ImageData id );
    public abstract AreaBandIterator[] areaBandIterators ( ImageData id, int radius );
}
