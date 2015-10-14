import numpy as np
import cv2

def readFile(file, dx, dy):
	rdata = np.fromfile(file, dtype='float32')
	#rdata[rdata > 280] = 330
	
	# For global tracking
	#rdata[rdata > 220] = 330
	#rdata[rdata <= 221] = 100
	
	rdata -= 75
	rdata.shape = (dy,dx)
	data = rdata.astype(np.uint8)
	
	return data

def readFileTrmm(file, dx, dy):
	rdata = np.fromfile(file, dtype='float32')
	#rdata[rdata > 280] = 330
	
	# For global tracking
	#rdata[rdata > 220] = 330
	#rdata[rdata <= 221] = 100
	
	rdata = rdata / 100 * 255;
	rdata.shape = (dy,dx)
	data = rdata.astype(np.uint8)
	
	return data
	
def writeBinaryFile(file, flow, nx, ny):
	size = nx * ny * 2;
	shape = (size)
	rowFlow = flow.reshape(shape).astype(np.float32)
	rowFlow.tofile(file)
	print 'wrote file:', file
	
def draw_flow(img, flow, step=16):
    h, w = img.shape[:2]
    y, x = np.mgrid[step/2:h:step, step/2:w:step].reshape(2,-1)
    fx, fy = flow[y,x].T
    lines = np.vstack([x, y, x+fx, y+fy]).T.reshape(-1, 2, 2)
    lines = np.int32(lines + 0.5)
    vis = cv2.cvtColor(img, cv2.COLOR_GRAY2BGR)
    cv2.polylines(vis, lines, 0, (0, 255, 0))
    for (x1, y1), (x2, y2) in lines:
        cv2.circle(vis, (x1, y1), 1, (0, 255, 0), -1)
    return vis
    
    
def writeVectorField(file, flow, t):
	f = open(file, 'w')
	f.write('# vtk DataFile Version 2.0\n')
	f.write('Sample rectilinear grid\n')
	f.write('ASCII\n')
	f.write('DATASET STRUCTURED_POINTS\n')
	nx = len(flow[0])
	ny = len(flow)
	f.write('DIMENSIONS ' + str(nx) + ' ' + str(ny) + ' 1\n')
	f.write('ASPECT_RATIO 1 1 1\n')
	f.write('ORIGIN 0 0 0\n')
	f.write('POINT_DATA ' + str(nx*ny) + ' \n')
	f.write('SCALARS irtemp float\n')
	f.write('LOOKUP_TABLE default \n')
	for i in range(0,ny):
		for j in range(0,nx):
			f.write(str(t[i][j])+ '\n')

	f.write('VECTORS vectors float\n')
	for i in range(0,ny):
		for j in range(0,nx):
			f.write(str(flow[i][j][0]) + ' ' + str(flow[i][j][1]) + ' 0\n')
	print 'Wrote flow: ' + file

def writeSingleVTK(file, flow, t):
	f = open(file, 'w')
	nx = len(flow[0])
	ny = len(flow)
	f.write(str(nx) + ' ' + str(ny) + ' \n')
	for i in range(0,ny):
		for j in range(0,nx):
			f.write(str(t[i][j]) + ' ' + str(flow[i][j][0]) + ' ' + str(flow[i][j][1]) + ' 1\n')
	print 'Wrote flow: ' + file

