package com.crinqle.dlroom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.color.ICC_Profile;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.crinqle.dlroom.event.RawRasterSelectionListener;

import static com.crinqle.dlroom.Const.*;

public class ColorPanel extends JPanel implements MouseListener,
		MouseMotionListener {
	private static final int TYPE_NONE = 0;
	private static final int TYPE_DISPLAY = 1;
	private static final int TYPE_PROOFING = 2;

	private int f_t = TYPE_NONE;
	private ICC_Profile f_disp;
	private ICC_Profile f_proof;
	private RawRaster f_rr;
	private BufferedImage f_bi;
	private WritableRaster f_wr;

	private Collection<RawRasterSelectionListener> f_listeners = new LinkedList<RawRasterSelectionListener>();

	private int f_sx1;
	private int f_sy1;
	private int f_sx2;
	private int f_sy2;
	private RawRaster f_srr = null;

	private boolean f_isSelecting = false;
	private boolean f_isSelected = false;

	public ColorPanel() {
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void addRawRasterSelectionListener(RawRasterSelectionListener l) {
		f_listeners.add(l);
	}

	private void fireRawRasterSelectionEvent() {
		if (f_srr == null)
			return;

		Iterator iter = f_listeners.iterator();

		while (iter.hasNext())
			((RawRasterSelectionListener) iter.next()).subrasterSelected(this,
					f_srr);
	}

	public void setAsDisplay(ICC_Profile displayProfile) {
		f_disp = displayProfile;
		f_t = TYPE_DISPLAY;
	}

	public void setAsProofing(ICC_Profile proofingProfile,
			ICC_Profile displayProfile) {
		f_proof = proofingProfile;
		f_disp = displayProfile;
		f_t = (TYPE_PROOFING | TYPE_DISPLAY);
	}

	public RawRaster getRawRaster() {
		return f_rr;
	}

	public void setRawRaster(RawRaster rr) {
		f_rr = rr;

		final int w = f_rr.getWidth();
		final int h = f_rr.getHeight();
		final int bits = f_rr.getBits();
		final int bc = f_rr.getBandCount();

		RawRaster copy = f_rr.createBlankCopy();

		if (bits != 8) {
			final int shift = Math.abs(bits - 8);

			// System.err.println("  Depth scaling...");

			for (int y = 0; y < h; ++y)
				for (int x = 0; x < w; ++x) {
					copy.setSample(x, y, R, f_rr.getSample(x, y, R) >> shift);
					copy.setSample(x, y, G, f_rr.getSample(x, y, G) >> shift);
					copy.setSample(x, y, B, f_rr.getSample(x, y, B) >> shift);
				}
		}

		f_bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		/*
		 * FIXME! This is just a placeholder until I figure out exactly what
		 * should go here. For now, I'll just set the Raster of the
		 * BufferedImage as a WritableRaster which uses the RawRaster object as
		 * a DataBuffer object.
		 */
		final int scanlineStride = w;
		final int[] bankIndices = { 0, 1, 2 };
		final int[] bandOffsets = { 0, 0, 0 };

		f_wr = Raster.createBandedRaster(copy, w, h, scanlineStride,
				bankIndices, bandOffsets, new Point(0, 0));

		f_bi.setData(f_wr);

		setPreferredSize(new Dimension(w, h));

		repaint();
	}

	public void mouseClicked(MouseEvent evt) {
		final int x = evt.getX();
		final int y = evt.getY();

	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

	public void mousePressed(MouseEvent evt) {
		f_sx1 = evt.getX();
		f_sy1 = evt.getY();

		// System.out.println("  Getting new subraster @ (" + f_sx1 + ", " +
		// f_sy1 + ")...");
	}

	public void mouseReleased(MouseEvent evt) {
		f_sx2 = evt.getX();
		f_sy2 = evt.getY();

		repaint();

		if (f_isSelecting) {
			System.out.println("  ==> Ending new subraster @ (" + f_sx2 + ", "
					+ f_sy2 + ")...");

			sortSelection();

			final int w = f_sx2 - f_sx1;
			final int h = f_sy2 - f_sy1;

			f_srr = f_rr.subraster(f_sx1, f_sy1, w, h);

			// DEBUG!! ImageData.println(rr);

			f_isSelecting = false;
			f_isSelected = true;

			fireRawRasterSelectionEvent();
		}
	}

	public void mouseDragged(MouseEvent evt) {
		f_isSelecting = true;
		f_isSelected = false;

		f_sx2 = evt.getX();
		f_sy2 = evt.getY();

		// System.out.println("  --> Moving new subraster to (" + f_sx2 + ", " +
		// f_sy2 + ")...");

		repaint();
	}

	public void mouseMoved(MouseEvent evt) {
	}

	private void sortSelection() {
		int temp;

		if (f_sx1 > f_sx2) {
			temp = f_sx1;
			f_sx1 = f_sx2;
			f_sx2 = temp;
		}
		if (f_sy1 > f_sy2) {
			temp = f_sy1;
			f_sy1 = f_sy2;
			f_sy2 = temp;
		}
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawRenderedImage(f_bi, null);

		if (f_isSelecting || f_isSelected) {
			if (f_sx1 == f_sx2 && f_sy1 == f_sy2)
				return;

			int temp;
			int sx1 = f_sx1;
			int sy1 = f_sy1;
			int sx2 = f_sx2;
			int sy2 = f_sy2;

			if (sx1 > sx2) {
				temp = sx1;
				sx1 = sx2;
				sx2 = temp;
			}
			if (sy1 > sy2) {
				temp = sy1;
				sy1 = sy2;
				sy2 = temp;
			}

			g2d.setXORMode(Color.white);
			g2d.drawRect(sx1, sy1, sx2 - sx1, sy2 - sy1);
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java dlroom.ColorPanel <raw filename>");
			System.exit(1);
		}

		try {
			com.crinqle.dlroom.codec.RawCodec codec = com.crinqle.dlroom.codec.RawCodec
					.getInstance(new java.io.File(args[0]));

			CaptureData cd = codec.decode();
			RawRaster rr = new RawRaster(cd);

			ColorPanel cp = new ColorPanel();
			cp.setRawRaster(rr);

			JScrollPane scroller = new JScrollPane(cp);

			JFrame frame = new JFrame("ColorPanel Test Driver");
			frame.setContentPane(scroller);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
