import numpy
import fileutils
import cv2

def writeHeader(f, nx, ny, nz):
	f.write('# vtk DataFile Version 2.0\n')
	f.write('Sample rectilinear grid\n')
	f.write('ASCII\n')
	f.write('DATASET STRUCTURED_POINTS\n')
	f.write('DIMENSIONS ' + str(nx) + ' ' + str(ny) + ' ' + str(nz) + '\n')
	f.write('ASPECT_RATIO 1 1 1\n')
	f.write('ORIGIN 0 0 0\n')
	f.write('POINT_DATA ' + str(nx*ny*nz) + ' \n')
	f.write('SCALARS irtemp float\n')
	f.write('LOOKUP_TABLE default \n')


def writeScalars(f, t, nx, ny):
	for i in range(0,ny):
		for j in range(0,nx):
			f.write(str(t[i][j]) + '\n')

def writeVectors(f, flow, nx, ny):
	for i in range(0,ny):
		for j in range(0,nx):
			f.write(str(flow[i][j][0]) + ' ' + str(flow[i][j][1]) + ' 1\n')



model = 'nakazawa-trmm'
year = 2007
month = 1
stDate = 2
enDate = 4
dimx = 161 
dimy = 41

			
model = 'india-trmm'
year = 2005
month = 7
stDate = 25
enDate = 26
dimx = 101 
dimy = 97

#model = 'aila-trmm'
#year = 2009
#month = 5
#stDate = 24
#enDate = 25
#dimx = 61 
#dimy = 89

model = 'niamey-trmm'
year = 2006
month = 9
stDate = 11
enDate = 11
dimx = 41 
dimy = 41

file = []
opFile = []
revFile = []
#ct = 0;
for d in range(stDate,enDate+1):
	for t in range(0,48,6):
		#if t % 8 == 0 or t % 8 == 1:
			fileName = '../data/' + model + '/' + model + '-' + str(year) + '-' + str(month) + '-' + str(d) + '-' + str(t) + '.boff'
			file.append(fileName)
			fileName = '../vector/' + model + '/' + model  + '-' + str(year) + '-' + str(month) + '-' + str(d) + '-' + str(t) + '.field'
			opFile.append(fileName)
			fileName = '../vector/' + model + '/' + model  + '-' + str(year) + '-' + str(month) + '-' + str(d) + '-' + str(t) + '.rfield'
			revFile.append(fileName)
			#ct += 1
		
no = len(file)
t1 = fileutils.readFileTrmm(file[0],dimx,dimy)
t = t1

nx = len(t1[0])
ny = len(t1)
#op = '../vector/' + model + '.vtk';
#f = open(op, 'w')
#writeHeader(f, nx, ny, no - 1)

#for i in range(1, no):
#	writeScalars(f,t1, nx, ny);
#	t2 = fileutils.readFile(file[i],687,660)
#	t1 = t2;
#	print 'wrote scalars ' + str(i)
	
t1 = t;	

#f.write('VECTORS vectors float\n')

for i in range(1, no):
	t2 = fileutils.readFileTrmm(file[i],dimx,dimy)
	flow = cv2.calcOpticalFlowFarneback(t1, t2, 0.5, 3, 15, 3, 5, 1.2, 0)
	#flow = cv2.calcOpticalFlowFarneback(t1, t2, 0.5, 3, 15, 3, 7, 1.5, 0)
	
	# Old opencv
	#flow = cv2.calcOpticalFlowFarneback(t1, t2, None, 0.5, 3, 15, 3, 5, 1.2, 0)
	#writeVectors(f,flow, nx, ny)
	fileutils.writeBinaryFile(opFile[i-1], flow, nx, ny)
	
	flow = cv2.calcOpticalFlowFarneback(t2, t1, 0.5, 3, 15, 3, 5, 1.2, 0)
	fileutils.writeBinaryFile(revFile[i], flow, nx, ny)
	
	t1 = t2
	# print 'wrote vectors ' + str(i)
	#cv2.imshow('flow', fileutils.draw_flow(t1, flow))
	#print 'done' + str(i)
	#cv2.waitKey(300)
	
	
	
	

