package com.crinqle.dlroom;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.crinqle.dlroom.util.EtchedTitledPanel;



public class FileChooserPanel
      extends EtchedTitledPanel
      implements ListSelectionListener, MouseListener
{
    private File          f_dir;
    private FileListPanel f_dp;
    private FileListPanel f_fp;
    private JLabel        f_dirLabel;

    public FileChooserPanel ( File dir )
    {
        super("Files");

        addMouseListener(this);

        System.out.println("Working dir: " + dir.getAbsolutePath());

        f_dir = dir;

        f_dp = new FileListPanel(f_dir, FileListPanel.DIRS);
        f_dp.addListSelectionListener(this);
        f_dp.setSelectedIndex(0);

        Object obj = f_dp.getSelectedValue();
        System.out.println("  FileListPanel.getSelectedValue(): " + obj);

        // WARN - OMG reusing this parameter?  WTF?
        final File selectedDir = (File)f_dp.getSelectedValue();

        System.out.println("WTF - Selected dir: " + selectedDir);

        f_fp       = new FileListPanel(selectedDir);
        f_dirLabel = new JLabel(dir.toString());
        Border b1 = new EmptyBorder(15, 15, 15, 15);
        Border b2 = new BevelBorder(BevelBorder.LOWERED);
        f_dirLabel.setBorder(new CompoundBorder(b1, b2));
        f_dirLabel.setHorizontalAlignment(JLabel.CENTER);

        JScrollPane dpScroller = new JScrollPane(f_dp);
        JScrollPane fpScroller = new JScrollPane(f_fp);

        dpScroller
              .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dpScroller
              .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        fpScroller
              .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fpScroller
              .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel dpLabel = new JLabel("Directories");
        JLabel fpLabel = new JLabel("Files");

        dpLabel.setBackground(Color.gray);
        fpLabel.setBackground(Color.gray);

        dpLabel.setHorizontalAlignment(JLabel.CENTER);
        fpLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel dpPanel = new JPanel();
        dpPanel.setLayout(new BoxLayout(dpPanel, BoxLayout.Y_AXIS));
        dpPanel.add(dpLabel);
        dpPanel.add(dpScroller);

        JPanel fpPanel = new JPanel();
        fpPanel.setLayout(new BoxLayout(fpPanel, BoxLayout.Y_AXIS));
        fpPanel.add(fpLabel);
        fpPanel.add(fpScroller);

        JPanel bothPanel = new JPanel();
        bothPanel.setLayout(new GridLayout(1, 2));
        bothPanel.add(dpPanel);
        bothPanel.add(fpPanel);

        setLayout(new BorderLayout());
        add(f_dirLabel, BorderLayout.NORTH);
        add(bothPanel);

        setMinimumSize(new Dimension(100, 400));
    }

    public void addListSelectionListener ( ListSelectionListener l )
    {
        f_fp.addListSelectionListener(l);
    }

    public void mouseClicked ( MouseEvent evt )
    {
        if ( evt.getButton() == MouseEvent.BUTTON3 )
            updateFiles(f_dir.getParentFile());
    }

    public void mouseEntered ( MouseEvent evt ) {}
    public void mouseExited ( MouseEvent evt ) {}
    public void mousePressed ( MouseEvent evt ) {}
    public void mouseReleased ( MouseEvent evt ) {}
    public void mouseDragged ( MouseEvent evt ) {}
    public void mouseMoved ( MouseEvent evt ) {}

    public void valueChanged ( ListSelectionEvent evt )
    {
        Object src = evt.getSource();

        if ( !evt.getValueIsAdjusting() )
        {
            if ( !(src instanceof JList<?>) ) return; // Ignore if not from JList

            JList<File> jlist = (JList<File>)src;

            final List<File> selectedValuesList = jlist.getSelectedValuesList();

            for ( File file : selectedValuesList )
                updateFiles(file);
        }
    }

    protected void updateFiles ( File file )
    {
        if ( file.isDirectory() )
        {
            f_dir = file;
            f_dirLabel.setText(file.toString());
            f_dp.updateFiles(file);
            f_fp.updateFiles(file);
        }
    }
}
