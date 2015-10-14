package hope.it.works.ct;

import hope.it.works.DisjointSetsInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import vgl.iisc.utils.MyIntList;
import vgl.iisc.utils.Utilities;

public class SplitTree {
	
	int [] prev;
	int [] next;
	int [] prevCt;
	int [] cMap;
	public TriangleDataPrim data;
	MyIntList minima = new MyIntList();
	
	// Statistics
	public float [] fn;
	public int [] compCt;
	public int statCt = 0;
	int [] minCMap;
	public int [] pair1;
	public int [] pair2;
	public int pairCt = 0;
	
	public void findSplitTree(TriangleDataPrim data) {
		// initialize data
		this.data = data;
		int nv = data.noVertices;
		sortVerts();
		prev = new int[nv * 3];
		next = new int[nv];
		prevCt = new int[nv];
		Arrays.fill(prev, -1);
		Arrays.fill(next, -1);
		DisjointSetsInt dj = new DisjointSetsInt(data.noVertices + 1);
		
		cMap = new int[nv];
		minCMap = new int[nv];
		Arrays.fill(cMap, -1);
		HashSet<Integer> comps = new HashSet<Integer>();
		
		
		fn = new float[nv];
		compCt = new int[nv];
		pair1 = new int[nv];
		pair2 = new int[nv];
		
		// compute split tree
		for(int i = 0;i < nv;i ++) {
			int v = verts[i];
			comps.clear();
			for(int j = 0;j < data.vertices[v].star.length;j ++) {
				int t = data.vertices[v].star.get(j);
				int tin = t * 3;
				int v1 = data.triangles[tin];
				int v2 = data.triangles[tin + 1];
				int v3 = data.triangles[tin + 2];
				
				if(v2 == v) {
					comps.add(dj.find(v1));
				} else if(v3 == v) {
					comps.add(dj.find(v1));
					comps.add(dj.find(v2));
				}
			}
			int np = comps.size();
			if(np == 0) {
				// minima
				minima.add(v);
				int c = dj.find(v);
				cMap[c] = v;
				
				minCMap[c] = v;
				
				fn[statCt] = data.fnVertices[v];
				if(statCt == 0) {
					compCt[statCt] = 1;
				} else {
					compCt[statCt] = compCt[statCt - 1] + 1;
				}
				statCt ++;
			} else if (np == 1) {
				// regular
				int c = comps.iterator().next();
				addArc(v,c);
				dj.union(dj.find(c), dj.find(v));
				c = dj.find(v);
				cMap[c] = v;
				
			} else if(np == 2) {
				// saddle
				Iterator<Integer> it = comps.iterator();
				int c1 = it.next();
				addArc(v,c1);
				int c2 = it.next();
				addArc(v,c2);
				
				int v1 = minCMap[c1];
				int v2 = minCMap[c2];
				
				int remove = v1;
				int remain = v2;
				if(data.less(v1, v2)) {
					remove = v2;
					remain = v1;
				}
				
				// pair remove with v; and make the remain minimum of new component 
				pair1[pairCt] = remove;
				pair2[pairCt ++] = v;
				dj.union(dj.find(c1), dj.find(c2));
				dj.union(dj.find(c1), dj.find(v));
				int c = dj.find(v);
				cMap[c] = v;
				
				minCMap[c] = remain;
				
				fn[statCt] = data.fnVertices[v];
				compCt[statCt] = compCt[statCt - 1] - 1;
				statCt ++;
			} else if(np == 3) {
				Iterator<Integer> it = comps.iterator();
				int c1 = it.next();
				addArc(v,c1);
				int c2 = it.next();
				addArc(v,c2);
				int c3 = it.next();
				addArc(v,c3);
				
				int v1 = minCMap[c1];
				int v2 = minCMap[c2];
				int v3 = minCMap[c3];
				
				int remove1 = v1;
				int remove2 = v2;
				int remain = v3;
				
				if(data.less(v1, v2) && data.less(v1, v3)) {
					remove1 = v2;
					remove2 = v3;
					remain = v1;
				} else if(data.less(v2, v1) && data.less(v2, v3)) {
					remove1 = v1;
					remove2 = v3;
					remain = v2;
				}

				// pair the removes with v; and make the remain minimum of new component 
				pair1[pairCt] = remove1;
				pair2[pairCt ++] = v;
				
				pair1[pairCt] = remove2;
				pair2[pairCt ++] = v;
				
				dj.union(dj.find(c1), dj.find(c2));
				dj.union(dj.find(c1), dj.find(c3));
				dj.union(dj.find(c1), dj.find(v));
				int c = dj.find(v);
				cMap[c] = v;
				
				minCMap[c] = remain;
				
				fn[statCt] = data.fnVertices[v];
				compCt[statCt] = compCt[statCt - 1] - 2;
				statCt ++;
			} else {
				Utilities.er("Really shouldn't happen for this data");
			}
		}
	}
	
	private void addArc(int v, int c) {
		int pv = cMap[c];
		int vin = v * 3;
		prev[vin + prevCt[v]] = pv;
		prevCt[v] ++;
		next[pv] = v;
	}

//	public ArrayList<Integer> verts;
	public int [] verts;
	private void sortVerts() {
		verts = new int[data.noVertices];
		for(int i = 0;i < data.noVertices;i ++) {
			verts[i] = i;
		}
		myArrays.sort(verts);
	}
	
	public ArrayList<Feature> features = new ArrayList<Feature>();
	
	int [] fMap;
	public void findFeatures(float [] th) {
		fMap = new int[data.noVertices];
		Arrays.fill(fMap, -1);
		MyIntList curEx = new MyIntList();
		curEx.addAll(minima);
		for(int i = 0;i < th.length;i ++) {
			curEx = findFeature(curEx, th[i], i);
		}
	}

	private MyIntList findFeature(MyIntList curEx, float th, int level) {
		MyIntList nextEx = new MyIntList();
		HashSet<Integer> comps = new HashSet<Integer>();
		HashSet<Integer> ex = new HashSet<Integer>();
		HashSet<Integer> pf = new HashSet<Integer>();
		for(int i = 0;i < curEx.length;i ++) {
			int v = curEx.get(i);
			ex.add(v);
			int ep = -1;
			while(data.fnVertices[v] <= th) {
				ep = v;
				v = next[v];
			}
			if(ep == -1) {
				nextEx.add(curEx.get(i));
			} else {
				comps.add(ep);
			}
		}
		int [] q = new int[data.noVertices];
		int begin = 0;
		int end = 0;
		pf.clear();
		for(Iterator<Integer> it = comps.iterator();it.hasNext();) {
			int stv = it.next();
			Feature f = new Feature();
			f.level = level;
			q[end ++] = stv;
			int min = -1;
			while(begin != end) {
				int v = q[begin ++];
				if(fMap[v] == -1) {
					f.verts.add(v);
					for(int i = 0;i < prevCt[v];i ++) {
						q[end ++] = prev[v * 3 + i];
					}
					if(prevCt[v] == 0) {
						if(min == -1) {
							min = v;
						} else if(data.less(v,min)) {
							min = v;
						}
					}
				} else {
					pf.add(fMap[v]);
				}
			}
			f.v = min;
			for(Iterator<Integer> fit = pf.iterator();fit.hasNext();) {
				int fin = fit.next();
				Feature ff = features.get(fin);
				if(f.v == -1 || data.less(ff.v, f.v)) {
					f.v = ff.v;
				}
				f.prev.add(fin);
			}
			if(f.verts.size() > 0) {
				int fin = features.size();
				features.add(f);
				for(Iterator<Integer> vit = f.verts.iterator();vit.hasNext();) {
					int vv = vit.next();
					fMap[vv] = fin;
				}
				for(int i = 0;i < f.prev.length;i ++) {
					int ffin = f.prev.get(i);
					Feature ff = features.get(ffin);
					ff.next.add(fin);
				}
				nextEx.add(stv);
			}
		}
		
		return nextEx;
	}
	
	
	
	MyArrays myArrays = new MyArrays();
	public class MyArrays {

		private static final int INSERTIONSORT_THRESHOLD = 7;

		public void sort(int [] a) {
			int [] aux = clone(a);
			mergeSort(aux, a, 0, a.length, 0);
		}
		
		private int [] clone(int [] a) {
			int[] aux = new int[a.length];
			for(int i = 0;i < a.length;i ++) {
				aux[i] = a[i];
			}
			return aux;
		}
		private void mergeSort(int[] src, int[] dest, int low, int high, int off) {
			int length = high - low;

			// Insertion sort on smallest arrays
			if (length < INSERTIONSORT_THRESHOLD) {
				for (int i = low; i < high; i++)
					for (int j = i; j > low && compare(dest[j - 1], dest[j]) > 0; j--)
						swap(dest, j, j - 1);
				return;
			}

			// Recursively sort halves of dest into src
			int destLow = low;
			int destHigh = high;
			low += off;
			high += off;
			int mid = (low + high) >>> 1;
			mergeSort(dest, src, low, mid, -off);
			mergeSort(dest, src, mid, high, -off);

			// If list is already sorted, just copy from src to dest. This is an
			// optimization that results in faster sorts for nearly ordered lists.
			if (compare(src[mid - 1], src[mid]) <= 0) {
				System.arraycopy(src, low, dest, destLow, length);
				return;
			}

			// Merge sorted halves (now in src) into dest
			for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
				if (q >= high || p < mid && compare(src[p], src[q]) <= 0)
					dest[i] = src[p++];
				else
					dest[i] = src[q++];
			}
		}

		private void swap(int[] x, int a, int b) {
			int t = x[a];
			x[a] = x[b];
			x[b] = t;
		}
	}

	public int compare(int o1, int o2) {
		if(data.less(o1, o2)) {
			return -1;
		}
		return 1;
	}
}
