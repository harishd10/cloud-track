/**
 * Author : Harish D.
 */
package vgl.iisc.volray.ui.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class TransferFunctionEditor extends JComponent {

	public class RGBA {
		public float r;

		public float g;

		public float b;

		public float a;
	}

	class ColorPoint implements Comparable<ColorPoint> {
		float fValue;

		float r, g, b;

		boolean flat = false;

		Rectangle bounds = new Rectangle();

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(ColorPoint o) {
			if (fValue < o.fValue) {
				return -1;
			} else if (fValue > o.fValue) {
				return 1;
			}
			return 0;
		}
	}

	class OpacityPoint implements Comparable<OpacityPoint> {
		float fValue;

		float o;

		Rectangle bounds = new Rectangle();

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(OpacityPoint o) {
			if (fValue < o.fValue) {
				return -1;
			} else if (fValue > o.fValue) {
				return 1;
			}
			return 0;
		}

	}

	private static final long serialVersionUID = -8246612483818096790L;

	ArrayList<ColorPoint> pts = new ArrayList<ColorPoint>();

	ArrayList<OpacityPoint> opts = new ArrayList<OpacityPoint>();

	Rectangle bounds;

	Rectangle ptBound = new Rectangle();

	Rectangle opBound = new Rectangle();

	ColorPoint cur = null;

	OpacityPoint op = null;

	boolean opEndPt = false;

	int noEdges = 2;
	int curEdge = 2;
	
	float [][] col = new float [][] {{255, 247, 236}, {254, 232, 200}, {253, 212, 158}, {253, 187, 132}, 
			{252, 141, 89}, {239, 101, 72}, {215, 48, 31}, {179, 0, 0}, {127, 0, 0}};
	
	float [][] greyCol = new float [][] { {255, 255, 255}, {240, 240, 240}, {217, 217, 217}, {189, 189, 189},
			{150, 150, 150}, {115, 115, 115}, {82, 82, 82}, {37, 37, 37}, {0, 0, 0}			
	};
	
	float [][] rcol = new float [][] { {160,  0, 200}, {30,  60, 255 }, {0 ,210 ,140 } ,{160, 230 , 50 }, {230, 175,  45 }
			};

	public TransferFunctionEditor() {

//		ColorPoint p1 = new ColorPoint();
//		p1.fValue = 0;
//		p1.r = 1;
//		p1.g = 1;
//		p1.b = 1;
//		ColorPoint p2 = new ColorPoint();
//		p2.fValue = 0.5f;
//		p2.r = 0;
//		p2.g = 1;
//		p2.b = 0;
//		ColorPoint p3 = new ColorPoint();
//		p3.fValue = 1f;
//		p3.r = 1;
//		p3.g = 0;
//		p3.b = 0;
//		
//		pts.add(p1);
//		pts.add(p2);
//		pts.add(p3);

		float fn = 0;
		float max = 255;
		for(int i = 0;i < 9;i ++){
			ColorPoint p1 = new ColorPoint();
			p1.fValue = fn;
			p1.r = col[i][0] / max;
			p1.g = col[i][1] / max;
			p1.b = col[i][2] / max;
			pts.add(p1);
			fn += 0.125f;
		}
//		for(int i = 0;i < 5;i ++) {
//			ColorPoint p1 = new ColorPoint();
//			if(i != 0) {
//				fn += 0.01;
//			}
//			p1.fValue = fn;
//			if(i != 0) {
//				fn -= 0.01;
//			}
//			p1.r = rcol[i][0] / max;
//			p1.g = rcol[i][1] / max;
//			p1.b = rcol[i][2] / max;
//			pts.add(p1);
//			fn += 0.2;
//			p1 = new ColorPoint();
//			p1.fValue = fn;
//			p1.r = rcol[i][0] / max;
//			p1.g = rcol[i][1] / max;
//			p1.b = rcol[i][2] / max;
//			pts.add(p1);
//		}
		OpacityPoint o1 = new OpacityPoint();
		o1.fValue = 0;
		o1.o = 0.5f;
		
		OpacityPoint o2 = new OpacityPoint();
		o2.fValue = 1f;
		o2.o = 0.5f;

		opts.add(o1);
		opts.add(o2);
		
		initListeners();
	}

	private void initListeners() {
		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				myMouseDragged(e);
			}

			public void mouseMoved(MouseEvent e) {
			}
		});
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				myMousePressed(e);
			}

			public void mouseReleased(MouseEvent e) {
				myMouseReleased(e);
			}
		});
	}

	private void myMouseDragged(MouseEvent e) {
		if (cur != null) {
			int i = pts.indexOf(cur);
			if (i < pts.size() - 1 && i > 0) {
				ColorPoint next = pts.get(i + 1);
				ColorPoint prev = pts.get(i - 1);
				Point p = e.getPoint();
				if ((next.bounds.x) > p.x && (prev.bounds.x + 10) < p.x) {
					cur.bounds.x = p.x;
					cur.fValue = (float) (cur.bounds.x - ptBound.x) / (float) ptBound.width;
					repaint();
				}
			}
		}
		if (op != null) {
			int i = opts.indexOf(op);
			Point p = e.getPoint();
			int y = p.y;
			if (y < opBound.y) {
				y = opBound.y;
			}
			if (y > opBound.y + opBound.height) {
				y = opBound.y + opBound.height;
			}
			float ff = opBound.y + opBound.height - y;
			ff /= opBound.height;
			op.bounds.y = y;
			op.o = ff;
			if (i < opts.size() - 1 && i > 0) {
				OpacityPoint next = opts.get(i + 1);
				OpacityPoint prev = opts.get(i - 1);
				if ((next.bounds.x) > p.x && (prev.bounds.x) < p.x) {
					op.bounds.x = p.x;
					op.fValue = (float) (op.bounds.x - opBound.x) / (float) opBound.width;
				}
			}
			repaint();
		}
	}

	private void myMousePressed(MouseEvent e) {
		Point p = e.getPoint();
		ColorPoint bef = null;
		OpacityPoint obef = null;
		for (Iterator<ColorPoint> it = pts.iterator(); it.hasNext();) {
			ColorPoint pt = it.next();
			if (pt.bounds.x < p.x) {
				bef = pt;
			}
			if (pt.bounds.contains(p)) {
				cur = pt;
			}
		}
		for (Iterator<OpacityPoint> it = opts.iterator(); it.hasNext();) {
			OpacityPoint pt = it.next();
			if (pt.bounds.x < p.x) {
				obef = pt;
			}
			if (pt.bounds.contains(p)) {
				op = pt;
			}
		}

		if (e.getButton() == MouseEvent.BUTTON3) {
			if (cur != null) {
				Color nc = JColorChooser.showDialog(this, "Select Color", new Color(cur.r, cur.g,
						cur.b));
				if (nc != null) {
					cur.r = (float) nc.getRed() / 255;
					cur.g = (float) nc.getGreen() / 255;
					cur.b = (float) nc.getBlue() / 255;
				}
				cur = null;
			} else {
				if (ptBound.contains(p)) {
					bef.flat = !bef.flat;
				}
			}
			repaint();
		}
		if (e.getClickCount() == 2) {
			if (cur == null && ptBound.contains(p)) {
				ColorPoint pt = new ColorPoint();
				pt.fValue = p.x - ptBound.x;
				pt.fValue /= (ptBound.width);
				pt.r = 0;
				pt.g = 0;
				pt.b = 0;
				int i = pts.indexOf(bef);
				pts.add(i + 1, pt);
			} else {
				int i = pts.indexOf(cur);
				if (i > 0 && i < pts.size() - 1) {
					pts.remove(cur);
				}
				cur = null;
			}
			if (op != null) {
				int i = opts.indexOf(op);
				if (i > 0 && i < opts.size() - 1) {
					opts.remove(op);
				}
			} else {
				if (opBound.contains(p)) {
					OpacityPoint o = new OpacityPoint();

					int y = p.y;
					if (y < opBound.y) {
						y = opBound.y;
					}
					if (y > opBound.y + opBound.height) {
						y = opBound.y + opBound.height;
					}
					float ff = opBound.y + opBound.height - y;
					ff /= opBound.height;
					o.bounds.y = y;
					o.bounds.x = p.x;
					o.o = ff;
					o.fValue = p.x - opBound.x;
					o.fValue /= (opBound.width);
					repaint();
					int i = opts.indexOf(obef);
					opts.add(i + 1, o);
				}
			}
			repaint();
		}
	}

	private void myMouseReleased(MouseEvent e) {
		cur = null;
		op = null;
	}

	public void paint(Graphics gr) {
		Graphics2D g = (Graphics2D) gr;
		Rectangle tmpBound = this.getBounds();
		g.setClip(tmpBound.x, tmpBound.y, tmpBound.width, tmpBound.height);
		Rectangle bounds = g.getClipBounds();
		this.bounds = bounds;
		int x1 = bounds.x + 40;
		int y1 = bounds.y + 10;
		int y2 = y1 + 20;
		int w = bounds.width - 50;
		ColorPoint cur = pts.get(0);
		ColorPoint next = pts.get(1);
		int n = 2;
		float diff = next.fValue - cur.fValue;
		for (int i = 0; i < w; i++) {
			float x = i;
			x /= w;
			if (x > next.fValue) {
				cur = next;
				next = pts.get(n);
				n++;
				diff = next.fValue - cur.fValue;
			}
			float n1 = (x - cur.fValue);
			float n2 = (next.fValue - x);
			float cr = (cur.r * n2 + next.r * n1) / diff;
			float cg = (cur.g * n2 + next.g * n1) / diff;
			float cb = (cur.b * n2 + next.b * n1) / diff;
			if (cur.flat) {
				cr = cur.r;
				cg = cur.g;
				cb = cur.b;
			}
			if (cr > 1) {
				cr = 1;
			}
			if (cb > 1) {
				cb = 1;
			}
			if (cg > 1) {
				cg = 1;
			}
			Color c = new Color(cr, cg, cb);
			g.setColor(c);
			g.drawLine(x1, y1, x1, y2);
			x1++;
		}
		x1 = bounds.x + 40;
		y2 = bounds.y + bounds.height - 15;
		for (Iterator<ColorPoint> it = pts.iterator(); it.hasNext();) {
			ColorPoint cpt = it.next();
			int x = (int) (w * cpt.fValue + x1);
			x -= 5;
			Color c = new Color(cpt.r, cpt.g, cpt.b);
			g.setColor(c);
			g.fillRect(x, y2, 10, 10);
			cpt.bounds.x = x;
			cpt.bounds.y = y2;
			cpt.bounds.width = 10;
			cpt.bounds.height = 10;
		}
		ptBound.x = x1;
		ptBound.y = y2;
		ptBound.width = w;
		ptBound.height = 25;
		y2 -= 30;
		y1 += 30;

		String[] s = { "0", "0.2", "0.4", "0.6", "0.8", "1.0" };
		for (int i = 1; i < 6; i++) {
			float f = i;
			f /= 5;
			int xx = (int) (x1 + w * f);
			g.drawLine(xx, y2, xx, y2 + 3);
		}
		for (int i = 0; i < 5; i++) {
			float f = i;
			f /= 5;
			int yy = (int) (y1 + (y2 - y1) * f);
			g.drawLine(x1, yy, x1 - 3, yy);
		}

		for (int i = 1; i < 6; i++) {
			float f = i;
			f /= 5;
			int yy = (int) (y2 - (y2 - y1) * f);
			int xx = (int) (x1 + w * f);

			g.drawChars(s[i].toCharArray(), 0, s[i].length(), xx - 10, y2 + 15);
			g.drawChars(s[i].toCharArray(), 0, s[i].length(), x1 - 25, yy + 5);
		}
		g.drawChars(s[0].toCharArray(), 0, 1, x1 - 15, y2 + 15);

		g.setColor(Color.BLACK);
		g.drawLine(x1, y1, x1, y2);
		g.drawLine(x1, y2, x1 + w, y2);

		opBound.x = x1;
		opBound.y = y1;
		opBound.width = w;
		opBound.height = y2 - y1;

		boolean first = true;
		int prevx = 0, prevy = 0;
		for (Iterator<OpacityPoint> it = opts.iterator(); it.hasNext();) {
			OpacityPoint op = it.next();
			int x = (int) (w * op.fValue + x1);
			int y = y2 - (int) (opBound.height * op.o);
			if (!first) {
				g.drawLine(prevx, prevy, x, y);
			} else {
				first = false;
			}
			prevx = x;
			prevy = y;
			int[] tx = new int[] { x - 5, x + 5, x };
			int[] ty = new int[] { y + 5, y + 5, y - 5 };
			g.fillPolygon(tx, ty, 3);
			op.bounds.x = x - 5;
			op.bounds.y = y - 5;
			op.bounds.width = 10;
			op.bounds.height = 10;
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		TransferFunctionEditor e = new TransferFunctionEditor();
		f.getContentPane().add(e);
		f.setSize(400, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle("Choose Transfer Function");
		f.setVisible(true);
	}

	public RGBA getColor(float fValue) {
		RGBA c = new RGBA();
		ColorPoint bef = null;
		ColorPoint af = null;
		OpacityPoint obef = null;
		OpacityPoint oaf = null;

		for (Iterator<ColorPoint> it = pts.iterator(); it.hasNext();) {
			ColorPoint pt = it.next();
			if (pt.fValue <= fValue) {
				bef = pt;
			}
		}
		for (Iterator<OpacityPoint> it = opts.iterator(); it.hasNext();) {
			OpacityPoint pt = it.next();
			if (pt.fValue <= fValue) {
				obef = pt;
			}
		}
		int i1 = pts.indexOf(bef);
		int i2 = opts.indexOf(obef);
		af = pts.get(i1 + 1);
		oaf = opts.get(i2 + 1);
		float ratio = (fValue - bef.fValue) / (af.fValue - bef.fValue);
		if (bef.flat) {
			ratio = 0;
		}
		c.r = bef.r + ratio * (af.r - bef.r);
		c.g = bef.g + ratio * (af.g - bef.g);
		c.b = bef.b + ratio * (af.b - bef.b);

		c.a = obef.o + (fValue - obef.fValue) / (oaf.fValue - obef.fValue) * (oaf.o - obef.o);

		if (fValue == 0 || c.r == -1) {
//			c.r = c.b = c.g = c.a = 0;
		}
		return c;
	}
}
