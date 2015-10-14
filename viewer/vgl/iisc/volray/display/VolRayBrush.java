/**
 * Author : Harish D.
 */
package vgl.iisc.volray.display;

import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;

/**
 *  This is an Abstract class that can be extended instead of implementing the GLBrush imterface,
 *  in case you wish to use the default functionality of transformations provided by us.
 */
public abstract class VolRayBrush implements GLBrush {

	private float [] curQuat;
	private float [] lastQuat;
	private float [] rotMat;
	
	private float [] lastRot;
	private float scale;
	
	private float tx;
	private float ty;
	private float tz;
	
	private static final float DEFAULT_SCALE = 1f;
	
	boolean transformed = true;
	
	public boolean isTransformed() {
		return transformed;
	}

	public void setTransformed(boolean transformed) {
		this.transformed = transformed;
	}

	public VolRayBrush() {
		curQuat = Trackball.trackBall(0, 0, 0, 0);
		rotMat = Trackball.buildRotMatrix(curQuat);
		lastRot = new float[] {0,0,0,0};
		scale = DEFAULT_SCALE;
		tx = 0;
		ty = 0;
		tz = 0;
	}

	
	/* (non-Javadoc)
	 * @see name.viewer.display.GLBrush#rotate(float, float, float, float)
	 */
	public void rotate(float x1, float y1, float x2, float y2) {
		lastQuat = Trackball.trackBall(x1, y1, x2, y2);
		curQuat = Trackball.addQuats(lastQuat,curQuat);
		rotMat = Trackball.buildRotMatrix(curQuat);
		transformed = true;
	}
	
	/* (non-Javadoc)
	 * @see name.viewer.display.GLBrush#scale(float, float, float, float)
	 */
	public void scale(float x1, float y1, float x2, float y2) {
		float t = y2 - y1;
		if(t < 0) {
			t = 1 - t / 2;
			t = 1 / t;
		} else {
			t = 1 + t / 2;
		}
		scale *= t;
		transformed = true;
	}
	
	/* (non-Javadoc)
	 * @see name.viewer.display.GLBrush#translate(float, float, float, float)
	 */
	public void translate(float x1, float y1, float x2, float y2) {
		double ex = Renderer.getExtent(this);
		if(x2 == -2 && y2 == -2) {
			tz += (y1 - x1) * ex;
		} else {
			tx += (x2 - x1) * ex;
			ty += (y2 - y1) * ex;
		}
		transformed = true;
	}
	
	/**
	 * This method is to be called in the draw method in order to perform the required transformations
	 * 
	 * @param gl
	 */
	protected void initializeState(GL2 gl) {
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		gl.glTranslatef(tx, ty, tz);
		gl.glScalef(scale,scale,scale);
		
		lastRot[0] = 0;
		lastRot[1] = 0;
		lastRot[2] = 0;
		lastRot[3] = 0;
		
		gl.glMultMatrixf(rotMat, 0);
		
		transformed = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.viewer.display.GLBrush#reset()
	 */
	public void reset() {
		curQuat = Trackball.trackBall(0, 0, 0, 0);
		rotMat = Trackball.buildRotMatrix(curQuat);
		
		lastRot[0] = 0;
		lastRot[1] = 0;
		lastRot[2] = 0;
		lastRot[3] = 0;
		
		scale = DEFAULT_SCALE;
		tx = ty = tz = 0;
		
		transformed = true;
	}
	
	/* (non-Javadoc)
	 * @see name.viewer.display.GLBrush#mouseClicked(float, float)
	 */
	public void mouseClicked(MouseEvent e) {
		// Empty. to be overriddem if it has to be used
	}

	public float[] getCurQuat() {
		return curQuat;
	}

	public void setCurQuat(float[] curQuat) {
		this.curQuat = curQuat;
	}

	public float[] getLastQuat() {
		return lastQuat;
	}

	public void setLastQuat(float[] lastQuat) {
		this.lastQuat = lastQuat;
	}

	public float[] getLastRot() {
		return lastRot;
	}

	public void setLastRot(float[] lastRot) {
		this.lastRot = lastRot;
	}

	public float[] getRotMat() {
		return rotMat;
	}

	public void setRotMat(float[] rotMat) {
		this.rotMat = rotMat;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getTx() {
		return tx;
	}

	public void setTx(float tx) {
		this.tx = tx;
	}

	public float getTy() {
		return ty;
	}

	public void setTy(float ty) {
		this.ty = ty;
	}

	public float getTz() {
		return tz;
	}

	public void setTz(float tz) {
		this.tz = tz;
	}
}
