package MyPBStorage;

public class LHFile {
	
	private String fileName;
	private int homePage;
	private String LtoP_File;
	private int M;
	private int sP;
	private int NumOfPages;
	private float ACL_Min;
	private float ACL_MAX;
	private float ACL;
	
	public LHFile() {

	}
	
	private int recordLength;
	
	private int curRecordLength;
	
	private int recordsPerPage;

	public int getCurTupleLength() {
		return curRecordLength;
	}


	public void setCurTupleLength(int curRecordLength) {
		this.curRecordLength = curRecordLength;
	}
	
	
	public int getTuplesPerPage() {
		return recordsPerPage;
	}


	public void setTuplesPerPage(int recordsPerPage) {
		this.recordsPerPage = recordsPerPage;
	}


	public int getTupleLength() {
		return recordLength;
	}


	public void setTupleLength(int recordLength) {
		this.recordLength = recordLength;
	}


	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getHomePage() {
		return homePage;
	}

	public void setHomePage(int homePage) {
		this.homePage = homePage;
	}

	public String getLtoP_File() {
		return LtoP_File;
	}

	public void setLtoP_File(String ltoP_File) {
		LtoP_File = ltoP_File;
	}

	public int getM() {
		return M;
	}

	public void setM(int m) {
		M = m;
	}

	public int getsP() {
		return sP;
	}

	public void setsP(int sP) {
		this.sP = sP;
	}

	public int getNumOfPages() {
		return NumOfPages;
	}

	public void setNumOfPages(int numOfPages) {
		NumOfPages = numOfPages;
	}

	public float getACL_Min() {
		return ACL_Min;
	}

	public void setACL_Min(float aCL_Min) {
		ACL_Min = aCL_Min;
	}

	public float getACL_MAX() {
		return ACL_MAX;
	}

	public void setACL_MAX(float aCL_MAX) {
		ACL_MAX = aCL_MAX;
	}

	public float getACL() {
		return ACL;
	}

	public void setACL(float aCL) {
		ACL = aCL;
	}

}
