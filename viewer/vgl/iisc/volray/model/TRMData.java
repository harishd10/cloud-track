/**
 * Author : Harish D
 */
package vgl.iisc.volray.model;

import java.util.ArrayList;
import java.util.HashSet;

import vgl.iisc.volray.ui.editor.TransferFunction;

public class TRMData {
	public class Vertex {
		public float x;
		public float y;
		public float z;
		public float fn;
		public int fnIndex; 
		
		public float nx;
		public float ny;
		public float nz;
		public int n;
		
		public HashSet<Integer> tris = new HashSet<Integer>();
	}
	
	public class Triangle {
		public int v1;
		public int v2;
		public int v3;
	}
	
	private ArrayList<Vertex> vertexList;
	private ArrayList<Triangle> triangleList;
	
	public Vertex [] vertices = null;
	public Triangle [] triangles = null;
	int [] verts;
	float maxFn = -Float.MAX_VALUE;
	float minFn = Float.MAX_VALUE;
	
	int noEdges;
	
	public TRMData() {
		vertexList = new ArrayList<Vertex>();
		triangleList = new ArrayList<Triangle>();
	}
	
	public void addVertex(float x,float y,float z,float fn) {
		Vertex v = new Vertex();
		v.x = x;
		v.y = y;
		v.z = z;
		v.fn = fn;
		vertexList.add(v);
		
		maxFn = Math.max(maxFn, fn);
		minFn = Math.min(minFn, fn);
	}
	
	public void addTriangle(int v1,int v2,int v3) {
		Triangle t = new Triangle();
		t.v1 = v1;
		t.v2 =  v2;
		t.v3 = v3;
		
		Vertex vt1 = vertexList.get(t.v1);
		Vertex vt2 = vertexList.get(t.v2);
		Vertex vt3 = vertexList.get(t.v3);
		
		float x1 = vt2.x - vt1.x;
		float y1 = vt2.y - vt1.y;
		float z1 = vt2.z - vt1.z;

		float x2 = vt3.x - vt2.x;
		float y2 = vt3.y - vt2.y;
		float z2 = vt3.z - vt2.z;
		
		float nx = y1 * z2 - y2 * z1;
		float ny = z1 * x2 - x1 * z2;
		float nz = x1 * y2 - x2 * y1;
		
		float sum = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		nx /= sum;
		ny /= sum;
		nz /= sum;
		
		vt1.nx += nx;
		vt1.ny += ny;
		vt1.nz += nz;
		vt1.n ++;
		
		vt2.nx += nx;
		vt2.ny += ny;
		vt2.nz += nz;
		vt2.n ++;

		vt3.nx += nx;
		vt3.ny += ny;
		vt3.nz += nz;
		vt3.n ++;

		int tin = triangleList.size();
		triangleList.add(t);
		vertexList.get(v1).tris.add(tin);
		vertexList.get(v2).tris.add(tin);
		vertexList.get(v3).tris.add(tin);
	}
	
	public void setup() {
		if(triangles != null) {
			return;
		}
		triangles = triangleList.toArray(new Triangle[0]);
		triangleList.clear();
		vertices = vertexList.toArray(new Vertex[0]);
		
		vertexList.clear();
		
		System.err.println(minFn + " : " + maxFn);
		float diff = maxFn - minFn;
		for(int i = 0;i < vertices.length;i ++) {
			vertices[i].nx /= vertices[i].n;
			vertices[i].ny /= vertices[i].n;
			vertices[i].nz /= vertices[i].n;
			
			float sum = vertices[i].nx * vertices[i].nx + vertices[i].ny * vertices[i].ny + vertices[i].nz * vertices[i].nz;
			sum = (float) Math.sqrt(sum);
			vertices[i].nx /= sum;
			vertices[i].ny /= sum;
			vertices[i].nz /= sum;
			
			vertices[i].fn = (vertices[i].fn - minFn) / diff; 
			vertices[i].fnIndex = (int) (vertices[i].fn * (TransferFunction.COLORMAPSIZE - 1)); 
			vertices[i].fnIndex *= 4;
		}
	}		

	public void setupGrid() {
		vertices = vertexList.toArray(new Vertex[0]);
		vertexList.clear();
		System.err.println(minFn + " : " + maxFn);
		
		float diff = maxFn - minFn;
		for(int i = 0;i < vertices.length;i ++) {
			vertices[i].fn = (vertices[i].fn - minFn) / diff;
			if(vertices[i].fn > 1) {
				vertices[i].fn = 1;
			}
			vertices[i].fnIndex = (int) (vertices[i].fn * (TransferFunction.COLORMAPSIZE - 1)); 
			vertices[i].fnIndex *= 4;
		}
	}		


	public int getNoVertices() {
		return vertices.length;
	}
}
