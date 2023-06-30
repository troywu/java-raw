package com.crinqle.dlroom;


import java.awt.color.*;
import java.awt.image.*;

import static com.crinqle.dlroom.Const.*;



/**
 * This class holds images with 16-bits per [band|sample|color|plane].
 * I'm using this class instead of BufferedImage or WritableRaster for
 * speed considerations in the color management workflow.
 * <p>
 * This class contains the raw data in an image, as well as the color
 * space profile for that image.  The color space profile is
 * maintained as a copy; accessors also retrieve a copy.
 *
 * <hr>
 * <p>
 * In the following discussion, column and row indicate pixels and
 * their color components, not pixels in a raster with width and
 * height.  This class really has no idea how the data is being used
 * -except- for the color management module, which is very closely
 * coupled with the implementation details.
 * <p>
 * The color-banded implementation of this class is ideally suited for
 * the AMD-optimized BLAS routines in the ACML libraries.  The
 * routines expect a column-major implementation of 2-dimensional
 * arrays (using 1-D access semantics: array[row + column * nRows]).
 * <p>
 * In the case of RGB, multiplying the (__nPixels__ X 3) matrix with
 * the (3 X __3__) matrix of the display transform in the ICC profile
 * will yield a (__nPixels__ X __3__) matrix containing the pixels
 * that have been transformed from the image color space into the
 * display's color space.
 */
public class ImageData extends DataBuffer
{
    private static final int         TYPE      = DataBuffer.TYPE_INT;
    protected final      int         f_w;
    protected final      int         f_h;
    protected final      int         f_bits;
    protected final      int         f_each;
    protected final      int         f_total;
    protected            int[]       f_a;
    protected            ICC_Profile f_profile = null;


    public static void println ( ImageData id )
    {
        final int   w     = id.f_w;
        final int   h     = id.f_h;
        final int   each  = id.f_each;
        final int   banks = id.getNumBanks();
        final int[] a     = id.f_a;

        for ( int bank = 0; bank < banks; ++bank )
        {
            final int offset = bank * each;

            System.out.print("\t-- Bank " + CHANNEL_NAME[bank] + "--\n  ");
            for ( int y = 0; y < h; ++y )
            {
                for ( int x = 0; x < w; ++x )
                      System.out.print(a[offset + x + y * w] + "\t");

                System.out.print("\n\n  ");
            }
        }
    }


    public ImageData ( int width, int height, int bits )
    {
        super(TYPE, width * height);
        f_w     = width;
        f_h     = height;
        f_bits  = bits;
        f_total = f_each = width * height;
        f_a     = new int[f_total];
    }
    public ImageData ( int width, int height, int numBanks, int bits )
    {
        super(TYPE, width * height, numBanks);
        f_w     = width;
        f_h     = height;
        f_bits  = bits;
        f_each  = width * height;
        f_total = f_each * numBanks;
        f_a     = new int[f_total];
    }
    public ImageData ( int width, int height, short[] data, int numBanks, int bits )
    {
        super(TYPE, width * height, numBanks);

        if ( data.length != size )
            throw new RuntimeException("Umm...We have a data size mismatch.");

        f_w     = width;
        f_h     = height;
        f_bits  = bits;
        f_each  = width * height;
        f_total = f_each * numBanks;

        f_a = new int[f_total];
        for ( int i = 0; i < f_total; ++i )
              f_a[i] = data[i];
    }
    protected ImageData ( ImageData id )
    {
        super(TYPE, id.f_total, id.getNumBanks());
        f_w     = id.f_w;
        f_h     = id.f_h;
        f_bits  = id.f_bits;
        f_each  = id.f_each;
        f_total = id.f_total;

        f_a = new int[f_total];

        if ( id.f_profile != null )
            f_profile = id.getProfile();
    }


    public int getHeight () { return f_h; }
    public int getWidth () { return f_w; }
    public int getBits () { return f_bits; }
    public int getBandCount () { return this.getNumBanks(); }

    public ICC_Profile getProfile () { return ICC_Profile.getInstance(f_profile.getData()); }
    public void setProfile ( ICC_Profile profile ) { f_profile = ICC_Profile.getInstance(profile.getData()); }

    public void zero () { for ( int i = 0; i < f_total; ++i ) f_a[i] = 0; }

    public int getSample ( int x, int y ) { return f_a[x + y * f_w]; }
    public int getSample ( int x, int y, int band ) { return f_a[(band * f_each) + (x + y * f_w)]; }
    public void setSample ( int x, int y, int val ) { f_a[x + y * f_w] = val; }
    public void setSample ( int x, int y, int band, int val ) { f_a[(band * f_each) + (x + y * f_w)] = val; }


    public ImageData subset ( int x, int y )
    {
        final int w = f_w - x;
        final int h = f_h - y;

        return subset(x, y, w, h);
    }
    public ImageData subset ( int x, int y, int width, int height )
    {
        final int banks = getNumBanks();

        ImageData id = new ImageData(width, height, banks, f_bits);

        int bo  = -f_each;
        int tbo = -id.f_each;

        for ( int bank = 0; bank < banks; ++bank )
        {
            bo += f_each;
            tbo += id.f_each;

            int offset = bo + (y * f_w) + (x - f_w);
            int target = tbo - width;

            for ( int row = 0; row < height; ++row )
            {
                offset += f_w;
                target += width;

                System.arraycopy(f_a, offset, id.f_a, target, width);
            }
        }

        return id;
    }
	/*
	public ImageData subset ( int x, int y )
	{
		final int w = f_w - x;
		final int h = f_h - y;
		final int banks = getNumBanks();

		ImageData id = new ImageData(w, h, banks, f_bits);

		int bo = -f_each;
		int tbo = -id.f_each;

		for ( int bank = 0; bank < banks; ++bank )
		{
			bo += f_each;
			tbo += id.f_each;

			int offset = bo + (y * f_w) + (x - f_w);
			int target = tbo - w;

			for ( int row = 0; row < h; ++row )
			{
				offset += f_w;
				target += w;

				System.arraycopy(f_a, offset, id.f_a, target, w);
			}
		}

		return id;
	}
	*/

    public int[] getData () { return f_a; }
    public void setData ( int[] array )
    {
        final int l = array.length;
        if ( l != f_total )
            throw new RuntimeException("Umm...setting array with the wrong number of elements!");

        System.arraycopy(array, 0, f_a, 0, l);
    }

    /*
     * **************************************************
     *
     * INTERFACE
     *
     * **************************************************
     */
    public int getElem ( int bank, int i ) { return f_a[bank * size + i]; }
    public void setElem ( int bank, int i, int val ) { f_a[bank * size + i] = val; }
}
