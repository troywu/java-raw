package com.crinqle.dlroom;

import java.awt.color.*;


public class ImageInfo
{
	public int order = 0x4949;
	public long timestamp = 0;

	public float[] preMul = new float[4];

	public int cameraRed = 1;
	public int cameraGreen = 1;
	public int cameraBlue = 1;

	public int rgb_max = 0x4000;
	public int black = 0;
	public int colors = 3;

	public boolean isCmy = false;
	public boolean isFoveon = false;
	public boolean useCoeff = false;

	public int ymag = 1;

	public String make = "Canon";
	public String model = "Canon EOS D30";

	public int height = 1448;
	public int width = 2176;
	public int filters = 0x94949494;
	public int rawWidth = 2224;
	public int rawHeight = 1456;

	public ICC_Profile color_profile;

	/*
	 * Canon vars
	 */
	public int table = 2;
}
