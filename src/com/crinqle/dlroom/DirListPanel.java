package com.crinqle.dlroom;

import java.io.*;
import java.util.*;
import javax.swing.*;
import com.crinqle.dlroom.util.*;


public class DirListPanel extends EtchedTitledPanel
{
	JList f_list = null;


	public DirListPanel ( File dir )
	{
		super("Directories in " + dir.getPath());

		f_list = new JList();
		updateDirs(dir);

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(f_list);
	}


	public void updateDirs ( File dir )
	{
		File[] entries = dir.listFiles();
		final int count = entries.length;

		Vector list = new Vector();

		for ( int i = 0; i < count; ++i )
			if ( entries[i].isDirectory() )
				list.add(entries[i]);

		f_list.setListData(list);
	}
}
