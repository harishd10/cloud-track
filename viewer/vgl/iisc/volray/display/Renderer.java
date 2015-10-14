/**
 * Author : Harish D
 */
package vgl.iisc.volray.display;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import vgl.iisc.volray.display.impl.OffRendererTime;
import vgl.iisc.volray.model.DataInfo;
import vgl.iisc.volray.ui.MainWindow;
import vgl.iisc.volray.utils.FileUtils;

import com.jogamp.opengl.util.gl2.GLUT;



public class Renderer implements GLEventListener {

	private GLU glu;
	private GL2 gl;
	private GLUT glut;
	private MainWindow mainWindow;
	
	private static Renderer volRenderer;
//	private OffRendererTime offBrush;
//	private LineGraph graph;
	
	private ArrayList<GLBrush> displayList;
	
	private double extent;
	private double scale;
	private float translate;
	
	private int width;
	private int height;
	
	private int cur = 0;
	
	private static boolean modeFlag;
	
	private int shaderProgram;
	
	public static Renderer getVolRenderer(MainWindow parent) {
		if(volRenderer == null) {
			volRenderer = new Renderer(parent);
		}
		return volRenderer;
	}

	public static Renderer getVolRenderer() {
		return volRenderer;
	}
	
	private Renderer(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		glu = new GLU();
		glut = new GLUT();
		displayList = new ArrayList<GLBrush>();
		extent = 2;
		scale = 1;
		translate = -6;
	}
	
	boolean first = true;
	
	/* (non-Javadoc)
	 * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
	 */
	public void display(GLAutoDrawable drawable) {
		if(first) {
			String [] model = {"input/sample.properties", "input/india.properties","input/nakazawa.properties","input/aila.properties", 
					"input/sample-trmm.properties", "input/india-trmm.properties", "input/nakazawa-trmm.properties"};
			
			openFile(model[1]);
			
			boolean takeSnapshot = false;
			if(takeSnapshot) {
				snapShots(drawable);
			}
			first = false;
		}
		draw(drawable);
	}

	private void snapShots(GLAutoDrawable drawable) {
		for (int i = 0; i < 48 * 10; i++) {
			draw(drawable);
			String no = "" + i;
			if (i < 10) {
				no = "00" + no;
			} else if (i < 100) {
				no = "0" + no;
			}
			if (i == 0) {
				move(1, 0);
				draw(drawable);
				move(-1, 0);
				draw(drawable);
			}
			String png = "images/nakazawa/nakazawa-" + no + ".png";
			System.out.println("Saving " + png + " ...");
			takeSnapShot(png);
			move(1, 0);
		}
	}
	private void draw(GLAutoDrawable drawable) {
		if(modeFlag) {
			modeFlag = false;
			reshape(drawable,0,0,width,height);
		}
		
//		GL gl = drawable.getGL();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		loadDefaults(gl);
		
		for(Iterator<GLBrush> it = displayList.iterator();it.hasNext();) {
			GLBrush brush = it.next();
			brush.draw(gl, glu, glut);
		}
	}
	
	void takeSnapShot(String op) {
		float [] buf = new float[width * height * 4];
		FloatBuffer fbuf = FloatBuffer.wrap(buf);
		gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_FLOAT, fbuf);
		try {
			FileOutputStream os = new FileOutputStream(op);
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			int ct = 0;
			
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int rr = (int) (buf[ct] * 255);
					int gg = (int) (buf[ct + 1] * 255);
					int bb = (int) (buf[ct + 2] * 255);
					int aa = (int) (buf[ct + 3] * 255);
					int rgba = 0;
					rgba |= (bb & 0xff);
					rgba |= ((gg & 0xff) << 8);
					rgba |= ((rr & 0xff) << 16);
					rgba |= ((aa & 0xff) << 24);
					img.setRGB(j, (height - i - 1), rgba);
					ct += 4;
				}
			}
			ImageIO.write(img, "png", os);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param gl
	 */
	private void loadDefaults(GL2 gl) {
		gl.glLoadIdentity();
		gl.glScaled(scale,scale,scale);
		if(Options.getProjectionMode() == Options.PERSPECTIVE) {
			gl.glTranslatef(0,0,translate);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.media.opengl.GLEventListener#displayChanged(javax.media.opengl.GLAutoDrawable,
	 *      boolean, boolean)
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
	 */
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.setSwapInterval(1);
		gl.glClearColor(0, 0, 0, 0);
		gl.glClearDepth(1.0);							// Depth Buffer Setup
		gl.glEnable(GL2.GL_DEPTH_TEST);						// Enables Depth Testing
		gl.glDepthFunc(GL2.GL_LEQUAL);							// The Type Of Depth Test To Do
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
		this.gl = new DebugGL2(gl);
//		this.gl = gl;
		drawable.setGL(this.gl);

		glu = new GLU();
		System.out.println("Init GL is " + this.gl.getClass().getName());
//		setupLight(this.gl);
		setFocus();
	}

	/* (non-Javadoc)
	 * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable, int, int, int, int)
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
//        GL gl = drawable.getGL();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION); 
        gl.glLoadIdentity();               
        
        double ratio = (double) width/ (double)height;
        double w = 2;
        double h = 2;
        if(width < height) {
        	h = h / ratio;
        } else {
        	w = w * ratio;
        }
     	
        int mode = Options.getProjectionMode();
        if(mode == Options.PERSPECTIVE) {
        	// 	For Perspective projection
        	glu.gluPerspective(45, (double) (width) / (double) (height), 0.001 , 1000);
        } else if(mode == Options.ORTHOGRAPHIC) {
	        // For Orthogonal projection
	        gl.glOrtho(-1 * w, w,-1 * h, h, -10000, 10000);
        }
        gl.glMatrixMode(GL2.GL_MODELVIEW);  
        gl.glLoadIdentity();
        
		for(Iterator<GLBrush> it = displayList.iterator();it.hasNext();) {
			GLBrush brush = it.next();
			brush.setScreenSize(width, height);
		}
	}
	
	public void addGLBrush(GLBrush obj) {
//		displayList.clear();
//		obj.init(gl, glu);
		displayList.add(obj);
	}
	
	public void openFile(String infoFile) {
		DataInfo info = FileUtils.parseInfoFile(infoFile);
		GLBrush offBrush = null;
		offBrush = new OffRendererTime();
		((OffRendererTime)offBrush).setMesh(info);
		if(!displayList.isEmpty()) {
			displayList.remove(0);
		}
		addGLBrush(offBrush);
		
		offBrush.setScreenSize(width, height);
		double ex = offBrush.getExtent();
		ex *= 2;
		extent = ex; //Math.max(extent, ex);
		translate = (float) (- 1.5 * extent);
		scale = 2 / extent;
		
		reset();
	}
	
//	String folder = "F:/rainfall/python/data/";
//	String folder = "H:/rainfall/python/data/";
	

//	public void openCustomMapFile(String modelFile, boolean viewOnly) {
//		DataInfo info = FileUtils.parseInfoFile(modelFile);
//		GLBrush offBrush = null;
//		if(viewOnly) {
////			offBrush = new OffRendererMap();
////			((OffRendererMap)offBrush).setMesh(info);
//		} else {
//			offBrush = new OffRendererTime();
//			((OffRendererTime)offBrush).setMesh(info);
//		}
//		if(!displayList.isEmpty()) {
//			displayList.remove(0);
//		}
//		addGLBrush(offBrush);
//		
//		offBrush.setScreenSize(width, height);
//		double ex = offBrush.getExtent();
//		ex *= 2;
//		extent = ex; //Math.max(extent, ex);
//		translate = (float) (- 1.5 * extent);
//		scale = 2 / extent;
//		
//		reset();
//	}

	public void setFocus() {
		mainWindow.setFocus();
	}
	
	public void transform(float x1, float y1, float x2, float y2, boolean sc) {
		x1 = 2 * x1 / width - 1;
		y1 = 2 * y1 / height - 1;
		
		x2 = 2 * x2 / width - 1;
		y2 = 2 * y2 / height - 1;

		if(displayList.size() == 0) {
			return;
		}
		GLBrush brush = displayList.get(cur);
		if(!sc) {
			brush.translate(x1, -1 * y1,x2,-1 * y2);
		} else {
			brush.scale(x1, -1 * y1,x2,-1 * y2);
		}
			
//		case Options.TRANSLATE :
//			brush.translate(x1, -1 * y1,x2,-1 * y2);
//			break;
//		}
		
	}

	public static void reset() {
		if(volRenderer.displayList.size() > 0) {
			GLBrush brush = volRenderer.displayList.get(volRenderer.cur);
			brush.reset();
		}
	}
	
	public void nextModel() {
		cur ++;
		cur = cur % displayList.size();
	}

	public void prevModel() {
		cur --;
		if(cur == -1) {
			cur = displayList.size() - 1;
		}
	}
	
	public static void modeChanged() {
		modeFlag = true;
	}
	
	public static double getExtent(GLBrush brush) {
		return volRenderer.extent;	
	}

	public static void resetExtent(GLBrush brush) {
		volRenderer.resetExtent();	
	}

	
	private void resetExtent() {
		extent = 0;
		for(Iterator<GLBrush> it = displayList.iterator(); it.hasNext();) {
			GLBrush obj = it.next();
			double ex = obj.getExtent();
			ex *= 2;
			extent = Math.max(extent, ex);
			translate = (float) (- 1.5 * extent);
			scale = 2 / extent;
		}
	}

	/**
	 * @param x
	 * @param y
	 */
	public void mouseClicked(MouseEvent e) {
		GLBrush brush = displayList.get(cur);
		brush.mouseClicked(e);
	}
	
	public static GL2 getVolGL() {
		return volRenderer.gl;
	}
	
	public static int getShaderProgram() {
		return volRenderer.shaderProgram;
	}
	
	public static void clear() {
		volRenderer.displayList.clear();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		
	}

	public static void move(int dx, int dy) {
		OffRendererTime off = (OffRendererTime) volRenderer.displayList.get(0);
		if(dx < 0) {
			off.prevTime();
		} else if(dx > 0) {
			off.nextTime();
		}
	}
	
	private boolean lightEnabled;
	void setupLight(GL2 gl) {
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);

        /* create light 0 and give it a position */
        //float light0pos[] = {1.1f, 1.1f, -1.1f, 1};
        float light0pos[] = {1f, 1f, 1f, 0};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0pos, 0);
        /* turn light 0 on */
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_RESCALE_NORMAL);

        float ambient[] = {0.3f, 0.3f, 0.3f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
        float specular[] = {1f, 1f, 1f, 1};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specular, 0);

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[]{0.5f, 0.5f, 0.5f, 1}, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 20);

        /*turn lighting on */
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glClearAccum(0, 0, 0, 0);
        lightEnabled = true;
	}

	public static void enableLight(GL2 gl) {
		volRenderer.toggleLightOn(gl);
	}

	public static void disableLight(GL2 gl) {
		volRenderer.toggleLightOff(gl);
	}
	
	private void toggleLightOn(GL2 gl) {
		if(lightEnabled) {
			gl.glEnable(GL2.GL_LIGHTING);
		}
	}
	
	private void toggleLightOff(GL2 gl) {
		lightEnabled = gl.glIsEnabled(GL2.GL_LIGHTING);
		if(lightEnabled) {
			gl.glDisable(GL2.GL_LIGHTING);
		}
	}
	
	public static void executeCommand() {
		OffRendererTime off = (OffRendererTime) volRenderer.displayList.get(0);
		off.executeCommand();
	}

	public static void animate() {
		OffRendererTime off = (OffRendererTime) volRenderer.displayList.get(0);
		off.toggleAnimate();
	}
}
