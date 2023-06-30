package com.crinqle.dlroom.math;


import javax.swing.JFrame;

import com.crinqle.dlroom.Const;
import com.crinqle.dlroom.CurvePanel;



public class SplineFrame extends JFrame
{
    public SplineFrame ()
    {
        super("Spline, Baby!");

        int channel = Const.R;
        int dim     = 1;
        int bits    = 16;

        NatCubic f_c = new NatCubic(channel, dim, bits);

        final int x0 = 10;
        final int y0 = 190;

        final int xf = 190;
        final int yf = 10;

        final int x1 = 75;
        final int y1 = 75;

        f_c.addPoint(x0, y0);
        f_c.addPoint(x1, y1);
        f_c.addPoint(xf, yf);

        CurvePanel panel = new CurvePanel(f_c);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
    }

    public static void main ( String[] args )
    {
        SplineFrame frame = new SplineFrame();

        frame.pack();
        frame.setVisible(true);
    }
}
