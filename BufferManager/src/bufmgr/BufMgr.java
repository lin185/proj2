/*
 * @author Binhao Lin, Chiahao Chen
 */

package bufmgr;

import global.PageId;
import global.Page;
import global.Minibase;
import diskmgr.*;

import java.io.IOException;
import chainexception.ChainException;

public class BufMgr {

	int numbufs;		//number of buffers in the buffer pool
	byte[][] bufPool; 	//array of bytes to represent buffer pool
	Descriptor[] bufDescr;	//buffer descriptors for frames
	LIRS lirs;
	
	int frameCount = 0;	
	CustomHashTable hashTable;
	
	String replacementPolicy;
	DiskMgr diskManager;
	
	/**
	* Create the BufMgr object.
	* Allocate pages (frames) for the buffer pool in main memory and
	* make the buffer manage aware that the replacement policy is* specified by replacerArg (e.g., LH, Clock, LRU, MRU, LIRS, etc.).
	*
	* @paramnumbufs number of buffers in the buffer pool
	* @paramlookAheadSize number of pages to be looked ahead
	* @paramreplacementPolicy Name of the replacement policy
	*/
	public BufMgr(int numbufs, int lookAheadSize, String replacementPolicy) {
		this.numbufs = numbufs;
		bufPool = new byte[numbufs][global.GlobalConst.PAGE_SIZE];
		bufDescr = new Descriptor[numbufs];
		for(int i = 0; i<numbufs; i++) {
			bufDescr[i] = new Descriptor();
		}
		hashTable = new CustomHashTable();
		
		
		//test hash table
		/*hashTable.printHashTable();
		PageId pid = new PageId(0); //<0,0>
		hashTable.put(pid, 0);
		hashTable.printHashTable();
		
		pid = new PageId(0);
		try{
			hashTable.remove(pid);
		}catch(Exception e) {
			System.out.println("remove error");
		}
		hashTable.printHashTable();
		
		pid = new PageId(1); //<1,0>
		hashTable.put(pid, 0);
		hashTable.printHashTable();
		
		pid = new PageId(0); //<0,1>
		hashTable.put(pid, 1);
		hashTable.printHashTable();*/
		
		
		
		
		this.replacementPolicy = replacementPolicy;
		diskManager = new DiskMgr();
		lirs = new LIRS();

	}
	
	
	/**
	* Pin a page.
	* First check if this page is already in the buffer pool.
	* If it is, increment the pin_count and return a pointer to this
	* page.
	* If the pin_count was 0 before the call, the page was a
	* replacement candidate, but is no longer a candidate.
	* If the page is not in the pool, choose a frame (from the
	* set of replacement candidates) to hold this page, read the
	* page (using the appropriate method from {\em diskmgr} package) and pin it.
	* Also, must write out the old page in chosen frame if it is dirty
	* before reading new page.__ (You can assume that emptyPage==false for
	* this assignment.)
	*
	* @param pageno page number in the Minibase.
	* @param page the pointer point to the page.
	* @param emptyPage true (empty page) false (nonempty page)
	*/
	public void pinPage(PageId pageno, Page page, boolean emptyPage) throws  
						PagePinnedException, HashEntryNotFoundException, 
						InvalidPageNumberException, FileIOException, IOException{
		
		System.out.printf("pinPage: %d start\n", pageno.pid);
		//hashTable.printAll();
		Tuple t = hashTable.get(pageno);
		//System.out.print(t.getFrameId());
		//If page is already in the buffer
		if(t != null){
			System.out.printf("Page: %d IN the buffer pool\n", pageno.pid);
			//find the frame number and increment pin_count			
			 bufDescr[t.getFrameId()].pin_count++;
			 page = new Page(bufPool[t.getFrameId()]);
			 System.out.printf("pinPage: %d end\n", pageno.pid);
			 return;
		}
		//If page is not in the buffer
		else {
			System.out.printf("Page: %d NOT in the buffer pool\n", pageno.pid);
			//choose a frame to replace.
			//int frameNum = frameCount++ % numbufs;
			int frameNum = lirs.getVictimPage(bufDescr); 

			//if(frameNum == -1)//error	
				
			//if old frame is dirty -> write out the old page
			if(bufDescr[frameNum].dirtybit == true){
				//System.out.printf("Frame %d is dirty\n, my pid is %s\n", frameNum, bufDescr[frameNum].pageno.pid);
				PageId pid = new PageId(bufDescr[frameNum].pageno.pid);
				Page p = new Page(bufPool[frameNum]);
				Minibase.DiskManager.write_page(pid, p);
			}
			
		//	hashTable.printAll();
		
		//	PageId rp = new PageId(bufDescr[frameNum].pageno.pid);
			//if(bufDescr[frameNum].pageno != null)
			//	System.out.println(bufDescr[frameNum].pageno.pid);	
			hashTable.remove(bufDescr[frameNum].pageno);
		 // hashTable.printAll();  
			//Read the new page 
			Page p = new Page();
			Minibase.DiskManager.read_page(pageno, p);
			bufPool[frameNum] = p.getData();
			PageId newpid = new PageId(pageno.pid);
			hashTable.put(newpid, frameNum);
			bufDescr[frameNum] = new Descriptor();
			bufDescr[frameNum].pin_count++;
			bufDescr[frameNum].pageno = newpid;
			
			 System.out.printf("pinPage: %d end\n", pageno.pid);
			
		//	hashTable.printAll();
		}
		
		
	}
	
	
	/**
	* Unpin a page specified by a pageId.
	* This method should be called with dirty==true if the client has
	* modified the page.
	* If so, this call should set the dirty bit
	* for this frame.
	* Further, if pin_count>0, this method should
	* decrement it.
	*If pin_count=0 before this call, throw an exception
	* to report error.
	*(For testing purposes, we ask you to throw
	* an exception named PageUnpinnedException in case of error.)
	*
	* @param pageno page number in the Minibase.
	* @param dirty the dirty bit of the frame
	*/

	public void unpinPage(PageId pageno, boolean dirty) throws PageUnpinnedException {
		System.out.printf("unpinPage %d start\n", pageno.pid);
		if(pageno == null)
			return;
//	hashTable.printAll();
		int frameId = hashTable.get(pageno).getFrameId();
		Descriptor d = bufDescr[frameId];
		if(d.pin_count == 0) {
			System.out.printf("pin_count == 0 error\nunpinPage %d end\n", pageno.pid);
			throw new PageUnpinnedException(null, "BUFMGR:PAGE_NOT_PINNED.");
		}	
		if(dirty){
			System.out.printf("page is dirty\nunpinPage %d end\n", pageno.pid);
			d.dirtybit = true;	    
		}
	    d.pin_count--;	
	}
		
	/**
	* Allocate new pages.* Call DB object to allocate a run of new pages and
	* find a frame in the buffer pool for the first page
	* and pin it. (This call allows a client of the Buffer Manager
	* to allocate pages on disk.) If buffer is full, i.e., you
	* can't find a frame for the first page, ask DB to deallocate
	* all these pages, and return null.
	*
	* @param firstpage the address of the first page.
	* @param howmany total number of allocated new pages.
	*
	* @return the first page id of the new pages.__ null, if error.
	*/
	public PageId newPage(Page firstpage, int howmany)  throws IOException, ChainException{
		System.out.println("Start newPage()");
		PageId pid = new PageId();
	
		//Allocate new pages
		try {
			Minibase.DiskManager.allocate_page(pid, howmany);
			System.out.println("allocated page id: " + pid.pid);
		}catch(Exception e) {
			System.out.println("End newPage() --- allocate error");
			return null;
		}
	
		//Allocate sucessfully, pin it
		try {
			pinPage(pid, firstpage, true);
			System.out.println("End newPage() -- allocate succeed");
			return pid;
		}catch(Exception e) {
			//pinpage error, deallocate page
			Minibase.DiskManager.deallocate_page(pid, howmany);
			System.out.println("End newPage() -- pinpage error");
			
			return null;
		}

	}
	
	
/**
* This method should be called to delete a page that is on disk.
* This routine must call the method in diskmgr package to
* deallocate the page.
*
* @param globalPageId the page number in the data base.
*/
public void freePage(PageId globalPageId) throws ChainException{}
/**
* Used to flush a particular page of the buffer pool to disk.
* This method calls the write_page method of the diskmgr package.
*
* @param pageid the page number in the database.
*/
public void flushPage(PageId pageid) {}
/**
* Used to flush all dirty pages in the buffer pool to disk
*
*/
public void flushAllPages() {}
	
	
	/**
	* Returns the total number of buffer frames.
	*/
	public int getNumBuffers() {
		return numbufs;
	}


	/**
	* Returns the total number of unpinned buffer frames.
	*/
	public int getNumUnpinned() {
		int ct = 0;
		for(int i=0; i<numbufs; i++) {
			if(bufDescr[i].pin_count == 0)
				ct++;
		}
		return ct;
	}
	
}
