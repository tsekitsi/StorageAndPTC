package PBStorage;

import java.util.Scanner;

public class TestPBStorage {

	

	public static byte[] longToByteArray(long value) {
		byte[] array = new byte[8];
		for (int i = 7; i >= 0; i--) {
			array[7 - i] = (byte) (value >> i * 8);
		}
		return array;
	}

	public static long byteArrayToLong(byte[] byteArray) {
		long value = 0;
		for (int i = 0; i < byteArray.length; i++) {

			value += ((long) byteArray[i] & 0xffL) << (8 * (7 - i));
		}
		return value;
	}

	public static void printBufferContent(byte[] tempBuffer) {
		String print = "[ ";
		for (int i = 0; tempBuffer[i] != 'x'; i++) {
			if (i >= 8) {
				print = print + " " + new String(new byte[] { tempBuffer[i] }) + " ";
			} else {
				print = print + " " + tempBuffer[i] + " ";
			}

		}
		print = print + "]";
		System.out.println(print);
	}

	public static void main(String[] args) {
		// create storage.
		PBStorage MyStorage = new PBStorage();
		boolean stop = false;
		while (!stop) {
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(System.in);
			String command = sc.nextLine();
			String[] commands = command.split(" ");
			if (commands[0].equalsIgnoreCase("list") && commands[1].equalsIgnoreCase("commands")) {
				System.out.println("Command List---");
				System.out.println("CreateAndLoad folderPath pageSize No.ofFiles;");
				System.out.println("LoadAndWrite folderPath;");
				System.out.println("LoadAndRead folderPath;");
				System.out.println("LoadAndDeallocate folderPath;");

			} else if (commands[0].equalsIgnoreCase("CreateAndLoad")) {
				String folderName = commands[1];
				int pageSize = Integer.parseInt(commands[2]);
				int nPages = Integer.parseInt(commands[3]);
				try {
					MyStorage.CreateStorage(folderName, pageSize, nPages);
					System.out.println(
							"--Storage has been created successfully" + "with length " + MyStorage.PBFile.length());
					MyStorage.LoadStorage(folderName);
					System.out.println(
							"--Storage has been loaded successfully" + "with length " + MyStorage.PBFile.length());
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (commands[0].equalsIgnoreCase("LoadAndWrite")) {

				try {

					String folderName = commands[1];
					// Load Storage
					MyStorage.LoadStorage(folderName);
					byte[] tempBuffer = new byte[MyStorage.pageSize];
					// asks for a Page numbers
					long firstPage = MyStorage.AllocatePage();
					long secondPage = MyStorage.AllocatePage();
					long thirdPage = MyStorage.AllocatePage();

					// asks for a Page number
					System.out.println("Newpage numbers: " + firstPage + " " + secondPage + " " + thirdPage);
					MyStorage.printStats();
					MyStorage.addPBFileEntry("pages", Long.toString(firstPage));
					// FRIST PAGE WRITTEN
					int counter = 0;
					for (int i = 0; i < tempBuffer.length; i++) {
						tempBuffer[i] = 'x';
					}
					for (int i = 0; i < longToByteArray(secondPage).length; i++) {
						tempBuffer[i] = longToByteArray(secondPage)[i];
						counter = i;
					}
					tempBuffer[counter + 1] = 'A';
					tempBuffer[counter + 2] = 'B';
					tempBuffer[counter + 3] = 'C';

					printBufferContent(tempBuffer);
					MyStorage.WritePage(firstPage, tempBuffer);

					// SECOND PAGE WRITTEN
					counter = 0;
					for (int i = 0; i < tempBuffer.length; i++) {
						tempBuffer[i] = 'x';
					}
					for (int i = 0; i < longToByteArray(thirdPage).length; i++) {
						tempBuffer[i] = longToByteArray(thirdPage)[i];
						counter = i;
					}
					tempBuffer[counter + 1] = 'D';
					tempBuffer[counter + 2] = 'E';
					tempBuffer[counter + 3] = 'F';
					printBufferContent(tempBuffer);
					MyStorage.WritePage(secondPage, tempBuffer);

					// THIRD PAGE WRITTEN
					counter = 0;
					for (int i = 0; i < tempBuffer.length; i++) {
						tempBuffer[i] = 'x';
					}
					for (int i = 0; i < longToByteArray(0).length; i++) {
						tempBuffer[i] = longToByteArray(0)[i];
						counter = i;
					}
					tempBuffer[counter + 1] = 'G';
					tempBuffer[counter + 2] = 'H';
					tempBuffer[counter + 3] = 'I';
					printBufferContent(tempBuffer);
					MyStorage.WritePage(thirdPage, tempBuffer);

					// 3 writes & 3 allocation
					MyStorage.printStats();

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (commands[0].equalsIgnoreCase("LoadAndRead")) {
				try {

					String folderName = commands[1];
					MyStorage.LoadStorage(folderName);
					byte[] tempBuffer = new byte[MyStorage.pageSize];
					long firstPage = MyStorage.getHomePage("pages");
					if (firstPage == -1) {
						System.out.println("file does not exist");
						continue;
					}
					MyStorage.ReadPage(firstPage, tempBuffer);
					printBufferContent(tempBuffer);
					byte[] part = new byte[8];
					for (int i = 0; i < 8; i++) {
						part[i] = tempBuffer[i];
					}
					long nextPage = byteArrayToLong(part);

					while (nextPage != 0) {
						MyStorage.ReadPage(nextPage, tempBuffer);
						printBufferContent(tempBuffer);
						for (int i = 0; i < 8; i++) {
							part[i] = tempBuffer[i];
						}
						nextPage = byteArrayToLong(part);
					}
					MyStorage.printStats();
				} catch (Exception e) {

					e.printStackTrace();

				}

			} else if (commands[0].equalsIgnoreCase("LoadAndDeallocate")) {
				try {

					String folderName = commands[1];
					MyStorage.LoadStorage(folderName);
					byte[] tempBuffer = new byte[MyStorage.pageSize];
					long firstPage = MyStorage.getHomePage("pages");
					MyStorage.ReadPage(firstPage, tempBuffer);

					byte[] part = new byte[8];
					for (int i = 0; i < 8; i++) {
						part[i] = tempBuffer[i];
					}
					long nextPage = byteArrayToLong(part);
					MyStorage.DeAllocatePage(firstPage);
					while (nextPage > 0) {
						MyStorage.ReadPage(nextPage, tempBuffer);
						MyStorage.DeAllocatePage(nextPage);
						for (int i = 0; i < 8; i++) {
							part[i] = tempBuffer[i];
						}
						nextPage = byteArrayToLong(part);
					}
					MyStorage.removePBFileEntry("pages");
					MyStorage.printStats();
				} catch (Exception e) {

					e.printStackTrace();

				}

			}
			else {
				System.out.println("Invalid Command... Quitting job");
				stop = true;
			}
		}

	}
}
