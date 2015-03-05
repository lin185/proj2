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
		ht = new ArrayList<ArrayList<Tuple>>(HTSIZE);
		for(int i = 0; i < HTSIZE; i++){
			ht.add(i, new ArrayList<Tuple>());
		}
	}
	/* get index number for where target tuple is stored
	** Return -1 if tuple not found
	*/
	public int  getTupleIndex(PageId target, ArrayList<Tuple> bucketList){
		int targetIndex = 0;
		System.out.println(target.pid);
		if(bucketList.isEmpty())
			return -1;
		for(Tuple t: bucketList){
			System.out.println("t.getPageID: " + t.getPageId());
			if(t.getPageId() == target){
				break;			
			}		
			targetIndex++;
		}
		
		return targetIndex > bucketList.size() ? -1 : targetIndex;
	}
	/* get the target Tuple
	** Return NULL if tuple not found
	*/
	public Tuple get (PageId pageId){
		//Find the right bucketList
		System.out.println(hash(pageId));
		ArrayList<Tuple> bucketList = ht.get(hash(pageId));
		int index = getTupleIndex(pageId, bucketList);
		System.out.println("INDEX: " + index);
		return  index == -1 ? null : bucketList.get(index);
	}
	/* put new tuple into the correct bucket */
	public void put (PageId pageId, Integer frameId){
		//If this is the first item in the bucket
		//if(ht.get(hash(pageId)) == null){
			//create a new lsit
		//	ArrayList<Tuple> bucketList = new ArrayList<Tuple>();
		//	bucketList.add(new Tuple (pageId, frameId ));
	//	}else{
			//add it to the end of the existing array
		        ht.get(hash(pageId)).add(new Tuple(pageId, frameId));
	//	}
	}
	/* remove the target tuple from the bucketList */
	public Tuple remove(PageId pageId){
		//Find the right bucketList		
		ArrayList<Tuple> bucketList = ht.get(hash(pageId));

		return bucketList.remove(getTupleIndex(pageId, bucketList));
	}
	/* Calculate the index number for hashTable directory */
	public Integer hash(PageId pageNum){
		return (a * pageNum.pid + b) % HTSIZE; 
	}

}
