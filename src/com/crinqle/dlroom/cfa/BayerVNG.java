package com.crinqle.dlroom.cfa;


import static com.crinqle.dlroom.Const.*;

import com.crinqle.dlroom.*;



public class BayerVNG implements Interpolation
{
    private final static int N  = 0;
    private final static int S  = 1;
    private final static int E  = 2;
    private final static int W  = 3;
    private final static int NE = 4;
    private final static int SW = 5;
    private final static int NW = 6;
    private final static int SE = 7;

    private final static int      GRAD_COUNT = 8;
    private final static String[] GRAD_NAMES = {"N", "S", "E", "W", "NE", "SW", "NW", "SE"};

    private       int[]              f_grads = new int[8];
    private       RawRaster          f_rr;
    private final int                f_max;
    private       AreaBandIterator[] f_abis;
    private       int                f_t;
    private       int                f_mask;
    private       boolean            f_debug = false;


    public BayerVNG ( RawRaster rr )
    {
        f_rr = rr;

        f_max  = (1 << f_rr.getBits()) - 1;
        f_abis = f_rr.areaBandIterators(2);
    }


    public void setDebug ( boolean isOn ) { f_debug = isOn; }


    public void interpolate ( int mask )
    {
        f_mask = mask;

        if ( (f_mask & ALL_MASK) == ALL_MASK )
        {
            System.err.println("  Doing all-channel interpolation...debugging unset.");
            setDebug(false);
        }

        System.out.println("    f_mask: " + f_mask);

        if ( (f_mask & R_MASK) == R_MASK )
            interpolateR();
        if ( (f_mask & G_MASK) == G_MASK )
            interpolateG();
        if ( (f_mask & H_MASK) == H_MASK )
            interpolateH();
        if ( (f_mask & B_MASK) == B_MASK )
            interpolateB();
    }


    private void interpolateR ()
    {
        System.err.println("    Doing Bayer VNG interpolation (R)...");

        int[] a;
        int   gsum;
        int   rsum;
        int   bsum;
        int   count;

        AreaBandIterator iter = f_abis[R];

        while ( iter.next() )
        {
            a = iter.getArea();
            final int x = iter.x();
            final int y = iter.y();

            computeCardinalGradients(a);
            computeChromaGradients(a);
            computeThreshold();

            count = 0;
            rsum  = 0;
            gsum  = 0;
            bsum  = 0;

            if ( f_grads[N] < f_t )
            {
                rsum = (a[12] + a[2]) >> 1;
                gsum = a[7];
                bsum = (a[6] + a[8]) >> 1;
                ++count;
            }
            if ( f_grads[S] < f_t )
            {
                rsum += (a[12] + a[22]) >> 1;
                gsum += a[17];
                bsum += (a[16] + a[18]) >> 1;
                ++count;
            }
            if ( f_grads[E] < f_t )
            {
                rsum += (a[12] + a[14]) >> 1;
                gsum += a[13];
                bsum += (a[8] + a[18]) >> 1;
                ++count;
            }
            if ( f_grads[W] < f_t )
            {
                rsum += (a[12] + a[10]) >> 1;
                gsum += a[11];
                bsum += (a[6] + a[16]) >> 1;
                ++count;
            }
            if ( f_grads[NE] < f_t )
            {
                rsum += (a[12] + a[4]) >> 1;
                gsum += (a[3] + a[7] + a[9] + a[13]) >> 2;
                bsum += a[8];
                ++count;
            }
            if ( f_grads[SW] < f_t )
            {
                rsum += (a[12] + a[20]) >> 1;
                gsum += (a[11] + a[15] + a[17] + a[21]) >> 2;
                bsum += a[16];
                ++count;
            }
            if ( f_grads[NW] < f_t )
            {
                rsum += (a[12] + a[0]) >> 1;
                gsum += (a[1] + a[5] + a[7] + a[11]) >> 2;
                bsum += a[6];
                ++count;
            }
            if ( f_grads[SE] < f_t )
            {
                rsum += (a[12] + a[24]) >> 1;
                gsum += (a[13] + a[17] + a[19] + a[23]) >> 2;
                bsum += a[18];
                ++count;
            }

            gsum = (int)(a[12] + ((gsum - rsum) / (float)count));
            bsum = (int)(a[12] + ((bsum - rsum) / (float)count));

            if ( gsum < 0 ) gsum = 0;
            else if ( gsum > f_max ) gsum = f_max;

            if ( bsum < 0 ) bsum = 0;
            else if ( bsum > f_max ) bsum = f_max;

            f_rr.setSample(x, y, G, gsum);
            f_rr.setSample(x, y, B, bsum);

            // if ( f_debug ) f_rr.setSample(x, y, R, 0);
            // if ( f_debug ) clearNeighbors(x,y);
        }
    }


    private void interpolateG ()
    {
        System.err.println("    Doing Bayer VNG interpolation (G)...");

        int[] a;
        int   gsum;
        int   rsum;
        int   bsum;
        int   count;

        AreaBandIterator iter = f_abis[G];

        while ( iter.next() )
        {
            a = iter.getArea();
            final int x = iter.x();
            final int y = iter.y();

            computeCardinalGradients(a);
            computeGreenGradients(a);
            computeThreshold();

            count = 0;
            rsum  = 0;
            gsum  = 0;
            bsum  = 0;

            if ( f_grads[N] < f_t )
            {
                rsum = (a[1] + a[3] + a[11] + a[13]) >> 2;
                gsum = (a[2] + a[12]) >> 1;
                bsum = a[7];
                ++count;
            }
            if ( f_grads[S] < f_t )
            {
                rsum += (a[21] + a[23] + a[11] + a[13]) >> 2;
                gsum += (a[22] + a[12]) >> 1;
                bsum += a[17];
                ++count;
            }
            if ( f_grads[E] < f_t )
            {
                rsum += a[13];
                gsum += (a[12] + a[14]) >> 1;
                bsum += (a[7] + a[9] + a[17] + a[19]) >> 2;
                ++count;
            }
            if ( f_grads[W] < f_t )
            {
                rsum += a[11];
                gsum += (a[12] + a[10]) >> 1;
                bsum += (a[5] + a[7] + a[15] + a[17]) >> 2;
                ++count;
            }
            if ( f_grads[NE] < f_t )
            {
                rsum += (a[3] + a[13]) >> 1;
                gsum += (a[8] + a[12]) >> 1;
                bsum += (a[7] + a[9]) >> 1;
                ++count;
            }
            if ( f_grads[SW] < f_t )
            {
                rsum += (a[11] + a[21]) >> 1;
                gsum += (a[16] + a[12]) >> 1;
                bsum += (a[15] + a[17]) >> 1;
                ++count;
            }
            if ( f_grads[NW] < f_t )
            {
                rsum += (a[1] + a[11]) >> 1;
                gsum += (a[6] + a[12]) >> 1;
                bsum += (a[5] + a[7]) >> 1;
                ++count;
            }
            if ( f_grads[SE] < f_t )
            {
                rsum += (a[13] + a[23]) >> 1;
                gsum += (a[18] + a[12]) >> 1;
                bsum += (a[17] + a[19]) >> 1;
                ++count;
            }

            rsum = (int)(a[12] + ((rsum - gsum) / (float)count));
            bsum = (int)(a[12] + ((bsum - gsum) / (float)count));

            if ( rsum > f_max ) rsum = f_max;
            else if ( rsum < 0 ) rsum = 0;

            if ( bsum > f_max ) bsum = f_max;
            else if ( bsum < 0 ) bsum = 0;

            f_rr.setSample(x, y, R, rsum);
            f_rr.setSample(x, y, B, bsum);

            // if ( f_debug ) f_rr.setSample(x, y, G, 0);
            // if ( f_debug ) clearNeighbors(x,y);
        }
    }


    private void interpolateH ()
    {
        System.err.println("    Doing Bayer VNG interpolation (H)...");

        int[] a;
        int   gsum;
        int   rsum;
        int   bsum;
        int   count;

        AreaBandIterator iter = f_abis[H];

        while ( iter.next() )
        {
            a = iter.getArea();
            final int x = iter.x();
            final int y = iter.y();

            computeCardinalGradients(a);
            computeGreenGradients(a);
            computeThreshold();

            count = 0;
            rsum  = 0;
            gsum  = 0;
            bsum  = 0;

            if ( f_grads[N] < f_t )
            {
                bsum = (a[1] + a[3] + a[11] + a[13]) >> 2;
                gsum = (a[2] + a[12]) >> 1;
                rsum = a[7];
                ++count;
            }
            if ( f_grads[S] < f_t )
            {
                bsum += (a[21] + a[23] + a[11] + a[13]) >> 2;
                gsum += (a[22] + a[12]) >> 1;
                rsum += a[17];
                ++count;
            }
            if ( f_grads[E] < f_t )
            {
                bsum += a[13];
                gsum += (a[12] + a[14]) >> 1;
                rsum += (a[7] + a[9] + a[17] + a[19]) >> 2;
                ++count;
            }
            if ( f_grads[W] < f_t )
            {
                bsum += a[11];
                gsum += (a[12] + a[10]) >> 1;
                rsum += (a[5] + a[7] + a[15] + a[17]) >> 2;
                ++count;
            }
            if ( f_grads[NE] < f_t )
            {
                bsum += (a[3] + a[13]) >> 1;
                gsum += (a[8] + a[12]) >> 1;
                rsum += (a[7] + a[9]) >> 1;
                ++count;
            }
            if ( f_grads[SW] < f_t )
            {
                bsum += (a[11] + a[21]) >> 1;
                gsum += (a[16] + a[12]) >> 1;
                rsum += (a[15] + a[17]) >> 1;
                ++count;
            }
            if ( f_grads[NW] < f_t )
            {
                bsum += (a[1] + a[11]) >> 1;
                gsum += (a[6] + a[12]) >> 1;
                rsum += (a[5] + a[7]) >> 1;
                ++count;
            }
            if ( f_grads[SE] < f_t )
            {
                bsum += (a[13] + a[23]) >> 1;
                gsum += (a[18] + a[12]) >> 1;
                rsum += (a[17] + a[19]) >> 1;
                ++count;
            }

            rsum = (int)(a[12] + ((rsum - gsum) / (float)count));
            bsum = (int)(a[12] + ((bsum - gsum) / (float)count));

            if ( rsum > f_max ) rsum = f_max;
            else if ( rsum < 0 ) rsum = 0;

            if ( bsum > f_max ) bsum = f_max;
            else if ( bsum < 0 ) bsum = 0;

            f_rr.setSample(x, y, R, rsum);
            f_rr.setSample(x, y, B, bsum);

            // if ( f_debug ) f_rr.setSample(x, y, G, 0);
            // if ( f_debug ) clearNeighbors(x,y);
        }
    }


    private void interpolateB ()
    {
        System.err.println("    Doing Bayer VNG interpolation (B)...");

        int[] a;
        int   gsum;
        int   rsum;
        int   bsum;
        int   count;

        AreaBandIterator iter = f_abis[B];

        while ( iter.next() )
        {
            a = iter.getArea();
            final int x = iter.x();
            final int y = iter.y();

            computeCardinalGradients(a);
            computeChromaGradients(a);
            computeThreshold();

            count = 0;
            rsum  = 0;
            gsum  = 0;
            bsum  = 0;

            if ( f_grads[N] < f_t )
            {
                bsum = (a[12] + a[2]) >> 1;
                gsum = a[7];
                rsum = (a[6] + a[8]) >> 1;
                ++count;
            }
            if ( f_grads[S] < f_t )
            {
                bsum += (a[12] + a[22]) >> 1;
                gsum += a[17];
                rsum += (a[16] + a[18]) >> 1;
                ++count;
            }
            if ( f_grads[E] < f_t )
            {
                bsum += (a[12] + a[14]) >> 1;
                gsum += a[13];
                rsum += (a[8] + a[18]) >> 1;
                ++count;
            }
            if ( f_grads[W] < f_t )
            {
                bsum += (a[12] + a[10]) >> 1;
                gsum += a[11];
                rsum += (a[6] + a[16]) >> 1;
                ++count;
            }
            if ( f_grads[NE] < f_t )
            {
                bsum += (a[12] + a[4]) >> 1;
                gsum += (a[3] + a[7] + a[9] + a[13]) >> 2;
                rsum += a[8];
                ++count;
            }
            if ( f_grads[SW] < f_t )
            {
                bsum += (a[12] + a[20]) >> 1;
                gsum += (a[11] + a[15] + a[17] + a[21]) >> 2;
                rsum += a[16];
                ++count;
            }
            if ( f_grads[NW] < f_t )
            {
                bsum += (a[12] + a[0]) >> 1;
                gsum += (a[1] + a[5] + a[7] + a[11]) >> 2;
                rsum += a[6];
                ++count;
            }
            if ( f_grads[SE] < f_t )
            {
                bsum += (a[12] + a[24]) >> 1;
                gsum += (a[13] + a[17] + a[19] + a[23]) >> 2;
                rsum += a[18];
                ++count;
            }

			/*
			if ( x == 3  &&  y == 3 )
			{
				System.out.println("@@ Before normalizing difference...");
				System.out.println("   count: " + count);
				System.out.println("   bsum: " + bsum);
				System.out.println("   (rsum - bsum) / (float)count: " + (((rsum - bsum) / (float)count)));
				System.out.println("   (gsum - bsum) / (float)count: " + (((gsum - bsum) / (float)count)));
				debug(x, y, rsum, gsum, a[12]);
			}
			*/

            rsum = (int)(a[12] + ((rsum - bsum) / (float)count));
            gsum = (int)(a[12] + ((gsum - bsum) / (float)count));

			/*
			if ( x == 3  &&  y == 3 )
			{
				System.out.println("@@ After normalizing difference...");
				debug(x, y, rsum, gsum, a[12]);
			}
			*/

            if ( rsum < 0 ) rsum = 0;
            else if ( rsum > f_max ) rsum = f_max;

            if ( gsum < 0 ) gsum = 0;
            else if ( gsum > f_max ) gsum = f_max;

			/*
			if ( x == 3  &&  y == 3 )
			{
				System.out.println("@@ After clamping...");
				debug(x, y, rsum, gsum, a[12]);
			}
			*/

            f_rr.setSample(x, y, R, rsum);
            f_rr.setSample(x, y, G, gsum);

            // if ( f_debug ) f_rr.setSample(x, y, B, 0);
            // if ( f_debug ) clearNeighbors(x,y);
        }
    }


    private void clearNeighbors ( final int dx, final int dy )
    {
        final int xmax = dx + 2;
        final int ymax = dy + 2;

        for ( int b = 0; b < 3; ++b )
        {
            for ( int x = (dx - 1); x < xmax; ++x )
            {
                f_rr.setSample(x, dy - 1, b, 0);
                f_rr.setSample(x, dy + 1, b, 0);
            }
            f_rr.setSample(dx - 1, dy, b, 0);
            f_rr.setSample(dx + 1, dy, b, 0);
        }
    }


    private void debug ( int x, int y, int r, int g, int b )
    {
        System.out.println("\tBayer VNG debug @ (" + x + ", " + y + ")...");

        debugGrads();

        System.out.println("\t\tRGB: (" + r + ", " + g + ", " + b + ")");
    }


    private void debugGrads ()
    {
        System.out.println("\t\t-- threshold: " + f_t);

        System.out.print("\t\t## gradients:");
        for ( int i = 0; i < GRAD_COUNT; ++i )
              System.out.print(" " + GRAD_NAMES[i] + "(" + f_grads[i] + ")");
        System.out.println();

        System.out.print("\t\t$$ selected gradients:");
        for ( int i = 0; i < GRAD_COUNT; ++i )
            if ( f_grads[i] < f_t )
                System.out.print(" " + GRAD_NAMES[i]);
        System.out.println();
    }


    private void computeThreshold ()
    {
        int max  = 0;
        int min  = 0x10000;
        int v    = 0;
        int mask = 0;

        for ( int i = 0; i < GRAD_COUNT; ++i )
        {
            v = f_grads[i];

            if ( v > max )
                max = v;
            if ( v < min )
                min = v;
        }

        f_t = min + (min >> 1) + ((max - min) >> 1);

        if ( f_t == 0 )
            ++f_t;
    }


    private void computeCardinalGradients ( int[] a )
    {
        final int ns =
              Math.abs(a[7] - a[17]) +        // center
              ((Math.abs(a[6] - a[16]) +        // left
                Math.abs(a[8] - a[18])) >> 1);    // right

        f_grads[N] =
              ns +
              Math.abs(a[2] - a[12]) +        // center
              ((Math.abs(a[1] - a[11]) +        // left
                Math.abs(a[3] - a[13])) >> 1);    // right

        f_grads[S] =
              ns +
              Math.abs(a[22] - a[12]) +        // center
              ((Math.abs(a[23] - a[13]) +        // left
                Math.abs(a[21] - a[11])) >> 1);    // right

        final int we =
              Math.abs(a[13] - a[11]) +        // center
              ((Math.abs(a[8] - a[6]) +        // top
                Math.abs(a[18] - a[16])) >> 1);    // bottom

        f_grads[W] =
              we +
              Math.abs(a[10] - a[12]) +        // center
              ((Math.abs(a[15] - a[17]) +        // top
                Math.abs(a[5] - a[7])) >> 1);    // bottom

        f_grads[E] =
              we +
              Math.abs(a[14] - a[12]) +        // center
              ((Math.abs(a[9] - a[7]) +        // top
                Math.abs(a[19] - a[17])) >> 1);    // bottom
    }


    private void computeChromaGradients ( int[] a )
    {
        final int news =
              Math.abs(a[16] - a[8]) +        // center
              ((Math.abs(a[7] - a[11]) +        // above
                Math.abs(a[13] - a[17])) >> 1);    // below

        f_grads[NE] =
              news +
              Math.abs(a[4] - a[12]) +        // center
              ((Math.abs(a[3] - a[7]) +        // left
                Math.abs(a[9] - a[13])) >> 1);    // right

        f_grads[SW] =
              news +
              Math.abs(a[20] - a[12]) +        // center
              ((Math.abs(a[21] - a[16]) +        // left
                Math.abs(a[15] - a[11])) >> 1);    // right

        final int sewn =
              Math.abs(a[18] - a[6]) +        // center
              ((Math.abs(a[13] - a[7]) +        // top
                Math.abs(a[17] - a[11])) >> 1);    // bottom

        f_grads[SE] =
              sewn +
              Math.abs(a[24] - a[12]) +        // center
              ((Math.abs(a[19] - a[13]) +        // top
                Math.abs(a[23] - a[17])) >> 1);    // bottom

        f_grads[NW] =
              sewn +
              Math.abs(a[0] - a[12]) +        // center
              ((Math.abs(a[5] - a[11]) +        // top
                Math.abs(a[1] - a[7])) >> 1);    // bottom
    }
    private void computeGreenGradients ( int[] a )
    {
        final int news =
              Math.abs(a[8] - a[16]);        // center

        f_grads[NE] =
              news +
              (Math.abs(a[4] - a[12]) +        // center
               Math.abs(a[3] - a[11]) +        // left
               Math.abs(a[9] - a[17]));        // right

        f_grads[SW] =
              news +
              (Math.abs(a[20] - a[12]) +        // center
               Math.abs(a[21] - a[13]) +        // left
               Math.abs(a[15] - a[7]));        // right

        final int sewn =
              Math.abs(a[18] - a[6]);        // center

        f_grads[SE] =
              sewn +
              (Math.abs(a[24] - a[12]) +        // center
               Math.abs(a[19] - a[7]) +        // top
               Math.abs(a[23] - a[11]));        // bottom

        f_grads[NW] =
              sewn +
              (Math.abs(a[0] - a[12]) +        // center
               Math.abs(a[5] - a[17]) +        // top
               Math.abs(a[1] - a[13]));        // bottom
    }


    /* **************************************************
     *
     * Driver
     *
     */
    public static void main ( String[] args )
    {
        RawRaster[][] rra = com.crinqle.dlroom.test.InterpProf.createImages(args);

        RawRaster rrR = rra[0][0];
        RawRaster rrG = rra[0][1];
        RawRaster rrH = rra[1][0];
        RawRaster rrB = rra[1][1];

        System.err.println("  Creating viewable images...");

        ColorPanel cpR = new ColorPanel();
        cpR.setRawRaster(rrR);
        ColorPanel cpG = new ColorPanel();
        cpG.setRawRaster(rrG);
        ColorPanel cpH = new ColorPanel();
        cpH.setRawRaster(rrH);
        ColorPanel cpB = new ColorPanel();
        cpB.setRawRaster(rrB);

        javax.swing.JScrollPane scrollerR = new javax.swing.JScrollPane(cpR);
        javax.swing.JScrollPane scrollerG = new javax.swing.JScrollPane(cpG);
        javax.swing.JScrollPane scrollerH = new javax.swing.JScrollPane(cpH);
        javax.swing.JScrollPane scrollerB = new javax.swing.JScrollPane(cpB);

        javax.swing.JViewport vpR = scrollerR.getViewport();
        javax.swing.JViewport vpG = scrollerG.getViewport();
        javax.swing.JViewport vpH = scrollerH.getViewport();
        javax.swing.JViewport vpB = scrollerB.getViewport();

        BayerVNGFrame frame = new BayerVNGFrame();

        frame.registerViewport(vpR);
        frame.registerViewport(vpG);
        frame.registerViewport(vpH);
        frame.registerViewport(vpB);

        javax.swing.JPanel holder = new javax.swing.JPanel();
        holder.setLayout(new java.awt.GridLayout(2, 2));
        holder.add(scrollerR);
        holder.add(scrollerG);
        holder.add(scrollerH);
        holder.add(scrollerB);

        frame.setContentPane(holder);
        frame.pack();
        frame.setVisible(true);
    }
}


class BayerVNGFrame extends javax.swing.JFrame implements javax.swing.event.ChangeListener
{
    private java.util.List<javax.swing.JViewport> f_list = new java.util.LinkedList<javax.swing.JViewport>();


    BayerVNGFrame ()
    {
        super("VNG Test Driver");
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }


    void registerViewport ( javax.swing.JViewport viewport )
    {
        viewport.addChangeListener(this);
        f_list.add(viewport);
    }


    public void stateChanged ( javax.swing.event.ChangeEvent evt )
    {
        Object obj = evt.getSource();

        javax.swing.JViewport viewport = null;

        if ( obj instanceof javax.swing.JViewport )
            viewport = (javax.swing.JViewport)obj;
        else
            return;

        java.awt.Point point = viewport.getViewPosition();

        for ( javax.swing.JViewport vp : f_list )
        {
            java.awt.Point p = vp.getViewPosition();

            if ( p.y != point.y || p.x != point.x )
                vp.setViewPosition(point);
        }
    }
}
