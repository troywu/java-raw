package com.crinqle.dlroom.cms;

import java.awt.color.*;


public class CMM
{
	/**
	 * Takes linear (TRC not applied) banded-16-bit-RGB input data
	 * and creates linear (TRC not applied) packed-8-bit-RGB data
	 * as output.
	 *
	 * @param input ICC RGB profile of the 'data' pixels.
	 * @param output ICC RGB profile of the output pixels.
	 * @param data The input array must have column-major access semantics:
	 *
	 *	data[row + column * nRows]
	 *
	 * @return An array containing packed-8-bit-RGB data in the
	 * 'output' colorspace.  The array will have column-major access semantics:
	 *
	 *	returned_array[row + column * nRows]
	 */
	public int[] l2lMatrixTransform ( ICC_ProfileRGB input, ICC_ProfileRGB output, int[] data )
	{
		/*
		 * 1. do matrix multiply (natively)
		 */
		return new int[1];
	}


	/**
	 * Takes linear (TRC not applied) banded-16-bit-RGB input data
	 * and creates non-linear (TRC applied) packed-8-bit-RGB data
	 * as output.
	 *
	 * @param input ICC RGB profile of the 'data' pixels.
	 * @param output ICC RGB profile of the output pixels.
	 * @param data The input array must have column-major access semantics:
	 *
	 *	data[row + column * nRows]
	 *
	 * @return An array containing packed-8-bit-RGB data in the
	 * 'output' colorspace.  The array will have column-major access semantics:
	 *
	 *	returned_array[row + column * nRows]
	 */
	public int[] l2nMatrixTransform ( ICC_ProfileRGB input, ICC_ProfileRGB output, int[] data )
	{
		/*
		 * 1. do matrix multiply (natively)
		 *
		 * 2. apply TRC (natively)
		 */
		return new int[1];
	}


	/**
	 * Takes non-linear (TRC applied) banded-16-bit-RGB input data
	 * and creates non-linear (TRC applied) packed-8-bit-RGB data
	 * as output.
	 *
	 * @param input ICC RGB profile of the 'data' pixels.
	 * @param output ICC RGB profile of the output pixels.
	 * @param data The input array must have column-major access semantics:
	 *
	 *	data[row + column * nRows]
	 *
	 * @return An array containing packed-8-bit-RGB data in the
	 * 'output' colorspace.  The array will have column-major access semantics:
	 *
	 *	returned_array[row + column * nRows]
	 */
	public int[] n2nmatrixTransform ( ICC_ProfileRGB input, ICC_ProfileRGB output, int[] data )
	{
		/*
		 * 1. linearize (natively)
		 *
		 * 2. do matrix multiply (natively)
		 *
		 * 3. apply TRC (natively)
		 */
		return new int[1];
	}


	private native int[] displayXform ( float[] input, float[] output, int[] data );
	private native int[] displayXform ( float[] input, double outGamma, float[] output, int[] data );
	private native int[] displayXform ( double inGamma, float[] input, double outGamma, float[] output, int[] data );
}
