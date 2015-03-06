package bufmgr;

import java.util.LinkedHashMap;

public class LRULA extends Replacer {
	
	private int uses;
	private LinkedHashMap<Integer, Integer> lrula;

	protected LRULA(BufMgr bufMgr) {
		super(bufMgr);
		uses = 0;
		lrula = new LinkedHashMap<Integer, Integer>(bufMgr.getNumBuffers());
	}

	@Override
	public int next() throws BufferPoolExceededException, PagePinnedException {
		//System.out.println("size of " + lrula.size());
		if(lrula.size() >= bufMgr.getNumBuffers()) {
			int min = uses+1, index = 0;
			for(int i = 0; i < bufMgr.getNumBuffers(); i++) {
				if(lrula.get(i) < min && status[i] != PINNED) {
					min = lrula.get(i);
					index = i;
				}
			}
			//System.out.println("uses: " + min + " at: " + index);
			if(min < uses) {
				lrula.put(index, lrula.get(index)+1);
				bufMgr.bufDescr[index].pin();
				status[index] = PINNED;
				uses++;
				return index;
			} else throw new BufferPoolExceededException (null, "BUFMGR: BUFFER_EXCEEDED.");
		} else {
			for(int i = 0; i < bufMgr.getNumBuffers(); i++) {
				if(lrula.get(i) == null) {
					//System.out.println("here at " + i);
					lrula.put(i, 1);
					bufMgr.bufDescr[i].pin();
					status[i] = PINNED;
					uses++;
					return i;
				}
			}
		}
		System.out.println("return -1");
		return -1;
	}
	
	public void pin(int index) throws InvalidPageNumberException {
		super.pin(index);
		for(int i = 1; i < bufMgr.prefetchSize && i+index < bufMgr.getNumBuffers(); i++) {
			if(status[i] == PINNED || i == 0) {
				if(lrula.containsKey((i+index) % bufMgr.getNumBuffers()))
					lrula.put((i+index) % bufMgr.getNumBuffers(), lrula.get((i+index) % bufMgr.getNumBuffers())+1);
				else lrula.put((i+index) % bufMgr.getNumBuffers(), 1);
				uses++;
			}
		}
	}

}
