package hope.it.works.ct;

import hope.it.works.DisjointSetsInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

public class JoinTree {
	
	public ArrayList<Feature> maxima = new ArrayList<Feature>();
	public ArrayList<Boolean> valid = new ArrayList<Boolean>();
	public TriangleDataPrim data;
	int [] vMap;
	public void findJoinTree(TriangleDataPrim data, float th) {
		this.data = data;
		sortVerts();
		DisjointSetsInt dj = new DisjointSetsInt(data.noVertices);
		
		HashSet<Integer> comps = new HashSet<Integer>();
//		HashSet<Integer> upperStar = new HashSet<Integer>();
		vMap = new int[data.noVertices];
		
		for(int i = 0;i < data.noVertices;i ++) {
			int v = verts.get(i);
			if(data.fnVertices[v] <= th) {
				break;
			}
			comps.clear();
//			upperStar.clear();
			for(int j = 0;j < data.vertices[v].star.length;j ++) {
				int t = data.vertices[v].star.get(j);
				int tin = t * 3;
				int v1 = data.triangles[tin];
				int v2 = data.triangles[tin + 1];
				int v3 = data.triangles[tin + 2];
				
				if(v1 == v) {
//					upperStar.add(v2);
//					upperStar.add(v3);
					comps.add(dj.find(v2));
					comps.add(dj.find(v3));
				} else if(v2 == v) {
//					upperStar.add(v3);
					comps.add(dj.find(v3));
				}
			}
			if(comps.isEmpty()) {
				Feature max = new Feature();
				max.v = v;
				max.verts.add(v);
				vMap[dj.find(v)] = maxima.size();
				maxima.add(max);
				valid.add(true);
			} else {
				Feature keep = null;
				int in = -1;
				for(Iterator<Integer> it = comps.iterator();it.hasNext();) {
					int c = it.next();
					Feature max = maxima.get(vMap[c]);
					if(keep == null || data.less(keep.v, max.v)) {
						keep = max;
						in = vMap[c];
					}
				}
				
				for(Iterator<Integer> it = comps.iterator();it.hasNext();) {
					int c = it.next();
					dj.union(dj.find(v), dj.find(c));
					if(vMap[c] == in) {
						continue;
					}
					Feature max = maxima.get(vMap[c]);
					valid.set(vMap[c], false);
					keep.verts.addAll(max.verts);
				}
				keep.verts.add(v);
				int c = dj.find(v);
				vMap[c] = in;
			}
		}
	}

	ArrayList<Integer> verts;
	private void sortVerts() {
		verts = new ArrayList<Integer>();
		for(int i = 0;i < data.noVertices;i ++) {
			verts.add(i);
		}
		Collections.sort(verts, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				if(data.less(o1, o2)) {
					return 1;
				}
				return -1;
			}
		});
	}
}
