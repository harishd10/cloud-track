package vgl.iisc.volray.model;

import static vgl.iisc.utils.Utilities.splitString;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class LineGraph {
	
	float [][] plots;
	float [][] promPlot;
	Float [] dist;
	float [] origDist;
	public float [] x;
	public float [][] y;
	public float [][] py;
	int noVals;
	public HashMap<Integer, Integer> edgeBranchMap;
	
	public float [][] featureDist;
	public float [] featureFn;
//	public float [][] curveDist;
	
	public LineGraph(String file) {
		this.ep = 1 - 0;
		readBinFile(file);
	}
	
	@SuppressWarnings("unchecked")
	public void readBinFile(String file) {
		try {
			ObjectInputStream ip = new ObjectInputStream(new FileInputStream(file));
			dist = (Float[]) ip.readObject();
			origDist = new float[dist.length];
			float max = dist[dist.length - 1];
			for(int i = 0;i < dist.length;i ++) {
				origDist[i] = dist[i];
				dist[i] /= max;
			}
			plots = (float[][]) ip.readObject();
			y = new float [plots.length][dist.length];
			py = new float [plots.length][dist.length];
			
			x = new float [dist.length];
			edgeBranchMap = (HashMap<Integer, Integer>) ip.readObject();
			promPlot = (float[][]) ip.readObject();
			
			featureDist = (float[][]) ip.readObject();
			featureFn = (float[]) ip.readObject();

//			curveDist = new float[featureDist.length][featureDist.length];
//			for(int i = 0;i < featureDist.length;i ++) {
//				for(int j = i;j < featureDist.length;j ++) {
//					curveDist[i][j] = findDist(i,j);
//					curveDist[j][i] = curveDist[i][j];
//				}
//			}
			ip.close();
			
//			dEp = new int[plots.length];
//			wtEp = new float [plots.length];
//			twtEp = new float [plots.length];
//			prDEp = new int [plots.length];
			
			for(int i = 0;i < plots.length;i ++) {
//				normal.branches.add(i);
//				pNormal.branches.add(i);
//				wtNormal.branches.add(i);
//				twtNormal.branches.add(i);
				saliency.branches.add(i);
			}
			System.out.println("Setting up data");
			setupData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readASCIIFile(String file) {
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String l = f.readLine();
			int no = Integer.parseInt(l.trim());
			
			l = f.readLine();
			String [] s = splitString(l);
			noVals = s.length;
			
			plots = new float [no][noVals];
			dist = new Float [noVals];
			y = new float [no][noVals];
			x = new float [noVals];
			
			float maxd = 0;
			for(int i = 0;i < noVals;i ++) {
				dist[i] = Float.parseFloat(s[i].trim());
				maxd = Math.max(maxd, dist[i]);
			}
			
			for(int j = 0;j < no;j ++) {
				l = f.readLine();
				s = splitString(l);
				for(int i = 0;i < noVals;i ++) {
					plots[j][i] = Float.parseFloat(s[i].trim());
				}
			}
			f.close();
			
			setupData();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	float rad = 40f;
	int pos = -1;
	
	private void setupData() {
		float maxd = 0;
		for(int i = 0;i < dist.length;i ++) {
			maxd = Math.max(maxd, dist[i]);
		}

		for(int i = 0;i < x.length;i ++) {
			x[i] = dist[i] / maxd;
			x[i] *= 1.6;
			x[i] -= 0.8;
		}
		
		// find saliency at radius = rad
		
		for(int i = 0;i < origDist.length;i ++) {
			if(origDist[i] <= rad) {
				pos = i;
			} else {
				break;
			}
		}
		
//		for(int i = 0;i < y.length;i ++) {
//			boolean first = true;
//			boolean pfirst = true;
//			for(int j = 0;j < y[i].length;j ++) {
//				y[i][j] = plots[i][j] * 1.6f - 0.8f;
//				py[i][j] = promPlot[i][j] * 1.6f - 0.8f;
//				
//				if(first) {
//					// longevity Ep
//					if(plots[i][j] < ep) {
//						first = false;
//						dEp[i] = j;
//					}
//					
//					// Weighted Ep
//					if(j > 0) {
//						wtEp[i] += (dist[j] - dist[j - 1]) * plots[i][j - 1];	
//					}
//				}
//				// Weighted Ep
//				if(j > 0) {
//					twtEp[i] += (dist[j] - dist[j - 1]) * plots[i][j - 1];	
//				}
//				
//				if(pfirst) {
//					if(promPlot[i][j] < ep) {
//						pfirst = false;
//						prDEp[i] = j;
//					}
//				}
//			}
//		}
		
//		for(int i = 0;i < curveDist.length;i ++) {
//			for(int j = i + 1;j < curveDist.length;j ++) {
//				CurvePair c = new CurvePair();
//				c.curve1 = i;
//				c.curve2 = j;
//				c.dist = curveDist[i][j];
//				similar.add(c);
//				
//				CurvePair c1 = new CurvePair();
//				c1.curve1 = i;
//				c1.curve2 = j;
//				c1.dist = Math.abs(plots[i][plots[i].length - 1] - plots[j][plots[i].length - 1]);
//				perDist.add(c1);
//			}
//		}
//		Collections.sort(normal.branches, longComp);
//		Collections.sort(pNormal.branches, pLongComp);
//		Collections.sort(wtNormal.branches, weightedComp);
//		Collections.sort(twtNormal.branches, tweightedComp);
		Collections.sort(saliency.branches, saliencyComp);
		
//		Collections.sort(similar);
//		Collections.sort(perDist);
//		cluster();
//		clusterPersistence();
	}
	
//	int [] dEp;
//	float [] wtEp;
//	float [] twtEp;
//	int [] prDEp;
	float ep;
	
//	WtComp weightedComp = new WtComp();
//	TWtComp tweightedComp = new TWtComp();
//	LongevityComp longComp = new LongevityComp();
//	PromLongComp pLongComp = new PromLongComp();
	SaliencyComp saliencyComp = new SaliencyComp();
	
	public class OrderedFeatures {
		public ArrayList<Integer> branches = new ArrayList<Integer>();
	}
	
//	class LongevityComp implements Comparator<Integer> {
//		public int compare(Integer o1, Integer o2) {
//			// descending order
//			return dEp[o2] - dEp[o1];
//		}
//	}

	
	class SaliencyComp implements Comparator<Integer> {
		public int compare(Integer o1, Integer o2) {
			// descending order
			if(plots[o2][pos] > plots[o1][pos]) {
				return 1;
			}
			if(plots[o1][pos] > plots[o2][pos]) {
				return -1;
			}
			int pp = plots[o1].length - 1;
			if(plots[o2][pp] > plots[o1][pp]) {
				return 1;
			}
			if(plots[o1][pp] > plots[o2][pp]) {
				return -1;
			}
			return 0;
		}
	}
	
	
//	class WtComp implements Comparator<Integer> {
//		public int compare(Integer o1, Integer o2) {
//			// descending order
//			if(wtEp[o2] > wtEp[o1]) {
//				return 1;
//			}
//			if(wtEp[o1] > wtEp[o2]) {
//				return -1;
//			}
//			return 0;
//		}
//	}
	
//	class TWtComp implements Comparator<Integer> {
//		public int compare(Integer o1, Integer o2) {
//			// descending order
//			if(wtEp[o2] > wtEp[o1]) {
//				return 1;
//			}
//			if(wtEp[o1] > wtEp[o2]) {
//				return -1;
//			}
//			return 0;
//		}
//	}
	
//	class PromLongComp implements Comparator<Integer> {
//		public int compare(Integer o1, Integer o2) {
//			// descending order
//			return prDEp[o2] - prDEp[o1];
//		}
//	}
	
	public void setEp(float ep) {
		this.ep = 1 - ep;
		setupData();
	}
	
	public OrderedFeatures normal = new OrderedFeatures();
	public OrderedFeatures pNormal = new OrderedFeatures();
	public OrderedFeatures wtNormal = new OrderedFeatures();
	public OrderedFeatures twtNormal = new OrderedFeatures();
	public OrderedFeatures saliency = new OrderedFeatures();

	
	
	public class CurvePair implements Comparable<CurvePair> {
		public int curve1;
		public int curve2;
		public float dist;
		
		public int compareTo(CurvePair o) {
			if(dist < o.dist) {
				return -1;
			} else if(dist > o.dist) {
				return 1;
			}
			return 0;
		}
	}
	
	public class Cluster {
		public HashSet<Integer> curves = new HashSet<Integer>();
	}
	
	public ArrayList<CurvePair> similar = new ArrayList<CurvePair>();
	public ArrayList<CurvePair> perDist = new ArrayList<CurvePair>();
	
	public Cluster [] clusters;
	public Cluster [] pclusters;
	
//	private float findDist(int i, int j) {
//		float pd1 = plots[i][0];
//		float pd2 = plots[j][0];
//		float ret = 0;
//		for(int d = 1;d < dist.length;d ++) {
//			float dd = dist[d] - dist[d - 1];
//			float area = dd * (pd1 - pd2);
//			area = Math.abs(area);
//			ret += area; 
//			pd1 = plots[i][d];
//			pd2 = plots[j][d];
//		}
//		return ret;
//	}

//	private void cluster() {
//		DisjointSets dj = new DisjointSets();
//		float min = similar.get(0).dist;
//		int l = similar.size();
//		float max = similar.get(l - 1).dist;
//		float diff = max - min;
//		System.out.println("Max Dist : " + max);
//		System.out.println("Min Dist : " + min);
//		diff /= 20;
//		diff += min;
//		
//		for(int i = 0;i < l;i ++) {
//			CurvePair c = similar.get(i);
//			if(c.dist > diff) {
//				break;
//			}
//			dj.union(dj.find(c.curve1), dj.find(c.curve2));
//		}
//		HashSet<Integer> set = new HashSet<Integer>();
//		for(int i = 0;i < plots.length;i ++) {
//			set.add(dj.find(i));
//		}
//		int noc = set.size();
//		System.out.println("No. of features : " + plots.length);
//		System.out.println("No. of clusters : " + noc);
//		
//		clusters = new Cluster[noc];
//		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
//		int ct= 0;
//		for(Iterator<Integer> it = set.iterator();it.hasNext();) {
//			int c = it.next();
//			map.put(c, ct);
//			clusters[ct] = new Cluster();
//			ct ++;
//		}
//		
//		for(int i = 0;i < plots.length;i ++) {
//			int s = dj.find(i);
//			int in = map.get(s);
//			clusters[in].curves.add(i + 1);
//		}
//	}
	
//	private void clusterPersistence() {
//		DisjointSets dj = new DisjointSets();
//		float min = perDist.get(0).dist;
//		int l = perDist.size();
//		float max = perDist.get(l - 1).dist;
//		float diff = max - min;
//		System.out.println("Max Dist : " + max);
//		System.out.println("Min Dist : " + min);
//		diff /= 10;
//		diff += min;
//		
//		for(int i = 0;i < l;i ++) {
//			CurvePair c = perDist.get(i);
//			if(c.dist > diff) {
//				break;
//			}
//			dj.union(dj.find(c.curve1), dj.find(c.curve2));
//		}
//		HashSet<Integer> set = new HashSet<Integer>();
//		for(int i = 0;i < plots.length;i ++) {
//			set.add(dj.find(i));
//		}
//		int noc = set.size();
////		System.out.println("No. of features : " + plots.length);
//		System.out.println("No. of Persistence clusters : " + noc);
//		
//		pclusters = new Cluster[noc];
//		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
//		int ct= 0;
//		for(Iterator<Integer> it = set.iterator();it.hasNext();) {
//			int c = it.next();
//			map.put(c, ct);
//			pclusters[ct] = new Cluster();
//			ct ++;
//		}
//		
//		for(int i = 0;i < plots.length;i ++) {
//			int s = dj.find(i);
//			int in = map.get(s);
//			pclusters[in].curves.add(i + 1);
//		}
//	}

}
