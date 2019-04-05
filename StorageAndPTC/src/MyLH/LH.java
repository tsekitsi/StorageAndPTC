package MyLH;

import PBStorage.PBFileEntry;
import PBStorage.PBStorage;
import PBStorage.TestPBStorage;
import PTCFramework.ConsumerIterator;
import PTCFramework.PTCFramework;
import PTCFramework.ProducerIterator;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.*;

public class LH {
	
	private static PBFileEntry e = new PBFileEntry();
	private static PBStorage MyStorage = new PBStorage();
	
	// file-related:
//	private static Integer m;
//	private static Integer sP;
//	private static Integer numOfPages;
//	private static double acl_Min;
//	private static double acl_Max;
//	private double acl;
	
	// storage-related:
	private static String folderName = ".";
	private static int pageSize = 1024;
	private static int nPages = 3;
	
	// insert
	private static void insertTuples(String fileName) {
		try {
			// read text file with the records to be inserted to storage:
			File file = new File(fileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.substring(1,line.length()-1);
				insertTuple(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// insert single tuple
	private static void insertTuple(String tuple) {
		
		// Check if we need to split:
		if (e.getACL()>e.getACL_Max()) {
			split();
		}
		
		String[] str = tuple.split(",");
		int key = Integer.parseInt(str[0]);
		Integer result = key%e.getM();
		
		// INSERT TO CHAIN INDEXED result
		// reading LtoP_file:			
		String jsonData = readFile("LtoP_File.json");
		try {
			JSONObject LtoP_map = new JSONObject(jsonData);
			long physicalAddress = LtoP_map.getInt(result.toString());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		System.out.println(Arrays.toString(str));
	}
	
	private static void split() {
		// TODO Auto-generated method stub
		
	}

	// delete
	
	private static void initializeFileSystem() {
		try {
			// reading LHConfig:			
			String jsonData = readFile("LHConfig.json");
			JSONObject jobj = new JSONObject(jsonData);
			
			String path_of_LtoPfile = jobj.getString("LtoP_File");
			
			// initializing LH file:
			e.setName(jobj.getString("FileName"));
			e.setHomePage("homePage");
			e.setLtoP_Map(path_of_LtoPfile);
			e.setM(jobj.getInt("M"));
			e.setsP(jobj.getInt("sP"));
			e.setNumOfPages(jobj.getInt("NumOfPages"));
			e.setACL_Min(jobj.getDouble("ACL_Min"));
			e.setACL_Min(jobj.getDouble("ACL_Max"));
			e.setACL_Min(jobj.getDouble("ACL"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private static void updateLHConfig() {
		double ACL_now = e.getACL();
		double ACL_Max_now = e.getACL_Max();
		double ACL_Min_now = e.getACL_Min();
		int NumOfPages_now = e.getNumOfPages();
		int sP_now = e.getsP();
		int M_now = e.getM();
		
		// write new, updated LHConfig:
		JSONObject jo = new JSONObject();
		try {
			jo.put("FileName", e.getName());
			jo.put("homePage", e.getHomePage());
			jo.put("LtoP_File", e.getLtoP_Map());
			jo.put("M", M_now);
			jo.put("sP", sP_now);
			jo.put("NumOfPages", NumOfPages_now);
			jo.put("ACL_Min", ACL_Min_now);
			jo.put("ACL_Max", ACL_Max_now);
			jo.put("ACL", ACL_now);
			PrintWriter pw = new PrintWriter("LHConfig.json");
			pw.write(jo.toString());
			pw.flush();
			pw.close();
		} catch (JSONException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void createLtoPfile(Map<Integer,Long> map, String path) {
		JSONObject jo = new JSONObject();
		try {
			for (Map.Entry<Integer,Long> entry : map.entrySet())
	            jo.put(entry.getKey().toString(), entry.getValue()); 
			PrintWriter pw = new PrintWriter(path);
			pw.write(jo.toString());
			pw.flush();
			pw.close();
		} catch (JSONException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void write_LtoPfile() { // or boolean
		// TODO writes to serialized logical-to-physical mapping file.
	}
	
	public static String readFile(String filename) {
	    String result = "";
	    try {
	        BufferedReader br = new BufferedReader(new FileReader(filename));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        result = sb.toString();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	private static void createStorage(String fName, int pSize, int numPages) {
		String folderName = fName;
		int pageSize = pSize;
		int nPages = numPages;
		try {
			MyStorage.CreateStorage(folderName, pageSize, nPages);
			System.out.println(
					"--Storage has been created successfully" + "with length " + MyStorage.PBFile.length());
			MyStorage.LoadStorage(folderName);
			System.out.println(
					"--Storage has been loaded successfully" + "with length " + MyStorage.PBFile.length());
			// reading LHConfig:			
			String jsonData = readFile("LHConfig.json");
			JSONObject jobj = new JSONObject(jsonData);
			int M_now = jobj.getInt("M");
			
			Map<Integer,Long> map = new HashMap<>();
			for(int i=0; i<M_now; i++) {
				long physicalAddress = MyStorage.AllocatePage();
				map.put(i, physicalAddress);
			}
			String path_of_LtoPfile = jobj.getString("LtoP_File");
			createLtoPfile(map,path_of_LtoPfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadStorage(String folderName) {
		try {
			MyStorage.LoadStorage(folderName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		initializeFileSystem();
		createStorage(folderName, pageSize, nPages);
		//updateLHConfig();
	}

}
