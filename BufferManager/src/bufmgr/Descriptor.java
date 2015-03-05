package bufmgr;

import global.PageId;
import java.lang.Integer;

public class Descriptor {
	PageId pageno;
	int pin_count;
	boolean dirtybit;
	
	int t1;
	int t2;
	
	int RD;
	int R;

	public Descriptor(){
		pageno = null;
		pin_count = 0;
		dirtybit = false;

		t1 = -1;
		t2 = 0;

		RD = Integer.MAX_VALUE;
		R = 0;
	}

}
