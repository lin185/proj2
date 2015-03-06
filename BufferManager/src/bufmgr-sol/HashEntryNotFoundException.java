package bufmgr;

import chainexception.ChainException;

public class HashEntryNotFoundException extends ChainException {
	
    public HashEntryNotFoundException(Exception e, String n) {
        super(e, n);
    }
}
