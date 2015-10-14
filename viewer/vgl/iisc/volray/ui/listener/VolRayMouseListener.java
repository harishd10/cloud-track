/**
 * Author : Harish D.
 */
package vgl.iisc.volray.ui.listener;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import vgl.iisc.volray.display.Renderer;


public class VolRayMouseListener implements MouseListener,MouseMotionListener {

	Point start;
	Renderer parent;
	boolean scale = false;
	
	public VolRayMouseListener(Renderer parent) {
		super();
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
//		Point p = e.getPoint();
//		parent.mouseClicked(p.x, p.y);
		parent.mouseClicked(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		start = e.getPoint();
		if(e.getButton() == MouseEvent.BUTTON1) {
			scale = false;
		} else if(e.getButton() == MouseEvent.BUTTON3) {
			scale = true;
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		try {
			Point p = e.getPoint();
			if(p.distance(start) > 1) {
				parent.transform(start.x, start.y, p.x, p.y, scale);
			} else {
				parent.transform(0,0,0,0,scale);
			}
			start = p;
		} catch (Exception ex) {
			
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	
	}

}
