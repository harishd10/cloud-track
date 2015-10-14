package vgl.iisc.volray.ui.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import vgl.iisc.volray.display.Options;
import vgl.iisc.volray.display.Renderer;



public class VolRayKeyListener implements KeyListener {

	
	Renderer parent;
	
	public VolRayKeyListener(Renderer parent) {
		super();
		this.parent = parent;
	}
	
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		switch(code) {
			
		case KeyEvent.VK_LEFT :
			Renderer.move(-128,0);
			break;
			
		case KeyEvent.VK_RIGHT :
			Renderer.move(128,0);
			break;

		case KeyEvent.VK_UP :
			Renderer.move(0, 128);
//			Options.incSimplification();
			break;
			
		case KeyEvent.VK_DOWN :
			Renderer.move(0, -128);
//			Options.decSimplification();
			break;
			
//		case KeyEvent.VK_O :
//			Options.setTransformationMode(Options.ROTATE);
//			break;
//			
//		case KeyEvent.VK_T :
//			Options.setTransformationMode(Options.TRANSLATE);
//			break;
//			
//		case KeyEvent.VK_S :
//			Options.setTransformationMode(Options.SCALE);
//			break;
//		
//		case KeyEvent.VK_X :
//			Options.setTransformationAxis(Options.X_AXIS);
//			break;
//			
//		case KeyEvent.VK_Y :
//			Options.setTransformationAxis(Options.Y_AXIS);
//			break;
//			
//		case KeyEvent.VK_Z :
//			Options.setTransformationAxis(Options.Z_AXIS);
//			break;
			
		case KeyEvent.VK_R :
			Renderer.reset();
			break;
		
		case KeyEvent.VK_M :
			Options.toggleProjectionMode();
			break;

		case KeyEvent.VK_C :
			Renderer.executeCommand();
			break;
			
		case KeyEvent.VK_A :
			Renderer.animate();
			break;
		default:
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
