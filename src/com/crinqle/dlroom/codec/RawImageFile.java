package com.crinqle.dlroom.codec;


import java.io.*;



/**
 * This class creates the correct endian-ness file type.
 */
abstract class RawImageFile implements Seekable
{
    protected RandomAccessFile f_rif;
    protected int              f_order = 0;


    RawImageFile ( File file ) throws IOException
    {
        f_rif = new RandomAccessFile(file, "r");
    }


    public static RawImageFile getInstance ( File file ) throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        RawImageFile     rif = null;

        final int order = raf.readShort();

        if ( order == 0x4949 )
            rif = new LSBRawImageFile(file);
        else
            rif = new MSBRawImageFile(file);

        /*
         * All raw image files need this.
         */
        rif.f_order = order;

        rif.skipBytes(2);

        return rif;
    }


    public int order () { return f_order; }


    public long getPosition () throws IOException { return f_rif.getFilePointer(); }
    public long seek ( long n ) throws IOException { f_rif.seek(n); return n; }
    public int skipBytes ( int n ) throws IOException { return f_rif.skipBytes(n); }
    public int bits ( int n ) throws Exception { throw new Exception("Yeah, right.  Talk to Sun."); }
    public int read () throws IOException { return f_rif.read(); }
    public int read ( byte[] b ) throws IOException { return f_rif.read(b); }
    public int read ( byte[] b, int offset, int length ) throws IOException { return f_rif.read(b, offset, length); }
    public int readFully ( byte[] b, int offset, int length ) throws IOException { f_rif.readFully(b, offset, length); return length; }
}


/**
 * Little-endian file (0x4949).
 * <p>
 * This is *NOT* the native interface of Java's java.io.DataInput
 * class.  So, I make the values myself.
 */
class LSBRawImageFile extends RawImageFile
{
    LSBRawImageFile ( File file ) throws IOException
    {
        super(file);
    }


    public int read2 () throws IOException
    {
        final int a = read();
        final int b = read();

        return a | (b << 8);
    }


    public int read4 () throws IOException
    {
        final int a = read();
        final int b = read();
        final int c = read();
        final int d = read();

        return a | (b << 8) | (c << 16) | (d << 24);
    }
}


/**
 * Big-endian file.
 * <p>
 * Since this is the native interface of Java's
 * java.io.DataInput class, I just return that class's values.
 */
class MSBRawImageFile extends RawImageFile
{
    MSBRawImageFile ( File file ) throws IOException
    {
        super(file);
    }


    public int read2 () throws IOException
    {
        return f_rif.readShort();

		/*
		final int a = read();
		final int b = read();

		return (a << 8) + b;
		*/
    }


    public int read4 () throws IOException
    {
        return f_rif.readInt();

		/*
		final int a = read();
		final int b = read();
		final int c = read();
		final int d = read();

		return (a << 24) + (b << 16) + (c << 8) + d;
		*/
    }
}
