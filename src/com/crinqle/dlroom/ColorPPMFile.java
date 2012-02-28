package com.crinqle.dlroom;

import java.awt.color.ICC_Profile;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.Calendar;

import com.crinqle.dlroom.util.*;
import static com.crinqle.dlroom.Const.*;

public class ColorPPMFile {
	public static final String MAGIC = "P6";
	public static final String ICC_HEADER = "ICCWorkingSpaceProfile:";
	// public static final int[][] FILTER_ARRAY = new int[2][2];

	public static final String TIME_CREATED_HEADER = "TimeCreated:";
	public static final String TIME_CONVERTED_HEADER = "TimeConverted:";

	private int f_height;
	private int f_width;
	private ICC_Profile f_profile;
	private Calendar f_timeCreated;
	private Calendar f_timeConverted;

	private String f_make;
	private String f_model;

	public ColorPPMFile() {
	}

	public int getHeight() {
		return f_height;
	}

	public void setHeight(int height) {
		f_height = height;
	}

	public int getWidth() {
		return f_width;
	}

	public void setWidth(int width) {
		f_width = width;
	}

	public String getMake() {
		return f_make;
	}

	public void setMake(String make) {
		f_make = make;
	}

	public String getModel() {
		return f_model;
	}

	public void setModel(String model) {
		f_model = model;
	}

	public Calendar getTimeCreated() {
		return f_timeCreated;
	}

	public void setTimeCreated(Calendar time) {
		f_timeCreated = time;
	}

	public Calendar getTimeConverted() {
		return f_timeConverted;
	}

	public void setTimeConverted(Calendar time) {
		f_timeConverted = time;
	}

	static public RawRaster decode(byte[] array) throws IOException, Exception {
		ByteArrayInputStream stream = new ByteArrayInputStream(array);

		return decode(stream);
	}

	static public RawRaster decode(InputStream stream) throws IOException,
			Exception {
		return f_decode(new PushbackInputStream(stream));
	}

	static public void encode(RawRaster raster, OutputStream stream)
			throws IOException {
		final byte[] array = encode(raster);
		final int length = array.length;

		stream.write(array, 0, length);
	}

	static public void encode_OLD(WritableRaster raster,
			ICC_Profile workingSpaceProfile, OutputStream stream)
			throws IOException {
		final byte[] array = encode_OLD(raster, workingSpaceProfile);
		final int length = array.length;

		stream.write(array, 0, length);
	}

	/**
	 * This method sucks because I only recognize 16-bit unsigned values for
	 * each sample.
	 */
	static public byte[] encode_OLD(final WritableRaster raster,
			ICC_Profile workingSpaceProfile) throws IOException {
		System.err.println("CPF: encode(): raster: " + raster);

		final int height = raster.getHeight();
		final int width = raster.getWidth();

		final byte[] profileArray = workingSpaceProfile.getData();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		String magicString = MAGIC + "\n";
		String profileString = "# " + ICC_HEADER;

		stream.write(magicString.getBytes());
		stream.write(profileString.getBytes());

		/*
		 * -------------------------------------------------- Base64-like
		 * encoding of embedded ICC Color Profile. Since PPM comments are based
		 * on single-line comments, I had to find a way to encode the profile
		 * without the possibility of running into a '\n' character.
		 */
		final byte[] encodedProfileArray = Base64Like.encode(profileArray);
		final int epaLength = encodedProfileArray.length;
		/*
		 * 
		 * End of the Base64-like encoding.
		 * --------------------------------------------------
		 */

		stream.write(encodedProfileArray);

		int rgb_max = 0;
		DataBuffer buffer = raster.getDataBuffer();
		int sample;
		int r, g, b;

		for (int bank = 0; bank < buffer.getNumBanks(); ++bank) {
			for (int k = 0; k < buffer.getSize(); ++k) {
				sample = buffer.getElem(bank, k);

				r = (sample & 0xff0000) >> 16;
				g = (sample & 0xff00) >> 8;
				b = (sample & 0xff);

				if (sample > rgb_max)
					rgb_max = sample;
			}
		}

		System.err.println("@@ CPF: encode(): rgb_max: " + rgb_max);

		String header = "\n" + width + "\n" + height + "\n" + rgb_max + "\n";

		stream.write(header.getBytes());

		byte[] array = new byte[width * 3 * 2];

		final int length = array.length;

		for (int y = 0; y < height; ++y) {
			Arrays.fill(array, (byte) 0);

			for (int x = 0; x < width; ++x)
				; // store16(array, x, y, raster);

			stream.write(array, 0, length);
		}

		array = stream.toByteArray();
		stream.close();

		return array;
	}

	/**
	 * This method recognizes RawRaster objects, and encodes them in either
	 * 8-bit or 16-bit PPM samples.
	 */
	static public byte[] encode(final RawRaster raster) throws IOException {
		final ImageInfo info = raster.getImageInfo();
		final int height = info.height;
		final int width = info.width;

		final ICC_Profile workingSpaceProfile = raster.getProfile();
		final byte[] profileArray = workingSpaceProfile.getData();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		String magicString = MAGIC + "\n";
		String profileString = "# " + ICC_HEADER;

		stream.write(magicString.getBytes());
		stream.write(profileString.getBytes());

		/*
		 * -------------------------------------------------- Base64-like
		 * encoding of embedded ICC Color Profile. Since PPM comments are based
		 * on single-line comments, I had to find a way to encode the profile
		 * without the possibility of running into a '\n' character.
		 */
		final byte[] encodedProfileArray = Base64Like.encode(profileArray);
		final int epaLength = encodedProfileArray.length;
		/*
		 * 
		 * End of the Base64-like encoding.
		 * --------------------------------------------------
		 */

		stream.write(encodedProfileArray);

		String header = "\n" + width + "\n" + height + "\n" + info.rgb_max
				+ "\n";

		stream.write(header.getBytes());

		// FILTER_ARRAY[0][0] = 0; FILTER_ARRAY[0][1] = 1;
		// FILTER_ARRAY[1][0] = 1; FILTER_ARRAY[1][1] = 2;

		char[] values = null;
		byte[] array = null;
		boolean is2 = true;

		if (info.rgb_max > 0xff)
			array = new byte[width * 3 * 2];
		else {
			array = new byte[width * 3];
			is2 = false;
		}

		final int length = array.length;

		for (int y = 0; y < height; ++y) {
			Arrays.fill(array, (byte) 0);

			for (int x = 0; x < width; ++x) {
				if (is2)
					store16(array, x, y, raster); // store16(array, x, y,
													// values[x]);
				else
					store8(array, x, y, raster); // store8(array, x, y,
													// values[x]);
			}

			stream.write(array, 0, length);
		}

		array = stream.toByteArray();
		stream.close();

		return array;
	}

	static private RawRaster f_decode(PushbackInputStream stream)
			throws IOException, Exception {
		ICC_Profile profile = null;
		ICC_Profile profileTemp = null;
		boolean commentFollows = false;

		/*
		 * Read magic.
		 */
		int c;
		byte[] magicArray = new byte[2];

		stream.read(magicArray, 0, 2);

		if (magicArray[0] != 'P'
				|| (magicArray[1] != '6' && magicArray[1] != '8'))
			return null;

		/*
		 * Read width.
		 */
		commentFollows = f_skipWhitespace(stream);

		if (commentFollows) {
			profileTemp = f_parseComment(stream);
			if (profileTemp != null)
				profile = profileTemp;
		}
		;

		final int width = Integer.parseInt(f_readToken(stream));

		/*
		 * Read height.
		 */
		commentFollows = f_skipWhitespace(stream);

		if (commentFollows) {
			profileTemp = f_parseComment(stream);
			if (profileTemp != null)
				profile = profileTemp;
		}
		;

		final int height = Integer.parseInt(f_readToken(stream));

		/*
		 * Reading RGB-max.
		 */
		commentFollows = f_skipWhitespace(stream);

		if (commentFollows) {
			profileTemp = f_parseComment(stream);
			if (profileTemp != null)
				profile = profileTemp;
		}
		;

		final int rgb_max = Integer.parseInt(f_readToken(stream));

		/*
		 * Get the final whitespace character.
		 */
		stream.read();

		System.err.println("CPF: magic: " + new String(magicArray));
		System.err.println("CPF: width: " + width);
		System.err.println("CPF: height: " + height);
		System.err.println("CPF: rgb_max: " + rgb_max);
		System.err.println("CPF: ICC_Profile: " + profile);

		/*
		 * Reading image data.
		 */
		ImageInfo info = new ImageInfo();
		info.width = width;
		info.height = height;
		info.rgb_max = rgb_max;

		RawRaster rr = new RawRaster(info.width, info.height, 3, 16);
		f_readImageData(rr, new DataInputStream(stream));

		return rr;
	}

	static void f_readImageData(RawRaster raster, DataInputStream stream)
			throws IOException {
		final int numBands = 3;
		final int width = raster.getWidth();
		final int height = raster.getHeight();
		int[] rgb = new int[3];
		int band;

		System.err.println("<...> Reading raster data...");

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				/*
				 * int a = stream.read(); int b = stream.read(); int c =
				 * stream.read(); int d = stream.read(); int e = stream.read();
				 * int f = stream.read();
				 * 
				 * System.err.println("  a: " + (int)(a));
				 * System.err.println("  b: " + (int)(b));
				 * System.err.println("  c: " + (int)(c));
				 * System.err.println("  d: " + (int)(d));
				 * System.err.println("  e: " + (int)(e));
				 * System.err.println("  f: " + (int)(f));
				 * 
				 * System.err.println("  a: " + Integer.toHexString(a));
				 * System.err.println("  b: " + Integer.toHexString(b));
				 * System.err.println("  c: " + Integer.toHexString(c));
				 * System.err.println("  d: " + Integer.toHexString(d));
				 * System.err.println("  e: " + Integer.toHexString(e));
				 * System.err.println("  f: " + Integer.toHexString(f));
				 * 
				 * rgb[0] = (a << 8) | b; rgb[1] = (c << 8) | d; rgb[2] = (e <<
				 * 8) | f;
				 */

				raster.setSample(x, y, R, stream.readUnsignedShort());
				raster.setSample(x, y, G, stream.readUnsignedShort());
				raster.setSample(x, y, B, stream.readUnsignedShort());

				// rgb[0] = stream.readUnsignedShort();
				// rgb[1] = stream.readUnsignedShort();
				// rgb[2] = stream.readUnsignedShort();

				/*
				 * if ( rgb[0] != 0 ) band = 0; else if ( rgb[1] != 0 ) band =
				 * 1; else band = 2;
				 */

				/*
				 * raster.setSample(x, y, 0, rgb[0]); raster.setSample(x, y, 1,
				 * rgb[1]); raster.setSample(x, y, 2, rgb[2]);
				 */

				/*
				 * System.err.println("(r, g, b) = (" + rgb[0] + ", " + rgb[1] +
				 * ", " + rgb[2] + ")"); System.err.println("  (x, y) = (" + x +
				 * ", " + y + "): " + band);
				 * 
				 * if ( x == 10 ) System.exit(10);
				 */
			}

			// System.err.print(".");
		}

		// System.err.println();
	}

	static private boolean f_skipWhitespace(PushbackInputStream stream)
			throws IOException {
		char c;

		while (Character.isWhitespace(c = (char) stream.read()))
			;

		stream.unread(c);

		if (c == '#')
			return true;

		return false;
	}

	static private String f_readToken(PushbackInputStream stream)
			throws IOException {
		StringBuffer buffer = new StringBuffer();
		char c;

		while (Character.isWhitespace(c = (char) stream.read()) == false)
			buffer.append(c);

		stream.unread(c);

		return buffer.toString();
	}

	static private ICC_Profile f_parseComment(PushbackInputStream stream)
			throws IOException {
		/*
		 * Read until newline character.
		 */
		StringBuffer buffer = new StringBuffer();
		char c;

		while ((c = (char) stream.read()) != '\n')
			buffer.append(c);

		String comment = buffer.toString();

		final int length = ICC_HEADER.length();
		final int index = comment.indexOf(ICC_HEADER);

		if (index >= 0) {
			final int profileIndex = index + length;
			final String profileString = comment.substring(profileIndex);
			final byte[] profileEncoding = profileString.getBytes();

			try {
				final byte[] profileDecoding = Base64Like
						.decode(profileEncoding);

				ICC_Profile profile = ICC_Profile.getInstance(profileDecoding);

				return profile;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		return null;
	}

	static private void store16(byte[] array, int x, int y, RawRaster raster) {
		final int offset = x * 6;
		final int r = raster.getSample(x, y, 0);
		final int g = raster.getSample(x, y, 1);
		final int b = raster.getSample(x, y, 2);

		array[offset + 0] = (byte) (r >> 8);
		array[offset + 1] = (byte) (r & 0xff);
		array[offset + 2] = (byte) (g >> 8);
		array[offset + 3] = (byte) (g & 0xff);
		array[offset + 4] = (byte) (b >> 8);
		array[offset + 5] = (byte) (b & 0xff);

		/*
		 * if ( y == 100 ) { System.out.println("(" + x + ", " + y +
		 * ") - (R, G, B) = (" + r + ", " + g + ", " + b + ")");
		 * 
		 * System.err.println("  array[" + offset + "+ 0]: " + array[offset +
		 * 0]); System.err.println("  array[" + offset + "+ 1]: " + array[offset
		 * + 1]); System.err.println("  array[" + offset + "+ 2]: " +
		 * array[offset + 2]); System.err.println("  array[" + offset + "+ 3]: "
		 * + array[offset + 3]); System.err.println("  array[" + offset +
		 * "+ 4]: " + array[offset + 4]); System.err.println("  array[" + offset
		 * + "+ 5]: " + array[offset + 5]); }
		 */
	}

	static private void store8(byte[] array, int x, int y, RawRaster raster) {
		final int offset = x * 3;
		final int r = raster.getSample(x, y, 0);
		final int g = raster.getSample(x, y, 1);
		final int b = raster.getSample(x, y, 2);

		array[offset + 0] = (byte) (r & 0xff); // Integer.byteValue(r);
		array[offset + 1] = (byte) (g & 0xff); // Integer.byteValue(g);
		array[offset + 2] = (byte) (b & 0xff); // Integer.byteValue(b);
	}

	/*
	 * static private void store16 ( byte[] array, int x, int y, int value ) {
	 * final int band = FILTER_ARRAY[y & 1][x & 1]; final int bi0 = band << 1;
	 * final int bi1 = bi0 | 1; final int offset = x * 6;
	 * 
	 * array[offset + bi0] = (byte)(value >> 8); array[offset + bi1] =
	 * (byte)(value & 0xff);
	 * 
	 * // if ( y == -1 ) // { // System.out.print("(" + x + ", " + y + ") - ");
	 * 
	 * // switch ( band ) // { // case 0: // System.out.println("(r) = (" +
	 * value + ")"); // break; // case 1: // System.out.println("(g) = (" +
	 * value + ")"); // break; // case 2: // System.out.println("(b) = (" +
	 * value + ")"); // break; // }
	 * 
	 * // System.err.println("  array[" + offset + "+ 0]: " + array[offset +
	 * 0]); // System.err.println("  array[" + offset + "+ 1]: " + array[offset
	 * + 1]); // System.err.println("  array[" + offset + "+ 2]: " +
	 * array[offset + 2]); // System.err.println("  array[" + offset + "+ 3]: "
	 * + array[offset + 3]); // System.err.println("  array[" + offset +
	 * "+ 4]: " + array[offset + 4]); // System.err.println("  array[" + offset
	 * + "+ 5]: " + array[offset + 5]); // } }
	 * 
	 * 
	 * static private void store8 ( byte[] array, int x, int y, int value ) {
	 * final int band = FILTER_ARRAY[y & 1][x & 1]; final int offset = x * 3;
	 * 
	 * array[offset + band] = (byte)(value & 0xff); }
	 */

	public static void main(String[] args) throws Exception {
		ICC_Profile profile = ICC_Profile.getInstance(args[0]);

		byte[] plain = profile.getData();
		byte[] encoding = Base64Like.encode(plain);
		byte[] decoding = Base64Like.decode(encoding);

		boolean equals = Arrays.equals(plain, decoding);

		System.out.println("enc: " + new String(encoding));

		if (!equals) {
			System.out.print("src:");
			for (int i = 0; i < plain.length; ++i)
				System.out.print(" " + plain[i]);
			System.out.println();

			System.out.print("dec:");
			for (int i = 0; i < decoding.length; ++i)
				System.out.print(" " + decoding[i]);
			System.out.println();
		}

		ICC_Profile profile2 = ICC_Profile.getInstance(decoding);

		final int argc = args.length;

		for (int i = 1; i < argc; ++i) {
			File file = new File(args[i]);

			try {
				if (file.exists())
					if (file.isFile())
						ColorPPMFile.decode(new FileInputStream(args[i]));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
