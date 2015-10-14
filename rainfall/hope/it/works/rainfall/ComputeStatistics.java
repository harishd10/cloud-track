package hope.it.works.rainfall;

import hope.it.works.ct.SplitTree;
import hope.it.works.ct.TriangleDataPrim;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import vgl.iisc.external.loader.impl.OffLoader;

public class ComputeStatistics {

	
	public static void main(String[] args) {
		OffLoader loader = new OffLoader();
		TriangleDataPrim data = new TriangleDataPrim(false, true);

		
		int x = 0;
		int noTimeSteps = 24;
		boolean biHour = true;
		
		// mumbai rainfall (india)
//		int noDays = 2;
//		int year = 2005;
//		int month = 7;
//		int dStart = 25;
//		String model = "india";
//		int nx = 687;
//		int ny = 660;

		// aila
//		int noDays = 2;
//		int year = 2009;
//		int month = 5;
//		int dStart = 24;
//		String model = "aila";
//		nx = 206;
//		ny = 302;

		// nakazawa
		int noDays = 1;
		int year = 2007;
		int month = 1;
		int dStart = 2;
		String model = "nakazawa";

		// sample
//		int noDays = 2;
//		int year = 2006;
//		int month = 6;
//		int dStart = 1;
//		String model = "sample";
		
		int nts = 0;
		for(int d = dStart;d < dStart + noDays;d ++) {
			for(int t = 0;t < noTimeSteps;t ++) {
//			for(int t = 0;t < noTimeSteps;t += 4) {
				nts ++;
				if(biHour) {
					nts ++;
				}
			}
		}
		
		String [] timeSteps = new String [nts];

		for(int d = dStart;d < dStart + noDays;d ++) {
			for(int t = 0;t < noTimeSteps;t ++) {
				timeSteps[x] = year + "-" + month + "-" + d + "-" + (t * 2);
				x ++;
				
				if(biHour) {
					timeSteps[x] = year + "-" + month + "-" + d + "-" + (t * 2 + 1);
					x ++;
				}
			}
		}
		
		
		int tStart = 0;
		int tEnd = timeSteps.length - 1;
		
		String [] t = new String[tEnd - tStart + 1];
		for(int i = 0;i < t.length;i ++) {
			t[i] = timeSteps[i + tStart];
		}
		timeSteps = t;

//		String folder = "F:/rainfall/python/";
		String folder = "/home/harishd/Desktop/rainfall/python/";
		loader.setInputFile(folder + "data/" + model + "/" + model + ".off");
		System.out.println("reading data");
		data.loadData(loader, "0");
		System.out.println("Finding time steps");
		ComputeStatistics comp = new ComputeStatistics();
		comp.findStats(data, folder, model, timeSteps);
	}

	public void findStats(TriangleDataPrim data, String folder, String model, String [] timeSteps) {
		try {
			PrintStream p = new PrintStream(model + ".stats");
			p.println(timeSteps.length);
			
			for(int i = 0;i < timeSteps.length;i ++) {
				init(data, folder + "data/" + model + "/" + model + "-" + timeSteps[i]+ ".boff");
				SplitTree st = new SplitTree();
				st.findSplitTree(data);
				
				int [] f = new int[st.statCt];
				int [] ct = new int[st.statCt];
				int cct = 0;
				for(int j = 0;j < st.statCt;j ++) {
					if(j == 0) {
						f[cct] = (int) st.fn[j];
						ct[cct] = st.compCt[j];
					} else {
						if(f[cct] == st.fn[j]) {
							ct[cct] = st.compCt[j];
						} else {
							if(f[cct] == 299) {
								i *= 1;
							}
							cct ++;
							f[cct] = (int) st.fn[j];
							ct[cct] = st.compCt[j];
						}
					}
				}
				cct ++;
				for(int j = 0;j < cct;j ++) {
					p.print(f[j] + " ");
				}
				p.println();
				for(int j = 0;j < cct;j ++) {
					p.print(ct[j] + " ");
				}
				p.println();
				
				for(int j = 0;j < st.pairCt;j ++) {
					p.print((int)data.fnVertices[st.pair1[j]] + " ");
				}
				p.println();
				for(int j = 0;j < st.pairCt;j ++) {
					p.print((int)data.fnVertices[st.pair2[j]] + " ");
				}
				p.println();
			}
			p.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	float [] fn;
	public void init(TriangleDataPrim data, String fValsFile) {
		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(fValsFile));
			byte [] vals = new byte[data.noVertices * 4];
			fn = new float[data.noVertices];
			buf.read(vals);
			ByteBuffer bbuf = ByteBuffer.wrap(vals);
			bbuf.order(ByteOrder.LITTLE_ENDIAN);
			
			for(int i = 0;i < data.noVertices;i ++) {
				data.fnVertices[i] = bbuf.getFloat();
				fn[i] = data.fnVertices[i];
			}
			System.out.println("Finished reading fn vals");
			updateTris(data);
			buf.close();
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	int[] v = new int[3];
	private void updateTris(TriangleDataPrim data) {
		for(int i = 0;i < data.triCt;i ++) {
			int tin = i * 3;
			int v1 = data.triangles[tin];
			int v2 = data.triangles[tin + 1];
			int v3 = data.triangles[tin + 2];
			
			
			if (data.less(v1, v2)) {
				if (data.less(v1, v3)) {
					if (data.less(v2, v3)) {
						v[0] = v1;
						v[1] = v2;
						v[2] = v3;
					} else {
						v[0] = v1;
						v[1] = v3;
						v[2] = v2;
					}
				} else {
					v[0] = v3;
					v[1] = v1;
					v[2] = v2;
				}
			} else {
				if (data.less(v2, v3)) {
					if (data.less(v1, v3)) {
						v[0] = v2;
						v[1] = v1;
						v[2] = v3;
					} else {
						v[0] = v2;
						v[1] = v3;
						v[2] = v1;
					}
				} else {
					v[0] = v3;
					v[1] = v2;
					v[2] = v1;
				}
			}
			data.triangles[tin] = v[0];
			data.triangles[tin + 1] = v[1];
			data.triangles[tin + 2] = v[2];
		}
	}


}
