package PBStorage;

public class PBFileEntry{
		private String name;
		private String homePage;
		private String LtoP_Map;
		private Integer M;
		private Integer sP;
		private Integer NumOfPages;
		private Float ACL_Min;
		private Float ACL_Max;
		private Float ACL;
		
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
			return LtoP_Map;
		}

		public void setLtoP_Map(String ltoP_Map) {
			LtoP_Map = ltoP_Map;
		}

		public Integer getM() {
			return M;
		}

		public void setM(Integer m) {
			M = m;
		}

		public Integer getsP() {
			return sP;
		}

		public void setsP(Integer sP) {
			this.sP = sP;
		}

		public Integer getNumOfPages() {
			return NumOfPages;
		}

		public void setNumOfPages(Integer numOfPages) {
			NumOfPages = numOfPages;
		}

		public Float getACL_Min() {
			return ACL_Min;
		}

		public void setACL_Min(Float aCL_Min) {
			ACL_Min = aCL_Min;
		}

		public Float getACL_Max() {
			return ACL_Max;
		}

		public void setACL_Max(Float aCL_Max) {
			ACL_Max = aCL_Max;
		}

		public Float getACL() {
			return ACL;
		}

		public void setACL(Float aCL) {
			ACL = aCL;
		}	
		
}