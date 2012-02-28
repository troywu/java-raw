package com.crinqle.dlroom.cfa;

import com.crinqle.dlroom.*;


/**
 * This class creates BandIterator objects.  ColorFilterArray uses the
 * filter spec to design the BandIterator object; there is a tacit
 * assumption being made about how the Bayers are setup (i.e., that:
 *
 *   "all filter patterns can be described by a repeating pattern of
 *   eight rows and two columns."
 *
 *	--Dave Coffin (dcraw.c)
 *
 * So long as that assumption holds up, this class will work
 * correctly; if not, the vars f_x1 through f_x<n> will have to be
 * turned into an array.  Until Bayers become that much more complex,
 * this implementation will suffice.  Of course, even if CFAs -do-
 * change, this interface will remain the same (and just slow down a
 * bit).
 *
 * What's very, -VERY- important about this class is that it makes
 * very precarious assumptions about the type of ImageData object.  It
 * can really only work with single-band capture data (raw CMOS or CCD
 * data).  That's because the raw data mixes all the colors into the
 * same band in the image data.  It's the offset and strides that
 * matter here; not the target band.  A multi-band iterator will need
 * to know the target band, too.  In fact, a multi-band iterator may
 * not be necessary (it's not clear that this makes sense...).
 *
 * If it does make sense, a separate class (perhaps
 * BayerMultiBandIterator) needs to be build to work with the
 * pre-interpolated multi-band capture data--which ideally is in RGB,
 * colorspace aside.  I'm not sure how to deal with 4-color images
 * yet, even the 4-color RGB images from some cameras.  More
 * investigation is necessary for 4-color images.
 *
 *	-- 9 March 2004 --
 *
 * This class will only work with Bayer CFAs.
 *
 * NOTE!
 *
 *	R: red
 *	G: green with R to right and left
 *	H: green with B to right and left
 *	B: blue
 */
class BayerBandIterator implements BandIterator
{
	private final int f_x1;
	private final int f_y1;
	private final int f_x2;
	private final int f_y2;
	private final int f_xs;
	private final int f_ys;

	protected ImageData f_id;
	protected final int f_r;
	protected final int f_w;
	protected final int f_h;
	protected final int f_total;
	protected final int f_wr;
	protected final int f_hr;
	protected int f_x = 0;
	protected int f_y = 0;

	protected int f_band = -1;


	/*
	 * This will flip between 0 and 1 to give the index
	 * into the <f_dx> array.
	 */
	private int f_rmod = 0;

	/*
	 * This only works because there are only 2 possible values.
	 * See above about why.
	 */
	private int[] f_dx = new int[2];


	BayerBandIterator ( ImageData id, int x1, int y1, int x2, int y2, int xstride, int ystride )
	{
		f_id = id;
		f_r = 0;
		f_wr = f_w = f_id.getWidth();
		f_hr = f_h = f_id.getHeight();
		f_total = f_w * f_h;
		f_x1 = x1;
		f_y1 = y1;
		f_x2 = x2;
		f_y2 = y2;
		f_xs = xstride;
		f_ys = ystride;

		/*
		 * Duh.  Not much of a computation here.
		 */
		f_dx[0] = x1;
		f_dx[1] = x2;

		f_x = x1 - f_xs;
		f_y = y1;

		// System.out.println("BayerBandIterator: init dim (" + f_w + ", " + f_h + ")");
		// System.out.println("BayerBandIterator: init @ (" + f_x + ", " + f_y + ")");
	}
	protected BayerBandIterator ( ImageData id, int x, int y, int radius )
	{
		if ( radius <= 1 )
			throw new RuntimeException("Umm--use the other ctor, dude.");

		f_id = id;
		f_r = radius;
		f_w = f_id.getWidth();
		f_h = f_id.getHeight();
		f_total = f_w * f_h;
		f_wr = f_w - f_r;
		f_hr = f_h - f_r;
		f_xs = 2;
		f_ys = 2;

		/*
		 * Compute the actual initial cursor position moving
		 * 'stride' CFA positions at a time.
		 */
		while ( x < f_r )
			x += f_xs;
		while ( y < f_r )
			y += f_ys;

		f_x1 = x;
		f_y1 = y;
		f_x2 = -1;
		f_y2 = -1;

		/*
		 * Duh.  Not much of a computation here.
		 */
		f_dx[0] = f_x1;
		f_dx[1] = f_x1;

		f_x = f_x1 - f_xs;
		f_y = f_y1;

		// System.out.println("--");
		// System.out.println("BayerBandIterator(radius): init actual dim (" + f_w + ", " + f_h + ")");
		// System.out.println("BayerBandIterator(radius): init effective dim (" + f_wr + ", " + f_hr + ")");
		// System.out.println("BayerBandIterator(radius): init @ (" + f_x + ", " + f_y + ")");
	}


	public boolean next()
	{
		f_x += f_xs;
		if ( f_x >= f_wr )
		{
			f_y += f_ys;
			f_x = f_dx[f_rmod ^= 1];
		}

		if ( f_y >= f_hr )
			return false;
		return true;
	}
	public boolean prev()
	{
		f_x -= f_xs;
		if ( f_x < f_r )
		{
			f_y -= f_ys;
			f_x = f_dx[f_rmod ^= 1];
		}

		if ( f_y < f_r )
			return false;
		return true;
	}


	public int get() { return f_id.getElem(index()); }
	public int index() { return f_x + f_y * f_w; }
	public int x() { return f_x; }
	public int y() { return f_y; }
	public void set ( int val ) { f_id.setElem(index(), val); }
}
