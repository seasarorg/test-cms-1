package org.seasar.cms.framework;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ResponseProcessor {

    boolean process(ServletContext context, HttpServletRequest httpRequest,
        HttpServletResponse httpResponse, Response response)
        throws IOException, ServletException;
}
