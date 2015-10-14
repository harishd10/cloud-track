/**
 * Author : Harish D.
 */
package vgl.iisc.volray.display;

import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * This interface should be implemented in order to display custom objects
 * in the viewer.  
 */
public interface GLBrush {
	
	public void init(GL2 gl, GLU glu);
	
	/**
	 * This method will be called from the Renderer if it is in its list of objects to the drawn.
	 * 
	 * @param gl The GL object that is used to call Open GL APIs
	 * @param glu The GLU object used to call the GL Utility APIs 
	 */
	public void draw(GL2 gl, GLU glu, GLUT glut);
	
	public void rotate(float x1,float x2,float y1,float y2);
	
	public void scale(float x1,float y1,float x2,float y2);
	
	public void translate(float x1,float y1,float x2,float y2);
	
	public void reset();
	
	public double getExtent();
	
	public void mouseClicked(MouseEvent e);
	
	public void release();
	
	public void setScreenSize(int width, int height);
}
