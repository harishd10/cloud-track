package vgl.iisc.volray.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyListener;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import vgl.iisc.volray.display.Renderer;
import vgl.iisc.volray.ui.editor.TransferFunction;
import vgl.iisc.volray.ui.listener.VolRayKeyListener;
import vgl.iisc.volray.ui.listener.VolRayMouseListener;

import com.jogamp.opengl.util.Animator;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private KeyListener volKeyListener;
	private VolRayMouseListener volMouseListener;
	private GLCanvas volCanvas;

	public void init() {

		JToolBar toolBar = ToolBar.getNameToolBar();
		this.add(toolBar, BorderLayout.PAGE_START);
		Renderer vol = Renderer.getVolRenderer(this);
		volKeyListener = new VolRayKeyListener(vol);
		volMouseListener = new VolRayMouseListener(vol);

		this.setSize(610, 661);
		this.setTitle("Cloud Viewer");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		TransferFunction.init(this);

		initGL();
	}

	private void initGL() {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		caps.setStencilBits(2);

		final Renderer vol = Renderer.getVolRenderer(this);
		volCanvas = new GLCanvas(caps);
		volCanvas.addGLEventListener(vol);
		volCanvas.addKeyListener(volKeyListener);
		volCanvas.addMouseListener(volMouseListener);
		volCanvas.addMouseMotionListener(volMouseListener);
		add(volCanvas, BorderLayout.CENTER);
		volCanvas.requestFocus();
		Animator anim = new Animator(volCanvas);
		anim.start();
	}

	public void setFocus() {
		volCanvas.requestFocus();
	}
}
