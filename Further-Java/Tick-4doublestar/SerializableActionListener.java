package uk.ac.cam.pv273.fjava.tick4star;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

public class SerializableActionListener implements Serializable, ActionListener 
{

	private static final long serialVersionUID = 1L;

	@Override
	public void actionPerformed(ActionEvent e) { }
}
