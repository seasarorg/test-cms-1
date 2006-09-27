package org.seasar.cms.ymir;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Ymir {

    void init();

    Request prepareForProcessing(String contextPath, String path,
            String method, String dispatcher, Map parameterMap,
            Map fileParameterMap, AttributeContainer attributeContainer)
            throws PageNotFoundException;

    Response processRequest(Request request);

    boolean processResponse(ServletContext context_,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            Request request, Response response) throws IOException,
            ServletException;

    void destroy();

    Object backupForInclusion(AttributeContainer attributeContainer);

    void restoreForInclusion(AttributeContainer attributeContainer,
            Object backupped);
}
