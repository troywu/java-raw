package com.crinqle.dlroom.test;


import static com.crinqle.dlroom.Const.ALL_MASK;
import static com.crinqle.dlroom.Const.B;
import static com.crinqle.dlroom.Const.B_MASK;
import static com.crinqle.dlroom.Const.G;
import static com.crinqle.dlroom.Const.G_MASK;
import static com.crinqle.dlroom.Const.H_MASK;
import static com.crinqle.dlroom.Const.R;
import static com.crinqle.dlroom.Const.R_MASK;

import com.crinqle.dlroom.CaptureData;
import com.crinqle.dlroom.RawRaster;



public class InterpProf
{
    public static RawRaster[][] createImages ( String[] args )
    {
        RawRaster[][] rra = new RawRaster[2][2];

        if ( args.length < 1 )
        {
            System.err
                  .println("Usage: java dlroom.BayerVNG <raw filename> [gamma]");
            System.exit(1);
        }

        try
        {
            com.crinqle.dlroom.codec.RawCodec codec
                  = com.crinqle.dlroom.codec.RawCodec.getInstance(new java.io.File(args[0]));
            CaptureData cd = codec.decode();

            // CaptureData cd = dlroom.test.BayerTest.makeDiagonalTestGrid();
            // CaptureData cd = dlroom.test.BayerTest.makeWhiteTestGrid();
            // CaptureData cd = dlroom.test.BayerTest.makeSmallWhiteTestGrid();
            // CaptureData cd = dlroom.test.BayerTest.makeColorTestGrid();

            RawRaster rrR = new RawRaster(cd);
            RawRaster rrG = new RawRaster(cd);
            RawRaster rrH = new RawRaster(cd);
            RawRaster rrB = new RawRaster(cd);

            /*
             * System.out.print("CaptureData:"); for ( int i = 0; i <
             * cd.getSize(); ++i ) System.out.print(" " + cd.getElem(i));
             * System.out.println();
             *
             * final int height = rrR.getHeight(); final int width =
             * rrR.getWidth();
             *
             * for ( int band = 0; band < 3; ++band ) {
             * System.out.println("RawRaster (post-conversion, band " + band +
             * ")");
             *
             * for ( int y = 0; y < height; ++y ) { for ( int x = 0; x < width;
             * ++x ) System.out.print(" " + rrR.getSample(x, y, band));
             * System.out.println("\n"); } System.out.println("\n"); }
             */

            rrR.interpolate(R_MASK);
            rrG.interpolate(G_MASK | H_MASK);
            rrH.interpolate(ALL_MASK);
            rrB.interpolate(B_MASK);

            /*
             * Applying gamma...
             */
            if ( args.length > 1 )
            {
                final double gamma = Double.parseDouble(args[1]);

                System.err.println("  Applying gamma: " + gamma + "...");
                rrR.applyGamma(gamma);
                rrG.applyGamma(gamma);
                rrH.applyGamma(gamma);
                rrB.applyGamma(gamma);
            }

            /*
             * Scaling up or down to 8-bit per channel...
             */
            final int bits = cd.getBits();

            shift(rrR, bits);
            shift(rrG, bits);
            shift(rrH, bits);
            shift(rrB, bits);

            rra[0][0] = rrR;
            rra[0][1] = rrG;
            rra[1][0] = rrH;
            rra[1][1] = rrB;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }

        return rra;
    }

    public static void main ( String[] args )
    {
        createImages(args);
    }

    private static void shift ( RawRaster rr, int bits )
    {
        if ( bits != 8 )
        {
            final int shift = Math.abs(bits - 8);
            final int w     = rr.getWidth();
            final int h     = rr.getHeight();

            System.err.println("  Depth scaling...");

            for ( int y = 0; y < h; ++y )
                for ( int x = 0; x < w; ++x )
                {
                    rr.setSample(x, y, R, rr.getSample(x, y, R) >> 4);
                    rr.setSample(x, y, G, rr.getSample(x, y, G) >> 4);
                    rr.setSample(x, y, B, rr.getSample(x, y, B) >> 4);
                }
        }
    }
}
