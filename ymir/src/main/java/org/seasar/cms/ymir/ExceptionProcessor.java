package org.seasar.cms.ymir;

public interface ExceptionProcessor {

    Response process(Throwable t);
}
