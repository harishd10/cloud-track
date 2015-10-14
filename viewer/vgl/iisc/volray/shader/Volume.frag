uniform sampler2D rayStart;
uniform sampler2D rayEnd;
uniform sampler3D volume;
uniform sampler1D transferFunction;
uniform vec2 screenDimensions;
uniform vec2 scalarRange;
uniform float stepSize;
uniform vec4 back;
uniform mat4 rotMat;

vec3 getGradient(vec3 startPoint);
vec4 getColor(vec2 d);

float gradStep;
vec4 dir = vec4(0,0,1,0);

void main()
{
	vec4 color = getColor(vec2(0,0));
	/* color = color + getColor(vec2(-0.5,-0.5));
	color = color + getColor(vec2(+0.5,-0.5));
	color = color + getColor(vec2(+0.5,+0.5));
	color = color + getColor(vec2(-0.5,+0.5));
	color /= 5; */
	gl_FragColor = color;
}

vec4 getColor(vec2 d) {
	vec4 color = vec4(0, 0, 0, 0);
	
	vec2 pos = (gl_FragCoord.xy + d) / screenDimensions.xy;
	vec3 startPoint = texture2D(rayStart, pos).xyz;
	vec3 endPoint = texture2D(rayEnd, pos).xyz;
	
	vec3 direction = endPoint - startPoint;
	int steps = int ( floor(length(direction) / stepSize));
	gradStep = 0.001;
	direction = normalize(direction) * stepSize;
	// assume range = 1 (0 to 1), so remove following line
	// float range = scalarRange.y - scalarRange.x;

	while(--steps > 0 && color.a < 1.0)
	{
		float curFn = texture3D(volume, startPoint).x;
		
		vec4 voxelColor = texture1D(transferFunction, curFn);
		
		// Compute gradient
		vec3 grad = getGradient(startPoint);
		float s = max(dot(grad, dir.xyz), 0);
		
		//vec4 ppos = rotMat * vec4(startPoint, 1);
		//vec4 halfv = normalize(-ppos + dir);
		//float specularity = pow(dot(halfv.xyz,grad),10.0);
		
		//Phong shading 
		// voxelColor.rgb = s * voxelColor.rgb + 0.3f * voxelColor.rgb + specularity * voxelColor.rgb;
		voxelColor.rgb = s * voxelColor.rgb + 0.5f * voxelColor.rgb;
		
		//Front to back blending. Also old code
		voxelColor.rgb *= voxelColor.a;
		color += (1.0 - color.a) * voxelColor;
		
		startPoint += direction;
	}
	
	color += (1.0 - color.a) * back;
	return color;
}

vec3 getGradient(vec3 sp) {
	float x = sp.x;
	float y = sp.y;
	float z = sp.z;
	
	float xp = x - gradStep; 
	float xn = x + gradStep;
	if(xp < 0) {
		xp = 0;
	}
	if(xn > 1) {
		xn = 1;
	}
	
	float yp = y - gradStep; 
	float yn = y + gradStep;
	if(yp < 0) {
		yp = 0;
	}
	if(yn > 1) {
		yn = 1;
	}

	float zp = z - gradStep; 
	float zn = z + gradStep;
	if(zp < 0) {
		zp = 0;
	}
	if(zn > 1) {
		zn = 1;
	}
	
	vec3 grad;
	grad.x = texture3D(volume, vec3(xn,y,z)).x - texture3D(volume, vec3(xp,y,z)).x;
	grad.y = texture3D(volume, vec3(x,yn,z)).x - texture3D(volume, vec3(x,yp,z)).x;
	grad.z = texture3D(volume, vec3(x,y,zn)).x - texture3D(volume, vec3(x,y,zp)).x;
	
	grad = (rotMat * vec4(grad, 0)).xyz;
	grad = normalize(grad);
	return grad;	
}
