package MyLH;

import PBStorage.PBFileEntry;
import PBStorage.PBStorage;

public class LH {
	
	// file-related:
	private Integer m = 3;
	private Integer sP = 0;
	private Integer numOfPages = 100;
	private double acl_Min = 1.25;
	private double acl_Max = 1.5;
	private double acl;
	
	// storage-related:
	private String folderName = ".";
	private int pageSize = 1024;
	
	//insert
	
	//delete
	
	//load (?)
	
	private void initializeFileSystem() throws Exception {
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
		// create PBStorage:
		//PBStorage pbs = new PBStorage();
		//pbs.CreateStorage(folderName, pageSize, numOfPages);
	}

	private void createLtoPfile(String path) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws Exception {
		LH lh = new LH();
		lh.initializeFileSystem();
	}

}
