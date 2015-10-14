package vgl.iisc.volray.utils;

import java.io.FileInputStream;
import java.util.Properties;

import vgl.iisc.volray.model.DataInfo;

public class FileUtils {
	
	public static DataInfo parseInfoFile(String infoFile) {
		DataInfo info = new DataInfo();
		try {
			info.infoFile = infoFile;
			Properties p = new Properties();
			FileInputStream fip = new FileInputStream(infoFile);
			p.load(fip);
			
			info.folder = p.getProperty("folder").trim();;
			info.modelName = p.getProperty("modelName").trim();
			info.offFile = info.folder + "data/" + info.modelName + "/" + info.modelName + ".off";
			
			info.noDays = Integer.parseInt(p.getProperty("noDays").trim());
			info.year = Integer.parseInt(p.getProperty("year").trim());
			info.month = Integer.parseInt(p.getProperty("month").trim());
			info.dStart = Integer.parseInt(p.getProperty("dStart").trim());
			info.nx = Integer.parseInt(p.getProperty("nx").trim());
			info.ny = Integer.parseInt(p.getProperty("ny").trim());
			
			info.trmm = Boolean.parseBoolean(p.getProperty("trmm").trim());
			
			info.stLat = Float.parseFloat(p.getProperty("stLat").trim());
			info.enLat = Float.parseFloat(p.getProperty("enLat").trim());
			info.stLon = Float.parseFloat(p.getProperty("stLon").trim());
			info.enLon = Float.parseFloat(p.getProperty("enLon").trim());
			
			info.th = Float.parseFloat(p.getProperty("threshold").trim());
			info.cloudFolder = p.getProperty("cloudFolder").trim();
			info.create = Boolean.parseBoolean(p.getProperty("createClouds"));
			fip.close();
		} catch (Exception e) {
			System.err.println("Error opening file");
			return null;
		}

		return info;
	}
	
}
