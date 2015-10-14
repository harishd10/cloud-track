uniform vec3 extent;
uniform vec3 minCorner;

void main() {
	gl_FrontColor = vec4((gl_Vertex.xyz - minCorner)/extent, 1);
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}