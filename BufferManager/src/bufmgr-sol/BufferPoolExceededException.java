
package bufmgr;

import chainexception.ChainException;

public class BufferPoolExceededException extends ChainException {
    
    public BufferPoolExceededException(Exception e, String n) {
        super(e, n);
    }
}
