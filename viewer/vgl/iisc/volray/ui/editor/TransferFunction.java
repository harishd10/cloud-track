/**
 * Author : Harish D.
 */
package vgl.iisc.volray.ui.editor;

import javax.swing.JDialog;
import javax.swing.JFrame;

import vgl.iisc.volray.ui.editor.TransferFunctionEditor.RGBA;

public class TransferFunction {
	
	private static TransferFunctionEditor editor;
	private static boolean changed = false;
	private static float min = 0;
	private static float max = 1;
	private static EditorDialog editorDialog;

	private static float[] colorMap;
	public static final int COLORMAPSIZE = 1024;

	private TransferFunction() {
		editor = new TransferFunctionEditor();
	}
	
	public static void init(JFrame parent) {
		if(editor == null) {
			editor = new TransferFunctionEditor();
			editorDialog = new EditorDialog(parent);
			editorDialog.getContentPane().add(editor);
			editorDialog.setSize(600, 600);
			editorDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			editorDialog.setTitle("Choose Transfer Function");
			editorDialog.setModal(true);
			
			colorMap = new float[COLORMAPSIZE * 4];
			updateColorMap();
		}
	}
	
	public static void init() {
		if(editor == null) {
			editor = new TransferFunctionEditor();
//			editorDialog = new EditorDialog(parent);
//			editorDialog.getContentPane().add(editor);
//			editorDialog.setSize(600, 600);
//			editorDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
//			editorDialog.setTitle("Choose Transfer Function");
//			editorDialog.setModal(true);
			
			colorMap = new float[COLORMAPSIZE * 4];
			updateColorMap();
		}
	}

	
	public static void setSelection(float mn, float mx) {
		min = mn;
		max = mx;
		updateColorMap();
		changed = true;
	}
	
	public static void show() {
		editorDialog.setVisible(true);
		updateColorMap();
		changed = true;
	}
	
	private static void updateColorMap() {
		for (int i = 0; i < COLORMAPSIZE; ++i) {
			float fValue = (float) i / ((float) COLORMAPSIZE);
			if(fValue >= min && fValue <= max) {
				RGBA color = editor.getColor(fValue);
				if (color.a != colorMap[4 * i + 3] || color.r != colorMap[4 * i]
						|| color.g != colorMap[4 * i + 1] || color.b != colorMap[4 * i + 2]) {
					colorMap[4 * i] = color.r;
					colorMap[4 * i + 1] = color.g;
					colorMap[4 * i + 2] = color.b;
					colorMap[4 * i + 3] = color.a;
				}
			} else {
				colorMap[4 * i] = 0;
				colorMap[4 * i + 1] = 0;
				colorMap[4 * i + 2] = 0;
				colorMap[4 * i + 3] = 0;
			}
		}
	}
	
	public static boolean isChanged() {
		return changed;
	}
	
	public static void changeUpdated() {
		changed = false;
	}
	
	public static float []  getColorMap() {
		return colorMap;
	}
}
