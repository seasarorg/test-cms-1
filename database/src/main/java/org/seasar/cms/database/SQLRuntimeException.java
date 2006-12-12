package org.seasar.cms.database;

/**
 * @author YOKOTA Takehiko
 */
public class SQLRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 684377420929252715L;

    public SQLRuntimeException() {
        super();
    }

    /**
     * @param message
     */
    public SQLRuntimeException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public SQLRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public SQLRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
