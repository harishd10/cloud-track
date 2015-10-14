package hope.it.works.ct;

import java.util.HashSet;

import vgl.iisc.utils.MyIntList;

public class Extrema {
	
	public int v;
	
	public MyIntList prev = new MyIntList(2);
	public MyIntList next = new MyIntList(2);
	public HashSet<Integer> verts = new HashSet<Integer>();
}
