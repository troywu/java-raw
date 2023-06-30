package com.crinqle.dlroom;


public class LUT
{
    private final int     f_chan;
    private final int     f_max;
    private final int     f_l;
    private       float[] f_a;
    private       int[]   f_ia;


    /**
     * @param channel   Channel to adjust
     * @param array     Values in LUT
     * @param targetMax Clamping value & scaling factor (to bring LUT values into target range)
     */
    public LUT ( int channel, float[] array, int targetMax )
    {
        f_chan = channel;
        f_max  = targetMax;
        f_l    = array.length;
        f_a    = array;

        f_ia = new int[f_l];
        int val;

        for ( int i = 0; i < f_l; ++i )
        {
            val = Math.round(f_a[i]); // (f_max * f_a[i]) / max);

            if ( val > f_max )
                f_ia[i] = f_max;
            else if ( val < 0 )
                f_ia[i] = 0;
            else
                f_ia[i] = val;
        }
    }
    /**
     * @param channel   Channel to adjust
     * @param array     Values in LUT
     * @param max       Normalizing factor (to bring LUT values between 0 and 1)
     * @param targetMax Clamping value & scaling factor (to bring LUT values into target range)
     */
    public LUT ( int channel, float[] array, float max, int targetMax )
    {
        f_chan = channel;
        f_max  = targetMax;
        f_l    = array.length;
        f_a    = array;

        f_ia = new int[f_l];
        int val;

        for ( int i = 0; i < f_l; ++i )
        {
            val = Math.round((f_max * f_a[i]) / max);

            if ( val > f_max )
                f_ia[i] = f_max;
            else if ( val < 0 )
                f_ia[i] = 0;
            else
                f_ia[i] = val;
        }
    }


    public int channel () { return f_chan; }
    public int[] array () { return f_ia; }


    public String toString ()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("LUT:");
        for ( int i = 0; i < f_ia.length; ++i )
              buffer.append(" " + f_ia[i]);

        return buffer.toString();
    }


	/*
	public void reverse()
	{
		int[] ia = new int[f_l];
		int k = 0;

		for ( int i = f_l-1; i >= 0; --i )
			ia[i] = f_ia[k++];

		f_ia = ia;
	}
	*/
}
