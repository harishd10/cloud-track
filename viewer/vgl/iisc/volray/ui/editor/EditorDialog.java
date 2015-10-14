/**
 * Author : Harish D.
 */
package vgl.iisc.volray.ui.editor;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class EditorDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	public EditorDialog(JFrame parent) {
		super(parent);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		super.repaint();
		super.setVisible(b);
	}
}
