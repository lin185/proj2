package bufmgr;

import global.*;
import java.util.ArrayList;

import diskmgr.*;

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
	public int getTupleIndex(PageId target, ArrayList<Tuple> bucketList){
		
		int targetIndex = 0;
		//System.out.println(target.pid);
	//	if(bucketList.isEmpty())
	//		return -1;

		for(targetIndex = 0; targetIndex < bucketList.size(); targetIndex++) {
			//System.out.printf("getTupleIndex, target.pid: %d  bucketElement.pid:%d\n",  target.pid, bucketList.get(targetIndex).getPageId().pid);
			if(bucketList.get(targetIndex).getPageId().pid == target.pid) {
				//System.out.printf("targetIndex: %d\n", targetIndex);
				return targetIndex ;
			}
		}
		return -1;
	}
	public void printAll(){
		for(int i = 0; i < HTSIZE; i++){
			System.out.print("HashCode: " + i);
			for(int j = 0; j < ht.get(i).size(); j++){
				System.out.print(" (" + ht.get(i).get(j).getPageId() + ","+ ht.get(i).get(j).getFrameId() + ")" );
			}
			System.out.println();
		}
	}
	
	/* get the target Tuple
	** Return NULL if tuple not found
	*/
	public Tuple get (PageId pageId){
		//Find the right bucketList
		
		if(pageId == null) {
			//System.out.printf("get frameId for pageId (null)\n");
			return null;
		}
		//System.out.printf("get frameId for pageId (%d)\n", pageId.pid);
		
		//System.out.printf("Get Bucketlist from hashtable %d\n", hash(pageId));
		ArrayList<Tuple> bucketList = ht.get(hash(pageId));
		int index = getTupleIndex(pageId, bucketList);
		//System.out.printf("Get index (%d) from Bucketlist\n", index);
		//System.out.printf("get index %d\n", index);
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
			//System.out.printf("Tuple <pid:%d, fid:%d> put into hashtable %d\n", pageId.pid, frameId, hash(pageId));
		      PageId p = new PageId(pageId.pid);  
			  ht.get(hash(pageId)).add(new Tuple(p, frameId));
	//	}
	}
	
	/* remove the target tuple from the bucketList */
	public void remove(PageId pageId) throws HashEntryNotFoundException{
		//Find the right bucketList		
		if(pageId == null)
			return;
		ArrayList<Tuple> bucketList = ht.get(hash(pageId));
		//System.out.println(bucketList);
		/*for(int i =0; i < bucketList.size(); i++){
			System.out.printf("(%d, %d) ", bucketList.get(i).getPageId(), bucketList.get(i).getFrameId());
		}*/
		//System.out.printf("HashCode %d PageId %d\n", hash(pageId), pageId.pid);
		int index = getTupleIndex(pageId, bucketList);
		//System.out.println("index: " + index);
		if (index == -1)
			throw new HashEntryNotFoundException(null, "HASHENTRYNOTFOUND");
		//	return;
		Tuple t = bucketList.get(index);
		//System.out.printf("Tuple <pid:%d, fid:%d> removed from hashtable %d\n", pageId.pid, t.getFrameId(), hash(pageId));
		
		bucketList.remove(index);
	}
	
	/* Calculate the index number for hashTable directory */
	public Integer hash(PageId pageNum){
		return (a * pageNum.pid + b) % HTSIZE; 
	}
	
	public void printHashTable() {
		System.out.printf("--------HashTable---------\n");
		for(int i = 0; i < HTSIZE; i++){
			ArrayList<Tuple> bucketList = ht.get(i);
			int bucket_size = bucketList.size();
			if(bucket_size > 0 ) {
				System.out.printf("%d: ", i);
				for(int j = 0; j < bucket_size; j++) {
					Tuple t = bucketList.get(j);
					System.out.printf("<pid:%d, fid:%d> ->", t.getPageId().pid, t.getFrameId());
				}
				System.out.printf("\n");
			}
		}
		System.out.printf("--------------------------\n\n");
	}

}
