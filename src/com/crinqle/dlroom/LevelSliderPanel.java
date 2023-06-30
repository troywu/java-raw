package com.crinqle.dlroom;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.crinqle.dlroom.event.BitDepthChangeListener;
import com.crinqle.dlroom.event.LUTChangeListener;

import static com.crinqle.dlroom.Const.*;



public class LevelSliderPanel
      extends JPanel
      implements ChangeListener, BitDepthChangeListener
{
    private final int f_chan;

    private int f_bits;
    private int f_max;

    private JSlider      f_sl;
    private JTextField   f_tf;
    private JSpinner     f_sp;
    private SpinnerModel f_spm;

    private LUT                           f_lut       = null;
    private Collection<LUTChangeListener> f_listeners = new LinkedList<LUTChangeListener>();

    public LevelSliderPanel ( int channel, int bits )
    {
        // super(CHANNEL_NAME[channel]);
        // setBackground(Color.lightGray);

        f_chan = channel;
        f_bits = bits;
        f_max  = 1 << f_bits;

        f_sl = new JSlider(JSlider.VERTICAL, -100, 100, 0);
        f_sl.addChangeListener(this);
        f_sl.setMinorTickSpacing(5);
        f_sl.setMajorTickSpacing(50);
        f_sl.setPaintTicks(true);
        // f_sl.setPaintTrack(true);
        // f_sl.setPaintLabels(true);

        JLabel label = new JLabel(CHANNEL_NICKNAME[channel]); // + " =");
        label.setHorizontalAlignment(JTextField.RIGHT);

        f_tf = new JTextField("1.00");
        f_tf.setHorizontalAlignment(JTextField.RIGHT);

        // JPanel p = new JPanel();
        // p.add(f_tf);
        // p.add(label);

        // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        GridBagLayout      bag     = new GridBagLayout();
        GridBagConstraints gc      = new GridBagConstraints();
        JPanel             sliders = new JPanel();
        sliders.setLayout(bag);

        // gc.fill = GridBagConstraints.BOTH;
        // bag.setConstraints(filler, gc);
        // gc.gridy = 2;

        gc.fill  = GridBagConstraints.VERTICAL;
        gc.gridy = 0;
        // gc.gridwidth = 2;
        bag.setConstraints(f_sl, gc);

        gc.fill      = GridBagConstraints.NONE;
        gc.gridwidth = 1;
        gc.gridy     = 1;
        bag.setConstraints(label, gc);

        // gc.gridx = 1;
        gc.gridy = 2;
        bag.setConstraints(f_tf, gc);

        // add(p);

        // add(filler);
        sliders.add(label);
        sliders.add(f_sl);
        sliders.add(f_tf);

        add(sliders);
        setPreferredSize(new Dimension(20, 256));

        // setBorder(new EmptyBorder(0,10,0,10));
    }

    public void addLUTChangeListener ( LUTChangeListener l )
    {
        f_listeners.add(l);
    }

    protected void fireLUTChangeEvent ()
    {
        for ( LUTChangeListener l : f_listeners )
            l.applyLUT(this, f_lut);
    }

    public void updateBits ( Object source, int bits )
    {
        f_bits = bits;
        f_max  = 1 << f_bits;
    }

    public void stateChanged ( ChangeEvent evt )
    {
        Object obj = evt.getSource();

        if ( obj == f_sl )
        {
            final float val  = 1 + (float)f_sl.getValue() / 100;
            String      text = Float.toString(val);

            if ( text.length() > 3 )
                text = text.substring(0, 4);
            else if ( text.length() < 4 )
                text = text + "0";

            f_tf.setText(text);

            /*
             * Calculate LUT...
             */
            final float[] array = new float[f_max];

            for ( int i = 0; i < f_max; ++i )
                  array[i] = (i * (float)val);

            final float max = array[f_max - 1];

            f_lut = new LUT(f_chan, array, f_max - 1);

            // System.out.println("@@ LevelSiderPanel: Adjusting channel " +
            // f_chan + " values...");
            // System.out.println("                    Adjusting " +
            // CHANNEL_NICKNAME[f_chan] + " values...");

            fireLUTChangeEvent();
        }
    }
}
