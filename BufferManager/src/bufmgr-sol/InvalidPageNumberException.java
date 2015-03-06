package bufmgr;

import chainexception.ChainException;

public class InvalidPageNumberException extends ChainException {
	
	public InvalidPageNumberException(Exception e, String n) {
        super(e, n);
    }
}
