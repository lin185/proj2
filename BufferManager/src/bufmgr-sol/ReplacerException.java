package bufmgr;

import chainexception.ChainException;

public class ReplacerException extends ChainException {
	
	public ReplacerException(Exception e, String n) {
        super(e, n);
    }
}
