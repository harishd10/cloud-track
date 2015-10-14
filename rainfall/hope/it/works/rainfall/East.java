package hope.it.works.rainfall;

import hope.it.works.ct.TriangleDataPrim;
import vgl.iisc.external.loader.impl.OffLoader;

public class East {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OffLoader loader = new OffLoader();
		TriangleDataPrim data = new TriangleDataPrim(false, true);

		
		int x = 0;
		int noTimeSteps = 24;
		boolean biHour = true;
		
		int noDays = 3;
		int year = 2007;
		int month = 1;
		int dStart = 2;
		String model = "nakazawa";
		int nx = 549;
		int ny = 137;
//		int nx = 161;
//		int ny = 41;

		
		int nts = 0;
		boolean trmm = false;
		if(trmm) {
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t += 3) {
					nts ++;
				}
			}
		} else {
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t ++) {
					nts ++;
					if(biHour) {
						nts ++;
					}
				}
			}
		}
		
		String [] timeSteps = new String [nts];
		String [] date = new String[timeSteps.length];
		int dd = dStart;
		int mm = month;
		int yy = year;
		if(trmm) {
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t += 3) {
					timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2);	
					date[x] = getDate(year,month,d,t*2);
					x ++;
				}
				dd ++;
				if(dd > monthDays[mm]) {
					dd = 1;
					mm ++;
					if(mm == 13) {
						mm = 1;
						yy ++;
					}
				}
			}
		} else {
	
			for(int d = dStart;d < dStart + noDays;d ++) {
				for(int t = 0;t < noTimeSteps;t ++) {
	//			for(int t = 0;t < noTimeSteps;t += 4) {
					if(biHour) {
						timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2);
						date[x] = getDate(year,month,d,t*2);
						x ++;
					}
					
	//				if(biHour) {
						timeSteps[x] = yy + "-" + mm + "-" + dd + "-" + (t * 2 + 1);	
						date[x] = getDate(year,month,d,t*2+1);
						x ++;
	//				}
				}
				dd ++;
				if(dd > monthDays[mm]) {
					dd = 1;
					mm ++;
					if(mm == 13) {
						mm = 1;
						yy ++;
					}
				}
			}
		}
		
		int tStart = 0;
		int tEnd = timeSteps.length - 1;
		
		String [] t = new String[tEnd - tStart + 1];
		String [] d = new String[tEnd - tStart + 1];
		for(int i = 0;i < t.length;i ++) {
			t[i] = timeSteps[i + tStart];
			d[i] = date[i + tStart];
		}
		date = d;
		timeSteps = t;

		String folder = "F:/rainfall/python/";
//		String folder = "/home/harishd/Desktop/rainfall/python/";
		loader.setInputFile(folder + "data/" + model + "/" + model + ".off");
		System.out.println("reading data");
		data.loadData(loader, "0");
		System.out.println("Finding time steps");
		TimeGraph tg = new TimeGraph();
		tg.findClouds(data, folder, model, timeSteps, nx, ny);
//		tg.createGraph();
		tg.findEastMovingClouds();
	}

	static int [] monthDays = {0,31,28,31,30,31,30,31,31,30,31,30,31};
	private static String getDate(int y, int m, int d, int t) {
		if(d > monthDays[m]) {
			d -= monthDays[m];
			m ++;
			if(m > 12) {
				m = 1;
				y ++;
			}
		}
		
		String ret = y + "/";
		if(m < 10) {
			ret += "0";
		}
		ret += m + "/"; 
		if(d < 10) {
			ret += "0";
		}
		ret += d + " ";
		int hr = t / 2;
		int min = (t % 2);
		if(hr < 10) {
			ret += "0";
		}
		ret += hr + ":";
		if(min == 0) {
			ret += "00";
		} else {
			ret += "30";
		}
		return ret;
	}

}
