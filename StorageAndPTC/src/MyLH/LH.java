package MyLH;

import PBStorage.PBFileEntry;
import PBStorage.PBStorage;
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
import org.json.*;

public class LH {
	
	// file-related:
	private static Integer m;
	private static Integer sP;
	private static Integer numOfPages;
	private static double acl_Min;
	private static double acl_Max;
	private double acl;
	
	// storage-related:
	private String folderName = ".";
	private int pageSize = 1024;
	
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
		String[] str = tuple.split(",");
		int key = Integer.parseInt(str[0]);
		int result = key%m;
		// INSERT TO CHAIN INDEXED result
		System.out.println(Arrays.toString(str));
	}
	
	// delete
	
	// load (?)
	
	private static void initializeFileSystem() {
		try {
			// reading the parameters from LHConfig:			
			String jsonData = readFile("LHConfig.json");
			JSONObject jobj = new JSONObject(jsonData);
			
			String path_of_LtoPfile = jobj.getString("LtoP_File");
			createLtoPfile(path_of_LtoPfile);
			
			// initialize LH file:
			PBFileEntry e = new PBFileEntry();
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

	private static void createLtoPfile(String path) {
		// TODO Auto-generated method stub
		
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
	
	public static void main(String[] args) throws Exception {
		initializeFileSystem();
		//insertTuple("Emp.txt");
		/*
		LH lh = new LH();
		lh.initializeFileSystem();
		ProducerIterator<byte []> textFileProducerIterator= new TextFileScanIterator();
		ConsumerIterator<byte []> relationConsumerIterator = new PutTupleInRelationIterator(35,".");
		PTCFramework<byte[],byte[]> fileToRelationFramework= new TextFileToRelationPTC(textFileProducerIterator, relationConsumerIterator);
		fileToRelationFramework.run();
		*/
	}

}
