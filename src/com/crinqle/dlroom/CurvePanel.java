package com.crinqle.dlroom;


import java.awt.BorderLayout;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.crinqle.dlroom.event.BitDepthChangeListener;
import com.crinqle.dlroom.event.LUTChangeListener;
import com.crinqle.dlroom.math.SplinePanel;



public class CurvePanel extends JPanel implements LUTChangeListener,
                                                  BitDepthChangeListener
{
    private SplinePanel                   f_sp;
    private Collection<LUTChangeListener> f_listeners = new LinkedList<LUTChangeListener>();

    public CurvePanel ( int channel, int bits )
    {
        f_sp = new SplinePanel(channel, bits);
        f_sp.addLUTChangeListener(this);

        JPanel spHolder = new JPanel();
        Border matte    = new EmptyBorder(10, 10, 10, 10);
        Border etch     = new BevelBorder(BevelBorder.LOWERED);
        Border border   = new CompoundBorder(matte, etch);

        setBorder(border);

        setLayout(new BorderLayout());

        add(f_sp, BorderLayout.CENTER);
    }

    public void applyLUT ( Object source, LUT lut )
    {
        fireLUTChangeEvent(lut);
    }

    public void updateBits ( Object source, int bits )
    {
        f_sp.updateBits(source, bits);
    }

    public void addLUTChangeListener ( LUTChangeListener l )
    {
        f_listeners.add(l);
    }

    protected void fireLUTChangeEvent ( LUT lut )
    {
        for ( LUTChangeListener l : f_listeners )
            l.applyLUT(this, lut);
    }
}
