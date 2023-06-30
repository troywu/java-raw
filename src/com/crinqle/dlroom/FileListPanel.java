package com.crinqle.dlroom;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionListener;



public class FileListPanel extends JPanel implements MouseListener
{
    public static final Color DIR_SELECTED_COLOR  = new Color(255, 192, 192);
    public static final Color FILE_SELECTED_COLOR = new Color(192, 255, 255);

    public static final int FILES = 0;
    public static final int DIRS  = 1;
    public static final int ALL   = 2;

    private final int   f_type;
    private       File  f_dir;
    private       JList f_list = null;

    public FileListPanel ( File dir )
    {
        // super(dir.getPath());

        f_type = FILES;

        ctor(dir);
    }

    public FileListPanel ( File dir, int type )
    {
        // super(dir.getPath());

        f_type = type;

        ctor(dir);
    }

    private void ctor ( File dir )
    {
        f_dir  = dir;
        f_list = new JList();
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

        updateFiles(dir);

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

    public void updateFiles ( File dir )
    {
        System.out.println("dir: " + dir);

        f_dir = new File(dir.getAbsolutePath());

        File[]    entries = f_dir.listFiles();
        final int count   = entries.length;
        File      parent  = new File(f_dir.getPath() + File.separator + "..");

        Vector<File> list = new Vector<File>();

        for ( int i = 0; i < count; ++i )
        {
            if ( entries[i].isHidden() )
                continue;

            switch ( f_type )
            {
                case FILES:
                    if ( entries[i].isFile() )
                    {
                        String lcfilename = entries[i].getName().toLowerCase();
                        if ( lcfilename.endsWith("crw") )
                            list.add(entries[i]);
                    }
                    break;

                case DIRS:
                    if ( entries[i].isDirectory() )
                        list.add(entries[i]);
                    break;
            }
        }

        f_list.setListData(list);
    }

    Object getSelectedValue ()
    {
        return f_list.getSelectedValue();
    }

    void setSelectedIndex ( int index )
    {
        f_list.setSelectedIndex(index);
    }
}


class FileRenderer extends JTextField implements ListCellRenderer
{
    private final Color f_sc;

    public FileRenderer ( Color sc )
    {
        f_sc = sc;
        setOpaque(true);
        setEditable(false);
    }

    public Component getListCellRendererComponent ( JList list, Object value,
                                                    int index, boolean isSelected, boolean cellHasFocus )
    {
        if ( value instanceof File )
            setText(((File)value).getName());

        setBorder(null);
        setBackground(isSelected ? f_sc : Color.white);

        return this;
    }
}
