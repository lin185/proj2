package bufmgr;

import global.PageId;

public class Descriptor {
	PageId pageno;
	int pin_count;
	boolean dirtybit;
	
	public Descriptor(){
		pageno = null;
		pin_count = 0;
		dirtybit = false;
	}

}
