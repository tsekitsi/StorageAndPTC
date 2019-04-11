package MyLH;

import PTCFramework.PTCFramework;
import PTCFramework.GetTupleFromRelationIterator;
import PTCFramework.TextFileScanIterator;
import PTCFramework.TextFileToRelationPTC;
import PTCFramework.ConsumerIterator;
import PTCFramework.ProducerIterator;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import MyPBStorage.LHFile;
import MyPBStorage.PBFileEntry;
import MyPBStorage.PBStorage;

public class LH {
	
	private static PBFileEntry e = new PBFileEntry();
	private static LHFile MyLHFile = new LHFile();
	static PBStorage MyStorage = new PBStorage();
	
	// storage-related:
	private static String folderName = ".";
	private static int pageSize = 1024;
	private static int nPages = 3;
	
	// insert
	private static void insertTuples(String fileName) {
		try {
			// read text file with the tuples to be inserted to storage:
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
	
	
	private static void insertTuple(String tuple) {
		
		// Check if we need to split:
		//if (e.getACL()>e.getACL_Max()) {
		//	split();
		//}
		
		String[] str = tuple.split(",");
		int key = Integer.parseInt(str[0]);
		Integer result = key%e.getM();
		
		// reading LtoP_file:			
		String jsonData = readFile("LtoP_File.json");
		try {
			JSONObject LtoP_map = new JSONObject(jsonData);
			long physicalAddress = LtoP_map.getInt(result.toString());
			// currentPage <-- read page at the physical address just calculated
			//while (isFull(currentPage)) {
				// currentPage <-- currentPage.next
			//}
			//appendTupleToPage(tuple, currentPage);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		System.out.println(Arrays.toString(str));
	}
	
	private static void split(HashMap<Integer, String> map) {
		try {
			// TODO Auto-generated method stub
			long nextPage = MyStorage.AllocatePage();
			if(nextPage == -1) {
				throw new RuntimeException();
			}
			byte[] new_page = new byte[MyStorage.pageSize];
			putNextPagePointer(new_page, -1);
			putNumberOfTuples(new_page, 1);
			MyLHFile.setNumOfPages(MyLHFile.getNumOfPages() + 1);
			map.put(MyLHFile.getM() + MyLHFile.getsP(), Long.toString(nextPage));
	
			List<byte[]> tuplelist = new ArrayList<>();
			List<byte[]> pages = new ArrayList<>();
			List<Long> pageAdd = new ArrayList<>();
			int pagesInChain = 0;
			boolean next = true;
	
			Long page = Long.parseLong(map.get(MyLHFile.getsP()));
			pageAdd.add(page);
	
			while (next) {
				byte[] tempBuffer = new byte[MyStorage.pageSize];
				MyStorage.ReadPage(page, tempBuffer);
				pages.add(tempBuffer);
				pagesInChain++;
	
				long np = getNextPage(tempBuffer);
				if (np == -1)
					next = false;
				else {
					page = np;
					next = true;
					pageAdd.add(np);
				}
	
				int nr = getNoOfTuples(tempBuffer);
				int start = 25, end = start + MyLHFile.getCurTupleLength();
				for (int i = 0; i < nr; i++) {
					byte[] tuple = new byte[MyLHFile.getCurTupleLength()];
					int l = 0;
					for (int j = start; j < end; j++)
						tuple[l++] = tempBuffer[j];
					start = start + MyLHFile.getTupleLength();
					end = start + MyLHFile.getCurTupleLength();
					tuplelist.add(tuple);
				}
	
			}
	
			for(byte[] p : pages) {
				for(int i = 25; i < p.length; i++)
					p[i] = 0;
				putNumberOfTuples(p, 0);
			}
			int entriesInChain = 0, tupInNewChain = 0;
			int q = 0;
			byte[] destPage = pages.get(q++);
			int tupInCurPage = 0;
			for (byte[] tuple : tuplelist) {
				PageID k = new PageID(tuple);
				int n = k.getKey() % (MyLHFile.getM() * 2);
				System.out.println("key->"+k.getKey()+" goTo->"+n);
				if (n == MyLHFile.getsP()) {
					entriesInChain++;
					if (tupInCurPage == MyLHFile.getTuplesPerPage()) {
						tupInCurPage = 0;
						destPage = pages.get(q++);
						writeTuple(tuple, destPage, 25 + (tupInCurPage * MyLHFile.getTupleLength()));
	
					}
					else {
						writeTuple(tuple, destPage, 25 + (tupInCurPage * MyLHFile.getTupleLength()));
						tupInCurPage++;
						putNumberOfTuples(destPage, tupInCurPage);
						
					}
				} else {
					if (tupInNewChain == MyLHFile.getTuplesPerPage()) {
						long pg = MyStorage.AllocatePage();
						if(pg == -1) {
							throw new RuntimeException();
						}
						MyLHFile.setNumOfPages(MyLHFile.getNumOfPages() + 1);
						putNextPagePointer(new_page, pg);
						MyStorage.WritePage(nextPage, new_page);
						new_page = new byte[MyStorage.pageSize];
						nextPage = pg;
						tupInNewChain = 1;
						putNextPagePointer(new_page, -1);
						putNumberOfTuples(new_page, tupInNewChain);
						writeTuple(tuple, new_page, 25 + (tupInNewChain * MyLHFile.getTupleLength()));
	
					} else {
						writeTuple(tuple, new_page, 25 + (tupInNewChain * MyLHFile.getTupleLength()));
	
						tupInNewChain++;
						putNumberOfTuples(new_page, tupInNewChain);
					}
				}
				
			}
			MyStorage.WritePage(nextPage, new_page);
			int pagesNeeded = (int) Math.ceil(entriesInChain / 7.0);
			if(pagesNeeded != pages.size()) {
				putNextPagePointer(pages.get(pagesNeeded - 1), -1);
			}
			
			for (int i = pagesNeeded; i < pageAdd.size(); i++) {
				MyStorage.DeAllocatePage(pageAdd.get(i));
				MyLHFile.setNumOfPages(MyLHFile.getNumOfPages() - 1);
			}
	
			for (int i = 0; i < pagesNeeded; i++) {
				MyStorage.WritePage(pageAdd.get(i), pages.get(i));
			}
		} catch (RuntimeException e1) {
			System.out.println("Insufficient Storage");
		} catch (Exception e2) {
		}
	}

	// delete
	
	public static void initializeFileSystem(byte[] tuple) throws Exception {
		PageID key = new PageID(tuple);
		try {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(new FileReader(MyStorage.PBFilesDirectory + "/FileIndex.json"));
			JsonObject jsonObject = (JsonObject) obj;
			JsonArray file = (JsonArray) jsonObject.get("PBFiles");
			for (JsonElement f : file) {
				JsonObject temp = f.getAsJsonObject();
				MyLHFile.setM(temp.get("M").getAsInt());
				MyLHFile.setsP(temp.get("sP").getAsInt());
				MyLHFile.setNumOfPages(temp.get("NumOfPages").getAsInt());
				MyLHFile.setACL_Min(temp.get("ACL_Min").getAsFloat());
				MyLHFile.setACL_MAX(temp.get("ACL_MAX").getAsFloat());
				MyLHFile.setFileName(temp.get("fileName").getAsString());
				MyLHFile.setHomePage(temp.get("homePage").getAsInt());
				MyLHFile.setLtoP_File(temp.get("LtoP_File").getAsString());
				MyLHFile.setTupleLength(100);
				MyLHFile.setTuplesPerPage(7);
				MyLHFile.setCurTupleLength(tuple.length);
				HashMap<Integer, String> hm = (HashMap) MyStorage
						.deserializeMap(MyStorage.PBFilesDirectory + "/" + MyLHFile.getLtoP_File());
				int n = key.getKey() % MyLHFile.getM();
				n = n < MyLHFile.getsP() ? key.getKey() % (MyLHFile.getM() * 2) : n;
				long n1 = Long.parseLong(hm.get(n));
				int status = addTupleToPage(tuple, n1, hm);
				boolean configUpdate = MyStorage.removePBFileEntry(MyLHFile.getFileName());
				configUpdate = upadteLHFile(MyLHFile);
				MyStorage.serializeMap(MyStorage.PBFilesDirectory+"/LtoP.ser", hm);
				

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean upadteLHFile(LHFile myLHFile) {
		try {
			JsonParser parser = new JsonParser();
			Object obj = parser.parse(new FileReader(MyStorage.PBFilesDirectory + "/FileIndex.json"));
			JsonObject jsonObject = (JsonObject) obj;
			JsonArray file = (JsonArray) jsonObject.get("PBFiles");
			for (JsonElement f : file) {
				JsonObject temp = f.getAsJsonObject();
				String name = temp.get("fileName").getAsString();
				if (name.equalsIgnoreCase(myLHFile.getFileName())) {
					return false;
				}
			}

			Gson gson = new Gson();
			List<LHFile> entry = gson.fromJson(file, new TypeToken<List<LHFile>>() {
			}.getType());
			entry.add(myLHFile);
			String json = "{\"PBFiles\":" + gson.toJson(entry) + "}";
			try {
				FileWriter f = new FileWriter(MyStorage.PBFilesDirectory + "/FileIndex.json", false);
				f.write(json);
				f.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		return true;
	}

//	private static void updateLHFile() {
//		double ACL_now = e.getACL();
//		double ACL_Max_now = e.getACL_Max();
//		double ACL_Min_now = e.getACL_Min();
//		int NumOfPages_now = e.getNumOfPages();
//		int sP_now = e.getsP();
//		int M_now = e.getM();
//		
//		// write new, updated LHConfig:
//		JSONObject jo = new JSONObject();
//		try {
//			jo.put("FileName", e.getName());
//			jo.put("homePage", e.getHomePage());
//			jo.put("LtoP_File", e.getLtoP_Map());
//			jo.put("M", M_now);
//			jo.put("sP", sP_now);
//			jo.put("NumOfPages", NumOfPages_now);
//			jo.put("ACL_Min", ACL_Min_now);
//			jo.put("ACL_Max", ACL_Max_now);
//			jo.put("ACL", ACL_now);
//			PrintWriter pw = new PrintWriter("LHConfig.json");
//			pw.write(jo.toString());
//			pw.flush();
//			pw.close();
//		} catch (JSONException | FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}

	private static void createLtoPfile(Map<Integer,Long> map, String path) {
		JSONObject jo = new JSONObject();
		try {
			for (Map.Entry<Integer,Long> entry : map.entrySet())
	            jo.put(entry.getKey().toString(), entry.getValue()); 
			PrintWriter pw = new PrintWriter(path);
			pw.write(jo.toString());
			pw.flush();
			pw.close();
		} catch (JSONException | FileNotFoundException e) {
			e.printStackTrace();
		}
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
	
//	private static void create_Storage(String fName, int pSize, int numPages) {
//		String folderName = fName;
//		int pageSize = pSize;
//		int nPages = numPages;
//		try {
//			MyStorage.CreateStorage(folderName, pageSize, nPages);
//			System.out.println(
//					"--Storage has been created successfully" + "with length " + MyStorage.PBFile.length());
//			MyStorage.LoadStorage(folderName);
//			System.out.println(
//					"--Storage has been loaded successfully" + "with length " + MyStorage.PBFile.length());
//			// reading LHConfig:			
//			String jsonData = readFile("LHConfig.json");
//			JSONObject jobj = new JSONObject(jsonData);
//			int M_now = jobj.getInt("M");
//			
//			Map<Integer,Long> map = new HashMap<>();
//			for(int i=0; i<M_now; i++) {
//				long physicalAddress = MyStorage.AllocatePage();
//				map.put(i, physicalAddress);
//			}
//			String path_of_LtoPfile = jobj.getString("LtoP_File");
//			createLtoPfile(map,path_of_LtoPfile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
//	private void loadStorage(String folderName) {
//		try {
//			MyStorage.LoadStorage(folderName);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static long byteArrayToLong(byte[] byteArray) {
		long value = 0;
		for (int i = 0; i < byteArray.length; i++) {

			value += ((long) byteArray[i] & 0xffL) << (8 * (7 - i));
		}
		return value;
	}
	
	public static int byteArrayToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
	public static byte[] longToByteArray(long value) {
		byte[] array = new byte[8];
		for (int i = 7; i >= 0; i--) {
			array[7 - i] = (byte) (value >> i * 8);
		}
		return array;
	}
	
	public static final byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
	
	public static long getNextPage(byte[] page) {
		return byteArrayToLong(Arrays.copyOfRange(page, 0, 8));
	}

	public static int getNoOfTuples(byte[] page) {
		return byteArrayToInt(Arrays.copyOfRange(page, 8, 12));
	}
	
	public static void putNextPagePointer(byte[] tempBuffer, long n) {
		// First 24 bytes for header
		// First 8 bytes for next page pointer (initially -1)
		for (int j = 0; j < longToByteArray(n).length; j++) {
			tempBuffer[j] = longToByteArray(n)[j];
		}

	}

	public static void putNumberOfTuples(byte[] tempBuffer, int n) {
		// next 4 bytes for number of tuples (initially 0)
		int k = 0;
		for (int j = 8; j < 8 + intToByteArray(n).length; j++) {
			tempBuffer[j] = intToByteArray(n)[k];
			k++;
		}
	}
	
	public static void writeTuple(byte[] tuple, byte[] buffer, int offset) {
		int l = 0;
		for (int i = offset; i < offset + tuple.length; i++) {
			buffer[i] = tuple[l++];
		}
	}
	
	public static int addTupleToPage(byte[] tuple, long n, HashMap<Integer, String> map) {
		try {
			System.out.println("pageID->" + byteArrayToInt(Arrays.copyOfRange(tuple, 0, 4)));
			byte[] tempBuffer = new byte[MyStorage.pageSize];
			if (n == -1) {
				System.out.println("Error Reading File");
			}
			MyStorage.ReadPage(n, tempBuffer);
			byte[] no_of_tuples = new byte[4];
			int l = 0;
			for (int i = 8; i < 12; i++) {
				no_of_tuples[l++] = tempBuffer[i];
			}
			int r = byteArrayToInt(no_of_tuples);
			if (r == MyLHFile.getTuplesPerPage()) {
				long next = getNextPage(tempBuffer);
				long prev = n;
				while(next != -1) {
					prev = next;
					MyStorage.ReadPage(next, tempBuffer);
					next = getNextPage(tempBuffer);
				}
				r = getNoOfTuples(tempBuffer);
				if(r == MyLHFile.getTuplesPerPage()) {
					long nextPage = MyStorage.AllocatePage();
					if(nextPage == -1) {
						throw new RuntimeException();
					}
					byte[] new_page = new byte[MyStorage.pageSize];
					putNextPagePointer(new_page, -1);
					putNumberOfTuples(new_page, 1);
					putNextPagePointer(tempBuffer, nextPage);
					writeTuple(tuple, new_page, 25 + (0 * MyLHFile.getTupleLength()));
					System.out.println("r->"+r+"offset"+(25 + (0 * MyLHFile.getTupleLength())));
					MyStorage.WritePage(nextPage, new_page);
					MyStorage.WritePage(prev, tempBuffer);
					MyLHFile.setNumOfPages(MyLHFile.getNumOfPages() + 1);
				}
				else {
					putNumberOfTuples(tempBuffer, r + 1);
					writeTuple(tuple, tempBuffer, 25 + (r * MyLHFile.getTupleLength()));
					MyStorage.WritePage(prev, tempBuffer);
				}
				

			} else {

				putNumberOfTuples(tempBuffer, r + 1);
				writeTuple(tuple, tempBuffer, 25 + (r * MyLHFile.getTupleLength()));
				MyStorage.WritePage(n, tempBuffer);
				l = 0;
				for (int i = 8; i < 12; i++) {
					no_of_tuples[l++] = tempBuffer[i];
				}
				r = byteArrayToInt(no_of_tuples);
			}
			float acl = (float)MyLHFile.getNumOfPages() / ((float)(MyLHFile.getM() + MyLHFile.getsP()));
			MyLHFile.setACL(acl);
			System.out.println(acl);
			while (acl > MyLHFile.getACL_MAX()) {
				System.out.println("Existing"+acl);
				split(map);
				if (MyLHFile.getsP() < MyLHFile.getM() - 1) {
					MyLHFile.setsP(MyLHFile.getsP() + 1);
				} else {
					MyLHFile.setM(MyLHFile.getM() * 2);
					MyLHFile.setsP(0);
				}
				acl = (float)MyLHFile.getNumOfPages() / ((float)(MyLHFile.getM() + MyLHFile.getsP()));
				MyLHFile.setACL(acl);
				System.out.println("after"+acl);
			}
			// write the tuple into the page
		} catch(RuntimeException e) {
			System.out.println("Insufficient Storage");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return -1;
	}
	
	public static void printPage(byte[] buffer) {
		long nextPage = byteArrayToLong(Arrays.copyOfRange(buffer, 0, 8));
		int noTuples = byteArrayToInt(Arrays.copyOfRange(buffer, 8, 12));
		int start = 25, end = start + MyLHFile.getCurTupleLength();
		System.out.println("NextPage:" + nextPage);
		System.out.println("Number of Tuples:" + noTuples);
		System.out.println("Tuples:");
		for (int i = 0; i < noTuples; i++) {
			System.out.println(byteArrayToInt(Arrays.copyOfRange(buffer, start, start + 4)));
			start = start + MyLHFile.getTupleLength();
			end = start + MyLHFile.getCurTupleLength();
		}
	}
	
	public static void printStorage(HashMap<Integer, String> map) {
		try {
			for (Integer key : map.keySet()) {
				System.out.println();
				System.out.println("Chain:" + key);
				
				long cur = Long.parseLong(map.get(key));
				long next = cur;
				while (next != -1) {
					cur = next;
					byte[] buffer = new byte[MyStorage.pageSize];
					MyStorage.ReadPage(cur, buffer);
					printPage(buffer);
					next = getNextPage(buffer);
					System.out.println("next"+next);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	public static void main(String[] args) {
		System.out.println("Enter your choice of operations below:");
		System.out.println("Available operations are:");
		System.out.println("1. CreateAndLoad folderPath pageSize No.of Pages;");
		System.out.println("2. LoadAndStore_LtoP folderPath");
		System.out.println("3. LoadStorageAndGet_LtoP folderPath");
		System.out.println("4. LoadAndWrite folderPath");
		PBStorage my_storage = new PBStorage();
		boolean stop = false;
		while (!stop) {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			String command = scanner.nextLine();
			String[] commands = command.split(" ");
			if (commands[0].equalsIgnoreCase("CreateAndLoad")) {
				String folder_name = commands[1];
				int page_size = Integer.parseInt(commands[2]);
				int page_num = Integer.parseInt(commands[3]);
				try {
					my_storage.createStorage(folder_name, page_size, page_num);
					System.out.println(
							"Storage has been created successfully" + "with length " + my_storage.PBFile.length());
					my_storage.loadStorage(folder_name);
					System.out.println(
							"Storage has been loaded successfully" + "with length " + my_storage.PBFile.length());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}else if (commands[0].equalsIgnoreCase("LoadStorageAndGet_LtoP")) {
				// Creating a LtoP mapping file
				Map<Integer, String> LtoP = new HashMap<Integer, String>();
				try {
					
					String folderName = commands[1];
					// Loading Storage
					my_storage.loadStorage(folderName);
					my_storage.printStats();
					my_storage.addLHConfigEntry("pages", -1, "LtoP.ser", (float)1.50, (float)1.50, (float)1.25, 3, 0, 3, 0);
					
					//Allocating M pages, initially M =3
					int m = 3;
					byte[] tempBuffer;
					for(int i = 0; i < m; i++) {
						long page = my_storage.AllocatePage();
						if(page == -1) {
							throw new RuntimeException();
						}
						tempBuffer = new byte[my_storage.pageSize];
						//First 24 bytes for header and 8 bytes for next page pointer, initially -1
						int counter = 0;
						for(int j = 0; j < longToByteArray(-1).length; j++) {
							tempBuffer[j] = longToByteArray(-1)[j];
							counter = j;
						}
						//4 bytes for number of records, initially 0
						byte[] test = new byte[4];
						int l = 0, k = 0;
						for(int j = counter + 1; j < 8 + LH.intToByteArray(0).length; j++) {
							tempBuffer[j] = LH.intToByteArray(0)[k++];
							counter = j;
							test[l] = LH.intToByteArray(0)[l];
							l++;
						}
						LtoP.put(i, Long.toString(page));
						my_storage.WritePage(page, tempBuffer);
					}
					FileOutputStream fos = new FileOutputStream(my_storage.PBFilesDirectory+"/LtoP.ser");
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(LtoP);
					oos.close();
					fos.close();
					System.out.println("Serialized HashMap data is saved in LtoP.ser");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch(RuntimeException err) {
					System.out.println("Storage Insufficient");
				}catch (Exception e) {
					e.printStackTrace();
				}
			} else if(commands[0].equalsIgnoreCase("LoadAndWrite")) {
				
				String folderName = commands[1];
				try {
					// Load Storage
					my_storage.loadStorage(folderName);
					my_storage.printStats();
					String filename = my_storage.getLtoPFileName("LtoPFile");
					System.out.println(my_storage.PBFilesDirectory+"/"+filename);
					HashMap<Integer, String> LtoP = my_storage.deserializeMap(my_storage.PBFilesDirectory+"/"+filename);
					System.out.println(Arrays.asList(LtoP));
					LH.printStorage(LtoP);
				}catch(Exception e) {
					e.printStackTrace();
				}
				
			} else if(commands[0].equalsIgnoreCase("Load_Write")) {
				String folderName = commands[1];
				try {
					// Load Storage
					my_storage.loadStorage(folderName);
					my_storage.printStats();
					LH.MyStorage = my_storage;
					ProducerIterator<byte []> textFileProducerIterator= new TextFileScanIterator();
					ConsumerIterator<byte []> relationConsumerIterator = new GetTupleFromRelationIterator(65,"myDisk1");
					PTCFramework<byte[],byte[]> fileToRelationFramework= new TextFileToRelationPTC(textFileProducerIterator, relationConsumerIterator);
					fileToRelationFramework.run();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("Invalid Command! Please try again!!");
				stop = true;
			}
		}
	}

}

class PageID {
	byte[] tuple;
	int id;
	public PageID(byte[] tuple) {
		this.tuple = new byte[tuple.length];
		for(int i = 0; i < tuple.length; i++)
			this.tuple[i] = tuple[i];
		id =  ByteBuffer.wrap(Arrays.copyOfRange(tuple, 0, 4)).getInt();
		
	}
	public int getKey() {
		return id;
	}
}

