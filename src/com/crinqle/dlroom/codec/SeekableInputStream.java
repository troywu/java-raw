package com.crinqle.dlroom.codec;


import java.io.*;

import com.crinqle.dlroom.util.*;



public abstract class SeekableInputStream implements Seekable
{
    protected final short[] f_a;
    protected final int     f_len;
    protected       int     f_i = 0;

    protected int f_have = 8;


    protected SeekableInputStream ( final byte[] array )
    {
        f_len = array.length;

        f_a = new short[f_len];

        for ( int i = 0; i < f_len; ++i )
              f_a[i] = (short)((array[i] < 0) ? 256 + array[i] : array[i]);
    }


    public static SeekableInputStream getInstance ( final int order, final byte[] array )
    {
        SeekableInputStream s = null;

        if ( order == 0x4949 )
            s = new LSBSeekableInputStream(array);
        else
            s = new MSBSeekableInputStream(array);

        return s;
    }


    public long getPosition () throws IOException
    {
        return f_i;
    }


    public abstract int read4 () throws IOException;
    public abstract int read2 () throws IOException;


    public int bits ( int need ) throws Exception
    {
        final int _need_save = need;
        int       stupid     = 0;

        if ( need > 32 )
            throw new Exception("Blow me.  You can't have that many bits.");

        if ( need == 0 )
            return 0;

        if ( f_i >= this.f_len )
        {
            int ret = 0;
            for ( int i = 0; i < need; ++i )
                  ret = (ret << 1) | 1;
            return ret;
        }

        int value = f_cache();

        if ( need <= f_have )
        {
            if ( need == f_have )
            {
                // if ( f_resetCache(need) )
                // ++stupid;

                f_resetCache(need);
            }


            else
            {
                value >>= (f_have - need);
                f_have -= need;
            }
        }
        else
        {
            /*
             * Here, we're going to use up all the cache.
             * So, we want to advance the cache pointer, and
             * reset our cache count to zero.
             */
            need -= f_have;

            // if ( f_resetCache(need) )
            // ++stupid;

            f_resetCache(need);

            while ( need > 0 )
            {
                if ( need < 8 )
                {
                    value <<= need;

                    /*
                     * Load cache.
                     * Only use (8 - need) bits.
                     * Don't reset cache; change the cache count.
                     */
                    value |= (f_cache() >> (8 - need));

                    f_have -= need;
                }
                else
                {
                    /*
                     * Load cache.  Use all bits.
                     * Reset cache.
                     */
                    value = (value << 8) | f_cache();

                    // if ( f_resetCache(need) )
                    // ++stupid;

                    f_resetCache(need);
                }

                need -= 8;
            }
        }

        // System.err.println("<FUCK!> need: (" + _need_save + ") ret: (" + value + ") stupid: (" + stupid + ")");
        // System.err.println("<FUCK!> need: (" + _need_save + ") ret: (" + value + ")");

        return value;
    }


    /*
     * God damn, this is retarded.
     *
     * In Canon compressed data, 0xff is always followed by 0x00;
     */
    private boolean f_resetCache ( final int need )
    {
        // boolean stupid = false;

        if ( f_a[f_i] == 0xff )
            ++f_i;

		/*
		 * -- BEGIN Stupidity --
		if ( f_a[f_i] == 0xff )
		{
			++f_i;

			stupid = true;
		}
		 * -- END Stupidity --
		 */

        ++f_i;
        f_have = 8;

        return false;
    }


    private int f_cache ()
    {
        final int v = f_a[f_i];

        if ( f_have == 8 )
            return v;

        final int shift = 8 - f_have;

        return (((v << shift) & 0x000000ff) >> shift);
    }


    public long seek ( long n )
    {
        f_i = 0;

        return skip(n);
    }


    public int skipBytes ( int n )
    {
        return (int)skip(n);
    }


    /*
     * --------------------------------------------------
     *
     * INTERFACE methods
     *
     * --------------------------------------------------
     */


    public int available () { return f_len; }
    public void close () {}
    public void mark ( int readlimit ) {}
    public boolean markSupported () { return false; }
    public void reset () {}


    public long skip ( long n )
    {
        if ( (f_i + n) > f_len )
            n = f_len - f_i;

        f_i += n;

        return n;
    }


    /*
     * --------------------------------------------------
     *
     * Reading methods
     *
     * --------------------------------------------------
     */


    /**
     * This method returns an *UNSIGNED* byte.  This correctness
     * is ensured by the ctor().
     */
    public int read () { return f_a[f_i++]; }


    public int read ( byte[] b )
    {
        return read(b, 0, b.length);
    }


    public int read ( byte[] b, int offset, int length )
    {
        if ( length <= 0 ) return 0;

        if ( length > b.length )
            length = b.length;

        if ( (f_i + length) > f_len )
            length = f_len - f_i;

        for ( int i = 0; i < length; ++i )
              b[i] = (byte)f_a[f_i + i];

        f_i += length;

        return length;
    }


    public int readFully ( byte[] b, int offset, int length )
    {
        return read(b, offset, length);
    }


    /*
     * Test driver.
     */
    public static void main ( String[] args )
    {
        long n = 0;

        n |= 5;

        n <<= 5;
        n |= 31;

        n <<= 8;
        n |= 10;

        n <<= 10;
        n |= 819;

        n <<= 8;
        n |= 7;

        n <<= 7;
        n |= 69;

        /* decimal (69) -- 7 bits */

        final int bitlength = 8 + 5 + 8 + 10 + 8 + 7;
        final int shift     = 64 - bitlength;

        n <<= shift;

        System.out.println("\n" + n + ": " + Long.toBinaryString(n) + "\n");
        System.out.println("\n" + n + ": " + Binary.toBinary(n) + "\n");

        int v = 0;

        char c1 = (char)((n >> 56) & 0x000000ff);
        char c2 = (char)((n >> 48) & 0x000000ff);
        char c3 = (char)((n >> 40) & 0x000000ff);
        char c4 = (char)((n >> 32) & 0x000000ff);
        char c5 = (char)((n >> 24) & 0x000000ff);
        char c6 = (char)((n >> 16) & 0x000000ff);
        char c7 = (char)((n >> 8) & 0x000000ff);
        char c8 = (char)((n >> 0) & 0x000000ff);

        byte[] array = new byte[] {(byte)c1, (byte)c2, (byte)c3, (byte)c4, (byte)c5, (byte)c6, (byte)c7, (byte)c8};

        for ( int i = 0; i < 8; ++i )
              System.out.println("array[" + i + "]: " + Binary.toBinary(array[i]));
        System.out.println();

        MSBSeekableInputStream s = new MSBSeekableInputStream(array);

        try
        {
            System.out.println("zeros (4): " + s.bits(4));

            int b = s.bits(4);

            System.out.println("bits (4): " + b);
            System.out.println("value (" + b + "): " + s.bits(b));


            System.out.println("zeros (4): " + s.bits(4));

            b = s.bits(4);

            System.out.println("bits (4): " + b);
            System.out.println("value (" + b + "): " + s.bits(b));


            System.out.println("zeros (4): " + s.bits(4));

            b = s.bits(4);

            System.out.println("bits (4): " + b);
            System.out.println("value (" + b + "): " + s.bits(b));
        }
        catch ( Exception e ) { System.err.println("Cannot get bits: " + e.getMessage()); System.exit(1); }
    }
}


class LSBSeekableInputStream extends SeekableInputStream
{
    LSBSeekableInputStream ( byte[] array )
    {
        super(array);
    }


    public int read2 () throws IOException
    {
        // return ((((short)f_a[f_i++]) << 8) + f_a[f_i++]);

        // return (short)((((short)f_a[f_i++])) | (((short)f_a[f_i++]) << 8));

        // return (f_a[f_i++] | (f_a[f_i++] << 8));

        return (read() | (read() << 8));
    }


    public int read4 () throws IOException
    {
        // return ((((int)f_a[f_i++]) << 24) + (((int)f_a[f_i++]) << 16) + (((int)f_a[f_i++]) << 8) + f_a[f_i++]);

        // return ((((int)f_a[f_i++])) | (((int)f_a[f_i++]) << 8) | (((int)f_a[f_i++]) << 16) | (((int)f_a[f_i++]) << 24));

        // return ((f_a[f_i++]) | (f_a[f_i++] << 8) | (f_a[f_i++] << 16) | (f_a[f_i++] << 24));

        return (read() | (read() << 8) | (read() << 16) | (read() << 24));
    }
}


class MSBSeekableInputStream extends SeekableInputStream
{
    MSBSeekableInputStream ( byte[] array )
    {
        super(array);
    }


    public int read2 () throws IOException
    {
        // return ((((short)f_a[f_i++]) << 8) | f_a[f_i++]);

        // return (short)((((short)f_a[f_i++])) | (((short)f_a[f_i++]) << 8));

        // return ((f_a[f_i++] << 8) | (f_a[f_i++]));

        return ((read() << 8) | (read()));
    }


    public int read4 () throws IOException
    {
        // return ((((int)f_a[f_i++]) << 24) | (((int)f_a[f_i++]) << 16) | (((int)f_a[f_i++]) << 8) | f_a[f_i++]);

        // return ((((int)f_a[f_i++]) >> 24) | (((int)f_a[f_i++]) << 16) | (((int)f_a[f_i++]) << 8) | (((int)f_a[f_i++])));

        // return ((f_a[f_i++] << 24) | (f_a[f_i++] << 16) | (f_a[f_i++] << 8) | (f_a[f_i++]));

        return ((read() << 24) | (read() << 16) | (read() << 8) | (read()));
    }
}
