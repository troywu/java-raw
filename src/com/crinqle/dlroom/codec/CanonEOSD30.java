package com.crinqle.dlroom.codec;

import java.awt.color.*;
import java.io.*;
import java.util.*;
import static com.crinqle.dlroom.Const.*;
import com.crinqle.dlroom.*;
import com.crinqle.dlroom.util.*;


/**
 * These are Dave Coffin's words:
 *

 A rough description of Canon's compression algorithm:

 +  Each pixel outputs a 10-bit sample, from 0 to 1023.

 +  Split the data into blocks of 64 samples each.

 +  Subtract from each sample the value of the sample two positions
 to the left, which has the same color filter.  From the two
 leftmost samples in each row, subtract 512.

 +  For each nonzero sample, make a token consisting of two four-bit
 numbers.  The low nibble is the number of bits required to
 represent the sample, and the high nibble is the number of
 zero samples preceding this sample.

 +  Output this token as a variable-length bitstring using
 one of three tablesets.  Follow it with a fixed-length
 bitstring containing the sample.

 The "first_decode" table is used for the first sample in each
 block, and the "second_decode" table is used for the others.


	-- 25 December 2003 --

I would reformulate this description.

Each pixel in the Canon EOS D30 is a 12-bit sample.  The 10 high-order
bits are stored in the "place" he describes.  The 2 low-order bits are
somewhere else.

Now, imagine "data" to be a single, linear, array of pixels.  Each
element of this array is a single sample (i.e., a single color value
captured by the filter array).  Group this array into subarrays of
length 64.

Now, the forward algorithm (the compression part) does what he
describes.  Of course, there are border cases which I think are not
explained in enough detail.  What if the block of 64 samples contains
only zeros (a scene with some serious black regions)?  Is there no
data for that region?  Does the number of black pixels carry over into
the next frame (block of 64)?

Secondly, there is no description of what this variable-length
bitstring looks like.  It would seem that the var-length bs is actualy
fixed-length (since it is metadata, and would make sense if it were
fixed-length--e.g., 8 bits).  Then, given that the metadata describes
the length of the string to follow, the following sample data is
actually var-length.  So, that makes no sense.


	-- 27 December 2003 --

A ton of changes.  First of all, I emailed Dave Coffin over Christmas
2003, and he clued me in about Huffman encodings and Canon's
compression algorithm.  An excerpt from his mail (28 Sept 2003):

	"In 2000, Canon's engineers needed a lossless compression
	algorithm for their upcoming PowerShot Pro90 and G1, and they
	needed it fast.  So they took Thomas Lane's lossy JPEG codec,
	cut out the DCT code, and used the Huffman code unmodified,
	without even understanding it.

	"This explains many oddities of PowerShot compression: Why
	0xff must be followed by 0x00.  Why compression restarts every
	64 pixels, ignoring row boundaries.  Why the variable "carry"
	exists.  None of these are necessary, and all of them make the
	compression _less_ effective.

	"To prevent easy reverse-engineering, Canon overwrote the JPEG
	header and Huffman table with zeroes.  Reading a JPEG file
	without its Huffman table is like opening a combination lock
	without the combination.

	"By the time the G3 was designed, dcraw was already in
	widespread use, so Canon didn't bother disguising anything.
	Now you know the rest of the story!"

		-- Dave Coffin  9/28/2003

So, he also explained that his code didn't explain everything in the
comments.  Specifically, the "variable length bitstring" nonsense
finally made sense after the explanation that it was a Huffman-encoded
variable length bitstring.  I finally figured out how to convert the
source pixel array into a decode tree.  The path of the tree (where
zero is left and one is right) encodes the value for the leaf.  The
leaf nodes is actually the "token" value that Dave talks about in his
"rough description of Canon's compression algorithm."

Once the token is retrieved, there is one more gotcha.  The sign bit
is the MSb (most-significant bit) of the fixed-length sample value.
Since these are differences, they are totally allowed to be negative.
This totally fucks with the ability for me to get the correct value
out of the encoder, but I've used the stupid

	while ( pixel < 65536 ) pixel -= 65536;

trick to deal with the sign problem.  Lastly, I haven't entirely
figured out what the f_carry variable is (I imagine it has something
to do with differences from one 64-sample block "carrying-over" to the
next block).


	-- 28 December 2003 --

I've finally got the huffman tree code working.  I'm also using the
new file-as-memory interface, and that's adding some speed.  I
couldn't imagine trying to do it with java.io.RandomAccessFile.  It
would probably be ass-slow, but that's probably worth a try.  I've
been able to get one file to resemble the output of the C file.  Of
course, the raw data looks like trash.  I'll have to work on it.  For
now, this is pretty good progress.

The huffman tree code of Dave's is deep.  My implementation is naive,
and it expects more support from the source arrays used to create the
decoding tables.  Still, I've hacked it.  See the following classes
for implementation details:

@see strong.util.HuffmanTree
@see StaticTree

Hmm...Still one piece of weirdness I can't figure.  What's the deal
with getting full 16-bit unsigned values out of the decompression?  I
thought that the max values (without getting the 2 low-bits) were
10-bit values.  Don't understand how this can happen.  Will look into
this more....

 */
class CanonEOSD30 extends RawCodec
{
	public static final int BIT_DEPTH = 12;
	public static final int BIT_REMAINDER = 16 - BIT_DEPTH;

	protected CaptureData f_cd = null;
	protected HuffmanTree f_ht1 = null;
	protected HuffmanTree f_ht2 = null;

	private int f_carry = 0;
	private int f_pixelIndex = 0;
	private int[] f_base = new int[2];

	protected int f_black = 0;


	CanonEOSD30 ( RawImageFile rif, int size, ImageInfo info )
	{
		super(rif, size, info);

		f_info.height = 1448;
		f_info.width = 2176;
		f_info.filters = 0x94949494;

		/*
		 * This is another departure.  Let's use 16-bits here.
		 *
		 *	-- 3 March 2004 --
		 *
		 * Let's not use 16-bits.  Let's make that transition
		 * downstream.  Let's use 12--the camera's native capture
		 * depth.
		 */
		// f_info.rgb_max = 0x10000;
		f_info.rgb_max = 0x1000;

		/*
		 * -- CRAP --
		 *
		 * What are these values, and how do they fit into
		 * the color space picture?
		 *
		 * No bias -- set the green pixels to have the
		 * same weight.
		 *
		 * Of course, in the non-interpolated raw image,
		 * the green values need to be halved....
		 */
		f_info.preMul[0] = 1.895f;
		f_info.preMul[2] = 1.403f;

		f_info.preMul[1] = f_info.preMul[3] = 1.0f;

		System.err.println("raw_height: " + f_info.rawHeight);
		System.err.println("raw_width: " + f_info.rawWidth);
		System.err.println("height: " + f_info.height);
		System.err.println("width: " + f_info.width);
		System.err.println("filters: " + f_info.filters);
		System.err.println("preMul[0]: " + f_info.preMul[0]);
		System.err.println("preMul[1]: " + f_info.preMul[1]);
		System.err.println("preMul[2]: " + f_info.preMul[2]);
		System.err.println("preMul[3]: " + f_info.preMul[3]);
	}


	public CaptureData decode()
	{
		f_cd = new CaptureData(f_info.width, f_info.height, FILTER_RGGB, 12);
		f_cd.setImageInfo(f_info);

		final int table = (f_info.table > 2) ? 2 : f_info.table;
		f_info.table = table;

		f_ht1 = new HuffmanTree(StaticTree.FIRST_TREE[table], 16);
		f_ht2 = new HuffmanTree(StaticTree.SECOND_TREE[table], 16);

		int black = 0;
		int black_count = 0;

		try
		{
			/*
			 * Create fast reading stream from file.
			 */
			final byte[] array = new byte[f_size];

			f_stream.seek(0);
			f_stream.readFully(array, 0, f_size);

			f_stream = SeekableInputStream.getInstance(f_info.order, array);

			/*
			 * Begin the decompression by seeking to place of the good 10-bits.
			 */
			int top = 0;
			int left = 0;

			switch ( f_info.rawWidth )
			{
			    case 2144: top =  8; left =  4; break; // G1
			    case 2224: top =  6; left = 48; break; // EOS D30
			    case 2376: top =  6; left = 12; break; // G2 or G3
			    case 2672: top =  6; left = 12; break; // S50
			    case 3152: top = 12; left = 64; break; // EOS D60
			}

			final int lowbits = f_canonHasLowbits();

			if ( lowbits == 0 )
				System.err.println("Yikes--we have low bit data!");
			if ( lowbits == 1 )
				System.err.println("Weird--we have uncompressed low-order bits first!");

			System.err.println("lowbits: " + lowbits);

			final int hiLocation = 540 + lowbits * f_info.rawHeight * f_info.rawWidth / 4;

			f_stream.seek(hiLocation);

			int[] pixel = new int[f_info.rawWidth * 8];
			int unfuck_iter = 0;

			System.err.println("Using decode table " + f_info.table + "...");
			System.err.println("Beginning total decompression (at file offset " + hiLocation + ") [" + f_stream.getPosition() + "]...");

			for ( int row = 0; row < f_info.rawHeight; row += 8 )
			{
				final long _before_save = f_stream.getPosition();

				f_unfuck(pixel, f_info.rawWidth >> 3, unfuck_iter);

				final long _after_save = f_stream.getPosition();

				System.err.println("Decompressed rows " + (row) + " through " + (row + 7) + " (file: " + _before_save + " - " + _after_save + ") iter " + unfuck_iter);
				++unfuck_iter;

				if ( lowbits != 0 )
				{
					final long save = f_stream.getPosition();

					f_stream.seek(26 + row * f_info.rawWidth / 4);

					int pi = 0;

					for ( int i = 0; i < f_info.rawWidth << 1; ++i )
					{
						int c = f_stream.read();

						for ( int r = 0; r < 8; r += 2 )
						{
							pixel[pi] = (pixel[pi] << 2) + ((c >> r) & 3);

							while ( pixel[pi] >= 65536 )
								pixel[pi] -= 65536;

							++pi;
						}
					}

					f_stream.seek(save);
				}

				// DEBUG: StringBuffer buffer = new StringBuffer("pixel row " + row + ": "); for ( int pi = 0; pi < f_info.rawWidth; ++pi ) buffer.append(pixel[pi] + " "); System.out.println(buffer);

				for ( int r = 0; r < 8; ++r )
				{
					for ( int col = 0; col < f_info.rawWidth; ++col )
					{
						// DEBUG: System.out.println("  @@ pixel[" + (r * f_info.rawWidth + col) + "]: " + pixel[r * f_info.rawWidth + col]);

						final int irow = row + r - top;

						if ( irow >= 0  &&  irow < f_info.height )
						{
							if ( col < left )
							{
								// System.out.println("  black (" + black_count + "): " + pixel[r * f_info.rawWidth + col]);
								black += pixel[r * f_info.rawWidth + col];
								++black_count;
							}
							else
							{
								final int i = irow * f_info.width + col - left;
								f_cd.setElem(i, pixel[r * f_info.rawWidth + col]);
							}
						}
					}
				}
			}
		}
		catch ( Exception e ) { e.printStackTrace(); System.exit(1); }

		// DEBUG: for ( int y = 0; y < f_info.height; ++y ) { StringBuffer buffer = new StringBuffer("irow " + y + ": "); for ( int x = 0; x < f_info.width; ++x ) buffer.append(f_cd.get(x,y) + " "); System.out.println(buffer.toString()); }

		System.err.println("total black: " + black + " (" + black_count + ")");

		// -- 3 March 2004 -- black = (black << BIT_REMAINDER) / ((f_info.rawWidth - f_info.width) * f_info.height);
		black = black / ((f_info.rawWidth - f_info.width) * f_info.height);

		f_info.black = black;

		System.err.println("average black: " + black);

		f_blackScale();

		// f_averageGreens();

		return f_cd;
	}


	protected void f_blackScale()
	{
		final int black = f_info.black;
		final int rgb_max = f_info.rgb_max -= black;
		int value;

		int _total_max = 0;
		int index = 0;

		for ( int y = 0; y < f_info.height; ++y )
		{
			for ( int x = 0; x < f_info.width; ++x )
			{
				value = f_cd.getElem(index) - black;

				if ( value == 0 )
					continue;

				if ( value < 0 )
					value = 0;

				else if ( value > rgb_max )
					value = rgb_max;

				f_cd.setElem(index, value);

				if ( value > _total_max )
					_total_max = value;

				++index;
			}
		}

		System.err.println("CanonEOSD30: maximum image value: " + _total_max);
	}


	/*
	private void f_averageGreens()
	{
		int g1 = 0;
		int g2 = 0;

		final int h = f_info.height - 1;
		final int w = f_info.width - 1;

		*
		* Consider a four-color CFA.  Average the duplicates.
		*
		for ( int y = 0; y < h; y += 2 )
		{
			for ( int x = 0; x < w; x += 2 )
			{
				g1 = f_cd.get(x+1, y);
				g2 = (f_cd.get(x, y+1) + g1) >> 1;

				f_cd.set(x+1, y, g2);
				f_cd.set(x, y+1, g2);
			}
		}
	}
	*/


	protected void f_unfuck ( int[] pixel, final int count, final int iter ) throws Exception
	{
		HuffmanTree tree = f_ht1;
		int pi = 0;
		int[] diffbuf = new int[64];
		int[] leaves = new int[64];
		int token = -1;
		int zeros = 0;
		int bitcount = 0;
		int sign = 0;
		int diff = 0;

		for ( int j = 0; j < count; ++j )
		{
			tree = f_ht1;

			for ( int i = 0; i < 64; ++i )
			{
				diffbuf[i] = 0;
				leaves[i] = -1;
			}

			for ( int i = 0; i < 64; ++i )
			{
				try
				{
					while ( (token = tree.find(f_stream.bits(1))) < 0 )
						;

					/*
					do
					{
						final int dir = f_stream.bits(1);

						System.err.println("  dir: " + dir);

						token = tree.find(dir);
					}
					while ( token < 0 );
					*/

					tree = f_ht2;

					// System.err.println("token: " + token);
					// leaves[i] = token;

					if ( token == 0  &&  i > 0 )
						break;

					if ( token == 0xff )
						continue;

					zeros = token >> 4;
					bitcount = token & 15;
					i += zeros;

					if ( bitcount == 0 )
						continue;

					sign = f_stream.bits(1);
					diff = f_stream.bits(bitcount - 1);

					if ( sign == 1 )
						diff += 1 << (bitcount - 1);
					else
						diff += (-1 << bitcount) + 1;

					if ( i < 64 )
						diffbuf[i] = diff;
				}
				catch ( Exception e )
				{
					System.err.println();
					System.err.println("--");
					System.err.println("iter: " + iter);
					System.err.println("i: " + i);
					// System.err.println("path: " + tree.findPath());
					System.err.println("token: " + token);
					System.err.println("--");
					System.err.println();

					e.printStackTrace();
					System.exit(1);
				}
			}

			diffbuf[0] += f_carry;
			f_carry = diffbuf[0];

			/*
			System.err.print("diffbuf: " );
			for ( int i = 0; i < 64; ++i )
				System.err.print(diffbuf[i] + " ");
			System.err.println();
			System.exit(1);
			*/

			for ( int i = 0; i < 64; ++i )
			{
				if ( f_pixelIndex++ % f_info.rawWidth == 0 )
					f_base[0] = f_base[1] = 512;

				final int pii = pi + i;

				pixel[pii] = (f_base[i & 1] += diffbuf[i]);
				
				while ( pixel[pii] < 0 )
					pixel[pii] += 65536;
				while ( pixel[pii] >= 65536 )
					pixel[pii] -= 65536;

				// System.out.print(" " + pixel[pi + i]);
			}
			// System.out.println();

			pi += 64;
		}
	}


	private int f_canonHasLowbits() throws Exception
	{
		byte[] test = new byte[8192];
		int ret = 1;
		int i;

		f_stream.seek(0);
		f_stream.readFully(test, 0, 8192);

		for ( i = 540; i < 8191; ++i )
		{
			if ( test[i] == 0xff )
			{
				if ( test[i+1] != 0 )
					return 1;
				ret = 0;
			}
		}
		return ret;
	}
}
