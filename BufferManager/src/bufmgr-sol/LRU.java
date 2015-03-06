package bufmgr;

import java.util.LinkedHashMap;

public class LRU extends Replacer {
	
	private int uses;
	private LinkedHashMap<Integer, Integer> lru;

	protected LRU(BufMgr bufMgr) {
		super(bufMgr);
		uses = 0;
		lru = new LinkedHashMap<Integer, Integer>(bufMgr.getNumBuffers());
	}

	@Override
	public int next() throws BufferPoolExceededException, PagePinnedException {
		//System.out.println("size of " + lru.size());
		if(lru.size() >= bufMgr.getNumBuffers()) {
			int min = uses+1, index = 0;
			for(int i = 0; i < bufMgr.getNumBuffers(); i++) {
				if(lru.get(i) < min && status[i] != PINNED) {
					min = lru.get(i);
					index = i;
				}
			}
			//System.out.println("uses: " + min + " at: " + index);
			if(min < uses) {
				lru.put(index, lru.get(index)+1);
				bufMgr.bufDescr[index].pin();
				status[index] = PINNED;
				uses++;
				return index;
			} else throw new BufferPoolExceededException (null, "BUFMGR: BUFFER_EXCEEDED.");
		} else {
			for(int i = 0; i < bufMgr.getNumBuffers(); i++) {
				if(lru.get(i) == null) {
					//System.out.println("here at " + i);
					lru.put(i, 1);
					bufMgr.bufDescr[i].pin();
					status[i] = PINNED;
					uses++;
					return i;
				}
			}
		}
		return INVALID_PAGEID;
	}
	
	public void pin(int index) throws InvalidPageNumberException {
		super.pin(index);
		if(lru.containsKey(index)) lru.put(index, lru.get(index)+1);
		else lru.put(index, 1);
		uses++;
	}
}
