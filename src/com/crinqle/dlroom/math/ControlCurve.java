package com.crinqle.dlroom.math;


import java.awt.*;

import com.crinqle.dlroom.*;
import com.crinqle.dlroom.event.*;



/**
 * This class represents a curve defined by a sequence of control
 * points
 */
public abstract class ControlCurve implements BitDepthChangeListener
{
    static final int EPSILON = 225;  /* square of distance for picking */

    private final int f_chan;

    protected final int f_dim;

    private   int     f_bits;
    protected int     f_max;
    protected int     f_scale;
    protected float[] f_range;
    protected Polygon pts;
    protected int     selection = -1;


    public ControlCurve ( int channel, int dim, int bits )
    {
        f_chan  = channel;
        f_dim   = dim;
        f_bits  = bits;
        f_max   = 1 << f_bits;
        f_scale = f_max / f_dim;

        f_range = new float[f_max];

        pts = new Polygon();

        pts.addPoint(0, f_dim - 1);
        pts.addPoint(f_dim - 1, 0);
    }


    public LUT getLUT () { LUT lut = new LUT(f_chan, f_range, (float)f_dim, f_max - 1); return lut; }


    public void updateBits ( Object source, int bits ) { f_bits = bits; f_max = 1 << f_bits; f_scale = f_max / f_dim; f_range = new float[f_max]; }


    /**
     * paint this curve into g.
     */
    public void paint ( Graphics g )
    {
        int[] xs = pts.xpoints;
        int[] ys = pts.ypoints;
        int   n  = pts.npoints;

        if ( xs[0] != 0 )
            g.drawLine(xs[0], ys[0], 0, ys[0]);
        if ( xs[n - 1] != (f_dim - 1) )
            g.drawLine(xs[n - 1], ys[n - 1], f_dim - 1, ys[n - 1]);

        for ( int i = 0; i < n; i++ )
        {
            final int x = xs[i];
            final int y = ys[i];

            g.fillRect(x - 2, y - 2, 5, 5);
        }
    }


    /**
     * return index of control point near to (x,y) or -1 if
     * nothing near
     */
    public int selectPoint ( int x, int y )
    {
        int mind = Integer.MAX_VALUE;
        selection = -1;
        for ( int i = 0; i < pts.npoints; i++ )
        {
            int d = sqr(pts.xpoints[i] - x) + sqr(pts.ypoints[i] - y);
            if ( d < mind && d < EPSILON )
            {
                mind      = d;
                selection = i;
            }
        }
        return selection;
    }


    /**
     * square of an int
     */
    static int sqr ( int x ) { return x * x; }


    /**
     * add a control point, return index of new control point
     */
    public int addPoint ( int x, int y )
    {
        final int n  = pts.npoints;
        int[]     xs = pts.xpoints;
        int[]     ys = pts.ypoints;

        pts.addPoint(xs[n - 1], ys[n - 1]); // f_dim-1, 0);
        pts.xpoints[n - 1] = x;
        pts.ypoints[n - 1] = y;

        pts = sortPoints();

        return selectPoint(x, y);
    }


    /**
     * set selected control point
     */
    public void setPoint ( int x, int y )
    {
        if ( selection >= 0 )
        {
            pts.xpoints[selection] = x;
            pts.ypoints[selection] = y;
        }
    }


    /**
     * Sort control points.
     */
    protected Polygon sortPoints ()
    {
        Polygon p = new Polygon(pts.xpoints, pts.ypoints, pts.npoints);

        int[] xs = p.xpoints;
        int[] ys = p.ypoints;

        final int n = p.npoints;
        int       temp;

        for ( int j = 0; j < n; ++j )
            for ( int i = (j + 1); i < n; ++i )
            {
                if ( xs[j] > xs[i] )
                {
                    temp  = xs[i];
                    xs[i] = xs[j];
                    xs[j] = temp;

                    temp  = ys[i];
                    ys[i] = ys[j];
                    ys[j] = temp;
                }
            }

        return p;
    }


    /**
     * remove selected control point
     */
    public void removePoint ()
    {
        if ( selection >= 0 )
        {
            pts.npoints--;
            for ( int i = selection; i < pts.npoints; i++ )
            {
                pts.xpoints[i] = pts.xpoints[i + 1];
                pts.ypoints[i] = pts.ypoints[i + 1];
            }
        }
    }


    public String toString ()
    {
        StringBuffer result = new StringBuffer();
        for ( int i = 0; i < pts.npoints; i++ )
              result.append(" " + pts.xpoints[i] + " " + pts.ypoints[i]);
        return result.toString();
    }
}
