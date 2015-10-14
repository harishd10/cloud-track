package hope.it.works.rainfall;

import hope.it.works.ct.Feature;
import hope.it.works.ct.SplitTree;
import hope.it.works.ct.TriangleDataPrim;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import vgl.iisc.external.loader.impl.OffLoader;
import vgl.iisc.utils.Utilities;

public class Cloud {

	public int time;
	public int [] extrema;
	public int [] criticalPoints;
	public int [] level;
	public int [] region;
	public float [] fn;
	public SplitTree st;
	
	public int [] regionVertices;
	public int [] index;
	
	public Cloud() {
	}
	
	public Cloud(String offFile, TriangleDataPrim data, String fValsFile, float [] th) {
		if(fValsFile == null) {
			initST(offFile, th);
		} else {
			initST(data, fValsFile, th);
		}
	}
	
	
	private void initST(String offFile, float [] th) {
		TriangleDataPrim data = new TriangleDataPrim(false, true);
		OffLoader loader = new OffLoader();
		loader.setInputFile(offFile);
		data.loadData(loader, "0");
		
		computeST(data, th);
	}
	
	private void initST(TriangleDataPrim data, String fValsFile, float [] th) {
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
		computeST(data, th);
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

	private void computeST(TriangleDataPrim data, final float [] th) {
		Arrays.sort(th);
		
		st = new SplitTree();
		st.findSplitTree(data);

		
		st.findFeatures(th);
		makeSimplyConnected(data);
		
		int noFeatures = st.features.size(); 
		extrema = new int[noFeatures];
		criticalPoints = new int[noFeatures];
		level = new int[noFeatures];
		region = new int[data.noVertices];
		
		regionVertices = new int[data.noVertices];
		index = new int[noFeatures + 1];
		int curIn = 0;
		
		Arrays.fill(region, -1);
		Arrays.fill(regionVertices, -1);
		for(int i = 0;i < noFeatures;i ++) {
			Feature min = st.features.get(i);
			extrema[i] = i;
			criticalPoints[i] = min.v;
			level[i] = min.level;
			
			index[i] = curIn;
			
			for(Iterator<Integer> it =  min.verts.iterator();it.hasNext();) {
				int vv = it.next();
				region[vv] = i;
				regionVertices[curIn ++] = vv;
			}
			// TODO remove when it is required
			min.verts.clear();
			min.verts = null;
		}
		index[noFeatures] = curIn;
	}

	public static String tmpFolder;
	public void write() {
		String fileName = tmpFolder + time + ".clouds";
		
		try {
			ObjectOutputStream op = new ObjectOutputStream(new FileOutputStream(fileName));
			
			op.writeInt(time);
			op.writeObject(extrema);
			op.writeObject(criticalPoints);
			op.writeObject(level);
			op.writeObject(region);
			op.writeObject(fn);
			
			op.writeObject(regionVertices);
			op.writeObject(index);
			
			op.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void read(int t) {
		String fileName = tmpFolder + t + ".clouds";
		
		try {
			ObjectInputStream ip = new ObjectInputStream(new FileInputStream(fileName));
			
			time = ip.readInt();
			
			if(time != t) {
				Utilities.er("Time not consistent!!");
			}
			extrema = (int[]) ip.readObject();
			criticalPoints = (int[]) ip.readObject();
			level = (int[]) ip.readObject();
			region = (int[]) ip.readObject();
			fn = (float[]) ip.readObject();
			
			regionVertices = (int[]) ip.readObject();
			index = (int[]) ip.readObject();
			ip.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void makeSimplyConnected(TriangleDataPrim data) {
		System.out.println("Filling holes: ");
		int noFeatures = st.features.size();
		
		for(int i = 0;i < noFeatures;i ++) {
			Feature f = st.features.get(i);
			HashSet<Integer> newVerts = new HashSet<Integer>();
			for(Iterator<Integer> it = f.verts.iterator(); it.hasNext();) {
				int v = it.next();
				if(isBorder(v, data, f.verts, newVerts)) {
					newVerts.addAll(fill(v, data, f.verts));
				}
			}
			f.verts.addAll(newVerts);
		}
	}
	
	int th = 50;
	private HashSet<Integer> fill(int v, TriangleDataPrim data, HashSet<Integer> verts) {
		HashSet<Integer> set = new HashSet<Integer>();
		ArrayList<Integer> q = new ArrayList<Integer>();
		q.add(v);
		HashSet<Integer> qq = new HashSet<Integer>();
		qq.add(v);
		while(q.size() > 0) {
			if(set.size() > th) {
				set.clear();
				return set;
			}
			int vv = q.remove(0);
			qq.remove(vv);
			set.add(vv);
			for(int j = 0;j < data.vertices[vv].star.length;j ++) {
				int t = data.vertices[vv].star.get(j);
				int tin = t * 3;
				int tv1 = data.triangles[tin];
				int tv2 = data.triangles[tin + 1];
				int tv3 = data.triangles[tin + 2];
				if(tv1 != vv && !verts.contains(tv1) && !qq.contains(tv1) && !set.contains(tv1)) {
					q.add(tv1);
					qq.add(tv1);
				}
				if(tv2 != vv && !verts.contains(tv2) && !qq.contains(tv2) && !set.contains(tv2)) {
					q.add(tv2);
					qq.add(tv2);
				}
				if(tv3 != vv && !verts.contains(tv3) && !qq.contains(tv3) && !set.contains(tv3)) {
					q.add(tv3);
					qq.add(tv3);
				}
			}
		}
		return set;
	}

	private boolean isBorder(int v, TriangleDataPrim data, HashSet<Integer> verts, HashSet<Integer> newVerts) {
		for(int j = 0;j < data.vertices[v].star.length;j ++) {
			int t = data.vertices[v].star.get(j);
			int tin = t * 3;
			int tv1 = data.triangles[tin];
			int tv2 = data.triangles[tin + 1];
			int tv3 = data.triangles[tin + 2];
			int v1, v2;
			if(tv1 == v) {
				v1 = tv2;
				v2 = tv3;
			} else if(tv2 == v) {
				v1 = tv1;
				v2 = tv3;
			} else {
				v1 = tv1;
				v2 = tv2;
			}
			if(!verts.contains(v1) && !newVerts.contains(v1)) {
				return true;
			}
			if(!verts.contains(v2) && !newVerts.contains(v2)) {
				return true;
			}
		}
		return false;
	}

}
