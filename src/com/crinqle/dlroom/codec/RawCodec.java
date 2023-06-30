package com.crinqle.dlroom.codec;


import java.awt.color.*;
import java.io.*;
import java.util.*;

import com.crinqle.dlroom.*;



/**
 * Returns CaptureData objects.
 */
public abstract class RawCodec
{
    protected       Seekable  f_stream;
    protected final int       f_size;
    protected       ImageInfo f_info;

    private static int[] f_table      = new int[4096];
    private static int   f_tableCount = 0;


    public static int getTableCount () { return f_tableCount; }
    public static int[] getTableArray () { return f_table; }


    RawCodec ( Seekable stream, int size, ImageInfo info )
    {
        f_stream = stream;
        f_size   = size;
        f_info   = info;
    }


    public static RawCodec getInstance ( File file ) throws Exception
    {
        final int    fsize = (int)file.length();
        RawImageFile rif   = RawImageFile.getInstance(file);

        RawCodec  codecImpl = null;
        ImageInfo info      = new ImageInfo();

        info.order = rif.order();
        final int hlen = rif.read4();

        // System.out.println("order: " + info.order);
        // System.out.println("hlen: " + hlen);

        byte[] headBytes = new byte[26];

        rif.read(headBytes, 0, 26);

        final String head = new String(headBytes);

        // System.out.print("head: ");
        // for ( int i = 0; i < 8; ++i )
        // System.out.print("[" + (char)headBytes[i] + "]");
        // System.out.println();
        // System.out.println("fsize: " + fsize);

        rif.seek(0);

        /*
         * Since the Canon is identified from the order, we don't
         * need the file magic.
         *
         * However, since we'll eventually be supporting other
         * camera models, this is good to have.  Plus, we need the
         * file offset to be at the correct location!
         */
        final int magic = rif.read4();

        // System.out.println("magic: " + magic);

        if ( info.order == 0x4949 || info.order == 0x4d4d )
        {
            if ( head.startsWith("HEAPCCDR") )
            {
                sf_parseCiff(rif, info, hlen, fsize - hlen);
            }
        }

		/*
		System.out.println("make: " + info.make);
		System.out.println("model: " + info.model);
		System.out.println("camera_red: " + info.cameraRed);
		System.out.println("camera_blue: " + info.cameraBlue);
		System.out.println("raw_width: " + info.rawWidth);
		System.out.println("raw_height: " + info.rawHeight);
		System.out.println("timestamp: " + info.timestamp);
		*/

        /*
         * Are we working with Canon files?
         */
        boolean isCanon = info.make.startsWith("Canon");

        if ( info.model.equalsIgnoreCase("Canon EOS D30") )
            codecImpl = new CanonEOSD30(rif, fsize, info);

        return codecImpl;
    }


    public abstract CaptureData decode (); // ( String wsProfilePath );


    private static void sf_parseCiff ( RawImageFile file, ImageInfo info, final int offset, final int length ) throws IOException
    {
        int wbi = 0;

        /*
         * seek to: offset + (length - 4)
         * seek to: read4() + offset;
         * read2()
         */
        file.seek(offset + (length - 4));

        final int tboff = file.read4() + offset;

        System.err.println("tboff: " + tboff);

        file.seek(tboff);

        final int nrecs = file.read2();

        System.err.println("nrecs: " + nrecs);

        for ( int i = 0; i < nrecs; ++i )
        {
            final int type = file.read2();
            final int len  = file.read4();
            final int roff = file.read4();
            final int aoff = offset + roff;
            final int save = (int)file.getPosition();

            System.err.println();
            System.err.println("-- Record " + i + " --");
            System.err.println("len: " + len);
            System.err.println("roff: " + roff);
            System.err.println("aoff: " + aoff);
            System.err.println("save: " + save);

            byte[] make   = new byte[64];
            byte[] model  = new byte[64];
            byte[] model2 = new byte[64];

            for ( int n = 0; n < 64; ++n )
                  make[i] = model[i] = model[2] = 0;

            switch ( type )
            {
                case 0x080a:
                    System.err.println("type: 0x080a (make & model) <==--");

                    file.seek(aoff);

                    file.readFully(make, 0, 64);

                    int l = 0;
                    while ( make[++l] != 0 )
                        ;

                    info.make = new String(make, 0, l);

                    file.seek(aoff + info.make.length() + 1);

                    file.readFully(model, 0, 64);

                    l = 0;
                    while ( model[++l] != 0 )
                        ;

                    info.model = new String(model, 0, l).trim();
                    break;

                case 0x102a:
                    System.err.println("type: 0x102a (wbi) <==--");

                    file.seek(aoff + 14);

                    wbi = file.read2();
                    break;

                case 0x102c:
                {
                    System.err.println("type: 0x102c (G2 white balance) <==--");

                    file.seek(aoff + 100);

                    final int a = file.read2();
                    final int b = file.read2();
                    final int c = file.read2();
                    final int d = file.read2();

                    System.err.println("a: " + a);
                    System.err.println("b: " + b);
                    System.err.println("c: " + c);
                    System.err.println("d: " + d);

                    if ( a == 0 )
                        info.cameraRed = 0;
                    else
                        info.cameraRed = b / a;

                    if ( d == 0 )
                        info.cameraBlue = 0;
                    else
                        info.cameraBlue = c / d;

                    System.err.println("(type 0x102c) wbi: [" + wbi + "] cameraRed: [" + info.cameraRed + "] cameraBlue: [" + info.cameraBlue + "]");

                    break;
                }

                case 0x0032:
                    System.err.println("type: 0x0032 (D30 white balance) <==--");

                    if ( info.model.equals("Canon EOS D30") )
                    {
                        file.seek(aoff + 72);

                        final int a = file.read2();
                        final int b = file.read2();
                        final int c = file.read2();
                        final int d = file.read2();

                        System.err.println("type 0x0032: a: " + a);
                        System.err.println("type 0x0032: b: " + b);
                        System.err.println("type 0x0032: c: " + c);
                        System.err.println("type 0x0032: d: " + d);

                        info.cameraRed  = b / a;
                        info.cameraBlue = c / d;

                        if ( wbi == 0 )
                            info.cameraRed = info.cameraBlue = 0;

                        System.err.println("(type 0x0032) wbi: [" + wbi + "] cameraRed: [" + info.cameraRed + "] cameraBlue: [" + info.cameraBlue + "]");
                    }
                    break;

                case 0x10a9:
                    System.err.println("type: 0x10a9 (D60 white balance) ( <==--");

                    file.seek(aoff + 2 + (wbi << 3));

                    info.cameraRed = file.read2();
                    info.cameraRed /= file.read2();
                    info.cameraBlue = file.read2();
                    info.cameraBlue = file.read2() / info.cameraBlue;
                    break;

                case 0x1031:
                    System.err.println("type: 0x1031 (raw dimensions) <==--");

                    file.seek(aoff + 2);

                    info.rawWidth = file.read2();
                    info.rawHeight = file.read2();
                    break;

                case 0x180e:
                    System.err.println("type: 0x180e (timestamp) <==--");

                    file.seek(aoff);

                    info.timestamp = file.read4();
                    break;

                case 0x1835:
                    System.err.println("type: 0x1835 (decoder table) <==--");

                    /*
                     * How many table entries to create?
                     */
                    file.seek(aoff);

                    info.table = file.read4();

                    System.err.println("@@ t: " + info.table);

                    break;
            }

            if ( type >> 8 == 0x28 || type >> 8 == 0x30 )
            {
                System.err.println("type: *RECURSION* <==--");
                sf_parseCiff(file, info, aoff, len);
            }

            // System.err.println("-- parsing CIFF almost done...about to seek to SAVE...");

            file.seek(save);
        }
    }


    /*
     * Test driver.
     */
    public static void main ( String[] args )
    {
        try
        {
            RawCodec codec = RawCodec.getInstance(new File(args[0]));

            final Calendar start = Calendar.getInstance();

            CaptureData data = codec.decode(); // ICC_Profile.getInstance(ColorSpace.CS_sRGB));

            final Calendar stop = Calendar.getInstance();

            System.err.println("  ==> Total decompression: " +
                               ((stop.getTimeInMillis() - start.getTimeInMillis()) / 1000.0) +
                               " sec");
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
