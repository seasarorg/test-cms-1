package org.seasar.cms.ymir.handler.impl;

import org.seasar.cms.ymir.WrappingRuntimeException;
import org.seasar.cms.ymir.handler.ExceptionHandler;

public class ThrowableHandler implements ExceptionHandler {

    public String handle(Throwable t) {

        if (t instanceof Error) {
            throw (Error) t;
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new WrappingRuntimeException(t);
        }
    }
}
