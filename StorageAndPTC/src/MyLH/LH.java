package MyLH;

import PBStorage.PBFileEntry;
import PBStorage.PBStorage;
import PTCFramework.ConsumerIterator;
import PTCFramework.PTCFramework;
import PTCFramework.ProducerIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class LH {
	
	// file-related:
	private static Integer m = 3;
	private Integer sP = 0;
	private Integer numOfPages = 100;
	private double acl_Min = 1.25;
	private double acl_Max = 1.5;
	private double acl;
	
	// storage-related:
	private String folderName = ".";
	private int pageSize = 1024;
	
	// insert
	private static void insertTuples(String fileName) {
		try {
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
		System.out.println(Arrays.toString(str));
	}
	
	// delete
	
	// load (?)
	
	private void initializeFileSystem() {
		// initialize LH file:
		String path_of_LtoPfile = "";
		createLtoPfile(path_of_LtoPfile);
		PBFileEntry e = new PBFileEntry();
		e.setName("MyLHFile");
		e.setHomePage("-1");
		e.setLtoP_Map(path_of_LtoPfile);
		e.setM(m);
		e.setsP(sP);
		e.setNumOfPages(numOfPages);
		e.setACL_Min(acl_Min);
		e.setACL_Max(acl_Max);
	}

	private void createLtoPfile(String path) {
		// TODO Auto-generated method stub
		
	}
	
	private void write_LtoPfile() { // or boolean
		// TODO writes to serialized logical-to-physical mapping file.
	}
	
	public static void main(String[] args) throws Exception {
		insertTuple("Emp.txt");
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
