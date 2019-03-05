package PBStorage;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.FileNotFoundException;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class PBStorage {
	public String PBFilesDirectory; // Folder where the storage exists
	public RandomAccessFile PBFile; // Actual storage file
	private long fileSize; // size of the storage
	public int pageSize; // size of each page
	private int bitMapSize; // Size of bitmap
	public int numPages; // No of pages in the storage
	public int numAllocated;
	public int numDeallocated;
	public int numRead;
	public int numWritten;

	/*
	 * CreateStorage - A storage consists of a random access file and a json file.
	 * This creates a folder where these two files are places. Initially, the random
	 * access file is created with a size equal to (num of pages)* (page size) +
	 * bitmap size + 16. 16 - The header of the storage which consists of pageSize
	 * and number of pages in the first 8 bytes. The remaining 8 are not being used.
	 * bitmap size - A bitmap whose size depends on the number of pages which keeps
	 * track of allocation of pages. (num of pages) * (page size) - Actual data
	 * present in the storage
	 */

	public void CreateStorage(String folderName, int pageSize, int nfiles) throws Exception {

		this.PBFilesDirectory = folderName;
		this.fileSize = nfiles * pageSize;
		this.pageSize = pageSize;
		String path = folderName + "/SimpleStorage";
		new File(folderName).mkdirs();
		this.numPages = (int) (this.fileSize / this.pageSize);

		this.bitMapSize = (int) Math.ceil(this.numPages / 8.0);

		if (this.bitMapSize % 16 != 0) {
			this.bitMapSize = (this.bitMapSize / 16 + 1) * 16;
		}
		// Allocating 16 extra bytes in the beginning for storage of parameters such as
		// pagesize.
		this.bitMapSize = this.bitMapSize + 16;
		this.PBFile = new RandomAccessFile(path, "rw");
		PBFile.seek(0);
		// Write the pagesize to the first 4 bytes in the file.
		PBFile.writeInt(pageSize);

		// Write number of pages to the next 4 bytes in the file
		PBFile.seek(4);
		PBFile.writeInt(this.numPages);

		PBFile.seek(0);

		this.fileSize = this.fileSize + this.bitMapSize;
		PBFile.setLength(fileSize);
		PBFile.seek(16);
		// Writing 0s to the randomaccessfile so that we physically claim the memory
		// required for the storage.
		// first writing for the bitmap
		for (int i = 16; i < this.bitMapSize; i++) {
			this.PBFile.write((byte) 0);
		}
		// Writing the file contents with 0s
		for (int i = this.bitMapSize; i < this.fileSize; i++) {
			PBFile.write((byte) 0);
		}
		// Create FileIndex.json in the folder
		String jsonFile = folderName + "/" + "FileIndex.json";
		File f = new File(jsonFile);
		f.createNewFile();

		String empty = "{\"PBFiles\":[]}";
		FileWriter fw;
		try {
			fw = new FileWriter(new File(jsonFile));
			fw.write(empty);
			fw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	/*
	 * Loading an existing storage. Set all the values like page size, folder Name ,
	 * no pf pages so that it can be used.
	 */
	public void LoadStorage(String folderName) throws Exception {
		String path = folderName + "/SimpleStorage";
		this.PBFile = new RandomAccessFile(path, "rw");

		this.fileSize = PBFile.length();

		// Read bytes 4 to 7 which we used to store the number of pages
		PBFile.seek(4);
		this.numPages = PBFile.readInt();
		this.PBFilesDirectory = folderName;

		// Read the first 4 bytes of the file which we used to store the page size while
		// creating the storage.
		PBFile.seek(0);
		this.pageSize = PBFile.readInt();

		this.bitMapSize = (int) Math.ceil(this.numPages / 8.0);

		if (this.bitMapSize % 16 != 0) {
			this.bitMapSize = (this.bitMapSize / 16 + 1) * 16;
		}
		this.bitMapSize = this.bitMapSize + 16;

		this.numAllocated = 0;
		this.numDeallocated = 0;
		this.numRead = 0;
		this.numWritten = 0;
	}

	public void UnloadStorage() {
		this.PBFile = null;
	}

	public void ReadPage(long n, byte[] buffer) throws Exception {
		// Go to the offset.
		long offset = n * this.pageSize + this.bitMapSize;
		PBFile.seek(offset);

		// read the page in buffer.
		PBFile.read(buffer);
		this.numRead++;
	}

	public void WritePage(long n, byte[] buffer) throws Exception {
		// Go to the required offset
		long offset = n * this.pageSize + this.bitMapSize;
		PBFile.seek(offset);

		// Write the buffer to the file.
		PBFile.write(buffer);
		this.numWritten++;
	}

	/*
	 * This function changes a bit in a byte and returns the int value of the new
	 * byte. Used in Deallocating a page where we set a bit to 0 if the page
	 * corresponding to it is deallocated
	 */
	private int WriteBitInAByte(int offset, int byteRead, int bitToBeWritten) {
		String binaryString = String.format("%8s", Integer.toBinaryString(byteRead & 0xFF)).replace(' ', '0');
		binaryString = binaryString.substring(0, offset) + bitToBeWritten + binaryString.substring(offset + 1);
		int byteWrite = Integer.parseInt(binaryString, 2);
		return byteWrite;
	}

	// Allocates a page and sets bit corresponding to that particular page to 1
	public long AllocatePage() throws Exception {

		PBFile.seek(16);
		// We use bits to keep track of allocated pages. The RandomAccessFile supports
		// only byte operations.
		// Thus, to allocate, we pick up bytes from the RandomAccessFile and then look
		// in the bits in the byte to see
		// if any of them is 0 or not.
		for (long i = 16; i < this.bitMapSize; i++) {
			int byteread;
			byteread = PBFile.read();
			// If the byte which is read has all 1's, then all the pages are allocated.
			// Don't look in that byte.
			if (byteread < 255) {
				PBFile.seek(i);

				// Convert the byte into a binary string.
				String binaryString = String.format("%8s", Integer.toBinaryString(byteread & 0xFF)).replace(' ', '0');

				// Look in the string to find the first 0 bit and set it to 1. Return that page
				// number
				for (int j = 0; j < 8; j++) {
					if (binaryString.charAt(j) != '1') {
						binaryString = binaryString.substring(0, j) + "1" + binaryString.substring(j + 1);
						int value = Integer.parseInt(binaryString, 2);
						char c = (char) value;
						PBFile.write(c);
						numAllocated++;
						// Return the page number only if the number of pages is more than the page we
						// are returning.
						if ((i - 16) * 8 + j < this.numPages)
							return ((i - 16) * 8 + j);
						else {

							System.out.println("Error in allocating a page");
							return -1;
						}

					}
				}
			}
		}
		System.out.println("Error in allocating a page");
		return -1;
	}

	// To deallocate a page n, we pick up the n/8th byte from the RandomAccessFile
	// and then change the corresponding bit in that byte to 0.
	public void DeAllocatePage(long n) throws Exception {
		PBFile.seek(16 + (n / 8));
		int byteRead = PBFile.read();
		int byteToBeWritten = WriteBitInAByte((int) (n % 8), byteRead, 0);
		PBFile.seek(16 + (n / 8));
		PBFile.write(byteToBeWritten);
		numDeallocated++;
	}

	public void printStats() {
		System.out.println("Number of pages Read:" + numRead + " " + "; Written:" + numWritten + " " + "; Allocated: "
				+ numAllocated + " " + "; Deallocated: " + numDeallocated);
	}

	/*
	 * Add a filename and other properties of files to json file created initially
	 * in create storage. To add additional properties , please add them in
	 * Entry.java file.
	 */
	public boolean addPBFileEntry(String fileName, String homePageNumber) {
		try {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(new FileReader(this.PBFilesDirectory + "/FileIndex.json"));
			JsonObject jsonObject = (JsonObject) obj;
			JsonArray file = (JsonArray) jsonObject.get("PBFiles");
			for (JsonElement f : file) {
				JsonObject temp = f.getAsJsonObject();
				String name = temp.get("name").getAsString();
				if (name.equalsIgnoreCase(fileName)) {
					return false;
				}
			}
			PBFileEntry e = new PBFileEntry();
			e.setName(fileName);
			e.setHomePage(homePageNumber);
			Gson gson = new Gson();
			List<PBFileEntry> entry = gson.fromJson(file, new TypeToken<List<PBFileEntry>>() {
			}.getType());
			entry.add(e);
			String json = "{\"PBFiles\":" + gson.toJson(entry) + "}";
			try {
				FileWriter f = new FileWriter(this.PBFilesDirectory + "/FileIndex.json", false);
				f.write(json);
				f.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}

	/* To remove an entry from json file */
	public boolean removePBFileEntry(String fileName) {
		boolean status = false;
		try {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(new FileReader(this.PBFilesDirectory + "/FileIndex.json"));
			JsonObject jsonObject = (JsonObject) obj;
			JsonArray file = (JsonArray) jsonObject.get("PBFiles");
			Gson gson = new Gson();
			List<PBFileEntry> entry = gson.fromJson(file, new TypeToken<List<PBFileEntry>>() {
			}.getType());
			for (int i = 0; i < entry.size(); i++) {
				PBFileEntry e = entry.get(i);
				String f = e.getName();
				if (f.equalsIgnoreCase(fileName)) {
					entry.remove(i);
					status = true;
					break;
				}
			}
			String json = "{\"PBFiles\":" + gson.toJson(entry) + "}";
			try {
				FileWriter f = new FileWriter(this.PBFilesDirectory + "/FileIndex.json", false);
				f.write(json);
				f.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return status;
	}

	/*
	 * To get information about the file from json
	 */
	public int getHomePage(String fileName) {
		try {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(new FileReader(this.PBFilesDirectory + "/FileIndex.json"));
			JsonObject jsonObject = (JsonObject) obj;
			JsonArray file = (JsonArray) jsonObject.get("PBFiles");
			for (JsonElement f : file) {
				JsonObject temp = f.getAsJsonObject();
				String name = temp.get("name").getAsString();
				if (name.equalsIgnoreCase(fileName)) {
					int homePageNumber = temp.get("homePage").getAsInt();
					return homePageNumber;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
