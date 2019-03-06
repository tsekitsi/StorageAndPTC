package PBStorage;

public class PBFileEntry{
		private String name;
		private String homePage;
		private String LtoP_Map;
		private int M;
		private int sP;
		private int NumOfPages;
		private double ACL_Min;
		private double ACL_Max;
		private double ACL;
		
		public void setName(String name){
			this.name = name;
		}
		
		public String getName(){
			return this.name;
		}
		
		public void setHomePage(String homePage){
			this.homePage = homePage;
		}
		
		public String getHomePage(){
			return this.homePage;
		}

		public String getLtoP_Map() {
			return this.LtoP_Map;
		}

		public void setLtoP_Map(String ltoP_Map) {
			this.LtoP_Map = ltoP_Map;
		}

		public int getM() {
			return this.M;
		}

		public void setM(int m) {
			this.M = m;
		}

		public int getsP() {
			return sP;
		}

		public void setsP(int sP) {
			this.sP = sP;
		}

		public int getNumOfPages() {
			return this.NumOfPages;
		}

		public void setNumOfPages(int numOfPages) {
			this.NumOfPages = numOfPages;
		}

		public double getACL_Min() {
			return this.ACL_Min;
		}

		public void setACL_Min(double aCL_Min) {
			this.ACL_Min = aCL_Min;
		}

		public double getACL_Max() {
			return ACL_Max;
		}

		public void setACL_Max(double aCL_Max) {
			this.ACL_Max = aCL_Max;
		}

		public double getACL() {
			return this.ACL;
		}

		public void setACL(double aCL) {
			this.ACL = aCL;
		}	
		
}