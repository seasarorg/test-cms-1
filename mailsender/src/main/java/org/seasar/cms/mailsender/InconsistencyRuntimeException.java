package org.seasar.cms.mailsender;

public class InconsistencyRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InconsistencyRuntimeException() {
    }

    public InconsistencyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InconsistencyRuntimeException(String message) {
        super(message);
    }

    public InconsistencyRuntimeException(Throwable cause) {
        super(cause);
    }
}
