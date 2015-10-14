package vgl.iisc.volray.shader;

import java.net.URL;

public class Shaders {
	public static final URL positionVertexShader = Shaders.class.getResource("Position.vert");
	public static final URL volumeVertexShader = Shaders.class.getResource("Volume.vert");
	public static final URL volumeFragmentShader = Shaders.class.getResource("Volume.frag");
}
