package Demo;

import PBStorage.PBStorage;

public class CreatePBStorageMain {
	public static void main(String args[]) throws Exception{
		PBStorage pbs1 = new PBStorage();
		pbs1.CreateStorage("myDisk1", 1024, 100);
	}
}