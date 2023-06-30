package com.crinqle.dlroom;


import com.crinqle.dlroom.cfa.ColorFilterArray;
import com.crinqle.dlroom.cfa.Interpolation;



public class RawRaster extends ImageData implements Interpolation
{
    private CaptureData f_src;
    private boolean     f_ii = false;

    public RawRaster ( int width, int height, int bandCount, int bits )
    {
        super(width, height, bandCount, bits);
        f_src = null;
    }

    public RawRaster ( CaptureData cd )
    {
        super(cd.getWidth(), cd.getHeight(), cd.getCFA().getBandCount(), cd
              .getBits());
        // super(cd.getWidth(), cd.getHeight(), cd.getCFA().getBandCount(),
        // cd.getCFA(), cd.getBits());

        f_src = cd;

        final int        width  = f_src.getWidth();
        final int        height = f_src.getHeight();
        final int        bits   = f_src.getBits();
        ColorFilterArray cfa    = f_src.getCFA();

        final int bandCount = cfa.getBandCount();

        super.zero();

        BandIterator[] iters = f_src.bandIterators();

        for ( int band = 0; band < bandCount; ++band )
        {
            BandIterator iter = iters[band];

            System.err.println("  RawRaster: Processing band " + band + "...");

            while ( iter.next() )
                setElem(band, iter.index(), iter.get());
        }
    }

    public void applyGamma ( double gamma )
    {
        gamma = 1.0 / gamma;
        final int    imax = (1 << getBits()) - 1;
        final double dmax = (double)imax;
        int          val;

        for ( int i = 0; i < f_total; ++i )
        {
            val = (int)(Math.pow(f_a[i] / dmax, gamma) * dmax);

            if ( val < 0 )
                val = 0;
            if ( val > imax )
                val = imax;

            f_a[i] = val;
        }
    }

    public void applyLUT ( LUT lut ) // , RawRaster rr )
    {
        final int[] array = lut.array();

        for ( int i = 0; i < f_total; ++i )
              f_a[i] = array[f_a[i]];
    }

    public void applyLUT ( LUT lut, RawRaster rr )
    {
        final int   channel = lut.channel();
        final int[] array   = lut.array();

        applyLUT(channel, array, rr);

        /*
         * if ( (channel & R_MASK) == R_MASK ) applyLUT(R, array, rr); if (
         * (channel & G_MASK) == G_MASK ) applyLUT(G, array, rr); if ( (channel
         * & B_MASK) == B_MASK ) applyLUT(B, array, rr);
         */
    }

    private void applyLUT ( int channel, int[] array, RawRaster rr )
    {
        final int offset = channel * f_each;
        final int end    = offset + f_each;

        // for ( int i = 0; i < offset; ++i )
        // rr.f_a[i] = f_a[i];
        for ( int i = offset; i < end; ++i )
              rr.f_a[i] = array[f_a[i]];
        // for ( int i = end; i < f_total; ++i )
        // rr.f_a[i] = f_a[i];
    }

    public RawRaster createCopy ()
    {
        RawRaster rr = createBlankCopy();
        rr.f_src = f_src;
        rr.setData(f_a);
        rr.setProfile(getProfile());
        return rr;
    }

    public RawRaster createBlankCopy ()
    {
        RawRaster rr = new RawRaster(getWidth(), getHeight(), getBandCount(),
                                     getBits()
        );
        rr.setProfile(getProfile());
        return rr;
    }

    public ImageInfo getImageInfo ()
    {
        if ( f_src != null )
            return f_src.getImageInfo();
        return null;
    }

    public void setImageInfo ( ImageInfo info )
    {
        if ( f_src != null )
            f_src.setImageInfo(info);
    }

    /**
     * This method returns a rectangular subset of the current raster. It also
     * uses the
     */
    public RawRaster subraster ( int x, int y, int width, int height )
    {
        return new RawRaster(f_src.subraster(x, y, width, height));
    }

    public RawRaster subraster ( int x, int y )
    {
        return new RawRaster(f_src.subraster(x, y));
    }

    public void interpolate ( int mask )
    {
        if ( f_src != null )
        {
            Interpolation i = f_src.getCFA().interpolation(this);
            i.interpolate(mask);
            f_ii = true;
        }
    }

    public BandIterator[] bandIterators ()
    {
        if ( f_src != null )
            return f_src.bandIterators();
        return null;
    }

    public AreaBandIterator[] areaBandIterators ( int radius )
    {
        if ( f_src != null )
            return f_src.areaBandIterators(radius);
        return null;
    }

    public static void main ( String[] args )
    {
        if ( args.length < 1 )
        {
            System.err.println("Usage: java dlroom.RawRaster <raw filename>");
            System.exit(1);
        }

        try
        {
            com.crinqle.dlroom.codec.RawCodec codec = com.crinqle.dlroom.codec.RawCodec
                  .getInstance(new java.io.File(args[0]));

            CaptureData cd = codec.decode();

            RawRaster rr = new RawRaster(cd);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
