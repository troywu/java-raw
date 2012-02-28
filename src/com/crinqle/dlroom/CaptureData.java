package com.crinqle.dlroom;

import com.crinqle.dlroom.cfa.*;


/**
 * This class holds images captured by the camera.  It's designed to
 * be a single band of intensity data.  This class should be converted
 * into a RawData object before the raw operations should be performed
 * (band separation, color interpolation).
 *
 * Since this contains only the intensity data, the information about
 * which color is above that position in the Color Filter Array must
 * be given in the ctor().

<pre>
   PowerShot 600 uses 0xe1e4e1e4:

	  0 1 2 3 4 5
	0 G M G M G M
	1 C Y C Y C Y
	2 M G M G M G
	3 C Y C Y C Y

   PowerShot A5 uses 0x1e4e1e4e:

	  0 1 2 3 4 5
	0 C Y C Y C Y
	1 G M G M G M
	2 C Y C Y C Y
	3 M G M G M G

   PowerShot A50 uses 0x1b4e4b1e:

	  0 1 2 3 4 5
	0 C Y C Y C Y
	1 M G M G M G
	2 Y C Y C Y C
	3 G M G M G M
	4 C Y C Y C Y
	5 G M G M G M
	6 Y C Y C Y C
	7 M G M G M G

   PowerShot Pro70 uses 0x1e4b4e1b:

	  0 1 2 3 4 5
	0 Y C Y C Y C
	1 M G M G M G
	2 C Y C Y C Y
	3 G M G M G M
	4 Y C Y C Y C
	5 G M G M G M
	6 C Y C Y C Y
	7 M G M G M G

   PowerShots Pro90 and G1 use 0xb4b4b4b4:

	  0 1 2 3 4 5
	0 G M G M G M
	1 Y C Y C Y C

   All RGB cameras use one of these Bayer grids:

	0x16161616:	0x61616161:	0x49494949:	0x94949494:

	  0 1 2 3 4 5	  0 1 2 3 4 5	  0 1 2 3 4 5	  0 1 2 3 4 5
	0 B G B G B G	0 G R G R G R	0 G B G B G B	0 R G R G R G
	1 G R G R G R	1 B G B G B G	1 R G R G R G	1 G B G B G B
	2 B G B G B G	2 G R G R G R	2 G B G B G B	2 R G R G R G
	3 G R G R G R	3 B G B G B G	3 R G R G R G	3 G B G B G B
</pre>
*/
public class CaptureData extends ImageData
{
	private ImageInfo f_info;
	private ColorFilterArray f_cfa;


	public CaptureData ( int width, int height, int filterSpec, int bits )
	{
		super(width, height, bits);

		f_cfa = ColorFilterArray.getInstance(filterSpec);
	}
	/*
	public CaptureData ( int width, int height, ColorFilterArray cfa, int bits )
	{
		super(width, height, bits);

		f_cfa = cfa;
	}
	*/
	public CaptureData ( ImageData id, ColorFilterArray cfa )
	{
		super(id);
		f_cfa = cfa;

		setData(id.f_a);
	}


	public ColorFilterArray getCFA() { return f_cfa; }
	public ImageInfo getImageInfo() { return f_info; }
	public void setImageInfo ( ImageInfo info ) { f_info = info; }


	public BandIterator[] bandIterators() { return f_cfa.bandIterators(this); }
	public AreaBandIterator[] areaBandIterators ( int radius ) { return f_cfa.areaBandIterators(this, radius); }


	public CaptureData subraster ( int x, int y )
	{
		return subraster(subset(x, y), x, y);
	}
	public CaptureData subraster ( int x, int y, int width, int height )
	{
		return subraster(subset(x, y, width, height), x, y);
	}
	private CaptureData subraster ( ImageData id, int x, int y ) { return new CaptureData(id, f_cfa.arrayAtOffset(x,y)); }
}
