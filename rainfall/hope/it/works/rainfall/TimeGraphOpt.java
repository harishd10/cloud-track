package hope.it.works.rainfall;

import hope.it.works.ct.TriangleDataPrim;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;

import vgl.iisc.utils.MyIntList;
import vgl.iisc.utils.Utilities;

public class TimeGraphOpt {
	
	public class Node {
		public int time;
		public int featureNo;
		
		public int cpVertex;
		
		public float x, y;
		public float dx, dy;
		public int size;
	}
	
	public class Edge {
		public int n1;
		public int n2;
		
		@Override
		public int hashCode() {
			String s = "" + n1 + " " + n2;
			if(n1 > n2) {
				s = "" + n2 + " " + n1;
			}
			return s.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			Edge e = (Edge) obj;
			return (e.n1 == n1 && e.n2 == n2);
		}
	}
	
	// Adjacent edges for each node
	public class Adjacencies {
		public MyIntList prev = new MyIntList(2);
		public MyIntList next = new MyIntList(2);
	}
	
	public ArrayList<Node> nodes;
	public ArrayList<Edge> edges;
	public HashSet<Edge> allEdges = new HashSet<TimeGraphOpt.Edge>();
	public ArrayList<Adjacencies> adj;
	
	Cloud [] clouds = new Cloud[2];
	int totalTime;
	public int [][] index;
	
	String [] ts;
	String model;
	String folder;
	int nx, ny;
	int nv;
	
	boolean createClouds = false;
	
	public void findClouds(TriangleDataPrim data, String folder, String model, String [] timeSteps, int nx, int ny) {
		this.folder = folder;
		this.model = model;
		ts = timeSteps;
		this.nx = nx;
		this.ny = ny;
		
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		adj = new ArrayList<Adjacencies>();
		// aila - 210
		// mumbai - 220
		// nakazawa - 221
		// nakazawa-long 220
		float [] th = {220};
		totalTime = timeSteps.length;
		nv = data.noVertices;
		
		// finding clouds across time steps
		System.out.println("Finding clouds across time steps");
		index = new int[totalTime][];
		int ct = 0;
		for(int i = 0;i < timeSteps.length;i ++) {
			Cloud cloud;
			if(createClouds) {
				cloud = new Cloud(null, data, folder + "data/" + model + "/" + model + "-" + timeSteps[i]+ ".boff", th);
				cloud.time = i;
				cloud.write();
			} else {
				cloud = new Cloud();
				cloud.read(i);
			}
			
			index[i] = new int[cloud.criticalPoints.length];
			for(int j = 0;j < index[i].length;j ++) {
				index[i][j] = ct ++;
				
				Node node = new Node();
				node.cpVertex = cloud.criticalPoints[j];
				node.time = i;
				node.featureNo = j;
				nodes.add(node);
				
				Adjacencies adj = new Adjacencies();
				this.adj.add(adj);
			}
		}
	}
	
	float ep = 0.0001f;
	public void createGraph() {
		
		System.out.println("Creating Graph!!!");
		
		clouds[0] = new Cloud();
		clouds[1] = new Cloud();
		int ct = 0;
		clouds[0].read(ct);
		clouds[1].read(ct + 1);
		int [] vecCt = new int[nodes.size()];
		int ein = 0;
		for(int i = 0;i < totalTime;i ++) {
			ct = i;
			int nt = ct + 1;
			
			if(nt < totalTime) {
				readVector(ct,0);
				for(int j = 0;j < vector.length;j ++) {
					int curCloud = clouds[0].region[j];
					if(curCloud == -1) {
						continue;
					}
					int x = j % nx;;
					int y = j / nx;
					float xx = x;
					float yy = y;
					
					int in = index[ct][curCloud];
					
					Node n = nodes.get(in);
					n.x += x;
					n.y += y;
					
					n.dx += vector[j][0];
					n.dy += vector[j][1];
					vecCt[in] ++;
					
					xx += vector[j][0];
					yy += vector[j][1];
					x = Math.round(xx);
					y = Math.round(yy);
					if(x < 0 || y < 0 || x >= nx || y >= ny) {
						continue;
					}
					
					int v = x + nx * y;
					int nextCloud = clouds[1].region[v];
					
					if(nextCloud != -1) {
						int nodeIn = index[nt][nextCloud];
						Edge e = new Edge();
						e.n1 = in;
						e.n2 = nodeIn;
						if(!allEdges.contains(e)) {
							edges.add(e);
							allEdges.add(e);
							
							Adjacencies ad = adj.get(nodeIn);
							ad.prev.add(ein);
							ad = adj.get(in);
							ad.next.add(ein);
							
							ein ++;
						}
					}
				}
			} else {
				for(int j = 0;j < vector.length;j ++) {
					int curCloud = clouds[0].region[j];
					if(curCloud == -1) {
						continue;
					}
					int x = j % nx;;
					int y = j / nx;
					int in = index[ct][curCloud];
					
					Node n = nodes.get(in);
					n.x += x;
					n.y += y;
					
					vecCt[in] ++;
				}
			}

			
			// update clouds for next iteration
			clouds[0] = clouds[1];
			nt ++;
			if(nt < totalTime) {
				clouds[1] = new Cloud();
				clouds[1].read(nt);
			} else {
				clouds[1] = null;
			}
		}
		for(int i = 0;i < vecCt.length;i ++) {
			Node n = nodes.get(i);
			n.size = vecCt[i];
			if(vecCt[i] == 0) {
				Utilities.er("Not posdsible. check cloud count");
			}
			n.dx /= vecCt[i];
			n.dy /= vecCt[i];
			n.x /= vecCt[i];
			n.y /= vecCt[i];
//			float norm = n.dx * n.dx + n.dy * n.dy;
//			norm = (float) Math.sqrt(norm);
//			if(norm > ep) {
//				n.dx = n.dx  * 10 / norm;
//				n.dy = n.dy  * 10 / norm;
//			} else {
//				n.dx = 0;
//				n.dy = 0;
//			}
		}
	}
	public float [][] vector, vector1;
	public float []vectors;
	public void readVector(int ct, int no) {
		if(vectors == null) {
			System.out.println("Initializing all vectors");
			vectors = new float[totalTime * nv * 2];
			int tot = 0;
			for(int t = 0;t < totalTime - 1;t ++) {
//				System.out.println("Time: " + t);
				String file = folder + "vector/" + model + "/" + model + "-" + ts[t] + ".field";
				try {
					byte [] b = new byte[nv*2*4];
					BufferedInputStream ip = new BufferedInputStream(new FileInputStream(file));
					ip.read(b);
					ip.close();
					ByteBuffer buf = ByteBuffer.wrap(b);
					buf.order(ByteOrder.LITTLE_ENDIAN);
					for(int i = 0;i < nv;i ++) {
						vectors[tot ++] = buf.getFloat();
						vectors[tot ++] = buf.getFloat();
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			System.out.println("Sone initialiing");
		}
		if(no == 0) {
			if(vector == null) {
				vector = new float[nv][2];
			}
			int st = ct * nv * 2;
			for(int i = 0;i < nv;i ++) {
				vector[i][0] = vectors[st ++];
				vector[i][1] = vectors[st ++];
			}
		} else {
			if(vector1 == null) {
				vector1 = new float[nv][2];
			}
			int st = ct * nv * 2;
			for(int i = 0;i < nv;i ++) {
				vector1[i][0] = vectors[st ++];
				vector1[i][1] = vectors[st ++];
			}
		}
	}
	
//	public void readRevVector(int ct) {
//		String file = folder + "vector/" + model + "/" + model + "-" + ts[ct] + ".rfield";
//		readVector(file);
//	}

	public void findEastMovingClouds() {
		
		System.out.println("Finding east moving clouds!!!");
		
//		clouds[0] = new Cloud();
//		clouds[1] = new Cloud();
//		int ct = 0;
//		clouds[0].read(ct);
//		clouds[1].read(ct + 1);
		for(int i = 0;i < totalTime;i ++) {
			findEastMovingCloud(i);
		}
	}

	public ArrayList<Integer> findEastMovingCloud(int time) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		int ct = time;
		int nt = ct + 1;
		System.out.println("Processing time " + ct + " of " + totalTime);
		if(clouds[0] == null) {
			clouds[0] = new Cloud();
		}
		clouds[0].read(ct);
		if(nt < totalTime) {
			readVector(ct,1);
			for(int j = 0;j < vector1.length;j ++) {
				int curCloud = clouds[0].region[j];
				if(curCloud == -1) {
					continue;
				}
				if(vector1[j][0] > 0) {
					// moving right
					int len = getTrackLength(ct, j);
					if(len >= 3) {
						System.out.println("East moving for time " + len + " from time " + ct);
						list.add(j);
					}
				}
			}
		}
		return list;
	}
	
	private int getTrackLength(int time, int v) {
		Cloud c = new Cloud();
		int len = 1;
		for(int i = time;i < totalTime - 1;i ++) {
			c.read(i + 1);
			readVector(i,0);
			float xx = v % nx;
			float yy = v / nx;
			xx += vector[v][0];
			yy += vector[v][1];
			int x = Math.round(xx);
			int y = Math.round(yy);
			
			if(vector[v][0] < 0) {
				break;
			}
			v = x + y * nx;
			if(x >= nx || x < 0 || y >= ny || y < 0 || c.region[v] == -1 || vector[v][0] < 0) {
				break;
			}
			len ++;
		}
		return len;
	}

	public ArrayList<Integer> findWestMovingClouds(int time) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		int ct = time;
		System.out.println("Processing time " + ct + " of " + totalTime);
		if(clouds[0] == null) {
			clouds[0] = new Cloud();
		}
		clouds[0].read(ct);
		readVector(ct,1);
		for(int j = 0;j < vector1.length;j ++) {
			int curCloud = clouds[0].region[j];
			if(curCloud == -1) {
				continue;
			}
			if(vector1[j][0] < 0) {
				// moving left
				list.add(j);
			}
		}
		return list;
	}
	
	public ArrayList<Integer> findWestMovingClouds(int time, int minLength) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		int ct = time;
		System.out.println("Processing time " + ct + " of " + totalTime);
		if(clouds[0] == null) {
			clouds[0] = new Cloud();
		}
		clouds[0].read(ct);
		readVector(ct,1);
		for(int j = 0;j < vector1.length;j ++) {
			if(j % 10000 == 0) {
				System.out.println(j + " of " + vector1.length);
			}
			int curCloud = clouds[0].region[j];
			if(curCloud == -1) {
				continue;
			}
			if(vector1[j][0] < 0) {
				// moving left
				boolean west= getWestTrackLength(ct, j, minLength);
				if(west) {
					list.add(j);
				}
			}
		}
		return list;
	}
	
	private boolean getWestTrackLength(int time, int v, int minLength) {
		if(minLength == 1) {
			return true;
		}
		Cloud c = new Cloud();
		int len = 1;
		for(int i = time;i < totalTime - 1;i ++) {
			c.read(i + 1);
			readVector(i,0);
			float xx = v % nx;
			float yy = v / nx;
			xx += vector[v][0];
			yy += vector[v][1];
			if(vector[v][0] >= 0) {
				break;
			}
			int x = Math.round(xx);
			int y = Math.round(yy);
			
			v = x + y * nx;
			if(x >= nx || x < 0 || y >= ny || y < 0 || c.region[v] == -1 || vector[v][0] < 0) {
				break;
			}
			len ++;
			if(len >= minLength) {
				return true;
			}
		}
		if(len < minLength) {
			return false;
		}
		return true;
	}

}
