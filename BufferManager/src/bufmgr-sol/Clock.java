package bufmgr;

public class Clock extends Replacer {
	
	private int ahead;
	
	protected Clock(BufMgr bufMgr) {
		super(bufMgr);
		ahead = -1;
	}

	@Override
	public int next() throws BufferPoolExceededException, PagePinnedException {
		int i = -1;
		ahead = (ahead + 1) % bufMgr.getNumBuffers();
		while(status[ahead] != AVAILABLE) {
			if(status[ahead] == REFERENCED) status[ahead] = AVAILABLE;
			if(i == bufMgr.getNumBuffers())
				throw new BufferPoolExceededException (null, "Replacer: next() error, buffer pool exceeded");
			i++;
			ahead = (ahead + 1) % bufMgr.getNumBuffers();
		}

		//System.out.println("ahead: " + ahead + " state: " + status[ahead]);

		if(bufMgr.bufDescr[ahead].pinCount > 0)
			throw new PagePinnedException (null, "Replacer: next() error, page pinned");
		
		bufMgr.bufDescr[ahead].pin();
		status[ahead] = PINNED;
		
		return ahead;
	}
}
