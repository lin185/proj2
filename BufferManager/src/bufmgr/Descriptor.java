package bufmgr;

import global.PageId;
import java.lang.Integer;

public class Descriptor {
	PageId pageno;
	int pin_count;
	boolean dirtybit;
	
	int t1;
	int t2;
	
	public Descriptor(){
		pageno = null;
		pin_count = 0;
		dirtybit = false;

		t1 = -1;
		t2 = -1;

	}

}
