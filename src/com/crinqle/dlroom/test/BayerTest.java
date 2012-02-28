package com.crinqle.dlroom.test;

import static com.crinqle.dlroom.Const.*;
import com.crinqle.dlroom.*;

public class BayerTest {
	public static final int DIM = 6 * 4;
	public static final int W = 3 * DIM;
	public static final int H = 3 * DIM;

	public static CaptureData makeSmallWhiteTestGrid() {
		CaptureData cd = new CaptureData(DIM, DIM, FILTER_RGGB, 8);

		for (int y = 0; y < DIM; ++y)
			for (int x = 0; x < DIM; ++x)
				cd.setSample(x, y, 255);

		return cd;
	}

	public static CaptureData makeWhiteTestGrid() {
		CaptureData cd = new CaptureData(W, H, FILTER_RGGB, 8);

		for (int y = 0; y < H; ++y)
			for (int x = 0; x < W; ++x)
				cd.setSample(x, y, 255);

		return cd;
	}

	public static CaptureData makeColorTestGrid() {
		CaptureData cd = new CaptureData(W, H, FILTER_RGGB, 8);

		setBlockBand(cd, 0, 0, B, 255);
		setBlockBand(cd, DIM, 0, G, 255);
		setBlockBand(cd, DIM, 0, H, 255);
		setBlockBand(cd, DIM << 1, 0, R, 255);

		setBlockBand(cd, 0, DIM, R, 255);
		setBlockBand(cd, 0, DIM, G, 255);
		setBlockBand(cd, 0, DIM, H, 255);
		setBlockBand(cd, DIM, DIM, R, 255);
		setBlockBand(cd, DIM, DIM, B, 255);
		setBlockBand(cd, DIM << 1, DIM, G, 255);
		setBlockBand(cd, DIM << 1, DIM, H, 255);
		setBlockBand(cd, DIM << 1, DIM, B, 255);

		makeGrayBlock(cd, 0, DIM << 1, 255);
		makeGrayBlock(cd, DIM, DIM << 1, 255 - 51);
		makeGrayBlock(cd, DIM << 1, DIM << 1, 255 - 102);

		return cd;
	}

	private static void setBlockBand(CaptureData cd, int dx, int dy, int band,
			int value) {
		int cfa_dx = 0;
		int cfa_dy = 0;

		switch (band) {
		case R:
			break;

		case G:
			cfa_dx = 1;
			cfa_dy = 0;
			break;

		case H:
			cfa_dx = 0;
			cfa_dy = 1;
			break;

		case B:
			cfa_dx = 1;
			cfa_dy = 1;
			break;

		default:
			throw new RuntimeException("Huh?  What band, exactly...?");
		}

		final int xmax = dx + DIM;
		final int ymax = dy + DIM;

		for (int y = dy + cfa_dy; y < ymax; y += 2)
			for (int x = dx + cfa_dx; x < xmax; x += 2)
				cd.setSample(x, y, value);
	}

	public static CaptureData makeDiagonalTestGrid() {
		CaptureData cd = new CaptureData(W, H, FILTER_RGGB, 8);

		makeGrayBlock(cd, 0, 0, 255);
		makeGrayBlock(cd, DIM, 0, 224);
		makeGrayBlock(cd, 2 * DIM, 0, 191);
		makeGrayBlock(cd, 0, DIM, 159);
		makeGrayBlock(cd, 2 * DIM, DIM, 127);
		makeGrayBlock(cd, 0, 2 * DIM, 95);
		makeGrayBlock(cd, DIM, 2 * DIM, 63);
		makeGrayBlock(cd, 2 * DIM, 2 * DIM, 0);

		final int xmax = DIM << 1;
		final int ymax = DIM << 1;

		for (int y = DIM; y < ymax; ++y)
			for (int x = DIM; x < xmax; ++x)
				cd.setSample(x, y, (y > x ? 255 : ((y == x) ? 127 : 0)));

		return cd;
	}

	private static void makeGrayBlock(CaptureData cd, int dx, int dy, int value) {
		if (value > 255 || value < 0)
			throw new RuntimeException("Can't have a value of " + value);

		final int xmax = dx + DIM;
		final int ymax = dy + DIM;

		for (int y = dy; y < ymax; ++y)
			for (int x = dx; x < xmax; ++x)
				cd.setSample(x, y, value);
	}
}
