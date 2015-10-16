package vgl.iisc.volray.display.impl;

import static vgl.iisc.utils.Utilities.splitString;
import hope.it.works.ct.TriangleDataPrim;
import hope.it.works.rainfall.AnalyseTimeGraph;
import hope.it.works.rainfall.Cloud;
import hope.it.works.rainfall.TimeGraph;
import hope.it.works.rainfall.TimeGraph.Adjacencies;
import hope.it.works.rainfall.TimeGraph.Direction;
import hope.it.works.rainfall.TimeGraph.Edge;
import hope.it.works.rainfall.TimeGraph.Node;

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

import vgl.iisc.external.loader.impl.OffLoader;
import vgl.iisc.utils.MyIntList;
import vgl.iisc.utils.Utilities;
import vgl.iisc.volray.display.Options;
import vgl.iisc.volray.display.VolRayBrush;
import vgl.iisc.volray.model.DataInfo;
import vgl.iisc.volray.model.TRMData;
import vgl.iisc.volray.ui.ToolBar;
import vgl.iisc.volray.ui.editor.TransferFunction;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class OffRendererTime extends VolRayBrush {

	GL2 gl;
	GLUT glut;
	
	int width, height;

	TRMData model;

	float extent;

	boolean initialized = false;
	boolean updated = true;
	float[] backColor = new float[] { 1, 1, 1, 1 };

	TRMData.Vertex[] vertices;
	TRMData.Triangle[] triangles;
	int[] col;
	float[] colorMap;
	boolean newModel = false;

	float maxx, maxy, maxz;
	float minx, miny, minz;
	
	int curTime = 0;
	int maxTime = 6;
	
	Cloud cloud = new Cloud();
	
	public void setMesh(DataInfo info) {
		if (model != null) {
			// cleanup
		}
		newModel = true;

		updated = true;
		loadFile(info.offFile);
		model.setup();

		double sumx = 0;
		double sumy = 0;
		double sumz = 0;

		vertices = model.vertices;
		triangles = model.triangles;
		extent = 0;

		maxx = maxy = maxz = -Float.MAX_VALUE;

		minx = miny = minz = Float.MAX_VALUE;

		for (int i = 0; i < vertices.length; i++) {
			double x, y, z;
			x = vertices[i].x;
			y = vertices[i].y;
			z = vertices[i].z;
			sumx += x;
			sumy += y;
			sumz += z;
		}

		sumx /= (vertices.length);
		sumy /= (vertices.length);
		sumz /= (vertices.length);
		for (int i = 0; i < vertices.length; i++) {
			vertices[i].x -= sumx;
			vertices[i].y -= sumy;
			vertices[i].z -= sumz;

			extent = Math.max(extent, Math.abs(vertices[i].x));
			extent = Math.max(extent, Math.abs(vertices[i].y));
			extent = Math.max(extent, Math.abs(vertices[i].z));

			maxx = Math.max(vertices[i].x, maxx);
			maxy = Math.max(vertices[i].y, maxy);
			maxz = Math.max(vertices[i].z, maxz);

			minx = Math.min(vertices[i].x, minx);
			miny = Math.min(vertices[i].y, miny);
			minz = Math.min(vertices[i].z, minz);
		}
		initTimeData(info);
		initColor();
	}

	AnalyseTimeGraph atg;
	String [] date;
	int nx, ny;
	float maxVal, diffVal;
	float stLat, enLat, stLon, enLon; 
	int noDays, year, month, dStart;
	boolean trmm;
	String modelName;
	String folder;
	
	TimeGraph tg = new TimeGraph();
	
	private void readModel(DataInfo info) {
		try {
			noDays = info.noDays;
			year = info.year;
			month = info.month;
			dStart = info.dStart;
			ny = info.ny;
			nx = info.nx;
			
			trmm = info.trmm;
			modelName = info.modelName;
			
			stLat = info.stLat;
			enLat = info.enLat;
			stLon = info.stLon;
			enLon = info.enLon;
			
			folder = info.folder;
			
			tg.createClouds = info.create;
			tg.th = new float[] {info.th};
			Cloud.tmpFolder = info.cloudFolder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void initTimeData(DataInfo info) {
		// Using CT
		OffLoader loader = new OffLoader();
		TriangleDataPrim data = new TriangleDataPrim(false, true);

		int x = 0;
		int noTimeSteps = 24;
		boolean biHour = true;
		
		
		readModel(info);
		
		int nts = 0;
		
		if(trmm) {
			maxVal = 100;
			diffVal = 60;
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t += 3) {
					nts ++;
				}
			}
		} else {
			maxVal = 330;
			diffVal = 0;
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t ++) {
					nts ++;
					if(biHour) {
						nts ++;
					}
				}
			}
		}
		
		
		String [] timeSteps = new String [nts];
		date = new String[timeSteps.length];
		int dd = dStart;
		int mm = month;
		int yy = year;
		if(trmm) {
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t += 3) {
					timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2);	
					date[x] = getDate(year,month,d,t*2);
					x ++;
				}
				dd ++;
				if(dd > monthDays[mm]) {
					dd = 1;
					mm ++;
					if(mm == 13) {
						mm = 1;
						yy ++;
					}
				}
			}
		} else {
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t ++) {
					if(biHour) {
						timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2);
						date[x] = getDate(year,month,d,t*2);
						x ++;
					}
					
					timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2 + 1);	
					date[x] = getDate(year,month,d,t*2+1);
					x ++;
				}
				dd ++;
				if(dd > monthDays[mm]) {
					dd = 1;
					mm ++;
					if(mm == 13) {
						mm = 1;
						yy ++;
					}
				}
			}
		}
		
		
		int tStart = 0;
		int tEnd = timeSteps.length - 1;
		
		String [] t = new String[tEnd - tStart + 1];
		String [] d = new String[tEnd - tStart + 1];
		for(int i = 0;i < t.length;i ++) {
			t[i] = timeSteps[i + tStart];
			d[i] = date[i + tStart];
		}
		date = d;
		timeSteps = t;

		loader.setInputFile(folder + "data/" + modelName + "/" + modelName + ".off");
		System.out.println("reading data");
		data.loadData(loader, "0");
		System.out.println("Finding time steps");
		
		tg.findClouds(data, folder, modelName, timeSteps, nx, ny);
		tg.createGraph();
		
		maxTime = timeSteps.length;
		System.out.println("max time" + maxTime);
		atg = new AnalyseTimeGraph(tg);
		atg.colorComponents();

		System.out.println("No. of components : " + atg.colours);
		
		curTime = 10;
		// mumbai
		curTime = 56;
		// curTime = 31 -- for east nakazawa
		// sample
//		curTime = 30;
		// aila
//		curTime = 39;
		// nakazawa-normal
		curTime = 26;
		// for east nakazawa
//		curTime = 31;
		
		cloud.read(curTime);
	}

	float th = 6f;
	boolean isClose(MyIntList used, int v) {
		for(int i = 0;i < used.length;i ++) {
			int v1 = used.get(i);
			if(dist(v,v1) <= th) {
				return true;
			}
		}
		return false;
	}

	private double dist(int v1, int v2) {
		float d = (vertices[v1].x - vertices[v2].x) * (vertices[v1].x - vertices[v2].x);
		d += (vertices[v1].y - vertices[v2].y) * (vertices[v1].y - vertices[v2].y);
//		d += (vertices[v1].z - vertices[v2].z) * (vertices[v1].z - vertices[v2].z);
		return Math.sqrt(d);
	}

	ArrayList<Integer> east;
	
	int [] monthDays = {0,31,28,31,30,31,30,31,31,30,31,30,31};
	private String getDate(int y, int m, int d, int t) {
		if(d > monthDays[m]) {
			d -= monthDays[m];
			m ++;
			if(m > 12) {
				m = 1;
				y ++;
			}
		}
		
		String ret = y + "/";
		if(m < 10) {
			ret += "0";
		}
		ret += m + "/"; 
		if(d < 10) {
			ret += "0";
		}
		ret += d + " ";
		int hr = t / 2;
		int min = (t % 2);
		if(hr < 10) {
			ret += "0";
		}
		ret += hr + ":";
		if(min == 0) {
			ret += "00";
		} else {
			ret += "30";
		}
		return ret;
	}

	private void loadFile(String fileName) {
		FileInputStream ff;
		model = new TRMData();
		try {
			ff = new FileInputStream(new File(fileName));
			InputStreamReader reader = new InputStreamReader(ff);
			BufferedReader f = new BufferedReader(reader);
			String line = f.readLine();
			if (line.equalsIgnoreCase("OFF")) {
				line = f.readLine();
			}
			String[] s = splitString(line);
			int nv = Integer.parseInt(s[0]);
			int nt = Integer.parseInt(s[1]);
			col = new int[nv];
			for (int i = 0; i < nv; i++) {
				line = f.readLine();
				s = splitString(line);
				float x = 0;
				float y = 0;
				float z = 0;
				float fn = 0;
				int col = -1;
				if (s.length == 3) {
					x = Float.parseFloat(s[0]);
					y = Float.parseFloat(s[1]);
					z = Float.parseFloat(s[2]);
					fn = y;
				} else if (s.length == 4) {
					x = Float.parseFloat(s[0]);
					y = Float.parseFloat(s[1]);
					z = Float.parseFloat(s[2]);
					fn = Float.parseFloat(s[3]);
				} else if (s.length == 5) {
					x = Float.parseFloat(s[0]);
					y = Float.parseFloat(s[1]);
					z = Float.parseFloat(s[2]);
					fn = Float.parseFloat(s[3]);
					col = Integer.parseInt(s[4]);
				}
				model.addVertex(x, y, z, fn);
				this.col[i] = col;
			}

			for (int i = 0; i < nt; i++) {
				line = f.readLine();
				s = splitString(line);
				int v1 = 0, v2 = 0, v3 = 0;
				if (s.length == 4) {
					if (!s[0].trim().equals("3")) {
						Utilities.er("Invalid off");
					}
					v1 = Integer.parseInt(s[1].trim());
					v2 = Integer.parseInt(s[2].trim());
					v3 = Integer.parseInt(s[3].trim());
				} else if (s.length == 3) {
					v1 = Integer.parseInt(s[0].trim());
					v2 = Integer.parseInt(s[1].trim());
					v3 = Integer.parseInt(s[2].trim());
				} else {
					Utilities.er("Invalid off");
				}
				model.addTriangle(v1, v2, v3);
			}
			System.out.println("Finished reading file");
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void init(GL2 gl, GLU glu) {
		this.gl = gl;
		colorMap = TransferFunction.getColorMap();
	}

	int list;

	boolean vertex = true;
	ArrayList<Integer> selList = new ArrayList<Integer>();
	int selectedVertex;
	
	long prevTime;
	public void draw(GL2 gl, GLU glu, GLUT glut) {
		if(animate) {
			long time = System.currentTimeMillis();
			long diff = time - prevTime;
			if(diff >= 200) {
				if(curTime == maxTime - 1) {
					curTime = 1;
					prevTime();
				} else {
					nextTime();
				}
				System.out.println("CurTime: " + curTime);
			}
		}
		if(this.glut == null) {
			this.glut = glut;
		}
		gl.glPushMatrix();
		
		if (newModel) {
			reset();
			newModel = false;
		}
		initializeState(gl);
		gl.glClearColor(backColor[0], backColor[1], backColor[2], backColor[3]);
		if (!initialized) {
			initialized = true;
			System.out.println("initialized");
			init(gl, glu);
			updated = true;
			list = gl.glGenLists(1);
			initTexture();
		}
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL2.GL_GREATER, 0);
		
		renderMap();
		
		if (TransferFunction.isChanged()) {
			colorMap = TransferFunction.getColorMap();
			updated = true;
			TransferFunction.changeUpdated();
		}
		
		if(clicked) {
			selectedVertex = -1;
			selectedVertex = getSelectedVertex();
			int v = selectedVertex;
			selList.clear();
			if(v == -1 && selList.size() > 0) {
				selList.clear();
				track = null;
			} else {
				selList.add(v);
			}
			clicked = false;
			updated = true;
//			updateTrack(v);
			redrawAll = true;
			paths.clear();
		}
		if(redrawAll && selectedVertex != -1) {
			paths.clear();
			int v = selectedVertex;
			if(vertex) {
				System.out.println(v);
				MyIntList list = getVertices(v);
				// Aila 28121
//				MyIntList list = getVertices(28724);
				// mumbai
//				MyIntList list = getVertices(249046);
//				list.addAll(getVertices(245671));
				// nakazawa
//				66289 59613
//				MyIntList list = getVertices(66289);
//				list.addAll(getVertices(59613));
				
				// teaser
				// 55759 28791
//				MyIntList list = getVertices(55759);
//				list.addAll(getVertices(28791));
				for(int i = 0;i < list.length;i ++) {
					updateTrack(list.get(i));
				}
			} else {
				hopefullyFinalTrack();
			}
			redrawAll = false;
		}
		if (updated) {
			System.out.println(curTime + " " + minx + " " + miny + " " + maxx + " " + maxy);
			renderModel();
			updated = false;
		}
		if(track == null) {
			gl.glCallList(list);
		}
		
		if(vertex) {
			renderTrack(vertex);
		} else {
			renderFinalTrack();
		}
		
//		gl.glDisable(GL2.GL_COLOR_MATERIAL);
//		gl.glDisable(GL2.GL_BLEND);
//		gl.glDisable(GL2.GL_LINE_SMOOTH);
//		gl.glDisable(GL2.GL_POINT_SMOOTH);
		
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL2.GL_ALPHA_TEST);
		gl.glPopMatrix();
		renderDate();
		
	}
	
	Cloud cur = new Cloud();
	Cloud nex = new Cloud();
	public MyIntList getVertices(int v) {
		cur.read(curTime);
		int xc = v % nx;
		int yc = v / nx;
		MyIntList q = new MyIntList();
		int cr = cur.region[v];
		int density = (int) Options.getLocalSampleDensity();
		int ar = 25;
		// mumbai
		// ar = 50;
		// if(v == 249046) {
		// 	density = 5;
		// }
		// aila
//		density = 10;
		// nakazawa
//		density = 12;
		for(int ii = xc - ar;ii < xc + ar;ii += density) {
			for(int jj = yc - ar;jj < yc + ar;jj += density) {
				if(ii >= nx || ii < 0 || jj >= ny || jj < 0) {
					continue;
				}
				int j = ii + jj * nx;
				if(cur.region[j] == cr) {
					q.add(j);
				}
			}
		}
		return q;
	}
	
	private void updateTrack(int v) {
//		paths.clear();
		if(v == -1) {
			return;
		}
		
		Path p = new Path();
		paths.add(p);
		
		p.addForward(v);
		p.addBackward(v);
		Cloud oc = new Cloud();
		Cloud c = new Cloud();
		int ov = v;
		oc.read(curTime);
		int max = Math.min(curTime + Options.getLocalLengthAfter(), maxTime - Options.getLocalLengthBefore());
//		int max = Math.min(curTime + 5, maxTime - 1);
//		int max = maxTime - 1;
		for(int i = curTime;i < max;i ++) {
			c.read(i + 1);
			atg.tg.readVector(i,0);
			float xx = v % nx;
			float yy = v / nx;
			xx += atg.tg.vector[v][0];
			yy += atg.tg.vector[v][1];
			int x = Math.round(xx);
			int y = Math.round(yy);
			
//			int pv = v;
			v = x + y * nx;
			if(x >= nx || x < 0 || y >= ny || y < 0 || c.region[v] == -1) {
//				v = findAlternateVertex(pv, oc, c);
//				if(v == -1) {
					break;
//				}
			}
			p.addForward(v);
			Cloud temp = c;
			c = oc;
			oc = temp;
		}
		
		v = ov;
		oc.read(curTime);
		int min = Math.max(curTime - 5, 1);
//		int min = 1;
		for(int i = curTime;i > min;i --) {
			c.read(i - 1);
			atg.tg.readRevVector(i);
			float xx = v % nx;
			float yy = v / nx;
			xx += atg.tg.vector[v][0];
			yy += atg.tg.vector[v][1];
			int x = Math.round(xx);
			int y = Math.round(yy);
			
			v = x + y * nx;
			if(x >= nx || x < 0 || y >= ny || y < 0 || c.region[v] == -1) {
				break;
			}
			p.addBackward(v);
			Cloud temp = c;
			c = oc;
			oc = temp;
		}
	}

	void renderTrack(boolean vertex) {
		for(Iterator<Path> it = paths.iterator();it.hasNext();) {
			Path p = it.next();
			p.draw(gl, vertex);
		}
	}

	void renderDate() {
//		gl.glColor3f(0, 0, 0);
//		int l = glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_18, date[curTime]);
//		gl.glRasterPos2f(-maxx, maxy + 1);
//		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, date[curTime]);
		ToolBar.setDate(date[curTime]);
	}

	private void renderMap() {
		gl.glColor3f(1,1,1);
		mapTex.enable();
		mapTex.bind();
		gl.glBegin(GL2.GL_QUADS);
		
//		gl.glTexCoord2d(0, 1);
//		gl.glVertex2d(minx, miny);
//
//		gl.glTexCoord2d(1, 1);
//		gl.glVertex2d(maxx, miny);
//
//		gl.glTexCoord2d(1, 0);
//		gl.glVertex2d(maxx, maxy);
//
//		gl.glTexCoord2d(0, 0);
//		gl.glVertex2d(minx, maxy);

//		double x1 = 0.4695 + stLon / 360.0;
//		double x2 = 0.4695 + enLon / 360.0;
//		double y1 = 0.4998 - enLat / 180.0;
//		double y2 = 0.4998 - stLat / 180.0;
		double x1 = 0.5 + stLon / 360.0;
		double x2 = 0.5 + enLon / 360.0;
		double y1 = 0.5 - enLat / 180.0;
		double y2 = 0.5 - stLat / 180.0;
		
		gl.glTexCoord2d(x1, y2);
		gl.glVertex2d(minx, miny);

		gl.glTexCoord2d(x2, y2);
		gl.glVertex2d(maxx, miny);

		gl.glTexCoord2d(x2, y1);
		gl.glVertex2d(maxx, maxy);

		gl.glTexCoord2d(x1, y1);
		gl.glVertex2d(minx, maxy);

		gl.glEnd();

		mapTex.disable();
	}

	Texture mapTex;
	Texture redArrow;
	Texture blueArrow;
	Texture redArrowFull;
	Texture blueArrowFull;
	
	private void initTexture() {
		try {
//			File f = new File("india.png");
//			File f = new File("nakazawa.png");
			File f = new File("World.png");
			
//			File f = new File(mapFile);
			mapTex = TextureIO.newTexture(f, false);
			mapTex.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			mapTex.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			
			f = new File("RedArrowFull.png");
			redArrow = TextureIO.newTexture(f, false);
			redArrow.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			redArrow.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

			f = new File("BlueArrowFull.png");
			blueArrow = TextureIO.newTexture(f, false);
			blueArrow.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			blueArrow.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			
			f = new File("RedArrowFull.png");
			redArrowFull = TextureIO.newTexture(f, false);
			redArrowFull.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			redArrowFull.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

			f = new File("BlueArrowFull.png");
			blueArrowFull = TextureIO.newTexture(f, false);
			blueArrowFull.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			blueArrowFull.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		} catch (IOException exc) {
			exc.printStackTrace();
			System.exit(1);
		}
	}
	
	void renderArrow(int v, int prev, int next) {
		float dx = vertices[next].x - vertices[prev].x;
		float dy = vertices[next].y - vertices[prev].y;
		float norm = dx * dx + dy * dy;
		norm = (float) Math.sqrt(norm);
		dx /= norm;
		dy /= norm;
		// 150 -- 3
		float w = extent / 150;
		w *= Options.getLocalArrowWidth();
		
		float x1 = vertices[v].x + w * dx;
		float y1 = vertices[v].y + w * dy;
		
		float px, py;
		if(dy == 0) {
			py = vertices[v].y + dy;
			// dx * px + dy * newy = 0
			px = - (dy * py) / dx;
		} else {
			px = vertices[v].x + dx;
			// dx * px + dy * newy = 0
			py = - (dx * px) / dy;
		}
		norm = px * px + py * py;
		norm = (float) Math.sqrt(norm);
		px /= norm;
		py /= norm;
		
		float x2 = x1 + w * px;
		float y2 = y1 + w * py;
		x1 -= w * px;
		y1 -= w * py;

		float x3 = vertices[v].x - w * dx;
		float y3 = vertices[v].y - w * dy;
		
		float x4 = x3 - w * px;
		float y4 = y3 - w * py;
		x3 += w * px;
		y3 += w * py;
		
		gl.glTexCoord2d(1, 0);
		gl.glVertex2f(x1, y1);
		gl.glTexCoord2d(1, 1);
		gl.glVertex2f(x2, y2);
		gl.glTexCoord2d(0, 1);
		gl.glVertex2f(x3, y3);
		gl.glTexCoord2d(0, 0);
		gl.glVertex2f(x4, y4);
	}

	
	void renderTrackArrow(int t, int p, int n) {
		float dx = track[n].midx - track[p].midx;
		float dy = track[n].midy - track[p].midy;
		float norm = dx * dx + dy * dy;
		norm = (float) Math.sqrt(norm);
		dx /= norm;
		dy /= norm;
		float w = extent / 150;
		w *= Options.getGlobalArrowWidth();
		
		float x1 = track[t].midx + w * dx;
		float y1 = track[t].midy + w * dy;
		
		float px, py;
		if(dy == 0) {
			py = track[t].midy + dy;
			// dx * px + dy * newy = 0
			px = - (dy * py) / dx;
		} else {
			px = track[t].midx + dx;
			// dx * px + dy * newy = 0
			py = - (dx * px) / dy;
		}
		norm = px * px + py * py;
		norm = (float) Math.sqrt(norm);
		px /= norm;
		py /= norm;
		
		float x2 = x1 + w * px;
		float y2 = y1 + w * py;
		x1 -= w * px;
		y1 -= w * py;

		float x3 = track[t].midx - w * dx;
		float y3 = track[t].midy - w * dy;
		
		float x4 = x3 - w * px;
		float y4 = y3 - w * py;
		x3 += w * px;
		y3 += w * py;
		
		gl.glColor3f(1,1,1);
		gl.glTexCoord2d(1, 0);
		gl.glVertex2f(x1, y1);
		gl.glTexCoord2d(1, 1);
		gl.glVertex2f(x2, y2);
		gl.glTexCoord2d(0, 1);
		gl.glVertex2f(x3, y3);
		gl.glTexCoord2d(0, 0);
		gl.glVertex2f(x4, y4);
	}

	void renderModel() {
		gl.glNewList(list, GL2.GL_COMPILE);

		gl.glBegin(GL2.GL_TRIANGLES);
		for (int x = 0; x < triangles.length; x++) {
			int i = triangles[x].v1;
			float[] col = getColor(i);
			gl.glColor4fv(col, 0);
			gl.glVertex2f(vertices[i].x, vertices[i].y);

			i = triangles[x].v2;
			col = getColor(i);
			gl.glColor4fv(col, 0);
			gl.glVertex2f(vertices[i].x, vertices[i].y);

			i = triangles[x].v3;
			col = getColor(i);
			gl.glColor4fv(col, 0);
			gl.glVertex2f(vertices[i].x, vertices[i].y);
		}
		gl.glEnd();

		gl.glEndList();
	}

	float[] curColor = new float[4];

	float [] colors;
	public void initColor() {
		int noColors = 10000;
		colors = new float[noColors*3];
//		long seed = 546982354789l;
		long seed = 256321548521l;
		Random rand = new Random(seed);
		for(int i = 0;i < noColors;i ++) {
			int ct = i * 3;
			colors[ct] = rand.nextFloat();
			colors[ct + 1] = rand.nextFloat();
			colors[ct + 2] = rand.nextFloat();
		}
	}
	
	
	private float[] getColor(int v1) {
		boolean highlight = Options.getHighlight();
		if(highlight) {
			int region = cloud.region[v1];
			int vcol = -1;
			int node = -1;
			if(region != -1) {
				node = atg.tg.index[curTime][region];
				vcol = atg.col[node];
			}
			if(!tmpPath.isEmpty()) {
				if(tmpPath.contains(node)){
					int col = vcol % 10000;
					int ct = col * 3;
					
					curColor[0] = colors[ct];
					curColor[1] = colors[ct + 1];
					curColor[2] = colors[ct + 2];
					curColor[3] = 1;
				} else {
					curColor[0] = 1;
					curColor[1] = 1;
					curColor[2] = 1;
					curColor[3] = 1;
				}
				return curColor;
			}
			if(vcol == -1) {
				curColor[0] = 1;
				curColor[1] = 1;
				curColor[2] = 1;
				curColor[3] = 0;
			} else {
				int col = vcol % 10000;
				int ct = col * 3;
				
				curColor[0] = colors[ct];
				curColor[1] = colors[ct + 1];
				curColor[2] = colors[ct + 2];
				if(isBorder(v1)) {
					curColor[3] = 1;
				} else {
//					float val = (float) ((cloud.fn[v1] - diffVal) / (maxVal - diffVal));
//					curColor[3] = (1 - val);
					curColor[3] = 0.3f;
				}
			}
			return curColor;
		} else {
			float mul = (TransferFunction.COLORMAPSIZE - 1);
			
			int region = cloud.region[v1];
			if(region == -1) {
				curColor[0] = 1;
				curColor[1] = 1;
				curColor[2] = 1;
				curColor[3] = 0;
				return curColor;
			}
			// TRMM
//			float val = (float) ((cloud.fn[v1] - 65) / 35);
//			IR
			float val = cloud.fn[v1] / 330f;
			int in = (int) (val * mul) * 4;
			curColor[0] = colorMap[in];
			curColor[1] = colorMap[in + 1];
			curColor[2] = colorMap[in + 2];
//			if(isBorder(v1)) {
//				curColor[3] = 1;
//			} else {
//				curColor[3] = 0.3f;
//			}
//			val = (float) ((cloud.fn[v1] - diffVal) / (maxVal - diffVal));
//			curColor[3] = (1 - val) * 2;
			curColor[3] = 0.5f;
			return curColor;
		}
	}

	private boolean isBorder(int v) {
		for(Iterator<Integer> tris = vertices[v].tris.iterator();tris.hasNext();) {
			int t = tris.next();
			int v1, v2;
			if(triangles[t].v1 == v) {
				v1 = triangles[t].v2;
				v2 = triangles[t].v3;
			} else if(triangles[t].v2 == v) {
				v1 = triangles[t].v1;
				v2 = triangles[t].v3;
			} else {
				v1 = triangles[t].v1;
				v2 = triangles[t].v2;
			}
			int region = cloud.region[v1];
			int vcol = -1;
			if(region != -1) {
				int node = atg.tg.index[curTime][region];
				vcol = atg.col[node];
			}
			if(vcol == -1) {
				return true;
			}
			region = cloud.region[v2];
			vcol = -1;
			if(region != -1) {
				int node = atg.tg.index[curTime][region];
				vcol = atg.col[node];
			}
			if(vcol == -1) {
				return true;
			}
		}
		return false;
	}

	public double getExtent() {
		return extent;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	public void updateBranches(HashMap<Integer, Integer> edgeBranchMap) {
		for (int i = 0; i < col.length; i++) {
			int e = col[i];
			if (e == -1) {
				col[i] = -1;
			} else {
				Integer in = edgeBranchMap.get(e);
				if (in == null) {
					col[i] = -1;
				} else {
					col[i] = (in + 1);
				}
			}
		}
	}

	public void setScreenSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void rotate(float x1, float y1, float x2, float y2) {
		// do nothing
	}
	
	public void nextTime() {
		curTime ++;
		if(curTime == maxTime) {
			curTime --;
		} else {
			cloud.read(curTime);
			updated = true;
		}
	}
	
	public void prevTime() {
		curTime --;
		if(curTime < 0) {
			curTime = 0;
		} else {
			cloud.read(curTime);
			updated = true;
		}
	}

	int x,y;
	boolean clicked = false;
	@Override
	public void mouseClicked(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON1 && event.isControlDown()) {
			java.awt.Point p = event.getPoint();
			x = p.x;
			y = p.y;
			clicked = true;
		}
	}
	
	int getSelectedVertex() {
		drawVerticesForSelection();
		FloatBuffer buffer = FloatBuffer.allocate(3);
		int[] viewport = new int[4];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glReadPixels(x, viewport[3] - y, 1, 1, GL2.GL_RGB, GL2.GL_FLOAT, buffer);
		float id = buffer.get();
		float c1 = buffer.get();
		float c2 = buffer.get();
		if (id == 1) {
			return -1;
		}
		int x = (int) (c1 * nx);
		int y = (int) (c2 * ny);
		int v = x + nx * y; 
		return v;
	}

	private void drawVerticesForSelection() {
		float[] color = new float[] {0.5f, 0, 0};
		gl.glBegin(GL2.GL_QUADS);
		color[1] = 0;
		color[2] = 0;
		gl.glColor3fv(color, 0);
		gl.glVertex2f(minx, miny);

		color[1] = 1;
		color[2] = 0;
		gl.glColor3fv(color, 0);
		gl.glVertex2d(maxx, miny);

		color[1] = 1;
		color[2] = 1;
		gl.glColor3fv(color, 0);
		gl.glVertex2d(maxx, maxy);

		color[1] = 0;
		color[2] = 1;
		gl.glColor3fv(color, 0);
		gl.glVertex2d(minx, maxy);
		gl.glEnd();
	}
	
	
	
	public class Path {
		MyIntList forward = new MyIntList();
		MyIntList backward = new MyIntList();
		
		public void clear() {
			forward.clear();
			backward.clear();
		}
		
		public void addForward(int v) {
			forward.add(v);
		}
		
		public void addBackward(int v) {
			backward.add(v);
		}
		
		public void addForward(int x, int y) {
			forward.add(x);
			forward.add(y);
		}
		
		public void addBackward(int x, int y) {
			backward.add(x);
			backward.add(y);
		}

		public void draw(GL2 gl, boolean vertex) {
			if(forward.length + backward.length < 5) {
				return;
			}
			
			gl.glLineWidth(2);
			gl.glColor3f(1,1,1);
//			drawLine(backward, vertex);
			redArrow.enable();
			redArrow.bind();
			drawGlyph(backward, true);
			redArrow.disable();
			
			gl.glColor3f(1,1,1);
//			drawLine(forward, vertex);
			blueArrow.enable();
			blueArrow.bind();
			drawGlyph(forward, false);
			blueArrow.disable();
		}
		
		void drawGlyph(MyIntList arr, boolean back) {
			gl.glBegin(GL2.GL_QUADS);
			if(back) {
				for (int i = arr.length-1; i >= 0; i-= 1) {
					int v = arr.get(i);
					int p = Math.min(arr.length - 1,i + 1);
					int n = Math.max(i - 1,0);
					renderArrow(v, arr.get(p), arr.get(n));
				}
			} else {
				//36119
				for (int i = 0; i < arr.length; i+= 1) {
					int v = arr.get(i);
					int p = Math.max(0, i - 1);
					int n = Math.min(i + 1, arr.length - 1);
					renderArrow(v, arr.get(p), arr.get(n));
				}
			}
			gl.glEnd();
		}

		void drawLine(MyIntList arr, boolean vertex) {
			
			if(vertex) {
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int i = 0; i < arr.length; i++) {
					int v = arr.get(i);
					gl.glVertex2f(vertices[v].x, vertices[v].y);
				}
				gl.glEnd();
			} else {
				gl.glBegin(GL.GL_LINE_STRIP);
				for(int i = 0;i < arr.length;i += 2) {
					int x = arr.get(i);
					int y = arr.get(i + 1);
					int v = x + y * nx;
					gl.glVertex2f(vertices[v].x, vertices[v].y);
				}
				gl.glEnd();

			}
		}
	}
	
	class Verts {
		HashSet<Integer> verts = new HashSet<Integer>();
	}
	
	class PathList {
		MyIntList arr = new MyIntList();
		ArrayList<Verts> v = new ArrayList<Verts>();
	}
	
	ArrayList<Path> paths = new ArrayList<Path>();
	boolean [] done;
	int degree = 5;
	HashSet<Integer> tmpPath = new HashSet<Integer>();

	class CloudTrack {
		HashSet<Integer> pts;
		HashSet<Integer> boundary;
		float midx, midy;
	}
	
	CloudTrack [] track;
	void hopefullyFinalTrack() {
		cur.read(curTime);
		HashSet<Integer> q = new HashSet<Integer>();
		ArrayList<Integer> tmp = new ArrayList<Integer>();

		for(Iterator<Integer> it = selList.iterator();it.hasNext();) {
			int vert = it.next();
			int region = cur.region[vert];
			
			if(region != -1) {
				int node = atg.tg.index[curTime][region];
//				int col = atg.col[node];
				q.add(node);
				tmp.add(vert);
			}
		}
		selList.clear();
		selList.addAll(tmp);
		if(q.size() == 0) {
			return;
		}
		
		track = new CloudTrack[maxTime];
		done = new boolean[atg.tg.nodes.size()];
		bfsct = 0;
		bfs(q, false);
		System.out.println("BFS ct backward: " + bfsct);
		done = new boolean[atg.tg.nodes.size()];
		bfsct = 0;
		bfs(q, true);
		System.out.println("BFS ct forward: " + bfsct);
//		newBfs(col);
		System.out.println("BFS done");
	}
	
	int bfsct;
	int minSize = 10;
	void bfs(HashSet<Integer> curTimeStep, boolean forward) {
		if(curTimeStep.size() == 0) {
			return;
		}
		int t = -1;
		HashSet<Integer> nextTimeStep = new HashSet<Integer>();
		HashSet<Integer> pts = new HashSet<Integer>();
		float ax = 0;
		float ay = 0;
		for(Iterator<Integer> it = curTimeStep.iterator();it.hasNext();) {
			int node = it.next();
			if(done[node]) {
				Utilities.er("how can a node be visited twice in bfs??!!!!");
			}
			done[node] = true;
			Adjacencies adj = atg.tg.adj.get(node);
			int size = forward?adj.next.length:adj.prev.length;
			for (int i = 0; i < size; i++) {
				int in = forward?adj.next.get(i):adj.prev.get(i);
				Edge e = atg.tg.edges.get(in);
				int next = forward?e.n2:e.n1;
				if(forward && e.n1 != node || !forward && e.n2 != node) {
					Utilities.er("??");
				}
				nextTimeStep.add(next);
			}
			bfsct ++;
			
			Node n = atg.tg.nodes.get(node);
			if(t == -1) {
				cur.read(n.time);
				t = n.time;
				System.out.println("bfs time: " + t);
			} else {
				if(t != n.time) {
					Utilities.er("possible?");
				}
			}
			if(n.size < minSize) {
				continue;
			}
			for(int j = cur.index[n.featureNo]; j < cur.index[n.featureNo + 1];j ++) {
				int v = cur.regionVertices[j];
				pts.add(v);
				ax += vertices[v].x;
				ay += vertices[v].y;
			}
		}
		track[t] = new CloudTrack();
		track[t].pts = pts;
		track[t].boundary = getBoundary(pts); 
		ax /= pts.size();
		ay /= pts.size();
		track[t].midx = ax;
		track[t].midy = ay;
		bfs(nextTimeStep, forward);
	}

	private HashSet<Integer> getBoundary(HashSet<Integer> pts) {
		HashSet<Integer> ret = new HashSet<Integer>();
		for(Iterator<Integer> it = pts.iterator();it.hasNext();) {
			int v = it.next();
			boolean boundary = false;
			for(Iterator<Integer> tris = vertices[v].tris.iterator();tris.hasNext();) {
				int t = tris.next();
				int v1, v2;
				if(triangles[t].v1 == v) {
					v1 = triangles[t].v2;
					v2 = triangles[t].v3;
				} else if(triangles[t].v2 == v) {
					v1 = triangles[t].v1;
					v2 = triangles[t].v3;
				} else {
					v1 = triangles[t].v1;
					v2 = triangles[t].v2;
				}
				if(!pts.contains(v1) || !pts.contains(v2)) {
					boundary = true;
					break;
				}
			}

			if(boundary) {
				ret.add(v);
			}
		}
		return ret;
	}

	
	HashSet<Integer> tris = new HashSet<Integer>();
	void renderFinalTrack() {
		if(track == null) {
			return;
		}
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL.GL_GREATER, 0);

		
//		int diff = 2;
		int diff = (int) Options.getGlobalSampleDensity();
		gl.glBegin(GL.GL_TRIANGLES);
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		int before = Options.getGlobalLengthBefore();
		int after = Options.getGlobalLengthAfter();
		
		if(!animate) {
			int t = curTime - diff;
			for(int i = 0;i < before;i++) {
				if(t < 0) {
					break;
				}
				list.add(0,t);
				t -= diff;
			}
			t = curTime;
			for(int i = 0;i < after;i++) {
				if(t >= maxTime) {
					break;
				}
				list.add(t);
				t += diff;
			}
		} else {
			list.add(curTime);
		}
//		System.out.println("\n\n time steps");
//		for(int xx = 0; xx < list.size();xx ++) {
//			System.out.println(list.get(xx));
//		}
//		System.out.println();
//		for(int i = curTime;i < maxTime;i +=diff) {
//			list.add(i);
//		}
		int blurNo = 1;
		float [] blur = new float[blurNo];
		blur[0] = 0.5f;
		if(blurNo > 1) {
			blur[1] = 0.3f;
			for(int i = 2;i < blurNo;i ++) {
				blur[i] = blur[i-1]/ 1.2f;
			}
		}
//		int ct = 0;
//		{
//			// 11 15 21 25 27 -- for sample
//			list.clear();
//			list.add(22);
//			list.add(30);
//			list.add(42);
//			list.add(50);
//			list.add(54);
//		}
		
//		{
//			// for aila 
//			// 9 19 29 39 49 59 69 79
//			list.clear();
//			list.add(9);
////			list.add(19);
////			list.add(29);
////			list.add(39);
//			list.add(49);
////			list.add(59);
//			list.add(69);
//		}
		
//		{
//			// Mumbai 12 22 32 42 52 | 62 72 82
//			list.clear();
//			list.add(02);
//			list.add(22);
//			list.add(42);
//			list.add(62);
//			list.add(72);
//		}
//		for(Iterator<Integer> it = list.iterator();it.hasNext();) {
		float [][] cols = {{237, 248, 177}, { 127, 205, 187},{  44, 127, 184 }}; 
//		float min = list.get(0);
//		float max = list.get(list.size() - 1);
		float min = -1;
		float max = -1;
		for(int xx = 0; xx < list.size();xx ++) {
			int i = list.get(xx);
			if(track[i] == null) {
				continue;
			}
			if(min == -1) {
				min = i;
			}
			max = i;
		}
		finalTimeStep = false;
		for(int xx = 0; xx < list.size();xx ++) {
			int i = list.get(xx);
//			if(xx != 0 && xx != list.size() - 1) {
//				continue;
//			}
//			double pos = (double)ct / list.size();
//			int ps = (int) (pos * 1024);
//			ps *= 4;
//			ct ++;
////			if(i == curTime) {
////				curColor[0] = 0;
////				curColor[1] = 0;
////				curColor[2] = 1;
////			} else {
//				curColor[0] = colorMap[ps];
//				curColor[1] = colorMap[ps + 1];
//				curColor[2] = colorMap[ps + 2];
////			}
//			if(i >= 63) {
//				finalTimeStep = true;
//			}
			double pos = ((double)(i) - min) / (max - min);
//			ct ++;
			if(pos < 0.5) {
				pos *= 2;
				curColor[0] = (float) ((1.0 - pos) * cols[0][0] + pos * cols[1][0]);
				curColor[1] = (float) ((1.0 - pos) * cols[0][1] + pos * cols[1][1]);
				curColor[2] = (float) ((1.0 - pos) * cols[0][2] + pos * cols[1][2]);
				pos /= 2;
			} else {
				pos -= 0.5;
				pos *= 2;
				curColor[0] = (float) ((1.0 - pos) * cols[1][0] + pos * cols[2][0]);
				curColor[1] = (float) ((1.0 - pos) * cols[1][1] + pos * cols[2][1]);
				curColor[2] = (float) ((1.0 - pos) * cols[1][2] + pos * cols[2][2]);
				pos /= 2;
				pos += 0.5;
			}
			curColor[0] /= 255f;
			curColor[1] /= 255f;
			curColor[2] /= 255f;
			
			for(int j = blurNo-1;j >= 0;j --) {
				if(i - j >= 0) {
					finalTrack(i - j, blur[j]);
				}
			}
		}
		
		gl.glEnd();
		
//		gl.glLineWidth(4);
//		gl.glBegin(GL.GL_LINE_STRIP);
//		for(int xx = 0; xx < list.size();xx ++) {
//			int i = list.get(xx);
//			if(track[i] == null) {
//				continue;
//			}
//			double pos = ((double)(i) - min) / (max - min);
//			ct ++;
//			curColor[0] = (float) (1.0 - pos);
//			curColor[1] = 0;
//			curColor[2] = (float) (pos);
//			gl.glColor3f(curColor[0],curColor[1],curColor[2]);
//			gl.glVertex2d(track[i].midx, track[i].midy);
//		}
//		gl.glEnd();
		
		if(!animate) {
			int first = 0;
			int p = list.get(0);
			for(int xx = 0; xx < list.size();xx ++) {
				int i = list.get(xx);
				int n = i;
				if(xx < list.size() - 1) {
					n = list.get(xx + 1);
				}
				if(track[i] == null) {
					continue;
				}
				if(track[p] == null) {
					p = i;
				}
				if(track[n] == null) {
					n = i;
				}
				if(i < curTime && first == 0) {
					redArrowFull.enable();
					redArrowFull.bind();
					first = 1;
					gl.glBegin(GL2.GL_QUADS);
				} else if(i >= curTime && first == 1) {
					gl.glEnd();
					redArrowFull.disable();
					blueArrowFull.enable();
					blueArrowFull.bind();
					first = 2;
					gl.glBegin(GL2.GL_QUADS);
				}
				renderTrackArrow(i,p,n);
				p = i;
			}
			if(first == 2) {
				gl.glEnd();
				blueArrowFull.disable();
			} else if(first == 1) {
				gl.glEnd();
				redArrowFull.disable();
			}
		}
		
		
		gl.glLineWidth(1);
//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL2.GL_ALPHA_TEST);
	}
	
	boolean finalTimeStep;
	void finalTrack(int time, float blur) {
		int i = time;
		if(track[i] == null) {
			return;
		}
		
		curColor[3] = blur;
//		gl.glColor3f(curColor[0], curColor[1], curColor[2]);
		tris.clear();
		for(Iterator<Integer> it = track[i].pts.iterator();it.hasNext();) {
			int v = it.next();
			tris.addAll(vertices[v].tris);
		}
		for(Iterator<Integer> it = tris.iterator();it.hasNext();) {
			int t = it.next();
			
			int v = triangles[t].v1;
			if(animate) {
				curColor = getColor(v);
			}
			if(track[i].boundary.contains(v)) {
				gl.glColor4f(0,0,0,1);
			} else if(track[i].pts.contains(v)) {
				gl.glColor4f(curColor[0],curColor[1],curColor[2], curColor[3]);
//				gl.glColor4f(1,0,0,1);
			} else {
				gl.glColor4f(1,1,1,0);
			}
			if(finalTimeStep) {
				if(track[i].boundary.contains(v)) {
					gl.glColor4f(1,0,0,1);
				} else if(track[i].pts.contains(v)) {
					gl.glColor4f(1,0,0,0.3f);
				} else {
					gl.glColor4f(1,1,1,0);
				}
			}
			
			gl.glVertex2f(vertices[v].x, vertices[v].y);
			
			v = triangles[t].v2;
			if(animate) {
				curColor = getColor(v);
			}

			if(track[i].boundary.contains(v)) {
				gl.glColor4f(0,0,0,1);
			} else if(track[i].pts.contains(v)) {
				gl.glColor4f(curColor[0],curColor[1],curColor[2], curColor[3]);
//				gl.glColor4f(1,0,0,1);
			} else {
				gl.glColor4f(1,1,1,0);
			}
			if(finalTimeStep) {
				if(track[i].boundary.contains(v)) {
					gl.glColor4f(1,0,0,1);
				} else if(track[i].pts.contains(v)) {
					gl.glColor4f(1,0,0,0.3f);
				} else {
					gl.glColor4f(1,1,1,0);
				}
			}
			gl.glVertex2f(vertices[v].x, vertices[v].y);
			
			v = triangles[t].v3;
			if(animate) {
				curColor = getColor(v);
			}

			if(track[i].boundary.contains(v)) {
				gl.glColor4f(0,0,0,1);
			} else if(track[i].pts.contains(v)) {
				gl.glColor4f(curColor[0],curColor[1],curColor[2], curColor[3]);
//				gl.glColor4f(1,0,0,1);
			} else {
				gl.glColor4f(1,1,1,0);
			}
			if(finalTimeStep) {
				if(track[i].boundary.contains(v)) {
					gl.glColor4f(1,0,0,1);
				} else if(track[i].pts.contains(v)) {
					gl.glColor4f(1,0,0,0.3f);
				} else {
					gl.glColor4f(1,1,1,0);
				}
			}
			gl.glVertex2f(vertices[v].x, vertices[v].y);
		}
	}

	public void executeCommand() {
		String s = JOptionPane.showInputDialog("Enter command:");
		if ((s != null) && (s.length() > 0)) {
			execute(s);
		}
	}
	
	boolean redrawAll;
	private void execute(String command) {
		String [] args = Utilities.splitString(command.trim());
		if(args.length == 0) {
			return;
		}
		String com = args[0].trim();
		if(com.equalsIgnoreCase("direction")) {
			findCloudInDirection(args[1].trim());
		}
		if(com.equalsIgnoreCase("track")) {
			if(args[1].trim().equals("local")) {
				vertex = true;
				updated = true;
				track = null;
				redrawAll = true;
			} else if(args[1].trim().equals("global")) {
				vertex = false;
				updated = true;
				redrawAll = true;
			}
		}
		if(com.equalsIgnoreCase("option")) {
			if(args.length > 2) {
				redrawAll = Options.setOptions(args[1].trim(), args[2].trim());
			} else {
				redrawAll = Options.setOptions(args[1].trim(), "");
			}
			updated = true;
		}
		if(com.equalsIgnoreCase("time")) {
			int time = Integer.parseInt(args[1].trim());
			if(time >= 0 && time < maxTime && time != curTime) {
				curTime = time;
				cloud.read(time);
				updated = true;
				redrawAll = false;
			}
		}
	}
	
	int minLength = 3;
	private void findCloudInDirection(String dir) {
		Direction d;
		if(dir.equalsIgnoreCase("east")) {
			d = Direction.East;
		} else if(dir.equalsIgnoreCase("west")) {
			d = Direction.West;
		} else if(dir.equalsIgnoreCase("north")) {
			d = Direction.North;
		} else if(dir.equalsIgnoreCase("south")) {
			d = Direction.South;
		} else {
			return;
		}
		east = atg.tg.findDirMovingCloud(curTime, minLength, d);
		System.out.println("Finished finding east moving clouds");
		MyIntList used = new MyIntList();
		for(int i = 0;i < east.size();i ++) {
			int v = east.get(i);
			if(!isClose(used,v)) {
				updateTrack(v);
				used.add(v);
			}
		}
		vertex = true;
		updated = true;
	}

	boolean animate = false;
	public void toggleAnimate() {
		animate = !animate;		
	}
}
