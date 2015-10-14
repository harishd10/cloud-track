package hope.it.works.data;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.Index3D;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import vgl.iisc.utils.Utilities;

public class TrmmNCDataLittleEndian {

	Variable lat, lon;
	NetcdfFile file;
	List<Variable> vars;
	boolean latFirst = false;

//	public float [] x;
//	public float [] y;
	public float [][] vals = new float[720][1440];
	public int [][] ct = new int[720][1440];
	
	public void initNCData(String ncFile) throws IOException, InvalidRangeException {
		file = NetcdfFile.open(ncFile);
		vars = file.getVariables();
//		int in = 0;
		for (Iterator<Variable> it = vars.iterator(); it.hasNext();) {
			Variable v = it.next();
//			System.out.println(in + " " + v.getFullName() + " " + v.getDataType());
			if(v.getFullName().toLowerCase().contains("latitude")) {
				lat = v;
			}
			if(v.getFullName().toLowerCase().contains("longitude")) {
				lon = v;
			}
//			in++;
		}
		vals = new float[720][1440];
		ct = new int[720][1440];
	}

	int dimx, dimy;
	float [][] data ;
	float maxData = 100;
	
	public void readData(String fileName, int channel, float blon, float elon, float blat, float elat) throws Exception {
		initNCData(fileName);
		Array lonarr = file.readSection(lon.getFullName());
		Array latarr = file.readSection(lat.getFullName());
		Array channels = file.readSection(vars.get(channel).getFullName());
		
		int [] dim = channels.getShape();
		// 1, lat, lon
		Index3D index = new Index3D(new int[] {dim[0],dim[1],dim[2]});
		float max = -Float.MAX_VALUE;
		float min = Float.MAX_VALUE;
		
		for(int i = 0;i < dim[1];i ++) {
			for(int j = 0;j < dim[2];j ++) {
				float x = lonarr.getFloat(j);
				float y = latarr.getFloat(i);
				Index3D i3d = getIndex(index,0,i,j);
				float val = channels.getFloat(i3d);
				
				x *= 4;
				y *= 4;
				int lt = Math.round(y);
				int ln = Math.round(x);
				
				// translate to positive coords. -180 lon -> 0  and -90 lat -> 0 
				
				ln += 720;
				lt += 360;
				
				if(ln == 1440) {
					ln = 0;
				}
				vals[lt][ln] += val;
				this.ct[lt][ln] ++;
			}
		}
		for(int i = 0;i < vals.length;i ++) {
			for(int j = 0;j < vals[i].length;j ++) {
				if(this.ct[i][j] > 0) {
					if(this.ct[i][j] > 1) {
						Utilities.er("Time to change equals to addition when assigning the value!!!!!");
					}
					vals[i][j] /= this.ct[i][j];
//				} else {
//					float lat = i - 360;
//					lat /= 4;
//					float lon = j - 720;
//					lon /= 4;
				}
				if(vals[i][j] < 0) {
					vals[i][j] = 0;
				}
			}
		}
		
		int xst = 0;
		int xen = 0;
		int yst = 0;
		int yen = 0;
		
		for(int i = 0;i < vals.length;i ++) {
			float lat = i - 360;
			lat /= 4;
			if(lat == blat) {
				yst = i;
			}
			if(lat == elat) {
				yen = i;
			}
		}
		for(int j = 0;j < vals[0].length;j ++) {
			float lon = j - 720;
			lon /= 4;
			if(lon == blon) {
				xst = j;
			}
			if(lon == elon) {
				xen = j;
			}
		}
		max = -Float.MAX_VALUE;
		min = Float.MAX_VALUE;

		dimx = xen - xst + 1;
		dimy = yen - yst + 1;
		data = new float[dimy][dimx];
		for(int i = 0;i < dimy;i ++) {
			for(int j = 0;j < dimx;j ++) {
				// swap values
				data[i][j] = maxData - vals[i + yst][j + xst];
				max = Math.max(max, data[i][j]);
				min = Math.min(min, data[i][j]);
			}
		}
		System.out.println("Min: " + min);
		System.out.println("Max: " + max);

		System.out.println("Dimension: " + dimx + " " + dimy);
	}
	
	private Index3D getIndex(Index3D index, int i, int j, int k) {
		return (Index3D) index.set(i, j, k);
	}

	public void close() throws IOException {
		file.close();
	}
	
	void writeOff(String off) {
		try {
			PrintStream pr = new PrintStream(off);
			int nv = dimx * dimy;
			int nt = (dimx - 1) * (dimy - 1) * 2;
			pr.println(nv + " " + nt);
			for(int i = 0;i < dimy;i ++) {
				for(int j = 0;j < dimx;j ++) {
					pr.println(j + " " + i + " 1 " + data[i][j]);
				}
			}
			for(int i = 0;i < dimy - 1;i ++) {
				for(int j = 0;j < dimx - 1;j ++) {
					int v1 = i * dimx + j;
					int v2 = v1 + 1;
					int v3 = v1 + dimx;
					int v4 = v3 + 1;
					
					pr.println("3 " + v1 + " " + v2 + " " + v3);
					pr.println("3 " + v2 + " " + v4 + " " + v3);
				}
			}
			pr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void writeBin(int t, String offFile) {
		System.out.println(offFile);
		try {
			BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(offFile));
			int nv = dimx * dimy;
			byte [] irtmp = new byte[nv * 4];
			ByteBuffer bbuf = ByteBuffer.wrap(irtmp);
			bbuf.order(ByteOrder.LITTLE_ENDIAN);
			FloatBuffer fbuf = bbuf.asFloatBuffer();;
			for(int i = 0;i < dimy;i ++) {
				for(int j = 0;j < dimx;j ++) {
					fbuf.put(data[i][j]);
				}
			}
			buf.write(irtmp);
			buf.close();;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	public static void main(String[] args) {
//		TrmmNCDataLittleEndian nd = new TrmmNCDataLittleEndian();
//		try {
//			nd.initNCData("F:/Data/rainfall/trmm/3B42.20070102.00.7A.nc");
//			nd.readData(7,0);
////			nd.writeOff("ir/models/20060911-13-00.off", -5f,5f,10f,20f);
//			nd.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(0);
//		}
//	}

	public static void main(String[] args) {
		TrmmNCDataLittleEndian nd = new TrmmNCDataLittleEndian();

		// for india
//		int year = 2005;
//		int month = 7;
//		String modelName = "india-trmm";
//		int stDate = 25;
//		int endDate = 26;
		
		// for Aila
//		int year = 2009;
//		int month = 5;
//		String modelName = "aila-trmm";
//		int stDate = 24;
//		int endDate = 25;
		
		// nakazawa
//		int year = 2007;
//		int month = 1;
//		String modelName = "nakazawa-trmm";
//		int stDate = 2;
//		int endDate = 4;
		
		// for niamey
		int year = 2006;
		int month = 6;
		String modelName = "sample-trmm";
		int stDate = 1;
		int endDate = 3;
		
		boolean writeOff = true;
		try {
			for(int d = stDate;d <= endDate;d ++) {
				int day = d;
				for(int t = 0; t <= 21;t += 3) {
					int time = t;
					String model = "3B42." + year;
					if(month < 10) {
						model += "0";
					}
					model += month;
					if(day < 10) {
						model += "0";
					}
					model += day + ".";
					if(time < 10) {
						model += "0";
					}
					model += time + ".7A.nc";
					System.out.println("Model: " + model);
					String file = "F:/Data/rainfall/" + "trmm" + "/" + model;
					
					// niamey
					nd.readData(file, 7, -5, 5, 10, 20);
					
					// indian subcontinent
//					ir.readData(file, 40, 110, -30, 30, 4);
					
					// india
//					nd.readData(file, 7, 60, 85, 6, 30);
					
					// mumbai
//					ir.readData(file, 65, 80, 15, 25, 1);
					
					// aila
//					nd.readData(file, 7, 80, 95, 8, 30);
					
					// nakazawa
//					nd.readData(file, 7, 100, 140, -5, 5);
					
					String folder = "F:/rainfall/python/data/" + modelName + "/";
					time *= 2;
					String off = modelName + "-" + year + "-" + month + "-" + day;
					if(writeOff) {
						System.out.println("Writing off file");
						nd.writeOff(folder + modelName + ".off");
						writeOff = false;
//						System.exit(0);
					}
					
					String off1 = folder + off + "-" + time + ".boff";
					nd.writeBin(0, off1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
