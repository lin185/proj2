package bufmgr;

import global.*;
import java.util.ArrayList;


public class CustomHashTable {
	private ArrayList<ArrayList<Tuple>> ht;
	//A, B, HTSIZE are arbitrary prime numbers
	private int a = 911;  
	private int b = 503;
	private final int HTSIZE = 1009;
	public CustomHashTable(){
		ht = new ArrayList<ArrayList<Tuple>>();
		
	}
	public Tuple get (PageId key){
		return null;
	}
	public void put (PageId pageId, Integer frameId){
		//If this is the first item in the bucket
		if(ht.get(hash(pageId)) == null){
			//create a new lsit
			ArrayList<Tuple> bucketList = new ArrayList<Tuple>();
			bucketList.add(new Tuple (pageId, frameId ));
		}else{
			//add it to the end of the existing array
		        ht.get(hash(pageId)).add(new Tuple(pageId, frameId));
		}
	}
	public Tuple remove(Integer key){
		return null;
	}
	public Integer hash(PageId pageNum){
		return (a * pageNum.pid + b) % HTSIZE; 
	}

}
