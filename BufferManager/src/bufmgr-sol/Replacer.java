package bufmgr;

import global.GlobalConst;

abstract class Replacer implements GlobalConst{
	
	public static final int AVAILABLE = 100;
	public static final int REFERENCED = 101;
	public static final int PINNED = 110;
	BufMgr bufMgr;
	
	int status[];
	
	protected Replacer(BufMgr bufMgr) {
		this.bufMgr = bufMgr;
		status = new int[bufMgr.getNumBuffers()];
		for(int i = 0; i < bufMgr.getNumBuffers(); i++) status[i] = AVAILABLE;
	}
	
	public abstract int next() throws BufferPoolExceededException, PagePinnedException;
	
	public void pin(int index) throws InvalidPageNumberException {
		bufMgr.bufDescr[index].pin();
		status[index] = PINNED;
	}
	
	public boolean unpin(int index) throws InvalidPageNumberException, PageNotPinnedException {
		bufMgr.bufDescr[index].unpin();
		if(bufMgr.bufDescr[index].pinCount == 0) status[index] = REFERENCED;
		return true;
	}
	
	public void free(int index) {
		bufMgr.bufDescr[index].unpin();
		status[index] = AVAILABLE;
	}
	
	
}
