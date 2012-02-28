package com.crinqle.dlroom;

import java.awt.Color;


/**
 * This class provides concrete BandIterator objects which are
 * created for a given Color Filter Array specification.
 */
public interface Const
{
	public static final Color SWING_BLUE = new Color(153, 153, 204);
	public static final Color SWING_MENU_BLUE = new Color(240, 25, 80);

	int BAND_COUNT_MAX = 8;

	int R = 0;
	int G = 1;
	int B = 2;
	int H = 3;

	String[] CHANNEL_NAME = { "Red", "Green", "Blue", "Hreen" };
	String[] CHANNEL_NICKNAME = { "R", "G", "B", "H" };

	int FILTER_BGGR = 0;
	int FILTER_GRBG = 1;
	int FILTER_GBRG = 2;
	int FILTER_RGGB = 3;

	int FILTER_GMCYMGCY = 100;
	int FILTER_CYGMCYMG = 101;
	int FILTER_CYMGYCGM = 102;
	int FILTER_YCMGCYGM = 103;

	int R_MASK = 1;
	int G_MASK = 1 << 1;
	int H_MASK = G_MASK << 8;
	int B_MASK = 1 << 2;

	int ALL_MASK = -1;

	int SLIDER_SIZE = 256;
}
