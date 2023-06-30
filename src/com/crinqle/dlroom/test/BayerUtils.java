package com.crinqle.dlroom.test;


import static com.crinqle.dlroom.Const.B;
import static com.crinqle.dlroom.Const.G;
import static com.crinqle.dlroom.Const.H;
import static com.crinqle.dlroom.Const.R;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.crinqle.dlroom.AreaBandIterator;
import com.crinqle.dlroom.CaptureData;
import com.crinqle.dlroom.Const;
import com.crinqle.dlroom.RawRaster;



class BayerUtils
{
    public final static double GAMMA = (1.0 / 1.8);

    /**
     * @param area <b>Must</b> be a square!
     */
    public static JPanel showColorFilterArea ( int[][] area, int bits, int band )
    {
        final int dim   = area.length;
        final int shift = bits - 8;
        final int r     = dim >> 1;
        final int max   = 1 << bits;

        final int[][] ba = new int[2][2];

        /*
         * Even! The corner is the same color as the band.
         */
        if ( (r % 2) == 1 )
        {
            switch ( band )
            {
                case G:
                    ba[0][0] = G;
                    ba[0][1] = R;
                    ba[1][0] = B;
                    ba[1][1] = H;
                    break;

                case H:
                    ba[0][0] = H;
                    ba[0][1] = B;
                    ba[1][0] = R;
                    ba[1][1] = G;
                    break;

                case B:
                    ba[0][0] = B;
                    ba[0][1] = H;
                    ba[1][0] = G;
                    ba[1][1] = R;
                    break;

                case R:
                    ba[0][0] = R;
                    ba[0][1] = G;
                    ba[1][0] = H;
                    ba[1][1] = B;
                    break;
            }
        }
        /*
         * Odd! The corner is not the band.
         */
        else
        {
            switch ( band )
            {
                case G:
                    ba[0][0] = H;
                    ba[0][1] = B;
                    ba[1][0] = R;
                    ba[1][1] = G;
                    break;

                case H:
                    ba[0][0] = G;
                    ba[0][1] = R;
                    ba[1][0] = B;
                    ba[1][1] = H;
                    break;

                case B:
                    ba[0][0] = R;
                    ba[0][1] = G;
                    ba[1][0] = H;
                    ba[1][1] = B;
                    break;

                case R:
                    ba[0][0] = B;
                    ba[0][1] = H;
                    ba[1][0] = G;
                    ba[1][1] = R;
                    break;
            }
        }

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(dim, dim));
        Color c = null;

        for ( int y = 0; y < dim; ++y )
        {
            // System.err.println("  ==> ");

            for ( int x = 0; x < dim; ++x )
            {
                int value = area[y][x];

                // System.err.print("\t" + value);

                double f = value / (double)max;

                // System.err.print("\t" + f + "(" + max + ")");

                f = Math.pow(f, GAMMA);

                value = (int)(f * 255);

                switch ( ba[y % 2][x % 2] )
                {
                    case G:
                    case H:
                        c = new Color(0, value, 0);
                        break;

                    case R:
                        c = new Color(value, 0, 0);
                        break;

                    case B:
                        c = new Color(0, 0, value);
                        break;

                    default:
                        throw new RuntimeException(
                              "What the hell band is this?  Use the constants, asshole.");
                }

                panel.add(makePanel(c));
            }

            // System.err.println("\n");
        }

        return panel;
    }

    /**
     * @param area <b>Must</b> be a square!
     */
    public static JPanel showRawFilterArea ( int[][] area, int bits, int band )
    {
        final int dim   = area.length;
        final int shift = bits - 8;
        final int r     = dim >> 1;
        final int max   = 1 << bits;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(dim, dim));
        Color c = null;

        for ( int y = 0; y < dim; ++y )
        {
            // System.err.print("  ==> ");

            for ( int x = 0; x < dim; ++x )
            {
                int value = area[y][x];

                // System.err.print("\t" + value);

                double f = value / (double)max;

                // System.err.print("\t" + f);

                f = Math.pow(f, GAMMA);

                value = (int)(f * 255);

                panel.add(makePanel(new Color(value, value, value)));
            }

            // System.err.println("\n");
        }

        return panel;
    }

    private static JPanel makePanel ( Color color )
    {
        JPanel    p    = new JPanel();
        final int size = 4;

        p.setBackground(color);

        p.setMaximumSize(new Dimension(size, size));
        p.setMinimumSize(new Dimension(size, size));
        p.setPreferredSize(new Dimension(size, size));

        return p;
    }

    public static void main ( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                  .println("Usage: java dlroom.BayerUtils <raw filename> <radius>");
            System.exit(1);
        }

        try
        {
            com.crinqle.dlroom.codec.RawCodec codec = com.crinqle.dlroom.codec.RawCodec
                  .getInstance(new java.io.File(args[0]));

            final int radius   = Integer.parseInt(args[1]);
            final int diameter = radius * 2 + 1;

            CaptureData cd = codec.decode();

            RawRaster rr = new RawRaster(cd);

            // AreaBandIterator abi = cd.areaBandIterator(CFA.R, radius);
            AreaBandIterator[] abis  = cd.areaBandIterators(radius);
            AreaBandIterator   abi   = abis[Const.R];
            int[]              array = null;

            if ( abi.next() )
            {
                array = abi.getArea();
            }

            /*
             * for ( int band = 0; band < 3; ++band ) { for ( int y = 0; y <
             * 2*radius+1; ++y ) { System.err.print("  band " + band + " --> ");
             *
             * for ( int x = 0; x < 2*radius+1; ++x ) System.err.print("\t" +
             * rr.sample(x, y, band));
             *
             * System.err.println("\n"); } System.err.println("\n"); }
             */

            JPanel grid = showColorFilterArea(array, 12, Const.R);

            JFrame frame = new JFrame("BayerUtils Test Driver");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(grid);

            frame.pack();
            frame.setVisible(true);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
