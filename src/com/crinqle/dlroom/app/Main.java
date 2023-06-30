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


public class Main extends JFrame implements ActionListener, WindowListener, LUTChangeListener, ListSelectionListener, RawRasterSelectionListener {
    /*
     * File menu
     */
    private final JMenuItem newItem = new JMenuItem("New...", 'n');
    private final JMenuItem openItem = new JMenuItem("Open...", 'o');
    private final JMenuItem saveItem = new JMenuItem("Save", 's');
    private final JMenuItem saveAsItem = new JMenuItem("Save as...");
    private final JMenuItem quitItem = new JMenuItem("Quit", 'q');

    /*
     * Edit menu
     */
    private final JMenuItem redoItem = new JMenuItem("Redo", 'y');
    private final JMenuItem undoItem = new JMenuItem("Undo", 'u');
    private final JMenuItem applyItem = new JMenuItem("Apply Curves", ' ');

    /*
     * Color menu
     */
    private final JMenuItem levelsItem = new JMenuItem("Levels...", 'l');
    private final JMenuItem curvesItem = new JMenuItem("Curves...", 'c');
    private final JMenuItem monProfItem = new JMenuItem("Set Monitor Profile...", 'm');
    private final JMenuItem imageWSItem = new JMenuItem("Set Image Working Space...", 'i');

    /*
     * Raw menu
     */
    private final JMenu rawMenu = new JMenu("Raw");
    private final JMenuItem biasItem = new JMenuItem("Array Element Bias...", 'b');
    private final JMenuItem interpItem = new JMenuItem("Interpolate", 'i');

    // private JMenuItem rawExportItem = new JMenuItem("Export...", 'e');
    // private JMenuItem monGammaItem = new JMenuItem("Adjust Monitor Gamma", 'g');
    // private JMenuItem rawImportItem = new JMenuItem("Import...", 'i');

    private final String prefsName = ".clearrc";
    private Prefs prefs = null;

    private File workingDir = null;

    private String monProfilePath = null;
    private String wsProfilePath = null;
    private ICC_Profile monProfile = null;
    private ICC_Profile wsProfile = null;

    /*
     * Undo/Redo, current image, current temp image
     */
    private RasterStack undoStack = new RasterStack();
    private RasterStack redoStack = new RasterStack();
    private RawRaster rr = null;
    private RawRaster rrtemp = null;
    private RawRaster srr = null;

    /*
     * Listeners...
     */
    private Collection<BitDepthChangeListener> bdcls = new LinkedList<BitDepthChangeListener>();

    /*
     * Panels...
     */
    private JScrollPane scroller;
    private JDesktopPane desktop;
    private JInternalFrame frame;

    // public static final ColorModel DISPLAY_COLOR_MODEL = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
    // private CME cme = CME.getInstance();

    // private ColorPanel cp = new ColorPanel();
    // private JFileChooser fileChooser = null;

    // private CurvePanel curveR = null;
    // private CurvePanel curveG = null;
    // private CurvePanel curveB = null;
    // private CurvePanel curveComp = null;
    // private JTabbedPane curveTabs = null;

    // private JSlider levelR = null;
    // private JSlider levelG = null;
    // private JSlider levelB = null;
    // private JSlider levelH = null;
    // private JPanel levelPanel = null;


    public Main() {
        super("Java Raw Decoder");

        addWindowListener(this);

        loadPreferences();
        initMenus();
        // initEditPanels();

        FileChooserPanel fileChooser = new FileChooserPanel(workingDir);
        fileChooser.addListSelectionListener(this);

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(1, 1));
        imagePanel.setBackground(Color.white);
        imagePanel.setPreferredSize(new Dimension(2000, 1000));

        scroller = new JScrollPane(imagePanel);

        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, fileChooser, desktop);

        add(splitter);
    }


    /*
     * **************************************************
     *
     * INTERFACES
     *
     * **************************************************
     */


    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        /*
         * File menu
         */
        if (source == quitItem) shutdown();
        else if (source == newItem) f_new ();
		else if (source == openItem) openDialog();
        else if (source == saveItem) saveDialog();
        else if (source == saveAsItem) saveAsDialog();

            /*
             * Edit menu
             */
        else if (source == redoItem) redo();
        else if (source == undoItem) undo();
        else if (source == applyItem) apply();

            /*
             * Color menu
             */
        else if (source == levelsItem) levelsDialog();
        else if (source == curvesItem) curvesDialog();
        else if (source == monProfItem) monProfDialog();
        else if (source == imageWSItem) imageWorkingSpaceDialog();

            /*
             * Raw menu
             */
        else if (source == biasItem) {
            biasDialog();
        } else if (source == interpItem) {
            interpolate();
        }

        // else if ( source == rawExportItem ) rawExportDialog();
        // else if ( source == rawImportItem ) rawImportDialog();
    }


    public void valueChanged(ListSelectionEvent evt) {
        Object src = evt.getSource();

        if (!evt.getValueIsAdjusting()) {
            if (src instanceof JList) {
                JList list = (JList) src;
                Object obj = list.getSelectedValue();

                if (obj instanceof File) {
                    File file = (File) obj;

                    if (file.isFile()) {
                        System.err.println("  Trying to import raw file: " + file + "...");

                        loadImage(file);
                    }
                }
            }
        }
    }


    private void loadImage(File file) {
        try {
            RawCodec codec = RawCodec.getInstance(file);
            CaptureData cd = codec.decode();

            final int bits = cd.getBits();

            rr = new RawRaster(cd);
            rr.setProfile(wsProfile);

            rrtemp = null;
            undoStack.removeAllElements();
            redoStack.removeAllElements();

            fireBitDepthChangeEvent(cd, bits);

            frame = new JInternalFrame(file.getPath());
            desktop.add(frame);

            displayImage();
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }


    public void addBitDepthChangeListener(BitDepthChangeListener l) {
        bdcls.add(l);
    }

    public void fireBitDepthChangeEvent(Object source, int bits) {
        System.out.println("  bit depth change: " + bits);
        Iterator iter = bdcls.iterator();
        while (iter.hasNext()) ((BitDepthChangeListener) iter.next()).updateBits(source, bits);
    }


    public void applyLUT(Object source, LUT lut) {
        if (rr == null)
            return;

        if (rrtemp == null)
            rrtemp = rr.createCopy();

        rr.applyLUT(lut, rrtemp);

        displayImage(rrtemp);
    }


    public void subrasterSelected(Object source, RawRaster rr) {
        System.out.println("Main.subrasterSelected()");
        srr = rr;
    }


    public void windowClosing(WindowEvent evt) {
        shutdown();
    }

    public void windowClosed(WindowEvent evt) {
        shutdown();
    }

    public void windowOpened(WindowEvent evt) {
    }

    public void windowIconified(WindowEvent evt) {
    }

    public void windowDeiconified(WindowEvent evt) {
    }

    public void windowActivated(WindowEvent evt) {
    }

    public void windowDeactivated(WindowEvent evt) {
    }


    /*
     * **************************************************
     *
     * PRIVATE
     *
     * **************************************************
     */


    private void loadPreferences() {
        String prefsPath = System.getProperty("user.home") + File.separator + prefsName;

        System.err.println("Loading application prefs from: " + prefsPath);

        try {
            prefs = Prefs.loadFromFile(new File(prefsPath));

            String wsPrefix = prefs.get("Directories", "Working Spaces");
            String monPrefix = prefs.get("Directories", "Monitor Profiles");

            String wsName = prefs.get("Color Spaces", "Default Working Space");
            String monName = prefs.get("Color Spaces", "Default Monitor Space");

            String wsPath = wsPrefix + File.separator + wsName;
            String monPath = monPrefix + File.separator + monName;

            String wdPath = prefs.get("Directories", "Default Image Directory");

            wsProfilePath = wsPath;
            monProfilePath = monPath;

            System.err.println("  Loading default working space [" + wsProfilePath + "]...");
            System.err.println("  Loading default monitor profile [" + monProfilePath + "]...");

            wsProfile = ICC_Profile.getInstance(wsProfilePath);
            monProfile = ICC_Profile.getInstance(monProfilePath);

            workingDir = new File(wdPath);

            // cme.initDeviceLink(wsPath, monPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void initMenus() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        quitItem.addActionListener(this);

        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');
        editMenu.add(redoItem);
        editMenu.add(undoItem);
        editMenu.addSeparator();
        editMenu.add(applyItem);

        redoItem.addActionListener(this);
        undoItem.addActionListener(this);
        applyItem.addActionListener(this);

        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        applyItem.setAccelerator(KeyStroke.getKeyStroke(' ')); // KeyEvent.VK_Z, InputEvent.CTRL_MASK));

        JMenu colorMenu = new JMenu("Color");
        colorMenu.setMnemonic('c');
        colorMenu.add(levelsItem);
        colorMenu.add(curvesItem);
        colorMenu.addSeparator();
        colorMenu.add(monProfItem);
        colorMenu.add(imageWSItem);

        levelsItem.addActionListener(this);
        curvesItem.addActionListener(this);
        monProfItem.addActionListener(this);
        imageWSItem.addActionListener(this);

        levelsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        curvesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));

        rawMenu.setEnabled(false);
        rawMenu.setMnemonic('r');
        rawMenu.add(biasItem);
        rawMenu.add(interpItem);

        biasItem.addActionListener(this);
        interpItem.addActionListener(this);

        biasItem.setAccelerator(KeyStroke.getKeyStroke('b')); // KeyEvent.VK_B, InputEvent.CTRL_MASK));
        interpItem.setAccelerator(KeyStroke.getKeyStroke('i')); // KeyEvent.VK_I, InputEvent.CTRL_MASK));

        JMenu spacerMenu = new JMenu();
        spacerMenu.setEnabled(false);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(colorMenu);
        menuBar.add(spacerMenu);
        menuBar.add(rawMenu);

        setJMenuBar(menuBar);

        // monGammaItem.addActionListener(this);
        // rawMenu.add(rawExportItem);
        // colorMenu.add(monGammaItem);
        // monGammaItem.setAccelerator(KeyStroke.getKeyStroke('g')); // KeyEvent.VK_G, InputEvent.CTRL_MASK));
        // undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        // undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyChar(KeyEvent.VK_ESCAPE))); // KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        // rawMenu.add(rawImportItem);
        // rawExportItem.addActionListener(this);
        // rawImportItem.addActionListener(this);
    }


    private void initEditPanels() {
        // JPanel dirPanel = new FileListPanel(workingDir, FileListPanel.DIRS);
        // JPanel filePanel = new FileListPanel(workingDir);
        FileChooserPanel fileChooser = new FileChooserPanel(workingDir);
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
        imagePanel.setLayout(new GridLayout(1, 1));
        imagePanel.setBackground(Color.white);
        imagePanel.setPreferredSize(new Dimension(2000, 1000));

        scroller = new JScrollPane(imagePanel);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(editPanel, BorderLayout.WEST);
        content.add(scroller, BorderLayout.CENTER);

        add(content);
        // setContentPane(content);
    }


    private void apply() {
        if (rrtemp != null) {
            if (rrtemp == null) return;
            push();
            rr = rrtemp;
            rrtemp = null;
        }
    }

    private void interpolate() {
        if (rr == null) return;
        try {
            push();
            rr = rr.createCopy();
            rr.interpolate(ALL_MASK);
            displayImage();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void push() {
        undoStack.push(rr);
    }

    private void undo() {
        if (undoStack.empty()) return;
        redoStack.push(rr);
        rr = undoStack.pop();
        displayImage();
    }

    private void redo() {
        if (redoStack.empty()) return;
        undoStack.push(rr);
        rr = redoStack.pop();
        displayImage();
    }


    private void displayImage(RawRaster raster) {
        // raster = colorConvert(raster);

        ColorPanel cp = new ColorPanel();
        cp.addRawRasterSelectionListener(this);
        cp.setRawRaster(raster);

        // JScrollPanel scroller = new JScrollPane(cp);
        // JViewport vp = scroller.getViewport();

        JViewport vp = scroller.getViewport();

        Point save = vp.getViewPosition();
        vp.setView(cp);
        vp.setViewPosition(save);

        // scroller.repaint();

        frame.add(scroller);
        try {
            frame.setMaximum(true);
        } catch (Exception e) {
        }
        frame.setVisible(true);
    }

    private void displayImage() {
        displayImage(rr);
    }


    private void levelsDialog() {
        System.out.println("  Adjusting levels...");
    }

    private void curvesDialog() {
        System.out.println("  Adjusting curves...");
    }

    private void biasDialog() {
        System.out.println("  Adjusting levels...");
    }


    private void monProfDialog() {
        JFileChooser dialog = new JFileChooser(".");

        int status = dialog.showOpenDialog(this);

        if (status != JFileChooser.APPROVE_OPTION)
            return;

        final File file = dialog.getSelectedFile();
        final String filename = file.getPath();

        System.err.println("Loading monitor profile " + filename + "...");

        monProfilePath = filename;
    }

    private void imageWorkingSpaceDialog() {
        JFileChooser dialog = new JFileChooser(".");

        int status = dialog.showOpenDialog(this);

        if (status != JFileChooser.APPROVE_OPTION)
            return;

        final File file = dialog.getSelectedFile();
        final String filename = file.getPath();

        System.err.println("Loading image color space " + filename + "...");

        wsProfilePath = filename;
    }


    private void f_new()
    {
        if (srr == null)
            return;

        ICC_Profile profile = rr.getProfile();

        rr = srr;
        rr.setProfile(profile);

        rrtemp = null;
        srr = null;

        undoStack.removeAllElements();
        redoStack.removeAllElements();

        fireBitDepthChangeEvent(rr, rr.getBits());

        displayImage();
    }

    private void openDialog() {
        if (wsProfilePath == null)
            imageWorkingSpaceDialog();

        JFileChooser dialog = new JFileChooser(".");

        int status = dialog.showOpenDialog(this);

        if (status != JFileChooser.APPROVE_OPTION)
            return;

        File file = dialog.getSelectedFile();
        final String filename = file.getPath() + ".cpf";
        file = new File(filename);

        System.err.println("Opening CPF " + file.getPath() + "...");

        try {
            final long length = file.length();
            byte[] array = new byte[(int) length];

            System.err.println("  Size of CPF: " + length);
            System.err.println("  Created large array for CPF processing: (" + (int) length + " bytes).");

            DataInputStream stream = new DataInputStream(new FileInputStream(file));
            stream.readFully(array);
            stream.close();

            rr = ColorPPMFile.decode(array); // rr = ColorPPMFile.decode(new FileInputStream(filename));
            rr.setProfile(wsProfile);

            /*
             * CMS change.
             */
            // rr = rr.get3ColorRGBRaster();

            displayImage();
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }


    private void rawExportDialog() {
        JFileChooser dialog = new JFileChooser(".");

        int status = dialog.showSaveDialog(this);

        if (status != JFileChooser.APPROVE_OPTION)
            return;

        final File file = dialog.getSelectedFile();
        final String filename = file.getPath() + ".cpf";

        System.err.println("Opening " + filename + "...");

        try {
            FileOutputStream stream = new FileOutputStream(new File(filename));

            ColorPPMFile.encode(rr, stream);
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }


    private void saveDialog() {
        saveAsDialog();
    }

    private void saveAsDialog() {
        JFileChooser dialog = new JFileChooser(".");

        int status = dialog.showSaveDialog(this);

        if (status != JFileChooser.APPROVE_OPTION)
            return;

        final File file = dialog.getSelectedFile();
        final String filename = file.getPath() + ".cpf";

        System.err.println("Saving " + filename + "...");

        try {
            System.err.println("Saving as: raster: " + rr);

            FileOutputStream stream = new FileOutputStream(new File(filename));

            ColorPPMFile.encode(rr, stream);
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }


    private void shutdown() { /* if ( cme != null ) { try { cme.dispose(); } catch ( Exception e ) { e.printStackTrace(); } } */
        dispose();
        System.exit(0);
    }


    /*
     * **************************************************
     *
     * MAIN
     *
     * **************************************************
     */


    public static void main(String[] args) {
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


    private RawRaster colorConvert(RawRaster src) {
        // if ( cme == null )
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

			cme.colorConvert(input, size, output);

			SampleModel sm = DISPLAY_COLOR_MODEL.createCompatibleSampleModel(width, height);
			return Raster.createWritableRaster(sm, idb, new Point(0,0));
		}
		catch ( Exception e ) { e.printStackTrace(); System.err.println(1); }
		*/

        return src;
    }
}
