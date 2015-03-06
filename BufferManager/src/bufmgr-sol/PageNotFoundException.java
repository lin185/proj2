package bufmgr;

import chainexception.ChainException;

public class PageNotFoundException extends ChainException {
	
	public PageNotFoundException(Exception e, String n) {
        super(e, n);
    }
}
