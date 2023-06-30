package com.crinqle.dlroom.cfa;


import static com.crinqle.dlroom.Const.*;

import com.crinqle.dlroom.*;



class BayerCFA extends ColorFilterArray
{
    BayerCFA ( int filterSpec )
    {
        super(filterSpec);

        f_bc = 3;
    }


    public AreaBandIterator[] areaBandIterators ( ImageData id, int radius )
    {
        switch ( f_fs )
        {
            case FILTER_BGGR:
                f_abis[B] = new BayerAreaBandIterator(id, 0, 0, radius);
                f_abis[H] = new BayerAreaBandIterator(id, 1, 0, radius);
                f_abis[G] = new BayerAreaBandIterator(id, 0, 1, radius);
                f_abis[R] = new BayerAreaBandIterator(id, 1, 1, radius);
                break;

            case FILTER_GRBG:
                f_abis[G] = new BayerAreaBandIterator(id, 0, 0, radius);
                f_abis[R] = new BayerAreaBandIterator(id, 1, 0, radius);
                f_abis[B] = new BayerAreaBandIterator(id, 0, 1, radius);
                f_abis[H] = new BayerAreaBandIterator(id, 1, 1, radius);
                break;

            case FILTER_GBRG:
                f_abis[H] = new BayerAreaBandIterator(id, 0, 0, radius);
                f_abis[B] = new BayerAreaBandIterator(id, 1, 0, radius);
                f_abis[G] = new BayerAreaBandIterator(id, 0, 1, radius);
                f_abis[R] = new BayerAreaBandIterator(id, 1, 1, radius);
                break;

            case FILTER_RGGB:
                f_abis[R] = new BayerAreaBandIterator(id, 0, 0, radius);
                f_abis[G] = new BayerAreaBandIterator(id, 1, 0, radius);
                f_abis[H] = new BayerAreaBandIterator(id, 0, 1, radius);
                f_abis[B] = new BayerAreaBandIterator(id, 1, 1, radius);
                break;
        }

        return f_abis;
    }


    /**
     * Returns a new BayerCFA based on the specified offsets.
     * Negative values are okay, because the array doesn't know
     * about the raster.
     *
     * @param dx X-axis offset from the origin.
     * @param dy Y-axis offset from the origin.
     */
    public ColorFilterArray arrayAtOffset ( int dx, int dy )
    {
        final int xmod = dx % 2;
        final int ymod = dy % 2;

        if ( xmod == 0 && ymod == 0 )
            return new BayerCFA(f_fs);

        BayerCFA cfa = null;

        switch ( f_fs )
        {
            case FILTER_BGGR:
                if ( xmod == 1 && ymod == 0 )
                    cfa = new BayerCFA(FILTER_GBRG);
                else if ( xmod == 0 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_GRBG);
                else if ( xmod == 1 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_RGGB);
                break;

            case FILTER_GRBG:
                if ( xmod == 1 && ymod == 0 )
                    cfa = new BayerCFA(FILTER_RGGB);
                else if ( xmod == 0 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_BGGR);
                else if ( xmod == 1 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_GBRG);
                break;

            case FILTER_GBRG:
                if ( xmod == 1 && ymod == 0 )
                    cfa = new BayerCFA(FILTER_BGGR);
                else if ( xmod == 0 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_RGGB);
                else if ( xmod == 1 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_GRBG);
                break;

            case FILTER_RGGB:
                if ( xmod == 1 && ymod == 0 )
                    cfa = new BayerCFA(FILTER_GRBG);
                else if ( xmod == 0 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_GBRG);
                else if ( xmod == 1 && ymod == 1 )
                    cfa = new BayerCFA(FILTER_BGGR);
                break;
        }

        return cfa;
    }


    public BandIterator[] bandIterators ( ImageData id )
    {
        switch ( f_fs )
        {
            case FILTER_BGGR:
                f_bis[R] = new BayerBandIterator(id, 1, 1, 1, 3, 2, 2);
                f_bis[G] = new BayerBandIterator(id, 1, 0, 0, 1, 2, 1);
                f_bis[B] = new BayerBandIterator(id, 0, 0, 0, 2, 2, 2);
                break;

            case FILTER_GRBG:
                f_bis[R] = new BayerBandIterator(id, 1, 0, 1, 2, 2, 2);
                f_bis[G] = new BayerBandIterator(id, 0, 0, 1, 1, 2, 1);
                f_bis[B] = new BayerBandIterator(id, 0, 1, 0, 3, 2, 2);
                break;

            case FILTER_GBRG:
                f_bis[R] = new BayerBandIterator(id, 0, 1, 0, 3, 2, 2);
                f_bis[G] = new BayerBandIterator(id, 0, 0, 1, 1, 2, 1);
                f_bis[B] = new BayerBandIterator(id, 1, 0, 1, 2, 2, 2);
                break;

            case FILTER_RGGB:
                f_bis[R] = new BayerBandIterator(id, 0, 0, 0, 2, 2, 2);
                f_bis[G] = new BayerBandIterator(id, 1, 0, 0, 1, 2, 1);
                f_bis[B] = new BayerBandIterator(id, 1, 1, 1, 3, 2, 2);
                break;
        }

        return f_bis;
    }


    public Interpolation interpolation ( RawRaster rr )
    {
        return new BayerVNG(rr);
    }
}
