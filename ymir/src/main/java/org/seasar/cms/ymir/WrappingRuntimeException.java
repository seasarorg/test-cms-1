package org.seasar.cms.ymir;

public class WrappingRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1904957525045873876L;

    public WrappingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrappingRuntimeException(Throwable cause) {
        super(cause);
    }
}
