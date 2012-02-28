package com.crinqle.dlroom.app;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import static com.crinqle.dlroom.Const.*;
import com.crinqle.dlroom.*;
import com.crinqle.dlroom.codec.*;
import com.crinqle.dlroom.event.*;
import com.crinqle.dlroom.math.*;
import com.crinqle.dlroom.util.*;


public class Main extends JFrame implements ActionListener, WindowListener, LUTChangeListener, ListSelectionListener, RawRasterSelectionListener
{
	/*
	 * File menu
	 */
	private JMenuItem f_newItem = new JMenuItem("New...", 'n');
	private JMenuItem f_openItem = new JMenuItem("Open...", 'o');
	private JMenuItem f_saveItem = new JMenuItem("Save", 's');
	private JMenuItem f_saveAsItem = new JMenuItem("Save as...");
	private JMenuItem f_quitItem = new JMenuItem("Quit", 'q');

	/*
	 * Edit menu
	 */
	private JMenuItem f_redoItem = new JMenuItem("Redo", 'y');
	private JMenuItem f_undoItem = new JMenuItem("Undo", 'u');
	private JMenuItem f_applyItem = new JMenuItem("Apply Curves", ' ');

	/*
	 * Color menu
	 */
	private JMenuItem f_levelsItem = new JMenuItem("Levels...", 'l');
	private JMenuItem f_curvesItem = new JMenuItem("Curves...", 'c');
	private JMenuItem f_monProfItem = new JMenuItem("Set Monitor Profile...", 'm');
	private JMenuItem f_imageWSItem = new JMenuItem("Set Image Working Space...", 'i');

	/*
	 * Raw menu
	 */
	private JMenu f_rawMenu = new JMenu("Raw");
	private JMenuItem f_biasItem = new JMenuItem("Array Element Bias...", 'b');
	private JMenuItem f_interpItem = new JMenuItem("Interpolate", 'i');

	// private JMenuItem f_rawExportItem = new JMenuItem("Export...", 'e');
	// private JMenuItem f_monGammaItem = new JMenuItem("Adjust Monitor Gamma", 'g');
	// private JMenuItem f_rawImportItem = new JMenuItem("Import...", 'i');

	private final String f_prefsName = ".clearrc";
	private Prefs f_prefs = null;

	private File f_workingDir = null;

	private String f_monProfilePath = null;
	private String f_wsProfilePath = null;
	private ICC_Profile f_monProfile = null;
	private ICC_Profile f_wsProfile = null;

	/*
	 * Undo/Redo, current image, current temp image
	 */
	private RasterStack f_undoStack = new RasterStack();
	private RasterStack f_redoStack = new RasterStack();
	private RawRaster f_rr = null;
	private RawRaster f_rrtemp = null;
	private RawRaster f_srr = null;

	/*
	 * Listeners...
	 */
	private Collection<BitDepthChangeListener> f_bdcls = new LinkedList<BitDepthChangeListener>();

	/*
	 * Panels...
	 */
	private JScrollPane f_scroller;
	private JDesktopPane f_desktop;
	private JInternalFrame f_frame;

	// public static final ColorModel DISPLAY_COLOR_MODEL = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
	// private CME f_cme = CME.getInstance();

	// private ColorPanel f_cp = new ColorPanel();
	// private JFileChooser f_fileChooser = null;

	// private CurvePanel f_curveR = null;
	// private CurvePanel f_curveG = null;
	// private CurvePanel f_curveB = null;
	// private CurvePanel f_curveComp = null;
	// private JTabbedPane f_curveTabs = null;

	// private JSlider f_levelR = null;
	// private JSlider f_levelG = null;
	// private JSlider f_levelB = null;
	// private JSlider f_levelH = null;
	// private JPanel f_levelPanel = null;


	public Main()
	{
		super("Digital Lightroom, Baby!");

		addWindowListener(this);

		f_loadPreferences();
		f_initMenus();
		// f_initEditPanels();

		FileChooserPanel fileChooser = new FileChooserPanel(f_workingDir);
		fileChooser.addListSelectionListener(this);

		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new GridLayout(1,1));
		imagePanel.setBackground(Color.white);
		imagePanel.setPreferredSize(new Dimension(2000, 1000));

		f_scroller = new JScrollPane(imagePanel);

		f_desktop = new JDesktopPane();
		f_desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, fileChooser, f_desktop);

		add(splitter);
	}


	/*
	 * **************************************************
	 *
	 * INTERFACES
	 *
	 * **************************************************
	 */


	public void actionPerformed ( ActionEvent evt )
	{
		Object source = evt.getSource();

		/*
		 * File menu
		 */
		if ( source == f_quitItem ) f_shutdown();
		else if ( source == f_newItem ) f_new();
		else if ( source == f_openItem ) f_openDialog();
		else if ( source == f_saveItem ) f_saveDialog();
		else if ( source == f_saveAsItem ) f_saveAsDialog();

		/*
		 * Edit menu
		 */
		else if ( source == f_redoItem ) f_redo();
		else if ( source == f_undoItem ) f_undo();
		else if ( source == f_applyItem ) f_apply();

		/*
		 * Color menu
		 */
		else if ( source == f_levelsItem ) f_levelsDialog();
		else if ( source == f_curvesItem ) f_curvesDialog();
		else if ( source == f_monProfItem ) f_monProfDialog();
		else if ( source == f_imageWSItem ) f_imageWorkingSpaceDialog();

		/*
		 * Raw menu
		 */
		else if ( source == f_biasItem ) { f_biasDialog(); }
		else if ( source == f_interpItem ) { f_interpolate(); }

		// else if ( source == f_rawExportItem ) f_rawExportDialog();
		// else if ( source == f_rawImportItem ) f_rawImportDialog();
	}


	public void valueChanged ( ListSelectionEvent evt )
	{
		Object src = evt.getSource();

		if ( ! evt.getValueIsAdjusting() )
		{
			if ( src instanceof JList )
			{
				JList list = (JList)src;
				Object obj = list.getSelectedValue();

				if ( obj instanceof File )
				{
					File file = (File)obj;

					if ( file.isFile() )
					{
						System.err.println("  Trying to import raw file: " + file + "...");

						f_loadImage(file);
					}
				}
			}
		}
	}


	private void f_loadImage ( File file )
	{
		try
		{
			RawCodec codec = RawCodec.getInstance(file);
			CaptureData cd = codec.decode();

			final int bits = cd.getBits();

			f_rr = new RawRaster(cd);
			f_rr.setProfile(f_wsProfile);

			f_rrtemp = null;
			f_undoStack.removeAllElements();
			f_redoStack.removeAllElements();

			fireBitDepthChangeEvent(cd, bits);

			f_frame = new JInternalFrame(file.getPath());
			f_desktop.add(f_frame);

			f_displayImage();
		}
		catch ( Exception e ) { e.printStackTrace(); f_shutdown(); }
	}


	public void addBitDepthChangeListener ( BitDepthChangeListener l ) { f_bdcls.add(l); }
	public void fireBitDepthChangeEvent ( Object source, int bits ) { System.out.println("  bit depth change: " + bits); Iterator iter = f_bdcls.iterator(); while ( iter.hasNext() ) ((BitDepthChangeListener)iter.next()).updateBits(source, bits); }


	public void applyLUT ( Object source, LUT lut )
	{
		if ( f_rr == null )
			return;

		if ( f_rrtemp == null )
			f_rrtemp = f_rr.createCopy();

		f_rr.applyLUT(lut, f_rrtemp);

		f_displayImage(f_rrtemp);
	}


	public void subrasterSelected ( Object source, RawRaster rr ) { System.out.println("Main.subrasterSelected()"); f_srr = rr; }


	public void windowClosing( WindowEvent evt ) { f_shutdown(); }
	public void windowClosed ( WindowEvent evt ) { f_shutdown(); }
	public void windowOpened ( WindowEvent evt ) {}
	public void windowIconified ( WindowEvent evt ) {}
	public void windowDeiconified ( WindowEvent evt ) {}
	public void windowActivated ( WindowEvent evt ) {}
	public void windowDeactivated ( WindowEvent evt ) {}


	/*
	 * **************************************************
	 *
	 * PRIVATE
	 *
	 * **************************************************
	 */


	private void f_loadPreferences()
	{
		String prefsPath = System.getProperty("user.home") + File.separator + f_prefsName;

		System.err.println("Loading application prefs from: " + prefsPath);

		try
		{
			f_prefs = Prefs.loadFromFile(new File(prefsPath));

			String wsPrefix = f_prefs.get("Directories", "Working Spaces");
			String monPrefix = f_prefs.get("Directories", "Monitor Profiles");

			String wsName = f_prefs.get("Color Spaces", "Default Working Space");
			String monName = f_prefs.get("Color Spaces", "Default Monitor Space");

			String wsPath = wsPrefix + File.separator + wsName;
			String monPath = monPrefix + File.separator + monName;

			String wdPath = f_prefs.get("Directories", "Default Image Directory");

			f_wsProfilePath = wsPath;
			f_monProfilePath = monPath;

			System.err.println("  Loading default working space [" + f_wsProfilePath + "]...");
			System.err.println("  Loading default monitor profile [" + f_monProfilePath + "]...");

			f_wsProfile = ICC_Profile.getInstance(f_wsProfilePath);
			f_monProfile = ICC_Profile.getInstance(f_monProfilePath);

			f_workingDir = new File(wdPath);

			// f_cme.initDeviceLink(wsPath, monPath);
		}
		catch ( Exception e ) { e.printStackTrace(); System.exit(1); }
	}


	private void f_initMenus()
	{
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(f_newItem);
		fileMenu.add(f_openItem);
		fileMenu.add(f_saveItem);
		fileMenu.add(f_saveAsItem);
		fileMenu.addSeparator();
		fileMenu.add(f_quitItem);

		f_newItem.addActionListener(this);
		f_openItem.addActionListener(this);
		f_saveItem.addActionListener(this);
		f_saveAsItem.addActionListener(this);
		f_quitItem.addActionListener(this);

		f_newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		f_openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		f_saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		f_quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');
		editMenu.add(f_redoItem);
		editMenu.add(f_undoItem);
		editMenu.addSeparator();
		editMenu.add(f_applyItem);

		f_redoItem.addActionListener(this);
		f_undoItem.addActionListener(this);
		f_applyItem.addActionListener(this);

		f_redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		f_undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		f_applyItem.setAccelerator(KeyStroke.getKeyStroke(' ')); // KeyEvent.VK_Z, InputEvent.CTRL_MASK));

		JMenu colorMenu = new JMenu("Color");
		colorMenu.setMnemonic('c');
		colorMenu.add(f_levelsItem);
		colorMenu.add(f_curvesItem);
		colorMenu.addSeparator();
		colorMenu.add(f_monProfItem);
		colorMenu.add(f_imageWSItem);

		f_levelsItem.addActionListener(this);
		f_curvesItem.addActionListener(this);
		f_monProfItem.addActionListener(this);
		f_imageWSItem.addActionListener(this);

		f_levelsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		f_curvesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));

		f_rawMenu.setEnabled(false);
		f_rawMenu.setMnemonic('r');
		f_rawMenu.add(f_biasItem);
		f_rawMenu.add(f_interpItem);

		f_biasItem.addActionListener(this);
		f_interpItem.addActionListener(this);

		f_biasItem.setAccelerator(KeyStroke.getKeyStroke('b')); // KeyEvent.VK_B, InputEvent.CTRL_MASK));
		f_interpItem.setAccelerator(KeyStroke.getKeyStroke('i')); // KeyEvent.VK_I, InputEvent.CTRL_MASK));

		JMenu spacerMenu = new JMenu();
		spacerMenu.setEnabled(false);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(colorMenu);
		menuBar.add(spacerMenu);
		menuBar.add(f_rawMenu);

		setJMenuBar(menuBar);

		// f_monGammaItem.addActionListener(this);
		// f_rawMenu.add(f_rawExportItem);
		// colorMenu.add(f_monGammaItem);
		// f_monGammaItem.setAccelerator(KeyStroke.getKeyStroke('g')); // KeyEvent.VK_G, InputEvent.CTRL_MASK));
		// f_undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		// f_undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyChar(KeyEvent.VK_ESCAPE))); // KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		// f_rawMenu.add(f_rawImportItem);
		// f_rawExportItem.addActionListener(this);
		// f_rawImportItem.addActionListener(this);
	}


	private void f_initEditPanels()
	{
		// JPanel dirPanel = new FileListPanel(f_workingDir, FileListPanel.DIRS);
		// JPanel filePanel = new FileListPanel(f_workingDir);
		FileChooserPanel fileChooser = new FileChooserPanel(f_workingDir);
		fileChooser.addListSelectionListener(this);

		final int bits = 8;

		CurvePanel curveR = new CurvePanel(R, bits);
		CurvePanel curveG = new CurvePanel(G, bits);
		CurvePanel curveB = new CurvePanel(B, bits);
		// CurvePanel curveComp = new CurvePanel(ALL_MASK, bits);

		curveR.addLUTChangeListener(this);
		curveG.addLUTChangeListener(this);
		curveB.addLUTChangeListener(this);
		// curveComp.addLUTChangeListener(this);

		JTabbedPane curveTabs = new JTabbedPane();
		curveTabs.add("Red", curveR);
		curveTabs.add("Green", curveG);
		curveTabs.add("Blue", curveB);
		// curveTabs.add("<All>", curveComp);

		JPanel curvePanel = new EtchedTitledPanel("Curves");
		curvePanel.add(curveTabs);

		LevelSliderPanel levelR = new LevelSliderPanel(R, bits);
		LevelSliderPanel levelG = new LevelSliderPanel(G, bits);
		LevelSliderPanel levelB = new LevelSliderPanel(B, bits);
		LevelSliderPanel levelH = new LevelSliderPanel(H, bits);

		levelR.addLUTChangeListener(this);
		levelG.addLUTChangeListener(this);
		levelB.addLUTChangeListener(this);
		levelH.addLUTChangeListener(this);

		addBitDepthChangeListener(curveR);
		addBitDepthChangeListener(curveG);
		addBitDepthChangeListener(curveB);
		// addBitDepthChangeListener(curveComp);
		addBitDepthChangeListener(levelR);
		addBitDepthChangeListener(levelG);
		addBitDepthChangeListener(levelB);
		addBitDepthChangeListener(levelH);

		JPanel levelPanel = new EtchedTitledPanel("Linear Detector Bias");
		levelPanel.setLayout(new GridLayout(1, 4));
		levelPanel.add(levelR);
		levelPanel.add(levelG);
		levelPanel.add(levelB);
		levelPanel.add(levelH);

		GridBagLayout bag = new GridBagLayout();
		GridBagConstraints gc = new GridBagConstraints();
		JPanel editPanel = new JPanel();

		/*
		editPanel.setBorder(new EmptyBorder(10,10,10,10));
		// editPanel.setBackground(SWING_BLUE);
		editPanel.setLayout(bag);

		gc.fill = GridBagConstraints.BOTH;
		gc.gridwidth = 1;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0; // GridBagConstraints.RELATIVE;
		gc.gridy = 0; // GridBagConstraints.RELATIVE;
		bag.setConstraints(fileChooser, gc);
		// gc.gridx = 1;
		// bag.setConstraints(filePanel, gc);

		// gc.gridx = 0;
		gc.gridy = 1; // GridBagConstraints.RELATIVE;
		// gc.gridwidth = 2;
		bag.setConstraints(levelPanel, gc);

		gc.gridy = 2; // GridBagConstraints.RELATIVE;
		bag.setConstraints(curvePanel, gc);
		*/

		// editPanel.setLayout(new GridLayout(3,1));
		editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));

		// editPanel.add(dirPanel);
		// editPanel.add(filePanel);
		// editPanel.add(new JPanel());
		editPanel.add(fileChooser);
		editPanel.add(curvePanel);
		editPanel.add(levelPanel);

		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new GridLayout(1,1));
		imagePanel.setBackground(Color.white);
		imagePanel.setPreferredSize(new Dimension(2000, 1000));

		f_scroller = new JScrollPane(imagePanel);

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(editPanel, BorderLayout.WEST);
		content.add(f_scroller, BorderLayout.CENTER);

		add(content);
		// setContentPane(content);
	}


	private void f_apply() { if ( f_rrtemp != null ) { if ( f_rrtemp == null ) return; f_push(); f_rr = f_rrtemp; f_rrtemp = null; } }
	private void f_interpolate() { if ( f_rr == null ) return; try { f_push(); f_rr = f_rr.createCopy(); f_rr.interpolate(ALL_MASK); f_displayImage(); } catch ( Exception e ) { e.printStackTrace(); System.exit(1); } }


	private void f_push() { f_undoStack.push(f_rr); }
	private void f_undo() { if ( f_undoStack.empty() ) return; f_redoStack.push(f_rr); f_rr = f_undoStack.pop(); f_displayImage(); }
	private void f_redo() { if ( f_redoStack.empty() ) return; f_undoStack.push(f_rr); f_rr = f_redoStack.pop(); f_displayImage(); }


	private void f_displayImage ( RawRaster raster )
	{
		// raster = f_colorConvert(raster);

		ColorPanel cp = new ColorPanel();
		cp.addRawRasterSelectionListener(this);
		cp.setRawRaster(raster);

		// JScrollPanel scroller = new JScrollPane(cp);
		// JViewport vp = scroller.getViewport();

		JViewport vp = f_scroller.getViewport();

		Point save = vp.getViewPosition();
		vp.setView(cp);
		vp.setViewPosition(save);

		// f_scroller.repaint();

		f_frame.add(f_scroller);
		try { f_frame.setMaximum(true); } catch ( Exception e ) {}
		f_frame.setVisible(true);
	}
	private void f_displayImage() { f_displayImage(f_rr); }


	private void f_levelsDialog() { System.out.println("  Adjusting levels..."); }
	private void f_curvesDialog() { System.out.println("  Adjusting curves..."); }
	private void f_biasDialog() { System.out.println("  Adjusting levels..."); }


	private void f_monProfDialog()
	{
		JFileChooser dialog = new JFileChooser(".");

		int status = dialog.showOpenDialog(this);

		if ( status != JFileChooser.APPROVE_OPTION )
			return;

		final File file = dialog.getSelectedFile();
		final String filename = file.getPath();

		System.err.println("Loading monitor profile " + filename + "...");

		f_monProfilePath = filename;
	}
	private void f_imageWorkingSpaceDialog()
	{
		JFileChooser dialog = new JFileChooser(".");

		int status = dialog.showOpenDialog(this);

		if ( status != JFileChooser.APPROVE_OPTION )
			return;

		final File file = dialog.getSelectedFile();
		final String filename = file.getPath();

		System.err.println("Loading image color space " + filename + "...");

		f_wsProfilePath = filename;
	}


	private void f_new()
	{
		if ( f_srr == null )
			return;

		ICC_Profile profile = f_rr.getProfile();

		f_rr = f_srr;
		f_rr.setProfile(profile);

		f_rrtemp = null;
		f_srr = null;

		f_undoStack.removeAllElements();
		f_redoStack.removeAllElements();

		fireBitDepthChangeEvent(f_rr, f_rr.getBits());

		f_displayImage();
	}
	private void f_openDialog()
	{
		if ( f_wsProfilePath == null )
			f_imageWorkingSpaceDialog();

		JFileChooser dialog = new JFileChooser(".");

		int status = dialog.showOpenDialog(this);

		if ( status != JFileChooser.APPROVE_OPTION )
			return;

		File file = dialog.getSelectedFile();
		final String filename = file.getPath() + ".cpf";
		file = new File(filename);

		System.err.println("Opening CPF " + file.getPath() + "...");

		try
		{
			final long length = file.length();
			byte[] array = new byte[(int)length];

			System.err.println("  Size of CPF: " + length);
			System.err.println("  Created large array for CPF processing: (" + (int)length + " bytes).");

			DataInputStream stream = new DataInputStream(new FileInputStream(file));
			stream.readFully(array);
			stream.close();

			f_rr = ColorPPMFile.decode(array); // f_rr = ColorPPMFile.decode(new FileInputStream(filename));
			f_rr.setProfile(f_wsProfile);

			/*
			 * CMS change.
			 */
			// f_rr = f_rr.get3ColorRGBRaster();

			f_displayImage();
		}
		catch ( Exception e ) { e.printStackTrace(); f_shutdown(); }
	}


	private void f_rawExportDialog()
	{
		JFileChooser dialog = new JFileChooser(".");

		int status = dialog.showSaveDialog(this);

		if ( status != JFileChooser.APPROVE_OPTION )
			return;

		final File file = dialog.getSelectedFile();
		final String filename = file.getPath() + ".cpf";

		System.err.println("Opening " + filename + "...");

		try
		{
			FileOutputStream stream = new FileOutputStream(new File(filename));

			ColorPPMFile.encode(f_rr, stream);
		}
		catch ( Exception e ) { e.printStackTrace(); f_shutdown(); }
	}


	private void f_saveDialog() { f_saveAsDialog(); }
	private void f_saveAsDialog()
	{
		JFileChooser dialog = new JFileChooser(".");

		int status = dialog.showSaveDialog(this);

		if ( status != JFileChooser.APPROVE_OPTION )
			return;

		final File file = dialog.getSelectedFile();
		final String filename = file.getPath() + ".cpf";

		System.err.println("Saving " + filename + "...");

		try
		{
			System.err.println("Saving as: raster: " + f_rr);

			FileOutputStream stream = new FileOutputStream(new File(filename));

			ColorPPMFile.encode(f_rr, stream);
		}
		catch ( Exception e ) { e.printStackTrace(); f_shutdown(); }
	}


	private void f_shutdown() { /* if ( f_cme != null ) { try { f_cme.dispose(); } catch ( Exception e ) { e.printStackTrace(); } } */ dispose(); System.exit(0); }


	/*
	 * **************************************************
	 *
	 * MAIN
	 *
	 * **************************************************
	 */


	public static void main ( String[] args )
	{
		Main app = new Main();

		app.setSize(2048, 2048);
		Methods.centerWindow(app);

		// app.pack();
		app.setVisible(true);
	}


		/*
		TiledImage image = new TiledImage(0, 0, raster.getWidth(), raster.getHeight(), 0, 0, raster.getSampleModel(), DISPLAY_COLOR_MODEL);
		image.setData(raster);

		ImageLayout layout = new ImageLayout(image);
		layout.setTileWidth(200);
		layout.setTileHeight(150);
		RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);

		RenderedOp op = JAI.create("format", pb, hints);

		BufferedImage image = new BufferedImage(CMS.COLOR_MODEL, raster, false, null);

		JPanel panel = new ImagePanel(op);
		*/


	private RawRaster f_colorConvert ( RawRaster src )
	{
		// if ( f_cme == null )
		// return src;

		/*
		try
		{
			final int width = src.getWidth();
			final int height = src.getHeight();

			DataBufferUShort sdb = (DataBufferUShort)src.getDataBuffer();
			final int numBanks = sdb.getNumBanks();
			final int size = sdb.getSize();
			short[] input = new short[numBanks * size];

			for ( int i = 0; i < numBanks; ++i )
				System.arraycopy(sdb.getData(0), 0, input, i * size, size);

			DataBufferInt idb = new DataBufferInt(size);
			int[] output = idb.getData();

			f_cme.colorConvert(input, size, output);

			SampleModel sm = DISPLAY_COLOR_MODEL.createCompatibleSampleModel(width, height);
			return Raster.createWritableRaster(sm, idb, new Point(0,0));
		}
		catch ( Exception e ) { e.printStackTrace(); System.err.println(1); }
		*/

		return src;
	}
}
