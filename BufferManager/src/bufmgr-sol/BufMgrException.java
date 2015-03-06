package bufmgr;

import chainexception.ChainException;

public class BufMgrException extends ChainException {
	
	public BufMgrException(Exception e, String n) {
        super(e, n);
    }
}
