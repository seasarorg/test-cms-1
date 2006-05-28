package org.seasar.cms.framework.impl;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.ResponseProcessor;

public class DefaultResponseProcessor implements ResponseProcessor {

    public boolean process(ServletContext context,
        HttpServletRequest httpRequest, HttpServletResponse httpResponse,
        Response response) throws IOException, ServletException {

        if (response == null) {
            return true;
        }

        if (response.getPath() == null) {
            return false;
        }

        if (response.isRedirect()) {
            httpResponse.sendRedirect(response.getPath());
        } else {
            context.getRequestDispatcher(response.getPath()).forward(
                httpRequest, httpResponse);
        }
        return false;
    }
}
