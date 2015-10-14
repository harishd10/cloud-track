package vgl.iisc.volray;

import javax.media.opengl.GLProfile;

import vgl.iisc.volray.ui.MainWindow;

public class MapViewer {

	public static void main(String[] args) {
		GLProfile.initSingleton(true);
		MainWindow main = new MainWindow();
		main.init();
		main.setVisible(true);
	}
}
