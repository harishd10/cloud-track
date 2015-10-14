package vgl.iisc.volray.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import vgl.iisc.utils.Utilities;

public class GridReader {

	public static enum VolumeType {
		RAW, ASCII, VRF
	};

	public static enum DataType {
		UNSIGNED_BYTE, SHORT, INTEGER, FLOAT, DOUBLE, XYZF, F
	};

	public static enum ByteOrder {
		BIG_ENDIAN, LITTLE_ENDIAN
	};
	
	private static float max, min;
	private static int [] dim;
	private static float [] scale;
	
	public static void writeStructuredGridToVRF(String fileName, String vrfFile, VolumeType inputType, DataType dataType, ByteOrder byteOrder) throws Exception {
		dim = getDimension(fileName);
		scale = getScale(fileName);

		int nv = dim[0] * dim[1] * dim[2];
		float[] data = null;
		if(inputType == VolumeType.RAW) {
			data = readRawFile(fileName, nv, dataType, byteOrder);
		} else if(inputType == VolumeType.ASCII) {
			data = readAsciiFile(fileName, nv, dataType);
		}
		System.out.println("No. of Vertices : " + nv);
		System.out.println("Finished reading file");
		writeToVRF(vrfFile, data);
	}

	public static float [] readVRF(String vrfFile, int [] dim, float [] scale) {
		try {
			ObjectInputStream inFile = new ObjectInputStream(new FileInputStream(vrfFile));
			dim[0] = inFile.readShort();
			dim[1] = inFile.readShort();
			dim[2] = inFile.readShort();
			
			scale[0] = inFile.readFloat();
			scale[1] = inFile.readFloat();
			scale[2] = inFile.readFloat();
			
			float [] data = (float[]) inFile.readObject();
			inFile.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void writeToVRF(String vrfFile, float[] data) {
		try {
			ObjectOutputStream outFile = new ObjectOutputStream(new FileOutputStream(vrfFile));
			outFile.writeShort(dim[0]);
			outFile.writeShort(dim[1]);
			outFile.writeShort(dim[2]);
			
			outFile.writeFloat(scale[0]);
			outFile.writeFloat(scale[1]);
			outFile.writeFloat(scale[2]);
			
			outFile.writeObject(data);
			outFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void normalizeData(float[] data) {
		float diff = max - min;
//		diff += 10;
		for(int i = 0;i < data.length;i ++) {
			data[i] = (data[i] - min) / diff;
		}
	}

	private static float[] readAsciiFile(String fileName, int nv, DataType dataType) {
		// TODO todo
		Utilities.er("Yet to be implemented");
		return null;
	}

	public static float [] readRawFile(String fileName, int nv, DataType dataType, ByteOrder byteOrder) throws Exception {
		float [] data = new float[nv];
		int totBytes = data.length;
		RandomAccessFile fl = new RandomAccessFile(fileName, "r");
		int mul = 1;
		
		switch (dataType) {
		case UNSIGNED_BYTE:
			totBytes *= 1;
			mul = 1;
			break;

		case SHORT:
			totBytes *= 2;
			mul = 2;
			break;

		case INTEGER:
			totBytes *= 4;
			mul = 4;
			break;

		case FLOAT:
			totBytes *= 4;
			mul = 4;
			break;

		case DOUBLE:
			totBytes *= 8;
			mul = 8;
			break;

		default:
			Utilities.er("Invalid data type for raw file");
		}
		byte[] ip = new byte[totBytes];
		fl.readFully(ip);
		fl.close();
		
		min = Float.MAX_VALUE;
		max = - Float.MAX_VALUE;
		for(int i = 0;i < data.length;i ++) {
			int in = i * mul;
			long val = 0;
			for(int j = 0;j < mul;j ++) {
				switch(byteOrder) {
				case BIG_ENDIAN:
					val = (val << 8) | (0xff & ip[in + j]);
					break;
					
				case LITTLE_ENDIAN:
					val = val | ((0xff & ip[in + j]) << (j * 8) );
					break;
					
				default:
					Utilities.er("Invalid byte order");					
				}
			}
			data[i] = getValue(val, dataType);
			min = Math.min(min, data[i]);
			max = Math.max(max, data[i]);
		}
		normalizeData(data);
		return data;
	}

	private static float getValue(long bytes, DataType dataType) {
		if(dataType == DataType.DOUBLE) {
			return (float) Double.longBitsToDouble(bytes);
		} else {
			int v = (int) (bytes & 0xffffffff);
			switch(dataType) {
			case UNSIGNED_BYTE:
			case SHORT:
			case INTEGER:
				return v;
				
			case FLOAT:
				return Float.intBitsToFloat(v);
			default:
				Utilities.er("Shouldn't come here!!");
			}
		}
		Utilities.er("Shouldn't come here!!");
		return 0;
	}

	public static int[] getDimension(String fileName) {
		try {
			int[] dim = new int[3];
			int in = fileName.lastIndexOf(".");
			fileName = fileName.substring(0, in);
			fileName += ".dim";
			FileInputStream ff = new FileInputStream(new File(fileName));
			InputStreamReader reader = new InputStreamReader(ff);
			BufferedReader f = new BufferedReader(reader);
			String s = f.readLine();
			String[] r = Utilities.splitString(s);
			dim[0] = Integer.parseInt(r[0].trim());
			dim[1] = Integer.parseInt(r[1].trim());
			dim[2] = Integer.parseInt(r[2].trim());
			f.close();
			return dim;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static float[] getScale(String fileName) {
		try {
			float[] scale = new float[3];
			int in = fileName.lastIndexOf(".");
			fileName = fileName.substring(0, in);
			fileName += ".scale";
			FileInputStream ff = new FileInputStream(new File(fileName));
			InputStreamReader reader = new InputStreamReader(ff);
			BufferedReader f = new BufferedReader(reader);
			String s = f.readLine();
			String[] r = Utilities.splitString(s);
			scale[0] = Float.parseFloat(r[0].trim());
			scale[1] = Float.parseFloat(r[1].trim());
			scale[2] = Float.parseFloat(r[2].trim());
			f.close();
			return scale;
		} catch (Exception e) {
			float[] scale = new float[] { 1, 1, 1 };
			return scale;
		}
	}
}
