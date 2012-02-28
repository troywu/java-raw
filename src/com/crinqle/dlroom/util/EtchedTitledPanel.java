package com.crinqle.dlroom.util;

import javax.swing.*;
import javax.swing.border.*;

public class EtchedTitledPanel extends JPanel implements
		com.crinqle.dlroom.Const {
	public EtchedTitledPanel(String title) {
		Border etch = new EtchedBorder(EtchedBorder.LOWERED);
		Border name = new TitledBorder(etch, title);
		this.setBorder(name);
	}
}
