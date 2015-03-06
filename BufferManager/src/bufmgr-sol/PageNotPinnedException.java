package bufmgr;

import chainexception.ChainException;

public class PageNotPinnedException extends ChainException {
    
    public PageNotPinnedException(Exception e, String n) {
        super(e, n);
    }
}
