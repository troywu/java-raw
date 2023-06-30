package com.crinqle.dlroom.util;


import java.awt.*;
import javax.swing.*;



public class Methods
{
    public static void centerWindow ( Window w )
    {
        Toolkit   kit   = Toolkit.getDefaultToolkit();
        Dimension total = kit.getScreenSize();
        Dimension size  = w.getSize();

        int x = (total.width - size.width) >> 2;
        int y = (total.height - size.height) >> 2;

        w.setLocation(x, y);
    }
}
