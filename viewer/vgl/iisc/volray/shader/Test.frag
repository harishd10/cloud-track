uniform sampler2D rayStart;
uniform sampler2D rayEnd;
uniform sampler3D volume;
uniform sampler1D transferFunction;
uniform vec2 screenDimensions;
uniform vec2 scalarRange;
uniform float stepSize;
uniform vec4 back;

void main()
{
	vec4 color = vec4(0, 0, 0, 0);
	
	vec3 startPoint = texture2D(rayStart, gl_FragCoord.xy/screenDimensions.xy).xyz;
	vec3 endPoint = texture2D(rayEnd, gl_FragCoord.xy/screenDimensions.xy).xyz;
	
	vec3 direction = endPoint - startPoint;
	int steps = int ( floor(length(direction) / stepSize));
	
	direction = normalize(direction) * stepSize;
	float range = scalarRange.y - scalarRange.x;
	
	int ct = 10;
	while(--steps > 0 && color.a < 1.0)
	{
		vec4 voxelColor = texture1D(transferFunction, ((texture3D(volume, startPoint).x - scalarRange.x)/ range));
		voxelColor.rgb *= voxelColor.a;
		color += (1.0 - color.a) * voxelColor;
		startPoint += direction;
	}
	
	color += (1.0 - color.a) * back; 
	gl_FragColor = color;
}

