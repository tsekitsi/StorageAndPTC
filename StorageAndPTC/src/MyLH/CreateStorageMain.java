package MyLH;

import PBStorage.PBStorage;

public class CreateStorageMain {
	public static void main(String args[]) throws Exception{
		PBStorage pbs1 = new PBStorage();
		pbs1.CreateStorage(".", 1024, 100);
	}
}