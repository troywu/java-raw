package com.crinqle.dlroom.util;

import java.util.*;


/**
 * A class which does Base64-like encoding (except for the
 * newline requirements).
 *
 * Excerpt from RFC-1521:
 *

                            Table 1: The Base64 Alphabet

      Value Encoding  Value Encoding  Value Encoding  Value Encoding
           0 A            17 R            34 i            51 z
           1 B            18 S            35 j            52 0
           2 C            19 T            36 k            53 1
           3 D            20 U            37 l            54 2
           4 E            21 V            38 m            55 3
           5 F            22 W            39 n            56 4
           6 G            23 X            40 o            57 5
           7 H            24 Y            41 p            58 6
           8 I            25 Z            42 q            59 7
           9 J            26 a            43 r            60 8
          10 K            27 b            44 s            61 9
          11 L            28 c            45 t            62 +
          12 M            29 d            46 u            63 /
          13 N            30 e            47 v
          14 O            31 f            48 w         (pad) =
          15 P            32 g            49 x
          16 Q            33 h            50 y

*/
public class Base64Like
{
	final public static byte PAD = '=';

	final public static byte[] CODES = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
					     'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
					     'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
					     'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
					     'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
					     'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
					     'w', 'x', 'y', 'z', '0', '1', '2', '3',
					     '4', '5', '6', '7', '8', '9', '+', '/',
					     '=' };

	final public static int[] SYMBOLS = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
					      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
					      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
					      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
					      -1, -1, -1,
					                  62,
					                      -1, -1, -1,
					                                  63,
					                                      52, 53,
					      54, 55, 56, 57, 58, 59, 60, 61,
					                                      -1, -1,
					      -1, 64, -1, -1, -1,
					                           0,  1,  2,  3,  4,
					       5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
					      15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
					      25,
					          -1, -1, -1, -1, -1, -1,
					                                  26, 27, 28,
					      29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
					      39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
					      49, 50, 51 };


	public static byte[] decode ( byte[] code ) throws Exception
	{
		final int ilen = code.length;
		final int olen = (ilen >> 2) * 3;
		int ii = 0;
		int oi = 0;

		byte[] plain = new byte[olen];

		Arrays.fill(plain, (byte)0);

		do
		{
			int a = SYMBOLS[code[ii++]];
			int b = SYMBOLS[code[ii++]];
			int c = SYMBOLS[code[ii++]];
			int d = SYMBOLS[code[ii++]];

			/*
			System.err.println("  decoding " + (char)CODES[a] + " (" + a + ")" + "...");
			System.err.println("  decoding " + (char)CODES[b] + " (" + b + ")" + "...");
			System.err.println("  decoding " + (char)CODES[c] + " (" + c + ")" + "...");
			System.err.println("  decoding " + (char)CODES[d] + " (" + d + ")" + "...");
			*/

			if ( a == 64  ||  b == 64 )
				break;

			plain[oi++] = (byte)((a << 2) | ((b & 48) >> 4));

			// System.err.println("    x = " + plain[oi-1]);

			if ( c == 64 )
				break;

			plain[oi++] = (byte)(((b << 4) | ((c & 60) >> 2)));

			// System.err.println("    y = " + plain[oi-1]);

			if ( d == 64 )
				break;

			plain[oi++] = (byte)(((c & 3) << 6) | (d & 63));

			// System.err.println("    z = " + plain[oi-1]);
		}
		while ( ii < ilen );

		if ( oi != olen )
		{
			byte[] p = new byte[oi];
			System.arraycopy(plain, 0, p, 0, oi);
			return p;
		}

		return plain;
	}


	public static byte[] encode ( byte[] plain )
	{
		final int ilen = plain.length;
		final int mod3 = ilen % 3;
		final int rem3 = 3 - mod3;

		final int ilenPadded = ilen + rem3;
		final int olen = (ilenPadded * 4) / 3;

		System.err.println("Encoding " + ilen + " bytes into " + olen + " bytes....");

		int oi = 0;
		int ii = 0;

		byte[] code = new byte[olen];

		Arrays.fill(code, (byte)0);

		while ( ii < ilen )
		{
			int x = (plain[ii++]);
			if ( x < 0 ) x += 256;

			// System.err.println("  encoding " + x + "...");

			code[oi++] = CODES[x >> 2];

			if ( ii < ilen )
			{
				int y = (plain[ii++]);
				if ( y < 0 ) y += 256;

				// System.err.println("  encoding " + y + "...");

				code[oi++] = CODES[((x & 3) << 4) | (y >> 4)];

				if ( ii < ilen )
				{
					int z = (plain[ii++]);
					if ( z < 0 ) z += 256;

					// System.err.println("  encoding " + z + "...");

					code[oi++] = CODES[((y & 15) << 2) | (z >> 6)];
					code[oi++] = CODES[z & 63];
				}
				else
				{
					/*
					 * Two characters at end,
					 *
					 * One-character pad.
					 */
					code[oi++] = CODES[(y & 15) << 2];
					code[oi++] = CODES[64];

					break;
				}
			}
			else
			{
				/*
				 * Only one character at end:
				 *
				 * Two-character pad.
				 */
				code[oi++] = CODES[(x & 3) << 4];
				code[oi++] = CODES[64];
				code[oi++] = CODES[64];

				break;
			}
		}

		while ( oi < olen )
			code[oi++] = PAD;

		return code;
	}


	public static void main ( String[] args )
	{
		try
		{
			byte[] plain = new byte[256];

			for ( int i = 0; i < 256; ++i )
				plain[i] = (byte)i;

			System.out.print("plain:");
			for ( int i = 0; i < 256; ++i )
				System.out.print(" " + i);
			System.out.println();

			byte[] code = Base64Like.encode(plain);

			String codeString = new String(code);

			System.out.println("code: " + codeString);

			byte[] recovered = Base64Like.decode(code);

			System.out.print("recovered:");
			for ( int i = 0; i < 256; ++i )
				System.out.print(" " + i);
			System.out.println();

			for ( int i = 0; i < 256; ++i )
				if ( recovered[i] != plain[i] )
					System.out.println("mismatch (" + recovered[i] + " != " + plain[i] + ") at position " + i);



			final int argc = args.length;

			for ( int i = 0; i < argc; ++i )
			{
				plain = args[i].getBytes();
				byte[] encoding = Base64Like.encode(plain);
				byte[] decoding = Base64Like.decode(encoding);

				if ( Arrays.equals(plain, decoding) == false )
					System.err.println("  Cannot encode " + new String(plain) + " properly (decoding: " + new String(decoding) + ")");

				System.out.println("arg " + i + ": " + new String(decoding));
			}
		}
		catch ( Exception e ) { e.printStackTrace(); System.exit(1); }
	}
}
