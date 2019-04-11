package PTCFramework;

import java.nio.ByteBuffer;

import MyLH.LH;

public class GetTupleFromRelationIterator implements ConsumerIterator <byte []>{
	
	public GetTupleFromRelationIterator(int tuplelength,String fileName) {

	}
	
	public void open(){

	}
	
	public void close(){
	}
	
	public boolean hasNext(){
		return true;
	}
	
	public byte [] next(){
		return null;
	}
	public void next(byte [] tuple) throws Exception{
		LH.initializeFileSystem(tuple);
	}

	@Override
	public void remove() {
		
	}
}