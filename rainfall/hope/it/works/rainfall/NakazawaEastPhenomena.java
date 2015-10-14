package hope.it.works.rainfall;

import hope.it.works.ct.TriangleDataPrim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import vgl.iisc.external.loader.impl.OffLoader;

public class NakazawaEastPhenomena {
	static int[] monthDays = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
			31 };
	
	
	String[] timeSteps;
	TimeGraphOpt tg;
	CloudParticles [] westClouds;
	TriangleDataPrim data;
	
	private static String getDate(int y, int m, int d, int t) {
		if (d > monthDays[m]) {
			d -= monthDays[m];
			m++;
			if (m > 12) {
				m = 1;
				y++;
			}
		}

		String ret = y + "/";
		if (m < 10) {
			ret += "0";
		}
		ret += m + "/";
		if (d < 10) {
			ret += "0";
		}
		ret += d + " ";
		int hr = t / 2;
		int min = (t % 2);
		if (hr < 10) {
			ret += "0";
		}
		ret += hr + ":";
		if (min == 0) {
			ret += "00";
		} else {
			ret += "30";
		}
		return ret;
	}

	public void init() {
		OffLoader loader = new OffLoader();
		data = new TriangleDataPrim(false, true);

		int x = 0;
		int noTimeSteps = 24;
		boolean biHour = true;

		int noDays = 3;
		int year = 2007;
		int month = 1;
		int dStart = 2;
		String model = "nakazawa-long";
//		int nx = 962;
//		int ny = 137;
		int nx = 481;
		int ny = 68;

		int nts = 0;
		boolean trmm = false;
		if (trmm) {
			for (int d = dStart; d < dStart + noDays; d++) {
				for (int t = 0; t < noTimeSteps; t += 3) {
					nts++;
				}
			}
		} else {
			for (int d = dStart; d < dStart + noDays; d++) {
				for (int t = 0; t < noTimeSteps; t++) {
					nts++;
					if (biHour) {
						nts++;
					}
				}
			}
		}

		timeSteps = new String[nts];
		int [] loc = new int[nts];
		String[] date = new String[timeSteps.length];
		int dd = dStart;
		int mm = month;
		int yy = year;
		int tt = 0;
		if (trmm) {
			for (int d = dStart; d < dStart + noDays; d++) {
				for (int t = 0; t < noTimeSteps; t += 3) {
					timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2);
					date[x] = getDate(year, month, d, t * 2);
					x++;
				}
				dd++;
				if (dd > monthDays[mm]) {
					dd = 1;
					mm++;
					if (mm == 13) {
						mm = 1;
						yy++;
					}
				}
			}
		} else {

			for (int d = dStart; d < dStart + noDays; d++) {
				for (int t = 0; t < noTimeSteps; t++) {
					if (biHour) {
						timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2);
						date[x] = getDate(year, month, d, t * 2);
						loc[x] = tt;
						x++;
					}
					tt ++;
					timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2 + 1);
					date[x] = getDate(year, month, d, t * 2 + 1);
					loc[x] = tt;
					tt ++;
					x++;
					
				}
				dd++;
				if (dd > monthDays[mm]) {
					dd = 1;
					mm++;
					if (mm == 13) {
						mm = 1;
						yy++;
					}
				}
			}
		}

		int tStart = 0;
		int tEnd = timeSteps.length - 1;

		String[] t = new String[tEnd - tStart + 1];
		String[] d = new String[tEnd - tStart + 1];
		for (int i = 0; i < t.length; i++) {
			t[i] = timeSteps[i + tStart];
			d[i] = date[i + tStart];
		}
		date = d;
		timeSteps = t;

		String folder = "H:/rainfall/python/";
		// String folder = "/home/harishd/Desktop/rainfall/python/";
		loader.setInputFile(folder + "data/" + model + "/" + model + ".off");
		System.out.println("reading data");
		data.loadData(loader, "0");
		System.out.println("Finding time steps");
		tg = new TimeGraphOpt();
		tg.findClouds(data, folder, model, timeSteps, nx, ny);
	}
	
	
	public void findWestMovingClouds(int minLength) {
		System.out.println("No. of tiem steps: " + timeSteps.length);
		westClouds = new CloudParticles[timeSteps.length];
		for(int i = 0;i< timeSteps.length - 1;i ++) {
			westClouds[i] = new CloudParticles();
//			westClouds[i].vertices = tg.findWestMovingClouds(i);
			westClouds[i].vertices = tg.findWestMovingClouds(i, minLength);
		}
		try {
			ObjectOutputStream oop = new ObjectOutputStream(new FileOutputStream("west-clouds-2.bin"));
			oop.writeObject(westClouds);
			oop.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	float[ ]x;
	float[] y;
	int[] n;
	
	void read() {
		try {
			String model = "nakazawa-long";
			OffLoader loader = new OffLoader();
			data = new TriangleDataPrim(false, true);
			String folder = "H:/rainfall/python/";
			loader.setInputFile(folder + "data/" + model + "/" + model + ".off");
			System.out.println("reading data");
			data.loadData(loader, "0");

			ObjectInputStream oip = new ObjectInputStream(new FileInputStream("west-clouds.bin"));
			westClouds = (CloudParticles[]) oip.readObject();
			oip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getCenters() {
		x = new float[westClouds.length];
		y = new float[westClouds.length];
		n = new int[westClouds.length];
		System.out.println("Finding centers");
		for(int i = 0;i < n.length-1;i ++) {
			for(Iterator<Integer> it = westClouds[i].vertices.iterator();it.hasNext();) {
				int v = it.next();
				n[i] ++;
				x[i] += data.x[v];
				y[i] += data.y[v];
			}
			x[i] /= n[i];
			y[i] /= n[i];
		}
		System.out.println("writing centers");
		try {
			PrintStream pr = new PrintStream("centers-2.txt");
			pr.println(x.length);
			for(int i = 0;i < x.length;i ++) {
				pr.println(x[i] + " " + y[i] + " " + n[i]);
			}
			pr.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NakazawaEastPhenomena nep = new NakazawaEastPhenomena();
		long st = System.currentTimeMillis();
		nep.init();
		nep.findWestMovingClouds(2);
		
//		nep.read();
		nep.getCenters();
		st = System.currentTimeMillis() - st;
		System.out.println("Time taken: " + st);
	}

}
