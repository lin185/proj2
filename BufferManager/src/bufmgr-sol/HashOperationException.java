package bufmgr;

import chainexception.ChainException;

public class HashOperationException extends ChainException {
	
	public HashOperationException(Exception e, String n) {
        super(e, n);
    }
}
