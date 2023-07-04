package com.crinqle.dlroom;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;



public class FileListPanel extends JPanel implements MouseListener
{
    public static final Color DIR_SELECTED_COLOR  = new Color(192, 255, 192);
    public static final Color FILE_SELECTED_COLOR = new Color(192, 255, 255);

    public static final int FILES = 0;
    public static final int DIRS  = 1;
    public static final int ALL   = 2;

    private final int         f_type;
    private       File        f_dir;
    private       JList<File> f_list = null;


    public FileListPanel ( File dir )
    {
        // super(dir.getPath());

        f_type = FILES;

        ctor(dir);
    }

    public FileListPanel ( File dir, int type )
    {
        // super(dir.getPath());

        System.out.println("FileListPanel.ctor() - dir: " + dir.getAbsolutePath());

        f_type = type;

        ctor(dir);
    }

    private void ctor ( File dir )
    {
        f_dir = dir;

        final DefaultListModel<File> listModel = updateFiles(dir);

        f_list = new JList<>(listModel);
        f_list.addMouseListener(this);

        Color selectedColor = null;

        switch ( f_type )
        {
            case FILES:
                selectedColor = FILE_SELECTED_COLOR;
                break;
            case DIRS:
                selectedColor = DIR_SELECTED_COLOR;
                break;
        }

        f_list.setCellRenderer(new FileRenderer(selectedColor));
        f_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Border empty = new EmptyBorder(10,10,10,10);
        // Border bevel = new BevelBorder(BevelBorder.LOWERED);
        // Border border = new CompoundBorder(empty, bevel);
        // setBorder(bevel);

        setLayout(new java.awt.GridLayout(1, 1));
        add(f_list);
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

    public void addListSelectionListener ( ListSelectionListener l )
    {
        f_list.addListSelectionListener(l);
    }

    public DefaultListModel<File> updateFiles ( File dir )
    {
        System.out.println("dir: " + dir);

        f_dir = new File(dir.getAbsolutePath());

        final File[] entries = f_dir.listFiles();

        System.out.println("(null) entries? " + (null == entries));

        final DefaultListModel<File> listModel = new DefaultListModel<>();

        if ( null == entries ) return listModel;

        System.out.println("How many entries? " + entries.length);

        final int count  = entries.length;
        File      parent = new File(f_dir.getPath() + File.separator + "..");

        Vector<File> list = new Vector<File>();

        for ( File entry : entries )
        {
            System.out.println("  entry: " + entry.getAbsolutePath());

            if ( entry.isHidden() )
                continue;

            switch ( f_type )
            {
                case FILES:
                    if ( entry.isFile() )
                    {
                        String lcfilename = entry.getName().toLowerCase();
                        if ( lcfilename.endsWith("crw") )
                        {
                            System.out.println("Found CRW; adding...");
                            listModel.addElement(entry);
                            //list.add(entry);
                        }
                    }
                    break;

                case DIRS:
                    if ( entry.isDirectory() )
                    {
                        System.out.println("Found <dir>; adding...");
                        //list.add(entry);
                        listModel.addElement(entry);
                    }
                    break;
            }
        }

        return listModel;
    }

    File getSelectedValue ()
    {
        return f_list.getSelectedValue();
    }

    void setSelectedIndex ( int index )
    {
        f_list.setSelectedIndex(index);
    }
}


class FileRenderer extends JTextField implements ListCellRenderer<File>
{
    private final Color f_sc;

    public FileRenderer ( Color sc )
    {
        f_sc = sc;
        setOpaque(true);
        setEditable(false);
    }
    @Override
    public Component getListCellRendererComponent ( JList<? extends File> list, File value, int index, boolean isSelected, boolean cellHasFocus )
    {
        final String filename = value.getName();

        System.out.println("  showing file: " + filename);

        setText(filename);
        setBorder(null);
        setBackground(isSelected ? f_sc : Color.white);

        return this;
    }

//    public Component getListCellRendererComponent ( JList<File> list, Object value,
//                                                    int index, boolean isSelected, boolean cellHasFocus )
//    {
//        if ( value instanceof File )
//            setText(((File)value).getName());
//
//        setBorder(null);
//        setBackground(isSelected ? f_sc : Color.white);
//
//        return this;
//    }
}
