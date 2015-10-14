/**
 * Author : Harish D.
 */
package vgl.iisc.volray.ui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import vgl.iisc.volray.display.Renderer;
import vgl.iisc.volray.ui.editor.TransferFunction;

public class ToolBar extends JToolBar implements ActionListener {

	private static final long serialVersionUID = -1369214904440657497L;
	
	public static final String FOPEN = "OpenFile";
	public static final String TFEDITOR= "TFEditor";
	
	public static final String RESET = "Reset";
	
	private JButton reset;
	
	ImageIcon resetn;
	ImageIcon reseto;

	ImageIcon funn;
	ImageIcon funp;
	ImageIcon fn;
	ImageIcon fo;

	private JButton fOpen;
	private JButton fnEditor;
	
	private JPanel leftPanel;
	private JPanel rightPanel;
	
	private JLabel dateLabel;
	
	private static ToolBar bar = new ToolBar();
	
	public static ToolBar getNameToolBar() {
		return bar;
	}
	
	private ToolBar() {
//		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		Color col = new Color(245,245,245);
		this.setBackground(col);
		Insets mar = new Insets(5,5,5,5);
		this.setMargin(mar);
		this.setBorderPainted(false);
		
		leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		leftPanel.setBackground(col);
		rightPanel.setBackground(col);
		
		addButtons();
		addDateField();
		
		this.add(leftPanel);
		this.add(rightPanel);
	}
	
	private void addDateField() {
		dateLabel = new JLabel();
		int width = 100;
		int height = 40;
		Dimension d = new Dimension(width, height);
		dateLabel.setMinimumSize(d);
		dateLabel.setPreferredSize(d);
		dateLabel.setMaximumSize(d);
		rightPanel.add(dateLabel);
	}

	private void addButtons() {
		
		String imgLocation = "images/f-n.png";
		URL imageURL = ToolBar.class.getResource(imgLocation);
		fn = new ImageIcon(imageURL);
		
		imgLocation = "images/f-o.png";
		imageURL = ToolBar.class.getResource(imgLocation);
		fo = new ImageIcon(imageURL);
		
		fOpen = makeButton(fn,FOPEN, "Open File", "F");
		fOpen.setMargin(new Insets(0,0,0,0));
		fOpen.setRolloverEnabled(true);
		fOpen.setRolloverIcon(fo);
		leftPanel.add(fOpen);

		imgLocation = "images/reset-n.png";
		imageURL = ToolBar.class.getResource(imgLocation);
		resetn = new ImageIcon(imageURL);
		
		imgLocation = "images/reset-o.png";
		imageURL = ToolBar.class.getResource(imgLocation);
		reseto = new ImageIcon(imageURL);
		
		reset = makeButton(resetn,RESET, "Reset Model","Reset");
		reset.setMargin(new Insets(0,0,0,0));
		reset.setRolloverEnabled(true);
		reset.setRolloverIcon(reseto);
		leftPanel.add(reset);
		
		imgLocation = "images/fun-p.png";
		imageURL = ToolBar.class.getResource(imgLocation);
		funp = new ImageIcon(imageURL);
		imgLocation = "images/fun-n.png";
		imageURL = ToolBar.class.getResource(imgLocation);
		funn = new ImageIcon(imageURL);

		fnEditor = makeButton(funn, TFEDITOR, "Change Transfer Function", "Fn");
		fnEditor.setMargin(new Insets(0,0,0,0));
		fnEditor.setRolloverEnabled(true);
		fnEditor.setRolloverIcon(funp);
		leftPanel.add(fnEditor);
	}

	protected JButton makeButton(ImageIcon ico, String actionCommand, String toolTipText, String altText) {
		// Look for the image.
		
		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setBackground(Color.black);
		
		if (ico != null) { // image found
			button.setIcon(ico);
		} else { // no image found
			button.setText(altText);
//			System.err.println("Resource not found: " + imgLocation);
		}
		return button;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		performCommand(cmd);
	}
	
	public void performCommand(String cmd) {
		if(FOPEN.equals(cmd)) {
			openFile();
		} else if(TFEDITOR.equals(cmd)) {
			TransferFunction.show();
		} else if(RESET.equals(cmd)) {
			Renderer.reset();
		}
	}
	
	JFrame frame;
	public void setFrame(JFrame fr) {
		frame = fr;
	}
	
	private void openFile() {
		FileDialog fDialog = new FileDialog(frame);
		fDialog.setMode(FileDialog.LOAD);
		FilenameFilter filter = new FilenameFilter() {
		
			public boolean accept(File arg0, String arg1) {
				int i = arg1.lastIndexOf(".");
				String ext = arg1.substring(i + 1);
				if(ext.equalsIgnoreCase("properties")) {
					return true;
				}
				return false;
			}
		
		};
		fDialog.setFilenameFilter(filter);
		fDialog.setFile("");
		fDialog.setVisible(true);
		
		String fileName = fDialog.getFile();
		String directory = fDialog.getDirectory();
		if (directory != null && fileName != null) {
			fileName = directory + fileName;
		}
		Renderer.getVolRenderer().openFile(fileName);
	}

	public static void changeState(String cmd) {
		bar.performCommand(cmd);
	}
	
	public static void setDate(String date) {
		bar.setDateLabel(date);
	}

	private void setDateLabel(String date) {
		dateLabel.setText(date);		
	}
}