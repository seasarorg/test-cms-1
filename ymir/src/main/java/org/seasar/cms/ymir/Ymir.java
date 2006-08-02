package org.seasar.cms.ymir;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Ymir {

    void init();

    Response processRequest(String contextPath, String path, String method,
        String dispatcher, Map parameterMap, Map fileParameterMap)
        throws PageNotFoundException;

    boolean processResponse(ServletContext context_,
        HttpServletRequest httpRequest, HttpServletResponse httpResponse,
        Response response) throws IOException, ServletException;

    void destroy();
}
