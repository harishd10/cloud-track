package hope.it.works.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import vgl.iisc.utils.Utilities;

public class IRDataLittleEndian {

	public static final int toAdd = 75;
	public static final int undef = 330;
	
	public static final int dimx = 9896;
	public static final int dimy = 3298;
	public static final float stx = 0.0182f;
	public static final float sty = -59.982f;
	public static final float incx = 0.036378335f;
	public static final float incy = 0.036383683f;
	
	public short [][][] vals;
	
	public IRDataLittleEndian() {
		
	}
	
	public void readData(String file, int blon, int elon, int blat, int elat, int steps) {
		int dy = 0;
		int dx = 0;
		
		float lat = sty + dimy * incy;
		for(int y = 0;y < dimy;y ++) {
			if(lat >= blat && lat <= elat) {
				dy ++;
			}
			
			lat -= incy;
		}
		float lon = stx + (dimx / 2 ) * incx;
		lon -= 360;
		
		for(int x = 0;x < dimx;x ++) {
			if(lon >= blon && lon <= elon) {
				dx ++;
			}
			lon += incx;
		}
		System.out.println("Dimension: " + dx + " " + dy);
		vals = new short[2][dy][dx];
		
		try {
			BufferedInputStream ip = new BufferedInputStream(new FileInputStream(file));
			readTime(0, ip, dx, dy, blon, elon, blat, elat);
			readTime(1, ip, dx, dy, blon, elon, blat, elat);
			ip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		dx /= steps;
		dy /= steps;
		System.out.println("Reduced dim: " + dx + " " + dy);
				
		short[][][] vals1 = new short[2][dy][dx];
		
		subsample(0,vals1, steps);
		subsample(1,vals1, steps);
		
		vals = vals1;
//		for(int y = 0;y < dy;y ++) {
//			for(int x = 0;x < dx;x ++) {
//				if(vals[0][y][x] != vals[1][y][x]) {
//					System.out.println("Not equal");
//					System.exit(0);
//				}
//			}
//		}
	}
	
	private void subsample(int t, short[][][] vals1, int steps) {
		int dy = vals1[t].length;
		int dx = vals1[t][0].length;
		
		int ypos = 0;
		for(int y = 0;y < dy;y ++) {
			int xpos = 0;
			for(int x = 0;x < dx;x ++) {
				vals1[t][y][x] = vals[t][ypos][xpos];
				xpos += steps;
			}
			ypos += steps;
		}
	}

	public void readTime(int t, BufferedInputStream ip, int dx, int dy, int blon, int elon, int blat, int elat) throws IOException {
		byte [] data = new byte[dimx];
		
		float lat = sty + dimy * incy;
		int ypos = dy - 1;
		int xpos = 0;
		float max = -Float.MAX_VALUE;
		float min = Float.MAX_VALUE;
		
		for(int y = 0;y < dimy;y ++) {
			ip.read(data);
			if(lat >= blat && lat <= elat) {
				xpos = 0;
				int xx = dimx / 2;
				float lon = stx + xx * incx;
				lon -= 360;
				for(int x = 0;x < dimx;x ++) {
					short irTmp = (short) (0xff & data[xx]);
					if(lon >= blon && lon <= elon) {
						vals[t][ypos][xpos] = irTmp;
						if(vals[t][ypos][xpos] < 0) {
							Utilities.er("!!!!");
						}
						max = Math.max(irTmp, max);
						min = Math.min(irTmp, min);
						vals[t][ypos][xpos] += 75;
						xpos ++;
					}
					lon += incx;
					xx ++;
					if(xx == dimx) {
						xx = 0;
					}
					if(xpos == dx) {
						break;
					}
				}
				ypos --;
			}
			lat -= incy;
		}
		System.out.println((max + 75) + " " + (min + 75));
	}
	
	public void writeOff(int t, String offFile) {
		System.out.println(offFile);
		
		int dimx = vals[t][0].length;
		int dimy = vals[t].length;
		
		try {
			PrintStream pr = new PrintStream(offFile);
			int nv = dimx * dimy;
			int nt = (dimx - 1) * (dimy - 1) * 2;
			pr.println(nv + " " + nt);
			for(int i = 0;i < dimy;i ++) {
				for(int j = 0;j < dimx;j ++) {
					pr.println(j + " " + i + " 1 " + vals[t][i][j]);
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
		
		int dimx = vals[t][0].length;
		int dimy = vals[t].length;
		
		try {
			BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(offFile));
			int nv = dimx * dimy;
			byte [] irtmp = new byte[nv * 4];
			ByteBuffer bbuf = ByteBuffer.wrap(irtmp);
			bbuf.order(ByteOrder.LITTLE_ENDIAN);
			FloatBuffer fbuf = bbuf.asFloatBuffer();;
			for(int i = 0;i < dimy;i ++) {
				for(int j = 0;j < dimx;j ++) {
					fbuf.put(vals[t][i][j]);
				}
			}
			buf.write(irtmp);
			buf.close();;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		
		// for india
//		int year = 2005;
//		int month = 7;
//		String modelName = "india";
//		int stDate = 24;
//		int endDate = 28;
		
		// for Aila
//		int year = 2009;
//		int month = 5;
//		String modelName = "aila";
//		int stDate = 24;
//		int endDate = 26;
		
		// nakazawa
//		int year = 2007;
//		int month = 1;
//		String modelName = "nakazawa";
//		int stDate = 2;
//		int endDate = 7;
		
		// sample
		int year = 2006;
		int month = 6;
		String modelName = "sample";
		int stDate = 1;
		int endDate = 2;
		
		// nakazawa-long
//		int year = 2007;
//		int month = 1;
//		String modelName = "nakazawa-long";
//		int stDate = 2;
//		int endDate = 13;
		
		IRDataLittleEndian ir = new IRDataLittleEndian();
		boolean writeOff = true;
		// should be either india-sc, india, or mumbai  
		for(int d = stDate;d <= endDate;d ++) {
			int day = d;
			for(int t = 0; t <= 23;t += 1) {
				int time = t;
				String model = "merg_" + year;
				if(month < 10) {
					model += "0";
				}
				model += month;
				if(day < 10) {
					model += "0";
				}
				model += day;
				if(time < 10) {
					model += "0";
				}
				model += time + "_4km-pixel";
				System.out.println("Model: " + model);
				String file = "F:/Data/rainfall/" + "sample" + "/" + model;
//				String file = "H:/Data/rainfall/ir/nakazawa/" + model;
				// niamey
//				ir.readData(file, -5, 5, 10, 20, 1);
				
				// indian subcontinent
//				ir.readData(file, 40, 110, -30, 30, 4);
				
				// india
//				ir.readData(file, 60, 85, 6, 30, 1);
				
				// mumbai
//				ir.readData(file, 65, 80, 15, 25, 1);
				
				// aila
				ir.readData(file, 80, 95, 8, 30, 2);
				
				// nakazawa
//				ir.readData(file, 100, 140, -5, 5, 2);
				
				// nakazawa-long
//				ir.readData(file, 90, 160, -5, 5, 2);
				
				// nakazawa-short
//				ir.readData(file, 90, 160, -5, 5, 4);
				
//				String folder = "F:/rainfall/python/data/nakazawa/";
				String folder = "H:/rainfall/python/data/nakazawa-short/";
				time *= 2;
				String off = modelName + "-" + year + "-" + month + "-" + day;
				if(writeOff) {
					System.out.println("Writing off file");
					ir.writeOff(0, folder + modelName + ".off");
					writeOff = false;
//					System.exit(0);
				}
				
				String off1 = folder + off + "-" + time + ".boff";
				time ++;
				String off2 = folder + off + "-" + time + ".boff";
				ir.writeBin(0, off1);
				ir.writeBin(1, off2);
			}
		}
	}

}
