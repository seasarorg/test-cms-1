package org.seasar.cms.ymir;

public interface ExceptionProcessor {

    String PATH_EXCEPTION_TEMPLATE = "/WEB-INF/template/exception/";

    String SUFFIX_EXCEPTION_TEMPLATE = ".html";

    Response process(Throwable t);
}
