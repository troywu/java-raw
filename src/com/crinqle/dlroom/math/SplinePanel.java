package com.crinqle.dlroom.math;


import static com.crinqle.dlroom.Const.ALL_MASK;
import static com.crinqle.dlroom.Const.SLIDER_SIZE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.crinqle.dlroom.LUT;
import com.crinqle.dlroom.event.BitDepthChangeListener;
import com.crinqle.dlroom.event.LUTChangeListener;



public class SplinePanel extends JPanel implements MouseListener,
                                                   MouseMotionListener, BitDepthChangeListener
{
    private ControlCurve                  c;
    private Polygon                       p         = new Polygon();
    private int                           index     = -1;
    private LUT                           lut;
    private Collection<LUTChangeListener> listeners = new LinkedList<LUTChangeListener>();

    public SplinePanel ( int channel, int bits )
    {
        c = new NatCubic(channel, SLIDER_SIZE, bits);

        addMouseListener(this);
        addMouseMotionListener(this);

        Dimension size = new Dimension(SLIDER_SIZE, SLIDER_SIZE);

        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);

        setBackground(Color.white);
    }

    public void addLUTChangeListener ( LUTChangeListener l )
    {
        listeners.add(l);
    }

    protected void fireLUTChangeEvent ()
    {
        for ( LUTChangeListener l : listeners )
            l.applyLUT(this, lut);
    } // Iterator iter = listeners.iterator(); while ( iter.hasNext() )
    // ((LUTChangeListener)iter.next()).applyLUT(this, lut); }

    public LUT getLUT ()
    {
        return lut;
    }

    public void addPoint ( int x, int y )
    {
        if ( x < 0 || y < 0 || x >= SLIDER_SIZE || y >= SLIDER_SIZE )
            return;

        c.addPoint(x, 255 - y);

        refresh();
    }

    public void updateBits ( Object source, int bits )
    {
        c.updateBits(source, bits);
    }

    public void mouseClicked ( MouseEvent evt )
    {
    }

    public void mouseEntered ( MouseEvent evt )
    {
    }

    public void mouseExited ( MouseEvent evt )
    {
    }

    public void mousePressed ( MouseEvent evt )
    {
        final int x = evt.getX();
        final int y = evt.getY();

        index = c.selectPoint(x, y);

        if ( evt.isShiftDown() )
            c.removePoint();

        else if ( index < 0 )
            index = c.addPoint(x, y);

        refresh();
    }

    public void mouseReleased ( MouseEvent evt )
    {
        lut = c.getLUT();

        // System.out.println("SplinePanel.refresh(): LUT: " + lut);
        // System.out.println();

        fireLUTChangeEvent();
    }

    public void mouseDragged ( MouseEvent evt )
    {
        int x = evt.getX();
        int y = evt.getY();

        if ( x < 0 )
            x = 0;
        else if ( x > (SLIDER_SIZE - 1) )
            x = (SLIDER_SIZE - 1);
        if ( y < 0 )
            y = 0;
        else if ( y > (SLIDER_SIZE - 1) )
            y = (SLIDER_SIZE - 1);

        c.setPoint(x, y);

        refresh();
    }

    public void mouseMoved ( MouseEvent evt )
    {
    }

    public void paintComponent ( Graphics g )
    {
        super.paintComponent(g);

        c.paint(g);
    }

    private void refresh ()
    {
        /*
         * Calculate the range....
         */
        repaint();
    }

    public static void main ( String[] args )
    {
        final int argc = args.length;
        int       bits = 8;

        if ( argc > 0 )
            bits = Integer.parseInt(args[0]);

        SplinePanel panel = new SplinePanel(ALL_MASK, bits);

        JFrame frame = new JFrame("Spline Test");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
