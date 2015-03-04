package bufmgr;

import global.*;
import java.util.ArrayList;
import java.util.Map.Entry;
public class CustomHashTable {
	private ArrayList<ArrayList<Entry<PageId, Integer>>> ht;
	//A, B, HTSIZE are arbitrary prime numbers
	private int a = 911;  
	private int b = 503;
	private final int HTSIZE = 1009;
	public CustomHashTable(){
		ht = new ArrayList<ArrayList<Entry<PageId, Integer>>>();
		
	}
	public Entry<PageId, Integer> get (PageId key){
		return null;
	}
	public void put (PageId pageId, Integer frameId){
		//If this is the first item in the bucket
		if(ht.get(hash(pageId)) == null){
			//create a new lsit
			ArrayList<Entry<PageId, Integer>> bucketList = new ArrayList<Entry<PageId, Integer>>(new Entry<PageId, Integer>(pageId, frameId ));
		}else{
			//add it to the end of the existing array
		        ht.get(hash(pageId)).put(new Entry<PageId, Integer>(pageId, frameId));
		}
	}
	public Entry<PageId, Integer> remove(Integer key){
		return null;
	}
	public Integer hash(PageId pageNum){
		return (a * pageNum.pid + b) % HTSIZE; 
	}

}
