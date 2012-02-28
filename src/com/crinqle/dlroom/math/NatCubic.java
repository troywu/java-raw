package com.crinqle.dlroom.math;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;


public class NatCubic extends ControlCurve
{
	final int STEPS = 100;


	public NatCubic ( int channel, int dim, int bits ) { super(channel, dim, bits); }


	/**
	 * calculates the natural cubic spline that interpolates
	 * y[0], y[1], ... y[n]
	 * The first segment is returned as
	 * C[0].a + C[0].b*u + C[0].c*u^2 + C[0].d*u^3 0<=u <1
	 * the other segments are in C[1], C[2], ...  C[n-1]
	 *
	 * We solve the equation

	 [2 1       ] [D[0]]   [3(x[1] - x[0])  ]
	 |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
	 |  1 4 1   | | .  | = |      .         |
	 |    ..... | | .  |   |      .         |
	 |     1 4 1| | .  |   |3(x[n] - x[n-2])|
	 [       1 2] [D[n]]   [3(x[n] - x[n-1])]
	 
	 * by using row operations to convert the matrix to upper triangular
	 * and then back substitution.  The D[i] are the derivatives at the knots.
	 */
	Cubic[] calcNaturalCubic(int n, int[] x)
	{
		float[] gamma = new float[n+1];
		float[] delta = new float[n+1];
		float[] D = new float[n+1];
		int i;
    
		gamma[0] = 1.0f/2.0f;
		for ( i = 1; i < n; i++)
			gamma[i] = 1/(4-gamma[i-1]);

		gamma[n] = 1/(2-gamma[n-1]);
    
		delta[0] = 3*(x[1]-x[0])*gamma[0];
		for ( i = 1; i < n; i++)
			delta[i] = (3*(x[i+1]-x[i-1])-delta[i-1])*gamma[i];

		delta[n] = (3*(x[n]-x[n-1])-delta[n-1])*gamma[n];
    
		D[n] = delta[n];
		for ( i = n-1; i >= 0; i--)
			D[i] = delta[i] - gamma[i]*D[i+1];


		/*
		 * now compute the coefficients of the cubics
		 */
		Cubic[] C = new Cubic[n];
		for ( i = 0; i < n; i++)
			C[i] = new Cubic((float)x[i], D[i], 3*(x[i+1] - x[i]) - 2*D[i] - D[i+1], 2*(x[i] - x[i+1]) + D[i] + D[i+1]);

		return C;
	}


	/**
	 * draw a cubic spline
	 */
	public void paint ( Graphics g )
	{
		super.paint(g);

		if (pts.npoints >= 2)
		{
			Cubic[] X = calcNaturalCubic(pts.npoints-1, pts.xpoints);
			Cubic[] Y = calcNaturalCubic(pts.npoints-1, pts.ypoints);
    
			/*
			 * very crude technique - just break each
			 * segment up into steps lines.
			 */
			Polygon p = new Polygon();

			p.addPoint(Math.round(X[0].eval(0)),
				   Math.round(Y[0].eval(0)));

			// System.out.println("How many cubics are there...?  There are " + X.length + " cubics!");

			int ri = 0;
			final float max = (float)f_dim - (float)1;
			final int total = f_scale * f_dim;
			final int startgap = f_scale * pts.xpoints[0];

			if ( pts.xpoints[0] != 0 )
				for ( ; ri < startgap; ++ri )
					f_range[ri] = (max - (float)pts.ypoints[0]);

			for (int i = 0; i < X.length; i++)
			{
				int steps = (pts.xpoints[i+1] - pts.xpoints[i]) * f_scale;

				if ( i == (X.length - 1) )
					steps += f_scale;

				// System.out.println("-----=====> steps: " + steps + " <=====-----");

				for (int j = 1; j <= steps; j++)
				{
					float u = j / (float)steps;
					final float xfp = X[i].eval(u);
					final float yfp = Y[i].eval(u);
					final int xp = Math.round(xfp);
					final int yp = Math.round(yfp);
					p.addPoint(xp, yp);

					f_range[ri++] = max - yfp;
				}
			}

			final int fi = pts.npoints - 1;
			if ( pts.xpoints[fi] != max )
				for ( ; ri < total; ++ri )
					f_range[ri] = max - (float)pts.ypoints[fi];

			g.setColor(Color.gray);
			g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
		}
	}
}
