package hope.it.works.rainfall;

import hope.it.works.rainfall.TimeGraph.Adjacencies;
import hope.it.works.rainfall.TimeGraph.Edge;

import java.util.Arrays;

public class AnalyseTimeGraph {
	
	public TimeGraph tg;
	public int [] col;
	
	public AnalyseTimeGraph(TimeGraph tg) {
		this.tg = tg;
		col = new int[tg.nodes.size()];
		Arrays.fill(col, -1);
	}
	
	public int colours = 0; 
	public void colorComponents() {
		int c = 0;
		int [] q = new int[col.length];
		for(int i = 0;i < col.length;i ++) {
			if(col[i] == -1) {
				int cur = c;
				c ++;
				
				int begin = 0;
				int end = 0;
				q[end ++] = i;
				col[i] = cur;
				while(begin < end) {
					int n = q[begin ++];
					Adjacencies adj = tg.adj.get(n);
					for(int j = 0;j < adj.next.length;j ++) {
						int en = adj.next.get(j);
						Edge e = tg.edges.get(en);
						int nn = e.n2;
						if(col[nn] == -1) {
							q[end++] = nn;
							col[nn] = cur;
						}
					}
					for(int j = 0;j < adj.prev.length;j ++) {
						int en = adj.prev.get(j);
						Edge e = tg.edges.get(en);
						int nn = e.n1;
						if(col[nn] == -1) {
							q[end++] = nn;
							col[nn] = cur;
						}
					}
				}
			}
		}
		colours = c;
	}
	
	public void colorCloudDescendants() {
		// TODO
	}
}
