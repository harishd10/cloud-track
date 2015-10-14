package vgl.iisc.volray.display.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

import vgl.iisc.volray.display.VolRayBrush;
import vgl.iisc.volray.ui.editor.TransferFunction;

public class ImgRenderer extends VolRayBrush {

	float [] colorMap;
	int width;
	int height;
	boolean initialized = false;
	int list;
	float [][] vals;
	int dimx = 11520;
	int dimy = 5632;
	
	int sx = 0;
	int sy = 0;
	
	int dx = 1024;
	int dy = 1024;
	
	@Override
	public void init(GL2 gl, GLU glu) {
		colorMap = TransferFunction.getColorMap();
		String pre = "H:/Data/Mars/mgsl_300x/meg128/";
		String file = pre + "megr00n000hb.img";
		vals = getData(11520, 5632, file);
		
//		-3600.0 : 294.0
//		float diff = Math.max(Math.abs(max),Math.abs(min));
//		System.out.println(max + " :: " + min);
		max = 294;
		min = -3600;
		float diff = max - min;

		for(int i = 0;i < dimy;i ++) {
			for(int j = 0;j < dimx;j ++) {
				vals[i][j] = (vals[i][j] - min) / diff;
//				vals[i][j] = (vals[i][j]) / diff;
//				vals[i][j] += 1;
//				vals[i][j] /= 2;
			}
		}
		System.out.println("Normalized");
	}

	boolean changed = true;
	@Override
	public void draw(GL2 gl, GLU glu, GLUT glut) {
		gl.glPushMatrix();
		initializeState(gl);
		
		if(!initialized) {
			initialized = true;
			init(gl, glu);
			list = gl.glGenLists(1);
		}
		
		if(changed) {
//			renderMap(gl);
			renderMapQuad(gl);
			changed = false;
		}
		gl.glScaled(100, 100, 100);
		gl.glCallList(list);
		
		gl.glPopMatrix();
	}

	void renderMap(GL2 gl) {
		gl.glNewList(list, GL2.GL_COMPILE );
		
		gl.glBegin(GL.GL_POINTS);
		
		int ex = sx + dx;
		int ey = sy + dy;
		
		int mx = (sx + ex) / 2;
		int my = (sy + ey) / 2;
		
		for(int i = sy;i < ey;i ++) {
			for(int j = sx;j < ex;j ++) {
				getColor(vals[i][j]);
				gl.glColor3f(col[0], col[1], col[2]);
				gl.glVertex2f(j - mx, i - my);
			}
		}
		
		gl.glEnd();
		
		gl.glEndList();
	}

	void renderMapQuad(GL2 gl) {
		sx = 768 + 740 - 5;
		sy = 1920 + 767 - 5;
		dx = 10;
		dy = 10;
		gl.glNewList(list, GL2.GL_COMPILE );
		
		gl.glBegin(GL2.GL_QUADS);
		
		int ex = sx + dx;
		int ey = sy + dy;
		
		int mx = (sx + ex) / 2;
		int my = (sy + ey) / 2;
		
		float max = -Float.MAX_VALUE;
		float min = Float.MAX_VALUE;
		
		for(int y = sy;y < ey;y ++) {
			for(int x = sx;x < ex;x ++) {
				max = Math.max(vals[y][x], max);
				min = Math.min(vals[y][x], min);
			}
		}

		System.out.println(min + " " + max);
		for(int y = sy;y < ey-1;y ++) {
			for(int x = sx;x < ex-1;x ++) {
//				getColor(vals[y][x]);
				getColor(vals[y][x], max, min);
				gl.glColor3f(col[0], col[1], col[2]);
				gl.glVertex2f(x - mx, y - my);
				
//				getColor(vals[y][x + 1]);
				getColor(vals[y][x + 1], max, min);
				gl.glColor3f(col[0], col[1], col[2]);
				gl.glVertex2f(x - mx + 1, y - my);

//				getColor(vals[y + 1][x + 1]);
				getColor(vals[y + 1][x + 1], max, min);
				gl.glColor3f(col[0], col[1], col[2]);
				gl.glVertex2f(x - mx + 1, y - my + 1);
				
//				getColor(vals[y + 1][x]);
				getColor(vals[y + 1][x], max, min);
				gl.glColor3f(col[0], col[1], col[2]);
				gl.glVertex2f(x - mx, y - my + 1);
			}
		}
		
		gl.glEnd();
		
		gl.glEndList();
	}
	
	float [] col = new float [] {0,0,0,0};
	private void getColor(float v1) {
		int in = (int) (v1 * (TransferFunction.COLORMAPSIZE - 1));
		in *= 4;
		col[0] = colorMap[in];
		col[1] = colorMap[in + 1];
		col[2] = colorMap[in + 2];
		col[3] = 1;
	}
	
	void getColor(float v1, float max, float min) {
		v1 = (v1 - min) / (5 * (max - min));
		v1 += 0.4f;
		int in = (int) (v1 * (TransferFunction.COLORMAPSIZE - 1));
		in *= 4;
		col[0] = colorMap[in];
		col[1] = colorMap[in + 1];
		col[2] = colorMap[in + 2];
		col[3] = 1;
	}
	
	@Override
	public double getExtent() {
		return 512;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setScreenSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	float max = - Float.MAX_VALUE;
	float min = Float.MIN_VALUE;
	
	public float [][] getData(int dimx, int dimy, String file) {
		try {
			int [] dim = new int [] {1, dimy, dimx};
			// dim[2] first followed by dim[1] followed by dim[0]
			
			// for a particular month
			float [][] vals = new float[dim[1]][dim[2]];
			try {
				ShortBuffer buf = getData(file, dimx, dimy);
				int ct = 0;
				for(int i = 0;i < dim[1];i ++) {
					for(int j = 0;j < dim[2];j ++) {
						vals[i][j] = buf.get(ct);
						max = Math.max(max, vals[i][j]);
						min = Math.min(min, vals[i][j]);
						ct ++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			return vals;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static ShortBuffer getData(String fileName, int dimx, int dimy) throws IOException {
		RandomAccessFile file = new RandomAccessFile(fileName, "r");
		int noBytes = dimx * dimy * 2;
		byte [] vals = new byte[noBytes];
		file.read(vals);
		ByteBuffer bbuf = ByteBuffer.wrap(vals);
//		bbuf.order(ByteOrder.LITTLE_ENDIAN);
		ShortBuffer fbuf = bbuf.asShortBuffer();
		file.close();
		return fbuf;
	}

	public void move(int ddx, int ddy) {
		sx += ddx;
		sy += ddy;
		if(sx + dx >= dimx) {
			sx = dimx - dx - 1;
		}
		if(sy + dy >= dimy) {
			sy = dimy - dy - 1;
		}
		if(sx < 0) {
			sx = 0;
		}
		if(sy < 0) {
			sy = 0;
		}
		System.out.println("New position: (" + sx + ", " + sy + ")");
		changed = true;
	}
}
