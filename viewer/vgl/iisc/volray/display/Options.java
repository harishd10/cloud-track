/**
 * Author : Harish D.
 */
package vgl.iisc.volray.display;


public class Options {

	public static final int NONE = 0;
	
	public static final int WIREFRAME = 1;
	public static final int SHADED = 2;
	
	public static final int ORTHOGRAPHIC = 6;
	public static final int PERSPECTIVE = 7;
	
	private int projectionMode;

	private static Options options = new Options();
	
	private Options() {
		// default options
		projectionMode = PERSPECTIVE;
	}
	
	public static int getProjectionMode() {
		return options.projectionMode;
	}

	public static void toggleProjectionMode() {
		if(options.projectionMode == ORTHOGRAPHIC) {
			options.projectionMode = PERSPECTIVE;
		} else {
			options.projectionMode = ORTHOGRAPHIC;
		}
		Renderer.modeChanged();
	}
	
	
	private float localArrowWidth = 3.5f;
	private float globalArrowWidth = 10;
	
	private float localSampleDensity = 9;
	private int localLengthBefore = 3;
	private int localLengthAfter = 5;
	
	private float globalSampleDensity = 5;
	private int globalLengthBefore = 5;
	private int globalLengthAfter = 3;

	private boolean highlight = true;
	
	public static float getLocalArrowWidth() {
		return options.localArrowWidth;
	}

	public static void setLocalArrowWidth(float localArrowWidth) {
		options.localArrowWidth = localArrowWidth;
	}

	public static float getGlobalArrowWidth() {
		return options.globalArrowWidth;
	}

	public static void setGlobalArrowWidth(float globalArrowWidth) {
		options.globalArrowWidth = globalArrowWidth;
	}

	public static float getLocalSampleDensity() {
		return options.localSampleDensity;
	}

	public static void setLocalSampleDensity(float localSampleDensity) {
		options.localSampleDensity = localSampleDensity;
	}

	public static int getLocalLengthBefore() {
		return options.localLengthBefore;
	}

	public static void setLocalLengthBefore(int localLengthBefore) {
		options.localLengthBefore = localLengthBefore;
	}

	public static int getLocalLengthAfter() {
		return options.localLengthAfter;
	}

	public static void setLocalLengthAfter(int localLengthAfter) {
		options.localLengthAfter = localLengthAfter;
	}

	public static float getGlobalSampleDensity() {
		return options.globalSampleDensity;
	}

	public static void setGlobalSampleDensity(float globalSampleDensity) {
		options.globalSampleDensity = globalSampleDensity;
	}

	public static int getGlobalLengthBefore() {
		return options.globalLengthBefore;
	}

	public static void setGlobalLengthBefore(int globalLengthBefore) {
		options.globalLengthBefore = globalLengthBefore;
	}

	public static int getGlobalLengthAfter() {
		return options.globalLengthAfter;
	}

	public static void setGlobalLengthAfter(int globalLengthAfter) {
		options.globalLengthAfter = globalLengthAfter;
	}

	public static boolean setOptions(String option, String value) {
		if(option.equalsIgnoreCase("localGlyphSize")) {
			setLocalArrowWidth(Float.parseFloat(value));
			return false;
		}
		if(option.equalsIgnoreCase("globalGlyphSize")) {
			setGlobalArrowWidth(Float.parseFloat(value));
			return false;
		}
		
		if(option.equalsIgnoreCase("localSampleDensity")) {
			setLocalSampleDensity(Float.parseFloat(value));
			return true;
		}
		if(option.equalsIgnoreCase("globalSampleDensity")) {
			setGlobalSampleDensity(Float.parseFloat(value));
			return false;
		}
		
		if(option.equalsIgnoreCase("localLengthBefore")) {
			setLocalLengthBefore(Integer.parseInt(value));
			return false;
		}
		if(option.equalsIgnoreCase("globalLengthBefore")) {
			setGlobalLengthBefore(Integer.parseInt(value));
			return false;
		}

		if(option.equalsIgnoreCase("localLengthAfter")) {
			setLocalLengthAfter(Integer.parseInt(value));
			return false;
		}
		if(option.equalsIgnoreCase("globalLengthAfter")) {
			setGlobalLengthAfter(Integer.parseInt(value));
			return false;
		}
		
		if(option.equalsIgnoreCase("color")) {
			toggleHighlight();
			return false;
		}
		return false;
	}

	public static boolean getHighlight() {
		return options.highlight;
	}
	
	public static void toggleHighlight() {
		options.highlight = ! options.highlight;
	}
	
}
